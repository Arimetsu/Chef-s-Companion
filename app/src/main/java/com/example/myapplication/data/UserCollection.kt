package com.example.myapplication.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserCollection(
    @DocumentId var id: String = "", // Firestore document ID
    var userId: String = "",         // ID of the user who owns this collection
    var name: String = "",           // Name of the collection (e.g., "Weeknight Dinners")
    var recipeIds: List<String> = emptyList() // List of FirebaseRecipe IDs in this collection
)

data class UserCollectionWithPreviews(
    val collection: UserCollection,
    val previewImageUrls: List<String> // URLs for the first few recipes
)

enum class RecipeViewMode {
    COLLECTIONS, // Show the grid of user collections
    ALL_RECIPES, // Show the grid of all saved recipes
    FAVORITES    // Show the grid of favorite recipes
}
