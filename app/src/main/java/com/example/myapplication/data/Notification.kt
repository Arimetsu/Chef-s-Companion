package com.example.myapplication.data

import androidx.annotation.DrawableRes // For placeholder drawables
import com.example.myapplication.R // Import your R file

// Enum to represent the type of notification
enum class NotificationType {
    LIKE_POST,     // Includes post image
    COMMENT_POST,  // Includes post image
    BOOKMARK_POST, // Includes post image
    STARTED_FOLLOWING // Includes follow button
    // Add other types if needed
}

// Data class for a single notification item
data class NotificationItem(
    val id: String,
    val type: NotificationType,
    @DrawableRes val userImageRes: Int, // Placeholder resource ID for user image
    val message: String, // The main notification text
    val timestampRelative: String, // e.g., "21h", "3d"
    @DrawableRes val postImageRes: Int? = null, // Optional placeholder for post image
    val isFollowing: Boolean? = null // Optional follow state for follow notifications
)

// Helper function to create dummy data (replace with your actual data source)
fun getDummyNotifications(): Map<String, List<NotificationItem>> {
    return mapOf(
        "Today" to listOf(
            NotificationItem("1", NotificationType.LIKE_POST, R.drawable.user, "Lorem ipsum dolor sit amet consectetur adipiscing and others liked your post.", "21h", R.drawable.tryfood),
            NotificationItem("2", NotificationType.COMMENT_POST, R.drawable.user, "Lorem ipsum dolor sit amet consectetur adipiscing commented at your post.", "21h", R.drawable.tryfood)
        ),
        "Yesterday" to listOf(
            NotificationItem("3", NotificationType.BOOKMARK_POST, R.drawable.user, "Lorem ipsum dolor sit amet consectetur adipiscing bookmarked your post.", "21h", R.drawable.tryfood),
            NotificationItem("4", NotificationType.STARTED_FOLLOWING, R.drawable.user, "Lorem ipsum dolor started following you.", "3d", isFollowing = false),
            NotificationItem("5", NotificationType.STARTED_FOLLOWING, R.drawable.user, "Lorem ipsum dolor started following you.", "4d", isFollowing = true),
            NotificationItem("6", NotificationType.LIKE_POST, R.drawable.user, "Lorem ipsum dolor sit amet consectetur adipiscing liked your post.", "21h", R.drawable.tryfood),
            NotificationItem("7", NotificationType.STARTED_FOLLOWING, R.drawable.user, "Lorem ipsum dolor started following you.", "3d", isFollowing = false),
            NotificationItem("8", NotificationType.LIKE_POST, R.drawable.user, "Lorem ipsum dolor sit amet consectetur adipiscing liked your post.", "21h", R.drawable.tryfood),
            NotificationItem("9", NotificationType.LIKE_POST, R.drawable.user, "Lorem ipsum dolor sit amet consectetur adipiscing liked your post.", "21h", R.drawable.tryfood),
            NotificationItem("10", NotificationType.LIKE_POST, R.drawable.user, "Lorem ipsum dolor sit amet consectetur adipiscing liked your post.", "21h", R.drawable.tryfood),
            NotificationItem("11", NotificationType.LIKE_POST,R.drawable.user, "Lorem ipsum dolor sit amet consectetur adipiscing liked your post.", "21h", R.drawable.tryfood)
        )
    )
    // Add more categories like "This Week", "Older" if needed
}

// Create placeholder drawables in your res/drawable folder:
// - user_placeholder.xml (e.g., a simple grey circle or vector icon)
// - post_placeholder_1.xml, post_placeholder_2.xml, post_placeholder_3.xml (e.g., grey squares or sample food images)
// - ic_arrow_back.xml (standard back arrow icon)