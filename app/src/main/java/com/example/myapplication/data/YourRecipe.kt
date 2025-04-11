package com.example.myapplication.data

data class Recipe(
    val nameOfPerson: String,
    val name: String,
    val imageResId: Int,
    val rating: String,
    val category: String,
    val cookingTime: String,
    val serving: Int,
    val favorite: Boolean
)
