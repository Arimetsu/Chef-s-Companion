package com.example.myapplication.data


import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.LocalDate

@IgnoreExtraProperties
data class PlannedMeal(
    @DocumentId var id: String = "",           // Firestore document ID
    var userId: String = "",         // ID of the user this plan belongs to
    var date: String = "",           // Store date as ISO string (e.g., "2023-10-27") for querying
    var mealType: String = "",       // e.g., "Breakfast", "Lunch", "Dinner", "Snack"
    var recipeIds: List<String> = emptyList(), // List of FirebaseRecipe IDs for this meal slot
    var notes: String? = null        // Optional notes for this specific meal plan slot
)
@IgnoreExtraProperties
data class DailyMealPlan(
    val date: LocalDate,
    val breakfast: List<Recipe> = emptyList(), // List of Recipe UI models
    val lunch: List<Recipe> = emptyList(),
    val dinner: List<Recipe> = emptyList(),
    val snacks: List<Recipe> = emptyList() // Or combine into one list if needed
)
