package com.example.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirebaseRecipe
import com.example.myapplication.data.Recipe // UI Model
import com.example.myapplication.data.UserCollection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

sealed class UserCollectionState {
    object Loading : UserCollectionState()
    data class Success(val collections: List<UserCollection>) : UserCollectionState()
    data class Error(val message: String) : UserCollectionState()
}

sealed class SavedRecipeListState {
    object Loading : SavedRecipeListState()
    // Success now used for both saved and all recipes scenarios
    data class Success(val recipes: List<Recipe>, val source: String = "saved") : SavedRecipeListState() // Add source marker
    data class Error(val message: String) : SavedRecipeListState()
    object Empty : SavedRecipeListState()
}


// State for Collection Creation
sealed class CollectionCreationState {
    object Idle : CollectionCreationState()
    object Loading : CollectionCreationState()
    data class Success(val collectionId: String) : CollectionCreationState()
    data class Error(val message: String) : CollectionCreationState()
}

object RecipeFilters {
    const val ALL = "All"
    const val FAVORITES = "Favorites"
}

private val _availableFilters = MutableStateFlow<List<String>>(emptyList())
val availableFilters: StateFlow<List<String>> = _availableFilters.asStateFlow()

private val _allRecipesState = MutableStateFlow<SavedRecipeListState>(SavedRecipeListState.Loading)
val allRecipesState: StateFlow<SavedRecipeListState> = _allRecipesState.asStateFlow()


class SavedRecipesViewModel : ViewModel() {

    private val TAG = "SavedRecipesVM"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val recipesCollection = db.collection("recipes")
    private val usersCollections = db.collection("user_collections")

    private val _userCollectionsState = MutableStateFlow<UserCollectionState>(UserCollectionState.Loading)
    val userCollectionsState: StateFlow<UserCollectionState> = _userCollectionsState.asStateFlow()

    private val _savedRecipeListState = MutableStateFlow<SavedRecipeListState>(SavedRecipeListState.Loading)
    val savedRecipeListState: StateFlow<SavedRecipeListState> = _savedRecipeListState.asStateFlow()

    // State for the creation process
    private val _collectionCreationState = MutableStateFlow<CollectionCreationState>(CollectionCreationState.Idle)
    val collectionCreationState: StateFlow<CollectionCreationState> = _collectionCreationState.asStateFlow()

    private var currentCollections: List<UserCollection> = emptyList()
    private var favoriteRecipeIds: Set<String> = emptySet()

    private val _allRecipesState = MutableStateFlow<SavedRecipeListState>(SavedRecipeListState.Loading)
    val allRecipesState: StateFlow<SavedRecipeListState> = _allRecipesState

    private val _allRecipesView = MutableStateFlow<RecipeListState>(RecipeListState.Loading)
    val allRecipesView: StateFlow<RecipeListState> = _allRecipesView.asStateFlow()

    private val _favoriteRecipesView = MutableStateFlow<RecipeListState>(RecipeListState.Loading)
    val favoriteRecipesView: StateFlow<RecipeListState> = _favoriteRecipesView.asStateFlow()



    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _userCollectionsState.value = UserCollectionState.Error("User not logged in")
            _savedRecipeListState.value = SavedRecipeListState.Error("User not logged in")
            return
        }
        fetchUserCollectionsAndFavorites(userId)
    }

    private fun fetchUserCollectionsAndFavorites(userId: String) {
        _userCollectionsState.value = UserCollectionState.Loading
        viewModelScope.launch {
            try {
                val collectionsSnapshot = usersCollections.whereEqualTo("userId", userId).get().await()
                currentCollections = collectionsSnapshot.toObjects<UserCollection>()

                // Ensure Favorites collection exists
                ensureFavoritesCollectionExists(userId)

                // Combine default filters with user collections
                val defaultFilters = listOf(RecipeFilters.ALL, RecipeFilters.FAVORITES)
                val userCollectionNames = currentCollections.map { it.name }
                _availableFilters.value = defaultFilters + userCollectionNames

                val favoritesCollection = currentCollections.find { it.name.equals(RecipeFilters.FAVORITES, ignoreCase = true) }
                favoriteRecipeIds = favoritesCollection?.recipeIds?.toSet() ?: emptySet()

                Log.d(TAG, "Fetched ${currentCollections.size} collections. Favorites count: ${favoriteRecipeIds.size}")
                _userCollectionsState.value = UserCollectionState.Success(currentCollections)
                fetchRecipesForSelection()

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching collections/favorites", e)
                val errorMsg = "Failed to load collections: ${e.localizedMessage}"
                _userCollectionsState.value = UserCollectionState.Error(errorMsg)
                _savedRecipeListState.value = SavedRecipeListState.Error(errorMsg)
            }
        }
    }

    private suspend fun ensureFavoritesCollectionExists(userId: String) {
        val hasFavorites = currentCollections.any { it.name.equals(RecipeFilters.FAVORITES, ignoreCase = true) }
        if (!hasFavorites) {
            try {
                val newFavorites = UserCollection(
                    id = usersCollections.document().id,
                    userId = userId,
                    name = RecipeFilters.FAVORITES,
                    recipeIds = emptyList()
                )
                usersCollections.document(newFavorites.id).set(newFavorites).await()
                currentCollections = currentCollections + newFavorites
                Log.d(TAG, "Created new Favorites collection for user $userId")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating Favorites collection", e)
            }
        }
    }

    // Fetch ALL recipes user has saved (across all collections) to allow selection
    fun fetchRecipesForSelection() {
        val userId = auth.currentUser?.uid ?: return
        _savedRecipeListState.value = SavedRecipeListState.Loading
        Log.d(TAG, "Fetching all saved recipes for selection")

        viewModelScope.launch {
            try {
                // Get all recipe IDs from all user collections first
                if (currentCollections.isEmpty() && _userCollectionsState.value !is UserCollectionState.Loading) {
                    // Maybe collections haven't loaded yet, or user has none
                    // Re-trigger initial fetch if needed, or handle empty case
                    fetchUserCollectionsAndFavorites(userId) // Re-fetch just in case
                    // Wait briefly? Or use combine? For simplicity, fetch then proceed
                    // kotlinx.coroutines.delay(500) // Not ideal
                }

                val allRecipeIds = currentCollections.flatMap { it.recipeIds }.distinct()
                Log.d(TAG,"All unique recipe IDs for user $userId: $allRecipeIds")

                if (allRecipeIds.isEmpty()) {
                    _savedRecipeListState.value = SavedRecipeListState.Success(emptyList())
                    return@launch
                }

                // Fetch actual recipe data for these IDs
                val fetchedFirebaseRecipes = mutableListOf<FirebaseRecipe>()
                allRecipeIds.chunked(30).forEach { chunk ->
                    if (chunk.isNotEmpty()) {
                        try {
                            val snapshot = recipesCollection.whereIn("__name__", chunk).get().await()
                            val recipesInChunk = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(FirebaseRecipe::class.java)?.copy(id = doc.id)
                            }
                            fetchedFirebaseRecipes.addAll(recipesInChunk)
                        } catch (e: Exception) { Log.w(TAG, "Error fetching selection recipe chunk: $chunk", e) }
                    }
                }
                Log.d(TAG, "Total FirebaseRecipes for selection: ${fetchedFirebaseRecipes.size}")

                val uiRecipes = fetchedFirebaseRecipes.map { fbRecipe ->
                    mapFirebaseRecipeToRecipe(fbRecipe, favoriteRecipeIds) // Mark favorites correctly
                }

                _savedRecipeListState.value = SavedRecipeListState.Success(uiRecipes)

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching recipes for selection", e)
                _savedRecipeListState.value = SavedRecipeListState.Error("Failed to load recipes: ${e.localizedMessage}")
            }
        }

    }


    fun fetchRecipesForDisplay(filter: String) {
        val userId = auth.currentUser?.uid ?: return

        _savedRecipeListState.value = SavedRecipeListState.Loading
        Log.d(TAG, "Fetching recipes for filter: $filter")

        viewModelScope.launch {
            try {
                val recipeIdsToFetch: List<String> = when (filter) {
                    RecipeFilters.ALL -> currentCollections.flatMap { it.recipeIds }.distinct()
                    RecipeFilters.FAVORITES -> favoriteRecipeIds.toList()
                    else -> currentCollections.find { it.id == filter }?.recipeIds ?: emptyList()
                }

                Log.d(TAG, "Recipe IDs to fetch for filter '$filter': $recipeIdsToFetch")

                if (recipeIdsToFetch.isEmpty()) {
                    _savedRecipeListState.value = SavedRecipeListState.Success(emptyList())
                    return@launch
                }

                val fetchedFirebaseRecipes = mutableListOf<FirebaseRecipe>()
                recipeIdsToFetch.chunked(30).forEach { chunk ->
                    if (chunk.isNotEmpty()) {
                        try {
                            val snapshot = recipesCollection.whereIn("__name__", chunk).get().await()
                            val recipesInChunk = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(FirebaseRecipe::class.java)?.copy(id = doc.id)
                            }
                            fetchedFirebaseRecipes.addAll(recipesInChunk)
                        } catch (e: Exception) { Log.w(TAG, "Error fetching display recipe chunk: $chunk", e) }
                    }
                }
                Log.d(TAG, "Total fetched FirebaseRecipes for display: ${fetchedFirebaseRecipes.size}")

                val uiRecipes = fetchedFirebaseRecipes.map { fbRecipe ->
                    mapFirebaseRecipeToRecipe(fbRecipe, favoriteRecipeIds)
                }

                _savedRecipeListState.value = SavedRecipeListState.Success(uiRecipes)

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching recipes for filter '$filter'", e)
                _savedRecipeListState.value = SavedRecipeListState.Error("Failed to load recipes: ${e.localizedMessage}")
            }
        }
    }

    // New function to create a collection
    fun createNewCollection(collectionName: String, selectedRecipeIds: List<String>) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _collectionCreationState.value = CollectionCreationState.Error("User not logged in")
            return
        }
        if (collectionName.isBlank()) {
            _collectionCreationState.value = CollectionCreationState.Error("Collection name cannot be empty")
            return
        }
        if (collectionName.equals(RecipeFilters.FAVORITES, ignoreCase = true) || collectionName.equals(RecipeFilters.ALL, ignoreCase = true) ) {
            _collectionCreationState.value = CollectionCreationState.Error("Cannot use reserved name 'Favorites' or 'All'")
            return
        }


        _collectionCreationState.value = CollectionCreationState.Loading
        viewModelScope.launch {
            try {
                val newCollectionDocRef = usersCollections.document() // Generate new ID
                val newCollection = UserCollection(
                    id = newCollectionDocRef.id,
                    userId = userId,
                    name = collectionName.trim(),
                    recipeIds = selectedRecipeIds.distinct() // Ensure unique IDs
                )

                newCollectionDocRef.set(newCollection).await()
                Log.i(TAG, "Successfully created new collection '${newCollection.name}' with ID: ${newCollection.id}")
                _collectionCreationState.value = CollectionCreationState.Success(newCollection.id)
                // Refresh collections list after successful creation
                fetchUserCollectionsAndFavorites(userId)

            } catch (e: Exception) {
                Log.e(TAG, "Error creating new collection '$collectionName'", e)
                _collectionCreationState.value = CollectionCreationState.Error("Failed to create collection: ${e.localizedMessage}")
            }
        }
    }

    fun resetCollectionCreationState() {
        _collectionCreationState.value = CollectionCreationState.Idle
    }


    private fun mapFirebaseRecipeToRecipe(fbRecipe: FirebaseRecipe, currentFavoriteIds: Set<String>): Recipe {
        return Recipe(
            id = fbRecipe.id,
            nameOfPerson = fbRecipe.authorName.takeIf { it.isNotEmpty() } ?: "Unknown Chef",
            name = fbRecipe.name.takeIf { it.isNotEmpty() } ?: "Unnamed Recipe",
            imageUrl = fbRecipe.imageUrl,
            averageRating = fbRecipe.averageRating,
            category = fbRecipe.category.takeIf { it.isNotEmpty() } ?: "Uncategorized",
            cookingTime = fbRecipe.cookingTime.takeIf { it.isNotEmpty() } ?: "N/A",
            serving = if (fbRecipe.servings > 0) fbRecipe.servings else 1,
            favorite = currentFavoriteIds.contains(fbRecipe.id) // Set favorite status based on fetched IDs
        )
    }

    fun toggleFavoriteStatus(recipeId: String, isCurrentlyFavorite: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        // Find the actual ID of the user's "Favorites" collection
        val favoritesCollectionId = currentCollections.find { it.name.equals(RecipeFilters.FAVORITES, ignoreCase = true) }?.id

        if (favoritesCollectionId == null) {
            Log.e(TAG, "Cannot toggle favorite: 'Favorites' collection not found for user $userId")
            // Potentially create the Favorites collection if it doesn't exist? Or show error.
            return
        }

        viewModelScope.launch {
            try {
                val updateAction = if (isCurrentlyFavorite) FieldValue.arrayRemove(recipeId) else FieldValue.arrayUnion(recipeId)
                usersCollections.document(favoritesCollectionId)
                    .update("recipeIds", updateAction)
                    .await()
                Log.d(TAG, "Toggled favorite status for $recipeId in collection $favoritesCollectionId")

                // Update local state immediately for better UX before full refresh
                val updatedFavorites = if (isCurrentlyFavorite) {
                    favoriteRecipeIds - recipeId
                } else {
                    favoriteRecipeIds + recipeId
                }
                favoriteRecipeIds = updatedFavorites

                // Trigger a refresh of the currently displayed list if it's Favorites or All
                val currentState = _savedRecipeListState.value
                if (currentState is SavedRecipeListState.Success) {
                    // Re-map the existing list with the new favorite status
                    val newlyMappedRecipes = currentState.recipes.map {
                        it.copy(favorite = favoriteRecipeIds.contains(it.id))
                    }
                    _savedRecipeListState.value = SavedRecipeListState.Success(newlyMappedRecipes)
                }
                // Optionally trigger full refresh if needed: fetchInitialData()

            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite status for $recipeId", e)
            }
        }
    }

    fun fetchAllRecipes() {
        _allRecipesState.value = SavedRecipeListState.Loading
        viewModelScope.launch {
            try {
                val snapshot = recipesCollection.get().await()
                val recipes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirebaseRecipe::class.java)?.copy(id = doc.id)
                }
                _allRecipesState.value = SavedRecipeListState.Success(
                    recipes.map { fbRecipe ->
                        mapFirebaseRecipeToRecipe(fbRecipe, favoriteRecipeIds)
                    },
                    "all" // Mark as all recipes
                )
            } catch (e: Exception) {
                _allRecipesState.value = SavedRecipeListState.Error("Failed to load recipes: ${e.localizedMessage}")
            }
        }
    }


}

