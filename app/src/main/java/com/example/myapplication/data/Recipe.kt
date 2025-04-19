package com.example.myapplication.data

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
@IgnoreExtraProperties
data class FirebaseRecipe(
    var id: String = "", // Will be set to Firestore document ID after saving/fetching
    var name: String = "",
    var imageUrl: String? = null, // URL from Firebase Storage
    var cuisine: String = "",
    var category: String = "",
    var servings: Int = 0,
    var prepTime: String = "", // Format like "HH hr MM min" or "MM min"
    var cookingTime: String = "", // Format like "HH hr MM min" or "MM min"
    var ingredients: List<Map<String, Any?>> = emptyList(),
    var instructions: List<String> = emptyList(), // List of instruction steps
    var personalNote: String = "",
    var collectionId: String? = null, // Optional: ID of the collection it's saved to
    var authorId: String = "", // ID of the user creating it
    var authorName: String = "", // Name of the user creating it
    var calories: String? = null,
    var protein: String? = null,
    var fat: String? = null,
    var carbohydrates: String? = null,

    // --- Rating Fields ---
    var userRatings: Map<String, Int> = emptyMap(), // Map: userId -> rating (1-5)
    var averageRating: Double = 0.0, // Calculated average rating
    var ratingCount: Int = 0,        // Total number of ratings received
    // --- End Rating Fields ---

    @ServerTimestamp var createdAt: Date? = null // Let Firestore set the timestamp
)
@IgnoreExtraProperties
data class IngredientInput(
    val name: String = "",
    val quantity: String = "", // Keep as String from UI input for flexibility (e.g., "1 1/2")
    val unit: String? = null   // Unit is optional
)