package com.example.myapplication.data

data class RecipeResult(
    val id: Int,
    val title: String,
    val serving: Int,
    val prepTime: String,
    val cookingTime: String,
    val tags: List<String>
)