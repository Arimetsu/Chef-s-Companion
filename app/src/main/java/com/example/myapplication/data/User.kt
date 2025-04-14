package com.example.myapplication.data

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String? = null, // Nullable for initial null value
    val emailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)