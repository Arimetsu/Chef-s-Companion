package com.example.myapplication.data

// Data class to hold the collected preferences
data class RecipePreferences(
    val ingredients: List<String>,
    val serving: Int?,
    val prepTime: String,
    val cookingTime: String,
    val cuisines: Set<String>,
    val categories: Set<String>
)