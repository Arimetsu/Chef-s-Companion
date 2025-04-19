package com.example.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirebaseRecipe
import com.example.myapplication.data.Recipe // UI Model
import com.example.myapplication.data.UserCollection
import com.example.myapplication.data.UserCollectionWithPreviews // Import this
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject // Import ktx.toObject
import com.google.firebase.firestore.ktx.toObjects // Import ktx.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

// Define constant here or import if defined elsewhere
const val DEFAULT_SAVED_COLLECTION_NAME = "Saved Recipes"

// --- State Sealed Classes ---
sealed class UserCollectionState {
    object Loading : UserCollectionState()
    data class Success(val collectionsWithPreviews: List<UserCollectionWithPreviews>) : UserCollectionState()
    data class Error(val message: String) : UserCollectionState()
}

sealed class SavedRecipeListState {
    object Loading : SavedRecipeListState()
    data class Success(val recipes: List<Recipe>, val source: String = "unknown") : SavedRecipeListState() // Add source marker
    data class Error(val message: String) : SavedRecipeListState()
    object Empty : SavedRecipeListState() // Keep if used
}

sealed class CollectionCreationState {
    object Idle : CollectionCreationState()
    object Loading : CollectionCreationState()
    data class Success(val collectionId: String) : CollectionCreationState()
    data class Error(val message: String) : CollectionCreationState()
}

object RecipeFilters {
    const val ALL = "All" // Represents view showing recipes from ALL collections
    const val FAVORITES = "Favorites" // Represents the specific "Favorites" collection/view
}

sealed class CollectionDetailState {
    object Loading : CollectionDetailState()
    data class Success(val collection: UserCollection, val recipes: List<Recipe>) : CollectionDetailState()
    data class Error(val message: String) : CollectionDetailState()
    object Deleted : CollectionDetailState()
}

sealed class CollectionUpdateState {
    object Idle : CollectionUpdateState()
    object Loading : CollectionUpdateState()
    object Success : CollectionUpdateState()
    data class Error(val message: String) : CollectionUpdateState()
}
// Remove duplicated state declarations if they exist outside the ViewModel

class SavedRecipesViewModel : ViewModel() {

    private val TAG = "SavedRecipesVM"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val recipesCollection = db.collection("recipes")
    private val usersCollections = db.collection("user_collections")

    // --- StateFlows ---
    private val _userCollectionsState = MutableStateFlow<UserCollectionState>(UserCollectionState.Loading)
    val userCollectionsState: StateFlow<UserCollectionState> = _userCollectionsState.asStateFlow()

    private val _savedRecipeListState = MutableStateFlow<SavedRecipeListState>(SavedRecipeListState.Loading)
    val savedRecipeListState: StateFlow<SavedRecipeListState> = _savedRecipeListState.asStateFlow()

    private val _collectionCreationState = MutableStateFlow<CollectionCreationState>(CollectionCreationState.Idle)
    val collectionCreationState: StateFlow<CollectionCreationState> = _collectionCreationState.asStateFlow()

    private val _collectionDetailState = MutableStateFlow<CollectionDetailState>(CollectionDetailState.Loading)
    val collectionDetailState: StateFlow<CollectionDetailState> = _collectionDetailState.asStateFlow()

    private val _collectionUpdateState = MutableStateFlow<CollectionUpdateState>(CollectionUpdateState.Idle)
    val collectionUpdateState: StateFlow<CollectionUpdateState> = _collectionUpdateState.asStateFlow()

    // Not using these separate states anymore, savedRecipeListState handles All/Favorites
    // private val _allRecipesState = MutableStateFlow<SavedRecipeListState>(SavedRecipeListState.Loading)
    // val allRecipesState: StateFlow<SavedRecipeListState> = _allRecipesState
    // private val _favoriteRecipesState = MutableStateFlow<SavedRecipeListState>(SavedRecipeListState.Loading)
    // val favoriteRecipesState: StateFlow<SavedRecipeListState> = _favoriteRecipesState


    // --- Internal State ---
    private var currentCollections: List<UserCollection> = emptyList()
    private var favoriteRecipeIds: Set<String> = emptySet()
    private val MAX_PREVIEW_IMAGES = 4 // Number of images for collection preview


    init {
        Log.d(TAG, "ViewModel initialized. Fetching initial data.")
        fetchInitialData()
    }

    // --- Initial Data Fetch ---
    private fun fetchInitialData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "User not logged in. Cannot fetch data.")
            val errorMsg = "User not logged in"
            _userCollectionsState.value = UserCollectionState.Error(errorMsg)
            _savedRecipeListState.value = SavedRecipeListState.Error(errorMsg)
            // Set other states to error as well if needed
            _collectionDetailState.value = CollectionDetailState.Error(errorMsg)
            return
        }
        // Fetch collections, ensuring necessary ones exist, and update states
        fetchUserCollectionsAndFavorites(userId)
    }

    // --- Fetch User Collections and Ensure Required Ones Exist ---
    private fun fetchUserCollectionsAndFavorites(userId: String) {
        _userCollectionsState.value = UserCollectionState.Loading
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching collections for user: $userId")
                val collectionsSnapshot = usersCollections.whereEqualTo("userId", userId).get().await()
                // Map snapshot documents to UserCollection objects, including their IDs
                var fetchedCollections = collectionsSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(UserCollection::class.java)?.copy(id = doc.id)
                }
                Log.d(TAG, "Initial fetch found ${fetchedCollections.size} collections.")

                // --- Ensure BOTH Favorites and Default Saved collections exist ---
                val needsFavorites = fetchedCollections.none { it.name.equals(RecipeFilters.FAVORITES, ignoreCase = true) }
                val needsDefaultSaved = fetchedCollections.none { it.name.equals(DEFAULT_SAVED_COLLECTION_NAME, ignoreCase = true) }
                var createdNewCollection = false

                if (needsFavorites) {
                    try {
                        val favId = usersCollections.document().id
                        val newFavorites = UserCollection(favId, userId, RecipeFilters.FAVORITES, emptyList())
                        usersCollections.document(favId).set(newFavorites).await()
                        fetchedCollections = fetchedCollections + newFavorites // Add to the list
                        createdNewCollection = true
                        Log.i(TAG, "Created 'Favorites' collection (ID: $favId) for user $userId.")
                    } catch (e: Exception) { Log.e(TAG, "Error creating 'Favorites' collection", e) }
                }
                if (needsDefaultSaved) {
                    try {
                        val savedId = usersCollections.document().id
                        val newDefault = UserCollection(savedId, userId, DEFAULT_SAVED_COLLECTION_NAME, emptyList())
                        usersCollections.document(savedId).set(newDefault).await()
                        fetchedCollections = fetchedCollections + newDefault // Add to the list
                        createdNewCollection = true
                        Log.i(TAG, "Created '$DEFAULT_SAVED_COLLECTION_NAME' collection (ID: $savedId) for user $userId.")
                    } catch (e: Exception) { Log.e(TAG, "Error creating '$DEFAULT_SAVED_COLLECTION_NAME' collection", e) }
                }
                // --- End Ensure ---

                if (createdNewCollection) {
                    Log.d(TAG, "Refetched collections list size after creation: ${fetchedCollections.size}")
                }

                currentCollections = fetchedCollections // Update the cached list

                // Update favoriteRecipeIds based on the potentially updated list
                val favoritesCollection = currentCollections.find { it.name.equals(RecipeFilters.FAVORITES, ignoreCase = true) }
                favoriteRecipeIds = favoritesCollection?.recipeIds?.toSet() ?: emptySet()
                Log.d(TAG, "Current favorite recipe IDs count: ${favoriteRecipeIds.size}")

                // --- Fetch Preview Images for Collections ---
                val allPreviewRecipeIds = currentCollections
                    .flatMap { it.recipeIds.take(MAX_PREVIEW_IMAGES) } // Get up to N IDs per collection
                    .distinct() // Fetch each unique ID only once

                val previewRecipesMap = mutableMapOf<String, FirebaseRecipe>() // Map ID to Recipe

                if (allPreviewRecipeIds.isNotEmpty()) {
                    Log.d(TAG, "Fetching preview images for ${allPreviewRecipeIds.size} unique recipe IDs.")
                    // Fetch recipe details for all preview IDs in batches of 30 (Firestore limit)
                    allPreviewRecipeIds.chunked(30).forEach { chunk ->
                        try {
                            val recipeSnapshot = recipesCollection.whereIn("__name__", chunk).get().await()
                            recipeSnapshot.documents.forEach { doc ->
                                doc.toObject(FirebaseRecipe::class.java)?.copy(id = doc.id)?.let { recipe ->
                                    previewRecipesMap[doc.id] = recipe // Store fetched recipe in map
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error fetching preview recipe chunk: ${chunk.take(5)}...", e)
                            // Continue fetching other chunks if possible
                        }
                    }
                    Log.d(TAG, "Fetched details for ${previewRecipesMap.size} preview recipes.")
                } else {
                    Log.d(TAG, "No preview recipe IDs to fetch.")
                }

                // --- Combine Collections with their Previews ---
                val collectionsWithPreviews = currentCollections.map { collection ->
                    val previewUrls = collection.recipeIds
                        .take(MAX_PREVIEW_IMAGES) // Take the first N IDs
                        .mapNotNull { id -> previewRecipesMap[id]?.imageUrl } // Get URL from fetched map
                        .filter { it.isNotEmpty() } // Ensure URL is not empty

                    UserCollectionWithPreviews(
                        collection = collection,
                        previewImageUrls = previewUrls
                    )
                }

                // --- Update State ---
                _userCollectionsState.value = UserCollectionState.Success(collectionsWithPreviews)
                Log.i(TAG, "Successfully updated UserCollectionState with ${collectionsWithPreviews.size} collections (including previews).")
                // Optionally reset or update recipe list state if needed after collection changes
                // _savedRecipeListState.value = SavedRecipeListState.Loading // Or trigger a fetch if a view depends on it

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching collections/favorites/previews", e)
                val errorMsg = "Failed to load collections: ${e.localizedMessage ?: "Unknown error"}"
                _userCollectionsState.value = UserCollectionState.Error(errorMsg)
                // Propagate error to other potentially dependent states
                _savedRecipeListState.value = SavedRecipeListState.Error(errorMsg)
            }
        }
    }

    // --- Fetch Recipes for the "All" or "Favorites" Views ---
    fun fetchRecipesForDisplay(filter: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _savedRecipeListState.value = SavedRecipeListState.Error("User not logged in")
            return
        }

        // Ensure filter is valid for this function
        if (filter != RecipeFilters.ALL && filter != RecipeFilters.FAVORITES) {
            Log.w(TAG, "fetchRecipesForDisplay called with invalid filter: $filter. Use fetchRecipesForCollection for specific collections.")
            _savedRecipeListState.value = SavedRecipeListState.Error("Invalid filter for recipe list view")
            return
        }

        _savedRecipeListState.value = SavedRecipeListState.Loading
        Log.i(TAG, "Fetching recipes for display view: '$filter'")

        viewModelScope.launch {
            try {
                // We need currentCollections and favoriteRecipeIds. Ensure they are loaded.
                // If currentCollections is empty, the initial fetch might still be running or failed.
                // A robust solution might involve waiting for the initial fetch using Flows/Jobs,
                // but for simplicity, we rely on it having completed or re-triggering if necessary.
                if (currentCollections.isEmpty() && _userCollectionsState.value !is UserCollectionState.Loading) {
                    Log.w(TAG, "Collections list is empty when fetching recipes for display. Re-triggering initial fetch.")
                    fetchInitialData() // Re-fetch collections first
                    // This might introduce a delay. Consider using combine or SharedFlow for better coordination.
                    kotlinx.coroutines.delay(1000) // Small delay to allow refetch (not ideal)
                }

                // Determine the list of recipe IDs to fetch based on the filter
                val recipeIdsToFetch: List<String> = when (filter) {
                    RecipeFilters.ALL -> {
                        // Get unique recipe IDs from ALL user collections
                        val allIds = currentCollections
                            .flatMap { it.recipeIds } // Combine IDs from all collections
                            .distinct() // Ensure uniqueness
                        Log.d(TAG, "Filter '$filter': Found ${allIds.size} unique IDs across ${currentCollections.size} collections.")
                        allIds
                    }
                    RecipeFilters.FAVORITES -> {
                        // Get IDs only from the cached favoriteRecipeIds set
                        val favIds = favoriteRecipeIds.toList()
                        Log.d(TAG, "Filter '$filter': Found ${favIds.size} favorite IDs.")
                        favIds
                    }
                    else -> {
                        Log.e(TAG, "Reached impossible state in fetchRecipesForDisplay filter logic.")
                        emptyList() // Should not happen due to earlier check
                    }
                }

                Log.d(TAG, "Total recipe IDs to fetch for view '$filter': ${recipeIdsToFetch.size}")

                if (recipeIdsToFetch.isEmpty()) {
                    Log.i(TAG, "No recipe IDs to fetch for filter '$filter'. Setting state to Success(EmptyList).")
                    _savedRecipeListState.value = SavedRecipeListState.Success(emptyList(), source = filter.lowercase())
                    return@launch
                }

                // Fetch actual FirebaseRecipe data for these IDs (using chunking)
                val fetchedFirebaseRecipes = mutableListOf<FirebaseRecipe>()
                recipeIdsToFetch.chunked(30).forEach { chunk ->
                    if (chunk.isNotEmpty()) {
                        try {
                            val snapshot = recipesCollection.whereIn("__name__", chunk).get().await()
                            val recipesInChunk = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(FirebaseRecipe::class.java)?.copy(id = doc.id)
                            }
                            fetchedFirebaseRecipes.addAll(recipesInChunk)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error fetching display recipe chunk: ${chunk.take(5)}...", e)
                            // Continue with other chunks
                        }
                    }
                }
                Log.d(TAG, "Total fetched FirebaseRecipes for display '$filter': ${fetchedFirebaseRecipes.size}")

                // Map FirebaseRecipes to UI Recipe models, marking favorites correctly
                val uiRecipes = fetchedFirebaseRecipes.map { fbRecipe ->
                    mapFirebaseRecipeToRecipe(fbRecipe, favoriteRecipeIds) // Pass current favorites
                }

                // Update the state with the fetched and mapped recipes
                _savedRecipeListState.value = SavedRecipeListState.Success(uiRecipes, source = filter.lowercase())
                Log.i(TAG, "Successfully updated SavedRecipeListState for filter '$filter' with ${uiRecipes.size} recipes.")

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching recipes for view mode '$filter'", e)
                _savedRecipeListState.value = SavedRecipeListState.Error("Failed to load recipes: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    // --- Fetch ALL saved recipes (used for adding to a collection) ---
    fun fetchRecipesForSelection() {
        val userId = auth.currentUser?.uid
        if(userId == null){
            _savedRecipeListState.value = SavedRecipeListState.Error("User not logged in")
            return
        }
        _savedRecipeListState.value = SavedRecipeListState.Loading
        Log.d(TAG, "Fetching all saved recipes for selection UI")

        viewModelScope.launch {
            try {
                // Ensure collections are loaded to get all IDs
                if (currentCollections.isEmpty() && _userCollectionsState.value !is UserCollectionState.Loading) {
                    Log.w(TAG, "Collections empty when fetching for selection. Triggering initial fetch.")
                    fetchInitialData()
                    kotlinx.coroutines.delay(1000) // Allow time for fetch (not ideal)
                }

                // Get all unique recipe IDs from all collections
                val allRecipeIds = currentCollections.flatMap { it.recipeIds }.distinct()
                Log.d(TAG, "All unique recipe IDs for user $userId selection: ${allRecipeIds.size}")

                if (allRecipeIds.isEmpty()) {
                    _savedRecipeListState.value = SavedRecipeListState.Success(emptyList(), source = "selection") // Mark source
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
                        } catch (e: Exception) {
                            Log.w(TAG, "Error fetching selection recipe chunk: ${chunk.take(5)}...", e)
                        }
                    }
                }
                Log.d(TAG, "Total FirebaseRecipes fetched for selection: ${fetchedFirebaseRecipes.size}")

                // Map to UI model, marking favorites
                val uiRecipes = fetchedFirebaseRecipes.map { fbRecipe ->
                    mapFirebaseRecipeToRecipe(fbRecipe, favoriteRecipeIds)
                }

                _savedRecipeListState.value = SavedRecipeListState.Success(uiRecipes, source = "selection") // Mark source

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching recipes for selection", e)
                _savedRecipeListState.value = SavedRecipeListState.Error("Failed to load recipes: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    // --- Toggle Favorite Status (Called from Saved Recipe Lists/Cards) ---
    // This function modifies the "Favorites" collection directly.
    fun toggleFavoriteStatus(recipeId: String, isCurrentlyFavorite: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        // Find the actual ID of the user's "Favorites" collection
        val favoritesCollection = currentCollections.find { it.name.equals(RecipeFilters.FAVORITES, ignoreCase = true) }

        if (favoritesCollection == null) {
            Log.e(TAG, "Cannot toggle favorite: 'Favorites' collection not found for user $userId. Attempting refetch.")
            // Attempt to refetch and potentially create the collection if it was missed somehow
            fetchInitialData()
            // Maybe show an error or disable the button temporarily
            return
        }
        val favoritesCollectionId = favoritesCollection.id

        viewModelScope.launch {
            val newFavoriteStatus = !isCurrentlyFavorite
            Log.d(TAG, "Toggling favorite status for recipe $recipeId in collection $favoritesCollectionId to $newFavoriteStatus")
            try {
                val updateAction = if (newFavoriteStatus) FieldValue.arrayUnion(recipeId) else FieldValue.arrayRemove(recipeId)
                usersCollections.document(favoritesCollectionId)
                    .update("recipeIds", updateAction)
                    .await()
                Log.d(TAG, "Firestore favorite status updated for $recipeId.")

                // --- Update local state IMMEDIATELY for better UX ---
                // 1. Update the `favoriteRecipeIds` set
                val updatedFavoritesSet = if (newFavoriteStatus) {
                    favoriteRecipeIds + recipeId
                } else {
                    favoriteRecipeIds - recipeId
                }
                favoriteRecipeIds = updatedFavoritesSet // Update the cached set

                // 2. Update the currently displayed list in `_savedRecipeListState`
                val currentListState = _savedRecipeListState.value
                if (currentListState is SavedRecipeListState.Success) {
                    // Re-map the existing list in the state with the new favorite status
                    val newlyMappedRecipes = currentListState.recipes.map { recipe ->
                        // Find the recipe and update its favorite property
                        if (recipe.id == recipeId) {
                            recipe.copy(favorite = newFavoriteStatus)
                        } else {
                            // Check if other recipes' favorite status needs update based on the set
                            // (Safer to just map based on the updated set)
                            recipe.copy(favorite = favoriteRecipeIds.contains(recipe.id))
                        }
                    }
                    // Emit the updated list back to the StateFlow
                    _savedRecipeListState.value = currentListState.copy(recipes = newlyMappedRecipes)
                    Log.d(TAG,"Updated local savedRecipeListState with new favorite status for $recipeId")
                }

                // Optionally trigger a full refresh of collections if previews might need updating
                fetchUserCollectionsAndFavorites(userId) // Refresh previews as favorite status affects them potentially

            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite status for $recipeId in SavedRecipesViewModel", e)
                // Consider reverting local state changes or showing an error
            }
        }
    }


    // --- Collection Management ---

    fun createNewCollection(collectionName: String, selectedRecipeIds: List<String>) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _collectionCreationState.value = CollectionCreationState.Error("User not logged in")
            return
        }
        val trimmedName = collectionName.trim()
        if (trimmedName.isBlank()) {
            _collectionCreationState.value = CollectionCreationState.Error("Collection name cannot be empty")
            return
        }
        // Prevent using reserved names
        if (trimmedName.equals(RecipeFilters.FAVORITES, ignoreCase = true) ||
            trimmedName.equals(RecipeFilters.ALL, ignoreCase = true) ||
            trimmedName.equals(DEFAULT_SAVED_COLLECTION_NAME, ignoreCase = true) // Also protect the default name
        ) {
            _collectionCreationState.value = CollectionCreationState.Error("Cannot use reserved name '$trimmedName'")
            return
        }
        // Check if collection with the same name already exists
        if (currentCollections.any { it.userId == userId && it.name.equals(trimmedName, ignoreCase = true) }) {
            _collectionCreationState.value = CollectionCreationState.Error("Collection '$trimmedName' already exists")
            return
        }


        _collectionCreationState.value = CollectionCreationState.Loading
        viewModelScope.launch {
            try {
                val newCollectionDocRef = usersCollections.document() // Generate new ID
                val newCollection = UserCollection(
                    id = newCollectionDocRef.id,
                    userId = userId,
                    name = trimmedName,
                    recipeIds = selectedRecipeIds.distinct() // Ensure unique IDs
                )

                newCollectionDocRef.set(newCollection).await()
                Log.i(TAG, "Successfully created new collection '${newCollection.name}' with ID: ${newCollection.id}")
                _collectionCreationState.value = CollectionCreationState.Success(newCollection.id)
                // Refresh collections list after successful creation
                fetchUserCollectionsAndFavorites(userId)

            } catch (e: Exception) {
                Log.e(TAG, "Error creating new collection '$trimmedName'", e)
                _collectionCreationState.value = CollectionCreationState.Error("Failed to create collection: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun resetCollectionCreationState() {
        _collectionCreationState.value = CollectionCreationState.Idle
    }

    fun fetchRecipesForCollection(collectionId: String) {
        _collectionDetailState.value = CollectionDetailState.Loading
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _collectionDetailState.value = CollectionDetailState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching details for collection: $collectionId")
                // 1. Get the specific collection document
                val collectionDoc = usersCollections.document(collectionId).get().await()
                val collection = collectionDoc.toObject(UserCollection::class.java)?.copy(id = collectionDoc.id) // Include ID

                if (collection == null) {
                    Log.w(TAG, "Collection $collectionId not found.")
                    _collectionDetailState.value = CollectionDetailState.Error("Collection not found")
                    return@launch
                }
                // Security check: Ensure the fetched collection belongs to the current user
                if (collection.userId != userId) {
                    Log.e(TAG, "User $userId attempted to access collection $collectionId belonging to ${collection.userId}.")
                    _collectionDetailState.value = CollectionDetailState.Error("Access denied to this collection")
                    return@launch
                }

                // 2. Get the recipe IDs from the collection
                val recipeIds = collection.recipeIds
                Log.d(TAG, "Found ${recipeIds.size} recipe IDs in collection '${collection.name}' ($collectionId)")

                if (recipeIds.isEmpty()) {
                    _collectionDetailState.value = CollectionDetailState.Success(collection, emptyList())
                    return@launch
                }

                // 3. Fetch the actual recipe data for these IDs
                val fetchedFirebaseRecipes = mutableListOf<FirebaseRecipe>()
                recipeIds.chunked(30).forEach { chunk ->
                    if (chunk.isNotEmpty()) {
                        try {
                            val snapshot = recipesCollection.whereIn("__name__", chunk).get().await()
                            val recipesInChunk = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(FirebaseRecipe::class.java)?.copy(id = doc.id)
                            }
                            fetchedFirebaseRecipes.addAll(recipesInChunk)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error fetching collection recipe chunk: ${chunk.take(5)}...", e)
                        }
                    }
                }
                Log.d(TAG, "Fetched ${fetchedFirebaseRecipes.size} FirebaseRecipe details for collection '${collection.name}'.")

                // Ensure favorite status is up-to-date (it should be from initial fetch, but double-check)
                if (favoriteRecipeIds.isEmpty() && currentCollections.isNotEmpty()) {
                    Log.w(TAG, "favoriteRecipeIds were empty when fetching collection detail. Recalculating.")
                    favoriteRecipeIds = currentCollections.find { it.name.equals(RecipeFilters.FAVORITES, ignoreCase = true) }?.recipeIds?.toSet() ?: emptySet()
                }

                // 4. Map to UI Recipe model, marking favorites
                val uiRecipes = fetchedFirebaseRecipes.map { fbRecipe ->
                    mapFirebaseRecipeToRecipe(fbRecipe, favoriteRecipeIds)
                }

                // 5. Update state
                _collectionDetailState.value = CollectionDetailState.Success(collection, uiRecipes)
                Log.i(TAG, "Successfully loaded collection details for '${collection.name}'.")

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching recipes for collection $collectionId", e)
                _collectionDetailState.value = CollectionDetailState.Error("Failed to load collection: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun updateCollectionName(collectionId: String, newName: String) {
        val userId = auth.currentUser?.uid ?: return
        val trimmedNewName = newName.trim()

        // --- Basic Validation ---
        if (trimmedNewName.isBlank()) {
            _collectionUpdateState.value = CollectionUpdateState.Error("Collection name cannot be empty.")
            return
        }
        if (trimmedNewName.equals(RecipeFilters.FAVORITES, ignoreCase = true) ||
            trimmedNewName.equals(DEFAULT_SAVED_COLLECTION_NAME, ignoreCase = true)) {
            _collectionUpdateState.value = CollectionUpdateState.Error("Cannot use reserved name '$trimmedNewName'.")
            return
        }
        if (currentCollections.any { it.userId == userId && it.id != collectionId && it.name.equals(trimmedNewName, ignoreCase = true) }) {
            _collectionUpdateState.value = CollectionUpdateState.Error("A collection named '$trimmedNewName' already exists.")
            return
        }

        _collectionUpdateState.value = CollectionUpdateState.Loading
        viewModelScope.launch {
            try {
                // --- Check if the collection being renamed is a protected one ---
                val collectionToUpdate = currentCollections.find { it.id == collectionId }
                if (collectionToUpdate == null) {
                    Log.w(TAG, "Attempted to update name for non-existent collection ID: $collectionId")
                    throw Exception("Collection not found.")
                }
                if (collectionToUpdate.userId != userId) {
                    Log.e(TAG,"Attempt to update name for collection $collectionId not owned by user $userId")
                    throw SecurityException("Permission denied to update this collection name.")
                }
                // *** ADDED CHECK for DEFAULT_SAVED_COLLECTION_NAME ***
                if (collectionToUpdate.name.equals(RecipeFilters.FAVORITES, ignoreCase = true) ||
                    collectionToUpdate.name.equals(DEFAULT_SAVED_COLLECTION_NAME, ignoreCase = true)) {
                    Log.w(TAG, "Attempted to rename a protected collection: '${collectionToUpdate.name}' (ID: $collectionId)")
                    // Set specific error state
                    _collectionUpdateState.value = CollectionUpdateState.Error("Cannot rename the '${collectionToUpdate.name}' collection.")
                    return@launch // Stop execution for this function
                }
                // --- End Check ---


                // Proceed with renaming if it's not protected
                usersCollections.document(collectionId)
                    .update("name", trimmedNewName)
                    .await()
                Log.i(TAG, "Updated collection $collectionId name to '$trimmedNewName'")
                _collectionUpdateState.value = CollectionUpdateState.Success

                // Refresh lists and potentially the detail view
                fetchUserCollectionsAndFavorites(userId)

                val currentDetailState = _collectionDetailState.value
                if (currentDetailState is CollectionDetailState.Success && currentDetailState.collection.id == collectionId) {
                    _collectionDetailState.value = currentDetailState.copy(
                        collection = currentDetailState.collection.copy(name = trimmedNewName)
                    )
                    Log.d(TAG, "Updated collection name in CollectionDetailState as well.")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error updating collection name for $collectionId", e)
                // Avoid setting a generic error if a specific one was already set (like "Cannot rename...")
                if (_collectionUpdateState.value !is CollectionUpdateState.Error) {
                    _collectionUpdateState.value = CollectionUpdateState.Error("Failed to update name: ${e.localizedMessage ?: "Unknown error"}")
                }
            } finally {
                if (_collectionUpdateState.value is CollectionUpdateState.Loading) {
                    _collectionUpdateState.value = CollectionUpdateState.Idle
                }
            }
        }
    }

    fun removeRecipesFromCollection(collectionId: String, recipeIdsToRemove: List<String>) {
        if (recipeIdsToRemove.isEmpty()) return
        val userId = auth.currentUser?.uid ?: return

        // --- Prevent removing from special collections via this generic method? ---
        // Although removing from Favorites should happen via toggleFavorite,
        // and removing from Saved Recipes via toggleBookmark.
        // This method is primarily for user-created collections.
        val collection = currentCollections.find { it.id == collectionId }
        if (collection?.name?.equals(RecipeFilters.FAVORITES, ignoreCase = true) == true) {
            Log.w(TAG, "Attempt to remove recipes from Favorites collection using generic remove method. Use toggleFavoriteStatus instead.")
            _collectionUpdateState.value = CollectionUpdateState.Error("Use the heart icon to remove from Favorites.")
            return
        }
        if (collection?.name?.equals(DEFAULT_SAVED_COLLECTION_NAME, ignoreCase = true) == true) {
            Log.w(TAG, "Attempt to remove recipes from '$DEFAULT_SAVED_COLLECTION_NAME' collection using generic remove method. Use toggleBookmarkStatus instead.")
            _collectionUpdateState.value = CollectionUpdateState.Error("Use the bookmark icon to remove.")
            return
        }
        // --- End Prevention ---


        _collectionUpdateState.value = CollectionUpdateState.Loading
        viewModelScope.launch {
            try {
                usersCollections.document(collectionId)
                    .update("recipeIds", FieldValue.arrayRemove(*recipeIdsToRemove.toTypedArray()))
                    .await()
                Log.i(TAG, "Removed ${recipeIdsToRemove.size} recipes from collection $collectionId")
                _collectionUpdateState.value = CollectionUpdateState.Success

                // Refresh the detail view after removal & main list previews
                fetchRecipesForCollection(collectionId) // Re-fetch details for the affected collection
                fetchUserCollectionsAndFavorites(userId) // Refresh overview list and previews

            } catch (e: Exception) {
                Log.e(TAG, "Error removing recipes from $collectionId", e)
                _collectionUpdateState.value = CollectionUpdateState.Error("Failed to remove recipes: ${e.localizedMessage ?: "Unknown error"}")
            } finally {
                if (_collectionUpdateState.value is CollectionUpdateState.Loading) {
                    _collectionUpdateState.value = CollectionUpdateState.Idle
                }
            }
        }
    }

    fun addRecipesToCollection(collectionId: String, recipeIdsToAdd: List<String>) {
        if (recipeIdsToAdd.isEmpty()) return
        val userId = auth.currentUser?.uid ?: return

        // Prevent adding to special collections via this method? Generally okay, but maybe confusing.
        val collection = currentCollections.find { it.id == collectionId }
        if (collection?.name?.equals(RecipeFilters.FAVORITES, ignoreCase = true) == true) {
            Log.w(TAG, "Adding recipes to Favorites collection using generic add method. Consider using toggleFavoriteStatus for consistency.")
            // Allow it, but log a warning. toggleFavoriteStatus handles UI updates better.
        }
        if (collection?.name?.equals(DEFAULT_SAVED_COLLECTION_NAME, ignoreCase = true) == true) {
            Log.w(TAG, "Adding recipes to '$DEFAULT_SAVED_COLLECTION_NAME' collection using generic add method. Consider using toggleBookmarkStatus.")
            // Allow it.
        }


        _collectionUpdateState.value = CollectionUpdateState.Loading
        viewModelScope.launch {
            try {
                // Use arrayUnion to add IDs without creating duplicates if they already exist
                usersCollections.document(collectionId)
                    .update("recipeIds", FieldValue.arrayUnion(*recipeIdsToAdd.toTypedArray()))
                    .await()
                Log.i(TAG, "Added/Ensured ${recipeIdsToAdd.size} recipes in collection $collectionId")
                _collectionUpdateState.value = CollectionUpdateState.Success

                // Refresh the detail view after adding & main list previews
                fetchRecipesForCollection(collectionId) // Re-fetch details for the affected collection
                fetchUserCollectionsAndFavorites(userId) // Refresh overview list and previews

            } catch (e: Exception) {
                Log.e(TAG, "Error adding recipes to $collectionId", e)
                _collectionUpdateState.value = CollectionUpdateState.Error("Failed to add recipes: ${e.localizedMessage ?: "Unknown error"}")
            } finally {
                if (_collectionUpdateState.value is CollectionUpdateState.Loading) {
                    _collectionUpdateState.value = CollectionUpdateState.Idle
                }
            }
        }
    }

    fun deleteCollection(collectionId: String) {
        val userId = auth.currentUser?.uid ?: return

        _collectionUpdateState.value = CollectionUpdateState.Loading // Indicate process start
        viewModelScope.launch {
            try {
                // --- Check if the collection being deleted is a protected one ---
                val collectionToDelete = currentCollections.find { it.id == collectionId }

                if (collectionToDelete == null) {
                    Log.w(TAG, "Attempted to delete non-existent collection ID: $collectionId")
                    throw Exception("Collection not found.")
                }
                if (collectionToDelete.userId != userId) {
                    Log.e(
                        TAG,
                        "Attempt to delete collection $collectionId not owned by user $userId"
                    )
                    throw SecurityException("Permission denied to delete this collection.")
                }
                // *** ADDED CHECK for DEFAULT_SAVED_COLLECTION_NAME ***
                if (collectionToDelete.name.equals(RecipeFilters.FAVORITES, ignoreCase = true) ||
                    collectionToDelete.name.equals(DEFAULT_SAVED_COLLECTION_NAME, ignoreCase = true)
                ) {
                    Log.w(
                        TAG,
                        "Attempted to delete a protected collection: '${collectionToDelete.name}' (ID: $collectionId)"
                    )
                    // Set specific error state
                    _collectionUpdateState.value =
                        CollectionUpdateState.Error("Cannot delete the '${collectionToDelete.name}' collection.")
                    // Set detail state back to Success if it was Deleted, otherwise leave it
                    val currentDetail = _collectionDetailState.value
                    if (currentDetail is CollectionDetailState.Deleted) {
                        // If we incorrectly set it to Deleted, try to revert (might need refetch)
                        Log.d(
                            TAG,
                            "Reverting detail state from Deleted because protected collection delete was denied."
                        )
                        fetchRecipesForCollection(collectionId) // Refetch to show it still exists
                    }
                    return@launch // Stop execution for this function
                }
                // --- End Check ---

                // Proceed with deletion if it's not protected
                usersCollections.document(collectionId).delete().await()
                Log.i(TAG, "Deleted collection $collectionId ('${collectionToDelete.name}')")

                _collectionDetailState.value =
                    CollectionDetailState.Deleted // Trigger navigation/UI change
                _collectionUpdateState.value =
                    CollectionUpdateState.Success // General success feedback

                // Refresh the main collections list
                fetchUserCollectionsAndFavorites(userId)

            } catch (e: Exception) {
                Log.e(TAG, "Error deleting collection $collectionId", e)
                // Avoid setting a generic error if a specific one was already set
                if (_collectionUpdateState.value !is CollectionUpdateState.Error) {
                    _collectionUpdateState.value =
                        CollectionUpdateState.Error("Failed to delete collection: ${e.localizedMessage ?: "Unknown error"}")
                }
                // If deletion failed, ensure detail state isn't stuck on Deleted
                if (_collectionDetailState.value is CollectionDetailState.Deleted) {
                    Log.d(
                        TAG,
                        "Reverting detail state from Deleted because delete operation failed."
                    )
                    fetchRecipesForCollection(collectionId) // Refetch
                }
            } finally {
                // Ensure update state is reset from Loading if not handled by specific error/success
                if (_collectionUpdateState.value is CollectionUpdateState.Loading) {
                    _collectionUpdateState.value = CollectionUpdateState.Idle
                }
            }
        }
    }

    // Reset the update state (call this from UI after handling Success/Error)
    fun resetCollectionUpdateState() {
        if (_collectionUpdateState.value != CollectionUpdateState.Idle) {
            Log.d(TAG,"Resetting CollectionUpdateState from ${_collectionUpdateState.value} to Idle.")
            _collectionUpdateState.value = CollectionUpdateState.Idle
        }
    }


    // --- Helper Functions ---

    // Function to get recipes available to add (all saved recipes minus those already in the target collection)
    fun getRecipesAvailableForAdding(targetCollectionId: String): List<Recipe> {
        // Find the target collection to get its current recipe IDs
        val targetCollection = currentCollections.find { it.id == targetCollectionId }
        val targetRecipeIds = targetCollection?.recipeIds?.toSet() ?: emptySet()

        // Get all recipes the user has saved across *all* collections
        val allSavedRecipesState = savedRecipeListState.value // Assuming this holds the result of fetchRecipesForSelection or similar
        if (allSavedRecipesState is SavedRecipeListState.Success) {
            // Filter out recipes already present in the target collection
            val availableRecipes = allSavedRecipesState.recipes.filter { it.id !in targetRecipeIds }
            Log.d(TAG, "Found ${availableRecipes.size} recipes available to add to collection $targetCollectionId")
            return availableRecipes
        } else {
            // If all saved recipes aren't loaded yet (e.g., state is Loading or Error)
            Log.w(TAG, "All saved recipes not yet available for adding selection. Current state: $allSavedRecipesState")
            // Optionally trigger fetchRecipesForSelection() if not already loading/done
            if (allSavedRecipesState !is SavedRecipeListState.Loading) {
                // fetchRecipesForSelection() // Be careful of triggering loops
            }
            return emptyList() // Return empty list for now
        }
    }

    // Map FirebaseRecipe to the simpler Recipe UI model used in lists/grids
    private fun mapFirebaseRecipeToRecipe(
        fbRecipe: FirebaseRecipe,
        currentFavoriteIds: Set<String> // Pass the up-to-date set of favorite IDs
    ): Recipe {
        return Recipe(
            id = fbRecipe.id,
            nameOfPerson = fbRecipe.authorName.takeIf { it.isNotEmpty() } ?: "Unknown Chef",
            name = fbRecipe.name.takeIf { it.isNotEmpty() } ?: "Unnamed Recipe",
            imageUrl = fbRecipe.imageUrl,
            averageRating = fbRecipe.averageRating,
            category = fbRecipe.category.takeIf { it.isNotEmpty() } ?: "Uncategorized",
            cookingTime = fbRecipe.cookingTime.takeIf { it.isNotEmpty() } ?: "N/A",
            serving = fbRecipe.servings.takeIf { it > 0 } ?: 1,
            favorite = currentFavoriteIds.contains(fbRecipe.id) // Set favorite status based on the provided set
        )
    }

    // Function to be called when ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared.")
        // Cancel any running coroutines if necessary (viewModelScope handles this automatically)
    }
}