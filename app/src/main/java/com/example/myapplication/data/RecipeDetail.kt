package com.example.myapplication.data

data class RecipeDetail(
    val id: String, // Use a unique ID
    val title: String,
    val tags: List<String>,
    var isFavorite: Boolean,
    var isBookmarked: Boolean,
    val servingSize: Int,
    val cookingTime: String,
    val prepTime: String,
    val ingredients: List<IngredientItem>,
    val procedure: String, // Can be a long string with newlines or List<String>
    val nutritionFacts: List<NutritionItem>? // Optional section
)

data class IngredientItem(
    val quantity: String,
    val unit: String?, // e.g., "tbsp", "grams", "cup" - can be null
    val name: String
)

data class NutritionItem(
    val nutrient: String,
    val amount: String?, // e.g., "5g", "100kcal"
    val dailyValue: String? // e.g., "10%"
)