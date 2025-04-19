package com.example.myapplication.viewModel


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// Import your data classes (ensure paths are correct)
import com.example.myapplication.data.FirebaseRecipe
import com.example.myapplication.data.User
import com.example.myapplication.data.SearchResultItem // Your sealed class for results
import com.example.myapplication.front_end.search.RecentSearchItem // Your data class for recents
import com.example.myapplication.front_end.search.SearchItemType // Your enum for recents
import com.example.myapplication.utils.RecentSearchManager // Your SharedPreferences manager
// Firebase Imports
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
// Coroutine/Flow Imports
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async // For concurrent queries
import kotlinx.coroutines.awaitAll // For concurrent queries

// --- State Definition for the Search UI ---
sealed class SearchState {
    data class Recent(val items: List<RecentSearchItem>) : SearchState()
    object Loading : SearchState()
    data class Success(val results: List<SearchResultItem>) : SearchState()
    data class Error(val message: String) : SearchState()
    object EmptyQuery : SearchState() // State when query is empty (can show Recents)
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "SearchViewModel"
    private val db = FirebaseFirestore.getInstance()
    private val recipesCollection = db.collection("recipes")
    private val usersCollection = db.collection("users")

    // Manager for local recent searches
    private val recentSearchManager = RecentSearchManager(application.applicationContext)

    // Input query from the UI
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Output state for the UI
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Recent(recentSearchManager.getRecentSearches()))
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    // Job reference to cancel ongoing searches
    private var searchJob: Job? = null

    init {
        // React to query changes for searching
        viewModelScope.launch {
            _searchQuery
                .debounce(400L) // Wait 400ms after user stops typing
                .distinctUntilChanged() // Only search if text actually changes
                .collectLatest { query -> // Cancel previous search if new query comes in
                    searchJob?.cancel() // Explicitly cancel previous job if running
                    if (query.isBlank()) {
                        // Show recents when query is cleared
                        _searchState.value = SearchState.Recent(recentSearchManager.getRecentSearches())
                    } else {
                        // Start a new search job
                        searchJob = performSearch(query)
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun performSearch(query: String): Job { // Return Job to allow cancellation
        return viewModelScope.launch { // Launch within viewModelScope
            _searchState.value = SearchState.Loading
            Log.d(TAG, "Performing search for: $query")
            try {
                val queryLower = query.lowercase()

                // --- Firestore Queries (Run concurrently) ---
                val recipeSearchDeferred = async { // async for parallel execution
                    recipesCollection
                        .orderBy("nameLower") // Ensure index exists on nameLower
                        .whereGreaterThanOrEqualTo("nameLower", queryLower)
                        .whereLessThanOrEqualTo("nameLower", queryLower + '\uf8ff')
                        .limit(15) // Limit recipe results
                        .get()
                        .await() // Suspend until this query finishes
                }

                val userSearchDeferred = async { // async for parallel execution
                    usersCollection
                        .orderBy("usernameLower") // Ensure index exists on usernameLower
                        .whereGreaterThanOrEqualTo("usernameLower", queryLower)
                        .whereLessThanOrEqualTo("usernameLower", queryLower + '\uf8ff')
                        .limit(10) // Limit user results
                        .get()
                        .await() // Suspend until this query finishes
                }

                // --- Await and Process Results ---
                val recipeResultsSnapshot = recipeSearchDeferred.await() // Get result from deferred
                val userResultsSnapshot = userSearchDeferred.await()   // Get result from deferred

                val recipes = recipeResultsSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirebaseRecipe::class.java)?.copy(id = doc.id)?.let { fbRecipe ->
                        SearchResultItem.RecipeResult(
                            id = fbRecipe.id, name = fbRecipe.name, authorName = fbRecipe.authorName,
                            imageUrl = fbRecipe.imageUrl, averageRating = fbRecipe.averageRating
                        )
                    }
                }
                val users = userResultsSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(uid = doc.id)?.let { user ->
                        SearchResultItem.UserResult(
                            uid = user.uid, username = user.username ?: "Unknown",
                            profileImageUrl = user.profileImageUrl,
                            followerCount = 0L // Add logic if you store followers
                        )
                    }
                }

                // Combine results (consider sorting or ranking later if needed)
                val combinedResults = (recipes + users).shuffled() // Simple shuffle example

                Log.d(TAG, "Search successful. Found ${recipes.size} recipes, ${users.size} users.")
                _searchState.value = SearchState.Success(combinedResults)

                // Add successful query to recent searches
                if (combinedResults.isNotEmpty()) { // Only add if results were found
                    addQueryToRecent(query)
                }

            } catch (e: CancellationException) {
                Log.i(TAG, "Search job cancelled for query: $query")
                // Don't set error state if cancelled by new input
            } catch (e: Exception) {
                Log.e(TAG, "Error performing search for '$query'", e)
                _searchState.value = SearchState.Error("Search failed: ${e.localizedMessage}")
            }
            // Loading state is implicitly finished when Success or Error is emitted
        }
    }

    // --- Recent Search Functions ---

    fun addQueryToRecent(query: String) {
        if (query.isNotBlank()) {
            val newItem = RecentSearchItem(type = SearchItemType.QUERY, label = query.trim())
            recentSearchManager.addSearchItem(newItem)
            // Optionally refresh recent state immediately if needed,
            // but it will refresh when query becomes blank anyway.
        }
    }

    // Call this when user clicks a specific result item
    fun addItemToRecent(item: SearchResultItem) {
        val recentItem = when(item) {
            is SearchResultItem.RecipeResult -> RecentSearchItem(
                type = SearchItemType.RECIPE,
                label = item.name,
                secondaryLabel = item.authorName, // Example secondary info
                imageUrl = item.imageUrl
            )
            is SearchResultItem.UserResult -> RecentSearchItem(
                type = SearchItemType.USER,
                label = item.username,
                // secondaryLabel = "${item.followerCount} followers", // Example
                imageUrl = item.profileImageUrl
            )
        }
        recentSearchManager.addSearchItem(recentItem)
    }


    fun removeRecentSearch(item: RecentSearchItem) {
        recentSearchManager.removeSearchItem(item)
        // Refresh the recent list immediately if it's currently shown
        val currentState = _searchState.value
        if (currentState is SearchState.Recent) {
            _searchState.value = SearchState.Recent(recentSearchManager.getRecentSearches())
        }
    }

    fun clearRecentSearches() {
        recentSearchManager.clearRecentSearches()
        _searchState.value = SearchState.Recent(emptyList()) // Update state immediately
    }
}