package com.example.myapplication.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class FirebaseRecipe(
    var id: String = "", // Will be set to Firestore document ID after saving
    var name: String = "",
    var imageUrl: String? = null, // URL from Firebase Storage
    var cuisine: String = "",
    var category: String = "",
    var servings: Int = 0,
    var prepTime: String = "", // Format like "HH hr MM min" or "MM min"
    var cookingTime: String = "", // Format like "HH hr MM min" or "MM min"
    var ingredients: List<String> = emptyList(), // List of ingredient strings
    var instructions: List<String> = emptyList(), // List of instruction steps
    var personalNote: String = "",
    var collectionId: String? = null, // Optional: ID of the collection it's saved to
    var authorId: String = "", // ID of the user creating it
    var authorName: String = "", // Name of the user creating it

    // --- New Optional Nutritional Fields ---
    var calories: String? = null, // e.g., "350", "approx 350 kcal"
    var protein: String? = null,  // e.g., "20g"
    var fat: String? = null,      // e.g., "15g"
    var carbohydrates: String? = null, // e.g., "30g"
    // --- End of New Fields ---

    @ServerTimestamp var createdAt: Date? = null // Let Firestore set the timestamp
)