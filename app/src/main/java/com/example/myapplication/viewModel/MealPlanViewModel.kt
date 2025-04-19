package com.example.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirebaseRecipe
import com.example.myapplication.data.PlannedMeal
import com.example.myapplication.data.Recipe // UI Model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// State for fetching planned meals for a specific date
sealed class DailyMealPlanState {
    object Loading : DailyMealPlanState()
    // Success holds maps from Recipe ID to Recipe UI model for quick lookup, and the list of PlannedMeals
    data class Success(
        val plannedMeals: List<PlannedMeal>,
        val recipeDetailsMap: Map<String, Recipe> // Map RecipeId to Recipe UI Model
    ) : DailyMealPlanState()
    data class Error(val message: String) : DailyMealPlanState()
    object Idle : DailyMealPlanState()
}

// State for Adding/Updating meal plans
sealed class MealPlanUpdateState {
    object Idle : MealPlanUpdateState()
    object Loading : MealPlanUpdateState()
    object Success : MealPlanUpdateState()
    data class Error(val message: String) : MealPlanUpdateState()
}

class MealPlanViewModel : ViewModel() {

    private val TAG = "MealPlanViewModel"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val mealPlanCollection = db.collection("meal_plans")
    private val recipesCollection = db.collection("recipes")

    // State for the currently selected date's meal plan
    private val _dailyMealPlanState = MutableStateFlow<DailyMealPlanState>(DailyMealPlanState.Idle)
    val dailyMealPlanState: StateFlow<DailyMealPlanState> = _dailyMealPlanState.asStateFlow()

    // State for add/update operations
    private val _updateState = MutableStateFlow<MealPlanUpdateState>(MealPlanUpdateState.Idle)
    val updateState: StateFlow<MealPlanUpdateState> = _updateState.asStateFlow()

    // State for recipes available to add (used in AddMealsToMealPlanScreen)
    private val _availableRecipesState = MutableStateFlow<SavedRecipeListState>(SavedRecipeListState.Loading) // Reuse state from SavedRecipesViewModel
    val availableRecipesState: StateFlow<SavedRecipeListState> = _availableRecipesState.asStateFlow()


    // Fetch meal plan for a specific date
    fun fetchMealPlanForDate(date: LocalDate) {
        val userId = auth.currentUser?.uid ?: return // Need user
        _dailyMealPlanState.value = DailyMealPlanState.Loading
        Log.d(TAG, "Fetching meal plan for date: $date")

        viewModelScope.launch {
            try {
                val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                // Query for planned meals for this user and date
                val plannedMealsSnapshot = mealPlanCollection
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("date", dateString)
                    .get()
                    .await()

                val plannedMeals = plannedMealsSnapshot.toObjects<PlannedMeal>()
                Log.d(TAG, "Fetched ${plannedMeals.size} planned meal slots for $dateString")

                // Get all unique recipe IDs needed from the fetched planned meals
                val allRecipeIds = plannedMeals.flatMap { it.recipeIds }.distinct()

                val recipeDetailsMap = if (allRecipeIds.isNotEmpty()) {
                    fetchRecipeDetails(allRecipeIds)
                } else {
                    emptyMap()
                }

                _dailyMealPlanState.value = DailyMealPlanState.Success(plannedMeals, recipeDetailsMap)

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching meal plan for $date", e)
                _dailyMealPlanState.value = DailyMealPlanState.Error("Failed to load meal plan: ${e.localizedMessage}")
            }
        }
    }

    // Helper function to fetch details for a list of recipe IDs
    private suspend fun fetchRecipeDetails(recipeIds: List<String>): Map<String, Recipe> {
        val recipeMap = mutableMapOf<String, Recipe>()
        if (recipeIds.isEmpty()) return recipeMap

        try {
            recipeIds.chunked(30).forEach { chunk ->
                if (chunk.isNotEmpty()) {
                    val snapshot = recipesCollection.whereIn("__name__", chunk).get().await()
                    snapshot.documents.forEach { doc ->
                        val fbRecipe = doc.toObject(FirebaseRecipe::class.java)?.copy(id = doc.id)
                        if (fbRecipe != null) {
                            // Map FirebaseRecipe to Recipe UI model (simplified version)
                            recipeMap[fbRecipe.id] = Recipe(
                                id = fbRecipe.id,
                                nameOfPerson = fbRecipe.authorName, // Or handle differently
                                name = fbRecipe.name,
                                imageUrl = fbRecipe.imageUrl,
                                averageRating = fbRecipe.averageRating, // Use average rating
                                category = fbRecipe.category,
                                cookingTime = fbRecipe.cookingTime,
                                serving = fbRecipe.servings,
                                favorite = false // Favorite status needs separate logic if shown here
                            )
                        }
                    }
                }
            }
            Log.d(TAG, "Fetched details for ${recipeMap.size} recipes.")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching recipe details", e)
            // Handle error - maybe return partial map or throw?
        }
        return recipeMap
    }


    // Function to add recipes to a specific meal slot (creates or updates)
    fun addRecipesToMealSlot(date: LocalDate, mealType: String, recipeIdsToAdd: List<String>) {
        val userId = auth.currentUser?.uid ?: return
        if (recipeIdsToAdd.isEmpty()) return

        _updateState.value = MealPlanUpdateState.Loading
        Log.d(TAG, "Adding ${recipeIdsToAdd.size} recipes to $mealType on $date")

        viewModelScope.launch {
            try {
                val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                // Check if a document already exists for this user, date, and mealType
                val querySnapshot = mealPlanCollection
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("date", dateString)
                    .whereEqualTo("mealType", mealType)
                    .limit(1)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    // Create new document
                    val newPlanDocRef = mealPlanCollection.document()
                    val newPlannedMeal = PlannedMeal(
                        id = newPlanDocRef.id,
                        userId = userId,
                        date = dateString,
                        mealType = mealType,
                        recipeIds = recipeIdsToAdd.distinct() // Ensure unique IDs
                    )
                    newPlanDocRef.set(newPlannedMeal).await()
                    Log.i(TAG, "Created new meal plan entry for $mealType on $date")
                } else {
                    // Update existing document, add new recipe IDs (union)
                    val existingDocRef = querySnapshot.documents.first().reference
                    existingDocRef.update("recipeIds", FieldValue.arrayUnion(*recipeIdsToAdd.toTypedArray())).await()
                    Log.i(TAG, "Updated existing meal plan entry for $mealType on $date")
                }

                _updateState.value = MealPlanUpdateState.Success
                fetchMealPlanForDate(date) // Refresh the daily plan after update

            } catch (e: Exception) {
                Log.e(TAG, "Error adding recipes to meal slot", e)
                _updateState.value = MealPlanUpdateState.Error("Failed to add recipes: ${e.localizedMessage}")
            }
        }
    }

    // Function to fetch recipes available for adding (can reuse logic or call SavedRecipesViewModel)
    // This version fetches ALL recipes, you might want to fetch only user's saved recipes
    fun fetchAvailableRecipesForAdding(searchTerm: String = "") {
        _availableRecipesState.value = SavedRecipeListState.Loading
        viewModelScope.launch {
            try {
                var query: Query = recipesCollection.orderBy("name").limit(50) // Example query
                if (searchTerm.isNotBlank()) {
                    // Basic search by name - adjust as needed
                    query = query.whereGreaterThanOrEqualTo("name", searchTerm)
                        .whereLessThanOrEqualTo("name", searchTerm + '\uf8ff')
                }
                val snapshot = query.get().await()
                val firebaseRecipes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirebaseRecipe::class.java)?.copy(id = doc.id)
                }
                val uiRecipes = firebaseRecipes.map { fbRecipe ->
                    Recipe( id = fbRecipe.id, nameOfPerson = fbRecipe.authorName, name = fbRecipe.name, imageUrl = fbRecipe.imageUrl, averageRating = fbRecipe.averageRating, category = fbRecipe.category, cookingTime = fbRecipe.cookingTime, serving = fbRecipe.servings, favorite = false )
                }
                _availableRecipesState.value = SavedRecipeListState.Success(uiRecipes)

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching available recipes", e)
                _availableRecipesState.value = SavedRecipeListState.Error("Failed to load recipes: ${e.localizedMessage}")
            }
        }
    }


    fun resetUpdateState() {
        _updateState.value = MealPlanUpdateState.Idle
    }

}