package com.example.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirebaseRecipe
import com.example.myapplication.data.IngredientItem
import com.example.myapplication.data.NutritionItem
import com.example.myapplication.data.RecipeDetail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

sealed class RecipeDetailState {
    object Idle : RecipeDetailState()
    object Loading : RecipeDetailState()
    data class Success(val recipeDetail: RecipeDetail, val isOwner: Boolean) : RecipeDetailState()
    data class Error(val message: String) : RecipeDetailState()
}

sealed class RecipeUpdateState {
    object Idle : RecipeUpdateState()
    object Loading : RecipeUpdateState()
    object Success : RecipeUpdateState() // Generic success for now
    data class Error(val message: String) : RecipeUpdateState()
}


class RecipeDetailViewModel : ViewModel() {

    private val TAG = "RecipeDetailViewModel"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val recipesCollection = db.collection("recipes")
    // Assume collections for user interactions (adjust names as needed)
    private val userFavoritesCollection = db.collection("user_favorites")
    private val userBookmarksCollection = db.collection("user_bookmarks")


    private val _recipeDetailState = MutableStateFlow<RecipeDetailState>(RecipeDetailState.Idle)
    val recipeDetailState: StateFlow<RecipeDetailState> = _recipeDetailState.asStateFlow()

    private val _recipeUpdateState = MutableStateFlow<RecipeUpdateState>(RecipeUpdateState.Idle)
    val recipeUpdateState: StateFlow<RecipeUpdateState> = _recipeUpdateState.asStateFlow()


    fun fetchRecipeById(recipeId: String) {
        val currentUserId = auth.currentUser?.uid // Get current user ID once
        if (recipeId.isBlank()) {
            _recipeDetailState.value = RecipeDetailState.Error("Invalid Recipe ID")
            return
        }
        _recipeDetailState.value = RecipeDetailState.Loading
        viewModelScope.launch {
            try {
                // Fetch Recipe Data
                val documentSnapshot = recipesCollection.document(recipeId).get().await()
                val firebaseRecipe = documentSnapshot.toObject(FirebaseRecipe::class.java)?.copy(id = documentSnapshot.id)

                if (firebaseRecipe == null || firebaseRecipe.id.isBlank()) {
                    _recipeDetailState.value = RecipeDetailState.Error("Recipe not found or invalid data.")
                    return@launch
                }

                // Fetch User Interaction Data (concurrently if possible, or sequentially)
                var isFavorite = false
                var isBookmarked = false
                if (currentUserId != null) {
                    try { // Separate try-catch for user data fetching
                        val favDoc = userFavoritesCollection.document(currentUserId).get().await()
                        val bookmarkDoc = userBookmarksCollection.document(currentUserId).get().await()
                        isFavorite = (favDoc.get("recipeIds") as? List<*>)?.contains(recipeId) ?: false
                        isBookmarked = (bookmarkDoc.get("recipeIds") as? List<*>)?.contains(recipeId) ?: false
                        Log.d(TAG, "User $currentUserId - Favorite: $isFavorite, Bookmark: $isBookmarked for recipe $recipeId")
                    } catch (userFetchError: Exception) {
                        Log.w(TAG, "Could not fetch user favorite/bookmark status for recipe $recipeId", userFetchError)
                        // Proceed without favorite/bookmark status if fetching fails
                    }
                }

                // Determine Ownership
                val isOwner = currentUserId != null && currentUserId == firebaseRecipe.authorId

                // Map to UI Model, including fetched user interaction status
                val recipeDetail = mapFirebaseRecipeToDetail(firebaseRecipe, isFavorite, isBookmarked)

                // Emit Success State
                _recipeDetailState.value = RecipeDetailState.Success(recipeDetail, isOwner)
                Log.d(TAG, "Successfully fetched recipe: ${firebaseRecipe.id}. Owner: $isOwner")

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching recipe details for ID: $recipeId", e)
                _recipeDetailState.value = RecipeDetailState.Error("Failed to load recipe: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }


    fun updateRecipeRating(recipeId: String, newRating: Int) {
        val userId = auth.currentUser?.uid ?: return Unit.also { _recipeUpdateState.value = RecipeUpdateState.Error("User not logged in.") }
        if (recipeId.isBlank() || newRating < 0 || newRating > 5) return Unit.also { _recipeUpdateState.value = RecipeUpdateState.Error("Invalid input.") }

        _recipeUpdateState.value = RecipeUpdateState.Loading
        viewModelScope.launch {
            val recipeRef = recipesCollection.document(recipeId)
            try {
                db.runTransaction { transaction ->
                    val currentRecipe = transaction.get(recipeRef).toObject(FirebaseRecipe::class.java) ?: throw Exception("Recipe not found")
                    val currentRatings = currentRecipe.userRatings.toMutableMap()
                    if (currentRatings[userId] == newRating) return@runTransaction null // No change

                    if (newRating == 0) currentRatings.remove(userId) else currentRatings[userId] = newRating

                    val validRatings = currentRatings.filterValues { it in 1..5 }
                    val newRatingCount = validRatings.size
                    val newAverageRating = if (newRatingCount > 0) validRatings.values.sum().toDouble() / newRatingCount else 0.0
                    val formattedAverage = String.format("%.1f", newAverageRating).toDouble()

                    transaction.update(recipeRef, mapOf( "userRatings" to currentRatings, "ratingCount" to newRatingCount, "averageRating" to formattedAverage ))
                    null
                }.await()

                if(_recipeUpdateState.value == RecipeUpdateState.Loading) {
                    _recipeUpdateState.value = RecipeUpdateState.Success
                    fetchRecipeById(recipeId) // Re-fetch
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating rating for recipe $recipeId", e)
                _recipeUpdateState.value = RecipeUpdateState.Error("Failed to update rating: ${e.localizedMessage}")
            }
        }
    }

    fun toggleFavoriteStatus(recipeId: String, currentStatus: Boolean) {
        val userId = auth.currentUser?.uid ?: return // Need logged-in user
        val updateAction = if (currentStatus) FieldValue.arrayRemove(recipeId) else FieldValue.arrayUnion(recipeId)
        Log.d(TAG, "Toggling Favorite for recipe $recipeId for user $userId to ${!currentStatus}")

        viewModelScope.launch {
            try {
                // Assume document exists, create if not (more robust check needed in prod)
                userFavoritesCollection.document(userId).set(mapOf("userId" to userId), com.google.firebase.firestore.SetOptions.merge()).await() // Ensure doc exists
                userFavoritesCollection.document(userId).update("recipeIds", updateAction).await()
                // Optionally emit success/error via _recipeUpdateState or a dedicated state
                Log.i(TAG, "Favorite status updated for $recipeId")
                // Re-fetch to update the UI state immediately
                fetchRecipeById(recipeId)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite for $recipeId", e)
                // Optionally emit error state
            }
        }
    }

    fun toggleBookmarkStatus(recipeId: String, currentStatus: Boolean) {
        val userId = auth.currentUser?.uid ?: return // Need logged-in user
        val updateAction = if (currentStatus) FieldValue.arrayRemove(recipeId) else FieldValue.arrayUnion(recipeId)
        Log.d(TAG, "Toggling Bookmark for recipe $recipeId for user $userId to ${!currentStatus}")

        viewModelScope.launch {
            try {
                userBookmarksCollection.document(userId).set(mapOf("userId" to userId), com.google.firebase.firestore.SetOptions.merge()).await() // Ensure doc exists
                userBookmarksCollection.document(userId).update("recipeIds", updateAction).await()
                Log.i(TAG, "Bookmark status updated for $recipeId")
                fetchRecipeById(recipeId) // Re-fetch
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling bookmark for $recipeId", e)
            }
        }
    }


    fun resetUpdateState() {
        if (_recipeUpdateState.value is RecipeUpdateState.Success || _recipeUpdateState.value is RecipeUpdateState.Error) {
            _recipeUpdateState.value = RecipeUpdateState.Idle
        }
    }


    private fun mapFirebaseRecipeToDetail(
        fbRecipe: FirebaseRecipe,
        isFavorite: Boolean, // Pass user-specific status
        isBookmarked: Boolean // Pass user-specific status
    ): RecipeDetail {
        val ingredientsList = fbRecipe.ingredients.mapNotNull { map ->
            val name = map["name"] as? String ?: return@mapNotNull null
            val quantityAny = map["quantity"]
            val quantityString = when (quantityAny) { is Number -> quantityAny.toString(); is String -> quantityAny; else -> "" }
            IngredientItem( quantity = quantityString, unit = map["unit"] as? String, name = name )
        }
        val tagsList = mutableListOf<String>()
        if (fbRecipe.cuisine.isNotBlank()) tagsList.add(fbRecipe.cuisine)
        if (fbRecipe.category.isNotBlank()) tagsList.add(fbRecipe.category)
        val currentUserId = auth.currentUser?.uid
        val currentUserRating = if (currentUserId != null) fbRecipe.userRatings[currentUserId] ?: 0 else 0

        return RecipeDetail(
            id = fbRecipe.id,
            title = fbRecipe.name,
            imageUrl = fbRecipe.imageUrl,
            cuisine = fbRecipe.cuisine,
            tags = tagsList.distinct(),
            isFavorite = isFavorite, // Use fetched status
            isBookmarked = isBookmarked, // Use fetched status
            servingSize = fbRecipe.servings,
            cookingTime = fbRecipe.cookingTime,
            prepTime = fbRecipe.prepTime,
            ingredients = ingredientsList,
            procedureSteps = fbRecipe.instructions,
            nutritionFacts = null, // Placeholder
            authorName = fbRecipe.authorName,
            personalNote = fbRecipe.personalNote.takeIf { it.isNotBlank() },
            userRating = currentUserRating,
            authorId = fbRecipe.authorId
        )
    }
}