package com.example.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirebaseRecipe
import com.example.myapplication.data.IngredientItem
import com.example.myapplication.data.NutritionItem
import com.example.myapplication.data.RecipeDetail
import com.example.myapplication.data.UserCollection // Import UserCollection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions // Import SetOptions
import com.google.firebase.firestore.ktx.toObject // Import toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.example.myapplication.viewModel.RecipeFilters // Import for constant

// Define the constant here or import if defined elsewhere


sealed class RecipeDetailState {
    object Idle : RecipeDetailState()
    object Loading : RecipeDetailState()
    data class Success(val recipeDetail: RecipeDetail, val isOwner: Boolean) : RecipeDetailState()
    data class Error(val message: String) : RecipeDetailState()
}

sealed class RecipeUpdateState { // Keep this if used for rating updates, though not explicitly used in toggle functions now
    object Idle : RecipeUpdateState()
    object Loading : RecipeUpdateState()
    object Success : RecipeUpdateState()
    data class Error(val message: String) : RecipeUpdateState()
}

class RecipeDetailViewModel : ViewModel() {

    private val TAG = "RecipeDetailViewModel"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val recipesCollection = db.collection("recipes")
    private val usersCollections = db.collection("user_collections") // Use this collection

    private val _recipeDetailState = MutableStateFlow<RecipeDetailState>(RecipeDetailState.Idle)
    val recipeDetailState: StateFlow<RecipeDetailState> = _recipeDetailState.asStateFlow()

    // --- Fetch Recipe Data and Interaction Status ---
    fun fetchRecipeById(recipeId: String) {
        val currentUserId = auth.currentUser?.uid
        if (recipeId.isBlank()) {
            _recipeDetailState.value = RecipeDetailState.Error("Invalid Recipe ID")
            return
        }
        // Prevent re-fetching if already loaded (optional, can be removed if refresh is always desired)
        val currentState = _recipeDetailState.value
        if (currentState is RecipeDetailState.Success && currentState.recipeDetail.id == recipeId) {
            Log.d(TAG, "Recipe $recipeId potentially already loaded. Re-fetching interaction status.")
            // Still proceed to fetch, especially user interaction data which might change
        }

        _recipeDetailState.value = RecipeDetailState.Loading
        viewModelScope.launch {
            try {
                // 1. Fetch Recipe Data
                val documentSnapshot = recipesCollection.document(recipeId).get().await()
                val firebaseRecipe = documentSnapshot.toObject(FirebaseRecipe::class.java)?.copy(id = documentSnapshot.id)

                if (firebaseRecipe == null || firebaseRecipe.id.isBlank()) {
                    _recipeDetailState.value = RecipeDetailState.Error("Recipe not found or invalid data.")
                    Log.w(TAG, "Recipe not found for ID: $recipeId")
                    return@launch
                }

                // 2. Fetch User Collections and Determine Interaction Status
                var isFavorite = false
                var isBookmarked = false // Represents being in the DEFAULT_SAVED_COLLECTION
                var userRating = firebaseRecipe.userRatings[currentUserId] ?: 0

                if (currentUserId != null) {
                    try {
                        // Get all collections for the user to check status
                        val collectionsSnapshot = usersCollections.whereEqualTo("userId", currentUserId).get().await()
                        val userCollections = collectionsSnapshot.documents.mapNotNull {
                            it.toObject(UserCollection::class.java)?.copy(id = it.id)
                        }

                        // Check "Favorites" collection
                        val favoritesCollection = userCollections.find { it.name.equals(RecipeFilters.FAVORITES, ignoreCase = true) }
                        isFavorite = favoritesCollection?.recipeIds?.contains(recipeId) ?: false

                        // Check Default "Saved Recipes" collection for bookmark status
                        val defaultSavedCollection = userCollections.find { it.name.equals(DEFAULT_SAVED_COLLECTION_NAME, ignoreCase = true) }
                        isBookmarked = defaultSavedCollection?.recipeIds?.contains(recipeId) ?: false

                        Log.d(TAG, "User $currentUserId collections checked for $recipeId - Fav: $isFavorite, Bookmarked (in '$DEFAULT_SAVED_COLLECTION_NAME'): $isBookmarked, Rating: $userRating")

                    } catch (userFetchError: Exception) {
                        Log.w(TAG, "Could not fetch user collections for interaction status for recipe $recipeId", userFetchError)
                        // Proceed with defaults (false, false) if fetching collections fails
                    }
                }

                // 3. Determine Ownership
                val isOwner = currentUserId != null && currentUserId == firebaseRecipe.authorId

                // 4. Map to UI Model (Pass correct isFavorite and isBookmarked)
                val recipeDetail = mapFirebaseRecipeToDetail(firebaseRecipe, isFavorite, isBookmarked, userRating)

                // 5. Emit Success State
                _recipeDetailState.value = RecipeDetailState.Success(recipeDetail, isOwner)
                Log.d(TAG, "Successfully fetched recipe: ${firebaseRecipe.id}. Owner: $isOwner")

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching recipe details for ID: $recipeId", e)
                _recipeDetailState.value = RecipeDetailState.Error("Failed to load recipe: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    // --- Helper to Get or Create a User Collection by Name ---
    private suspend fun getOrCreateCollectionByName(userId: String, collectionName: String): String? {
        return try {
            // Attempt to find the existing collection
            val existingCollection = usersCollections
                .whereEqualTo("userId", userId)
                .whereEqualTo("name", collectionName)
                .limit(1)
                .get()
                .await()

            if (!existingCollection.isEmpty) {
                // Return the ID of the existing collection
                existingCollection.documents.first().id
            } else {
                // Create the collection if it doesn't exist
                val newCollectionRef = usersCollections.document() // Generate a new ID
                val newCollection = UserCollection(
                    id = newCollectionRef.id,
                    userId = userId,
                    name = collectionName,
                    recipeIds = emptyList() // Initially empty
                )
                newCollectionRef.set(newCollection).await() // Create the document
                Log.d(TAG, "Created new collection '$collectionName' with ID: ${newCollection.id} for user $userId")
                newCollection.id // Return the ID of the newly created collection
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding or creating collection '$collectionName' for user $userId", e)
            null // Return null if an error occurs
        }
    }

    // --- Toggle Favorite Status ---
    fun toggleFavoriteStatus(recipeId: String, currentStatus: Boolean) {
        val userId = auth.currentUser?.uid ?: return // Ensure user is logged in
        val currentState = _recipeDetailState.value
        // Ensure we are in a success state and the recipe ID matches
        if (currentState !is RecipeDetailState.Success || currentState.recipeDetail.id != recipeId) {
            Log.w(TAG, "Cannot toggle favorite. Invalid state or recipe ID mismatch.")
            return
        }

        val newFavoriteStatus = !currentStatus
        Log.d(TAG, "Toggling Favorite for recipe $recipeId for user $userId to $newFavoriteStatus")

        // Optimistic UI Update: Change the state immediately for responsiveness
        val optimisticDetail = currentState.recipeDetail.copy(isFavorite = newFavoriteStatus)
        _recipeDetailState.value = RecipeDetailState.Success(optimisticDetail, currentState.isOwner)

        viewModelScope.launch {
            try {
                // Get the ID of the "Favorites" collection, creating it if necessary
                val favoritesCollectionId = getOrCreateCollectionByName(userId, RecipeFilters.FAVORITES)
                if (favoritesCollectionId == null) {
                    // Handle the case where the collection couldn't be found or created
                    throw IllegalStateException("Could not get or create Favorites collection for user $userId.")
                }

                // Determine the Firestore update action (add or remove the recipe ID)
                val updateAction = if (newFavoriteStatus) FieldValue.arrayUnion(recipeId) else FieldValue.arrayRemove(recipeId)

                // Perform the update on the "Favorites" collection document
                usersCollections.document(favoritesCollectionId)
                    .update("recipeIds", updateAction)
                    .await()

                Log.i(TAG, "Firestore Favorite status updated successfully for $recipeId to $newFavoriteStatus in collection $favoritesCollectionId")
                // No need to re-fetch here, the optimistic update is now confirmed by the successful write.
                // The SavedRecipesViewModel will need to refresh its data independently when its screen becomes active.

            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite for $recipeId", e)
                // Revert Optimistic Update on Error: Restore the previous state
                _recipeDetailState.value = currentState
                // Optionally: Set an error state or show a message to the user
                // _recipeDetailState.value = RecipeDetailState.Error("Failed to update favorite status.")
            }
        }
    }

    // --- Toggle Bookmark Status ---
    fun toggleBookmarkStatus(recipeId: String, currentStatus: Boolean) {
        val userId = auth.currentUser?.uid ?: return // Ensure user is logged in
        val currentState = _recipeDetailState.value
        // Ensure we are in a success state and the recipe ID matches
        if (currentState !is RecipeDetailState.Success || currentState.recipeDetail.id != recipeId) {
            Log.w(TAG, "Cannot toggle bookmark. Invalid state or recipe ID mismatch.")
            return
        }

        // This status refers to membership in the DEFAULT_SAVED_COLLECTION_NAME
        val newBookmarkStatus = !currentStatus
        Log.d(TAG, "Toggling Bookmark (in '$DEFAULT_SAVED_COLLECTION_NAME') for recipe $recipeId for user $userId to $newBookmarkStatus")

        // Optimistic UI Update
        val optimisticDetail = currentState.recipeDetail.copy(isBookmarked = newBookmarkStatus)
        _recipeDetailState.value = RecipeDetailState.Success(optimisticDetail, currentState.isOwner)

        viewModelScope.launch {
            try {
                // Get or create the default "Saved Recipes" collection ID
                val defaultCollectionId = getOrCreateCollectionByName(userId, DEFAULT_SAVED_COLLECTION_NAME)
                if (defaultCollectionId == null) {
                    throw IllegalStateException("Could not get or create '$DEFAULT_SAVED_COLLECTION_NAME' collection for user $userId.")
                }

                // Determine the Firestore update action
                val updateAction = if (newBookmarkStatus) FieldValue.arrayUnion(recipeId) else FieldValue.arrayRemove(recipeId)

                // Perform the update on the default collection document
                usersCollections.document(defaultCollectionId)
                    .update("recipeIds", updateAction)
                    .await()

                Log.i(TAG, "Firestore Bookmark status (in '$DEFAULT_SAVED_COLLECTION_NAME') updated successfully for $recipeId to $newBookmarkStatus in collection $defaultCollectionId")
                // No need to re-fetch here for the detail screen state.
                // SavedRecipesViewModel needs to refresh independently.

            } catch (e: Exception) {
                Log.e(TAG, "Error toggling bookmark for $recipeId", e)
                // Revert Optimistic Update
                _recipeDetailState.value = currentState
                // Optionally: Set error state or show message
                // _recipeDetailState.value = RecipeDetailState.Error("Failed to update bookmark status.")
            }
        }
    }

    // --- Update Recipe Rating ---
    fun updateRecipeRating(recipeId: String, newRating: Int) {
        val userId = auth.currentUser?.uid ?: return // Need user ID
        if (recipeId.isBlank() || newRating < 0 || newRating > 5) {
            Log.w(TAG, "Invalid rating update request. RecipeID: $recipeId, Rating: $newRating")
            return // Basic validation
        }

        val currentState = _recipeDetailState.value
        if (currentState !is RecipeDetailState.Success || currentState.recipeDetail.id != recipeId) {
            Log.w(TAG, "Cannot update rating. Invalid state or recipe ID mismatch.")
            return
        }
        // Prevent unnecessary updates if the rating hasn't actually changed
        if (currentState.recipeDetail.userRating == newRating) {
            Log.d(TAG, "Rating unchanged ($newRating) for recipe $recipeId, skipping Firestore update.")
            return
        }

        viewModelScope.launch {
            val recipeRef = recipesCollection.document(recipeId)
            try {
                var finalAverageRating = 0.0
                var finalRatingCount = 0
                // Run rating update within a Firestore transaction for atomicity
                db.runTransaction { transaction ->
                    val currentRecipeDoc = transaction.get(recipeRef)
                    val currentRecipe = currentRecipeDoc.toObject(FirebaseRecipe::class.java)
                        ?: throw FirebaseFirestoreException("Recipe not found in transaction", FirebaseFirestoreException.Code.NOT_FOUND)

                    val currentRatingsMap = currentRecipe.userRatings.toMutableMap()
                    val oldRatingForUser = currentRatingsMap[userId]

                    // Double-check if the rating actually needs changing within the transaction
                    if (oldRatingForUser == newRating) {
                        Log.d(TAG, "Transaction check: Rating $newRating already set in Firestore for user $userId.")
                        // Assign current values to update local state accurately even if no DB change happens now
                        finalRatingCount = currentRecipe.ratingCount
                        finalAverageRating = currentRecipe.averageRating
                        return@runTransaction null // Indicate no change needed
                    }

                    // Update the user's rating in the map
                    if (newRating == 0) { // User cleared their rating
                        currentRatingsMap.remove(userId)
                    } else {
                        currentRatingsMap[userId] = newRating
                    }

                    // Recalculate average rating and count based on the *updated* map
                    val validRatings = currentRatingsMap.filterValues { it in 1..5 } // Consider only valid ratings (1-5)
                    finalRatingCount = validRatings.size
                    finalAverageRating = if (finalRatingCount > 0) {
                        validRatings.values.sum().toDouble() / finalRatingCount
                    } else {
                        0.0 // No valid ratings means average is 0
                    }
                    // Format to one decimal place for storage
                    val formattedAverage = String.format("%.1f", finalAverageRating).toDouble()

                    // Update Firestore document within the transaction
                    transaction.update(recipeRef, mapOf(
                        "userRatings" to currentRatingsMap, // The updated map of user ratings
                        "ratingCount" to finalRatingCount, // The new total count of ratings
                        "averageRating" to formattedAverage // The newly calculated and formatted average
                    ))
                    Log.d(TAG, "Transaction: Updated rating for user $userId to $newRating. New avg: $formattedAverage, count: $finalRatingCount")
                    // Return updated values to use outside the transaction
                    mapOf("avg" to formattedAverage, "count" to finalRatingCount)
                }.await() // Wait for the transaction to complete

                // Update the local StateFlow only AFTER the transaction succeeds
                val finalState = _recipeDetailState.value // Get the latest state again
                if (finalState is RecipeDetailState.Success && finalState.recipeDetail.id == recipeId) {
                    val updatedDetail = finalState.recipeDetail.copy(
                        userRating = newRating, // Update the user's own rating display
                        averageRating = finalAverageRating, // Update average display (use recalculated value)
                        ratingCount = finalRatingCount // Update count display (use recalculated value)
                    )
                    _recipeDetailState.value = RecipeDetailState.Success(updatedDetail, finalState.isOwner)
                    Log.d(TAG, "Local state updated after successful rating change for recipe $recipeId.")
                } else {
                    Log.w(TAG, "State changed during rating update for $recipeId, re-fetching might be needed.")
                    // Optionally re-fetch if state consistency is critical and complex to manage otherwise
                    fetchRecipeById(recipeId) // Re-fetch to ensure consistency if state changed unexpectedly
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error updating rating for recipe $recipeId", e)
                // On error, re-fetch the data from Firestore to ensure the UI reflects the actual DB state
                fetchRecipeById(recipeId)
                // Optionally set an error state
                // _recipeDetailState.value = RecipeDetailState.Error("Failed to update rating.")
            }
        }
    }

    // --- Map FirebaseRecipe to RecipeDetail UI Model ---
    private fun mapFirebaseRecipeToDetail(
        fbRecipe: FirebaseRecipe,
        isFavorite: Boolean,    // Determined from "Favorites" collection
        isBookmarked: Boolean, // Determined from DEFAULT_SAVED_COLLECTION_NAME collection
        userRating: Int         // User's specific rating from fbRecipe.userRatings
    ): RecipeDetail {
        // Map ingredients
        val ingredientsList = fbRecipe.ingredients.mapNotNull { map ->
            val name = map["name"] as? String ?: return@mapNotNull null
            val quantityAny = map["quantity"]
            val quantityString = when (quantityAny) {
                is Number -> if (quantityAny.toDouble() == quantityAny.toLong().toDouble()) quantityAny.toLong().toString() else quantityAny.toString()
                is String -> quantityAny
                else -> ""
            }
            IngredientItem(
                quantity = quantityString,
                unit = map["unit"] as? String, // Can be null
                name = name
            )
        }

        // Map tags (example: from cuisine and category)
        val tagsList = mutableListOf<String>()
        if (fbRecipe.cuisine.isNotBlank()) tagsList.add(fbRecipe.cuisine)
        if (fbRecipe.category.isNotBlank()) tagsList.add(fbRecipe.category)
        // Add other potential tag sources if they exist in FirebaseRecipe

        // Map nutrition facts (if available in FirebaseRecipe - currently assumed null)
        // val nutritionList = fbRecipe.nutrition?.map { NutritionItem(...) } ?: emptyList()

        return RecipeDetail(
            id = fbRecipe.id,
            title = fbRecipe.name.takeIf { it.isNotEmpty() } ?: "Untitled Recipe",
            imageUrl = fbRecipe.imageUrl, // Can be empty/null
            cuisine = fbRecipe.cuisine,
            tags = tagsList.distinct(), // Use the generated tags list
            isFavorite = isFavorite,    // Set based on check
            isBookmarked = isBookmarked, // Set based on check
            servingSize = fbRecipe.servings.takeIf { it > 0 } ?: 1, // Ensure serving size is at least 1
            cookingTime = fbRecipe.cookingTime,
            prepTime = fbRecipe.prepTime,
            ingredients = ingredientsList,
            procedureSteps = fbRecipe.instructions, // Assume instructions is List<String>
            nutritionFacts = null, // Replace with actual mapping if data exists
            authorName = fbRecipe.authorName.takeIf { it.isNotEmpty() } ?: "Unknown Author",
            personalNote = fbRecipe.personalNote.takeIf { it.isNotBlank() }, // Show note only if not blank
            userRating = userRating, // The user's own rating
            authorId = fbRecipe.authorId,
            ratingCount = fbRecipe.ratingCount, // Total number of ratings
            averageRating = fbRecipe.averageRating // Overall average rating
        )
    }
}