package com.example.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirebaseRecipe
import com.example.myapplication.data.Recipe // UI Model
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
// Removed: import com.google.firebase.firestore.ktx.toObjects // Not directly used anymore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

sealed class RecipeListState {
    object Loading : RecipeListState()
    data class Success(val recipes: List<Recipe>) : RecipeListState()
    data class Error(val message: String) : RecipeListState()
    object Empty : RecipeListState() // State for explicitly empty results
}

class HomeViewModel : ViewModel() {

    private val TAG = "HomeViewModel"
    private val db = FirebaseFirestore.getInstance()
    private val recipesCollection = db.collection("recipes")

    private val _recipeListState = MutableStateFlow<RecipeListState>(RecipeListState.Loading)
    val recipeListState: StateFlow<RecipeListState> = _recipeListState.asStateFlow()

    init {
        fetchRecipes("All")
    }

    fun fetchRecipes(category: String) {
        Log.d(TAG, "Fetching recipes for category: $category")
        _recipeListState.value = RecipeListState.Loading

        viewModelScope.launch {
            try {
                var query: Query = recipesCollection
                    .orderBy("averageRating", Query.Direction.DESCENDING)
                    .orderBy("createdAt", Query.Direction.DESCENDING) // Ensure composite index exists for (averageRating DESC, createdAt DESC)
                    .limit(30)

                // Apply category filter *after* ordering if possible, or adjust index
                // For simplicity, applying after initial query load for now.
                // Note: Firestore might require the filtered field to be ordered first sometimes.
                // Check Firestore console for index suggestions if queries fail.
                if (category != "All") { // Check if the requested category is NOT "All"
                    query = query.whereEqualTo("category", category) // Filter by the 'category' field
                    Log.d(TAG, "Applying category filter: $category")
                }

                val querySnapshot = query.get().await()

                if (querySnapshot.isEmpty) {
                    Log.d(TAG, "No recipes found in Firestore query for category '$category'")
                    _recipeListState.value = RecipeListState.Empty
                    return@launch // Exit coroutine
                }

                val recipes = mutableListOf<Recipe>()
                for (document in querySnapshot.documents) {
                    try {
                        // Manually get ID and convert
                        val fbRecipe = document.toObject(FirebaseRecipe::class.java)
                        if (fbRecipe != null) {
                            // Set the ID explicitly from the document snapshot
                            fbRecipe.id = document.id
                            // Now map using the complete fbRecipe object
                            recipes.add(mapFirebaseRecipeToRecipe(fbRecipe))
                        } else {
                            Log.w(TAG, "Failed to convert document ${document.id} to FirebaseRecipe")
                        }
                    } catch (e: Exception) {
                        // Catch potential errors during individual document conversion
                        Log.e(TAG, "Error parsing document ${document.id}", e)
                        // Optionally skip this document and continue
                    }
                }

                if (recipes.isEmpty()) {
                    // This case might happen if all documents failed to parse
                    Log.w(TAG, "No valid recipes could be mapped for category '$category'")
                    _recipeListState.value = RecipeListState.Empty
                } else {
                    Log.d(TAG, "Successfully loaded and mapped ${recipes.size} recipes for category '$category'")
                    _recipeListState.value = RecipeListState.Success(recipes)
                }

            } catch (e: Exception) {
                // Catch errors related to the Firestore query itself (permissions, index, network)
                val errorMsg = "Failed to fetch recipes: ${e.localizedMessage ?: "Unknown error"}"
                Log.e(TAG, "Firestore query error for category '$category'", e)
                _recipeListState.value = RecipeListState.Error(errorMsg)
            }
        }
    }

    private fun mapFirebaseRecipeToRecipe(fbRecipe: FirebaseRecipe): Recipe {
        // Ensure default values or checks for potentially null fields from Firestore
        // if your FirebaseRecipe class doesn't guarantee non-nullability from Firestore.
        // Since FirebaseRecipe now has defaults, direct access is usually safe,
        // but adding null checks provides extra safety.
        return Recipe(
            id = fbRecipe.id, // ID should be set before calling this map function
            nameOfPerson = fbRecipe.authorName.takeIf { it.isNotEmpty() } ?: "Unknown Chef",
            name = fbRecipe.name.takeIf { it.isNotEmpty() } ?: "Unnamed Recipe",
            imageUrl = fbRecipe.imageUrl,
            // ★ Access averageRating directly as it's defined in FirebaseRecipe ★
            averageRating = fbRecipe.averageRating, // Defaults to 0.0 if not present
            category = fbRecipe.category.takeIf { it.isNotEmpty() } ?: "Uncategorized",
            cookingTime = fbRecipe.cookingTime.takeIf { it.isNotEmpty() } ?: "N/A",
            serving = if (fbRecipe.servings > 0) fbRecipe.servings else 1, // Ensure servings >= 1
            favorite = false // Fetch favorite status separately as needed
        ).also {
            // Optional logging to verify mapping
            Log.v(TAG, "Mapped recipe: ${it.name} (ID: ${it.id}, Rating: ${it.averageRating})")
        }
    }
}