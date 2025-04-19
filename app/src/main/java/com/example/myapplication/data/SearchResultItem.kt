package com.example.myapplication.data

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
sealed class SearchResultItem {
    data class RecipeResult(
        val id: String,
        val name: String,
        val authorName: String,
        val imageUrl: String?,
        val averageRating: Double // Example fields for display
    ) : SearchResultItem()

    data class UserResult(
        val uid: String,
        val username: String,
        val profileImageUrl: String?,
        val followerCount: Long // Example field
    ) : SearchResultItem()

}