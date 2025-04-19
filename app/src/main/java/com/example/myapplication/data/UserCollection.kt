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
