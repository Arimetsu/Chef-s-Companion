package com.example.myapplication.data

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Recipe(
    val id: String,             // ★ ADDED: Crucial to identify the recipe for navigation/fetching details
    val nameOfPerson: String,   // Author's name for display
    val name: String,           // Recipe title
    val imageUrl: String?,      // ★ CHANGED: Use URL (String) from Firebase Storage, not Int ID
    val averageRating: Double = 0.0,         // Keep as String if that's how you store/display it
    val category: String,
    val cookingTime: String,    // Display-formatted cooking time
    val serving: Int,
    val favorite: Boolean       // Or fetch this status separately if needed
    // Add other simple fields needed ONLY for the card display if necessary
)
