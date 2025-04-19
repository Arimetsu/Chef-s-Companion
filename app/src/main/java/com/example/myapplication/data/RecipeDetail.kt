package com.example.myapplication.data

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class RecipeDetail(
    val id: String,             // Unique ID from FirebaseRecipe
    val title: String,          // Recipe name
    val imageUrl: String?,      // ★ ADDED: Need the image URL for the detail screen header/image
    val cuisine: String,        // ★ ADDED: Likely needed for display
    val tags: List<String>,     // Keep tags if you use them (maybe map from cuisine/category)
    var isFavorite: Boolean,
    var isBookmarked: Boolean,  // You'll need logic to fetch/update these states
    val servingSize: Int,
    val cookingTime: String,    // Display-formatted time
    val prepTime: String,       // Display-formatted time
    val ingredients: List<IngredientItem>, // Keep the good structure
    val procedureSteps: List<String>, // ★ CHANGED: List of steps, aligns with FirebaseRecipe
    val nutritionFacts: List<NutritionItem>?, // Optional section
    val authorName: String,     // ★ ADDED: Often useful to show the author
    val personalNote: String?,
    var userRating: Int = 0, // 0 = unrated, 1-5 = rating
    val authorId: String = ""
)
@IgnoreExtraProperties
data class IngredientItem(
    val quantity: String,   // Keep quantity as String for flexible display (e.g., "1 1/2")
    // Or use Double/Float if you need calculations, handle display separately.
    val unit: String?,      // e.g., "tbsp", "grams", "cup" - can be null
    val name: String
)

@IgnoreExtraProperties
data class NutritionItem(
    val nutrient: String,   // e.g., "Calories", "Protein"
    val amount: String?,    // e.g., "5g", "100kcal"
    val dailyValue: String? // e.g., "10%"
)