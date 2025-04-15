package com.example.myapplication.front_end.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack // Use material icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.R // Import your R file
import com.example.myapplication.data.NotificationItem
import com.example.myapplication.data.NotificationType
import com.example.myapplication.data.getDummyNotifications // Import dummy data function
import com.example.myapplication.ui.theme.MyApplicationTheme // Import your theme

// Define the green color
val NotificationGreen = Color(26, 77, 46)
val NotificationGrayText = Color.Gray // Or a specific grey like Color(0xFF888888)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    // Replace with ViewModel later for real data and actions
    notificationsGrouped: Map<String, List<NotificationItem>> = getDummyNotifications(),

) {
    Scaffold(
        topBar = {
            NotificationTopAppBar(
                onNavigateBack = { navController.popBackStack() } // Use navController to go back
            )
        },
        containerColor = Color.White // Set background explicitly if needed
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp) // Horizontal padding for the list content
        ) {
            notificationsGrouped.forEach { (timeBucket, notificationsInBucket) ->
                // Time Bucket Header (e.g., "Today", "Yesterday")
                item {
                    Text(
                        text = timeBucket,
                        style = MaterialTheme.typography.titleMedium, // Or headlineSmall
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                    )
                }

                // Notification Items for this bucket
                itemsIndexed(notificationsInBucket, key = { _, item -> item.id }) { index, notification ->
                    NotificationItemRow(
                        notification = notification,
                        onFollowClick = { notificationId, currentFollowState ->
                            // Handle follow/unfollow logic here (e.g., call ViewModel)
                            println("Follow clicked for $notificationId, current state: $currentFollowState")
                            // In a real app, you'd update the state based on ViewModel response
                        }
                    )
                    if (index < notificationsInBucket.size - 1) {
                        // Optional Divider between items, excluding the last one in the group
                        // Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationTopAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Notifications",
                color = NotificationGreen,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NotificationGreen
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White, // Or Color.Transparent if background handles it
            titleContentColor = NotificationGreen,
            navigationIconContentColor = NotificationGreen
        )
        // Add actions if needed
    )
}

@Composable
fun NotificationItemRow(
    notification: NotificationItem,
    onFollowClick: (id: String, currentState: Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), // Padding for each row
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Profile Image
        Image(
            painter = painterResource(id = notification.userImageRes),
            contentDescription = "User profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray), // Placeholder background
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Notification Text Column
        Column(modifier = Modifier.weight(1f)) { // Takes available space
            // Build annotated string if specific parts need bolding (example)
            val annotatedMessage = buildAnnotatedString {
                // Example: Find username patterns and make them bold if needed
                // For now, just append the whole message
                append(notification.message)
            }
            Text(
                text = annotatedMessage,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp, // Adjust size as needed
                maxLines = 3, // Allow some wrapping
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp // Adjust line spacing
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.timestampRelative,
                style = MaterialTheme.typography.bodySmall,
                color = NotificationGrayText,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right Side Content (Conditional)
        when (notification.type) {
            NotificationType.LIKE_POST,
            NotificationType.COMMENT_POST,
            NotificationType.BOOKMARK_POST -> {
                if (notification.postImageRes != null) {
                    Image(
                        painter = painterResource(id = notification.postImageRes),
                        contentDescription = "Post image",
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            NotificationType.STARTED_FOLLOWING -> {
                // Ensure isFollowing is not null for this type
                val isCurrentlyFollowing = notification.isFollowing ?: false
                FollowButton(
                    isFollowing = isCurrentlyFollowing,
                    onClick = { onFollowClick(notification.id, isCurrentlyFollowing) }
                )
            }
        }
    }
}

@Composable
fun FollowButton(
    isFollowing: Boolean,
    onClick: () -> Unit
) {
    // Remember the state locally within the button if needed for immediate UI feedback,
    // but the source of truth should come from the notification item / ViewModel
    // var followingState by remember { mutableStateOf(isFollowing) } // Optional local state

    if (isFollowing) {
        // Following Button (Outlined)
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.height(32.dp), // Adjust height
            shape = RoundedCornerShape(8.dp), // Match image corner radius
            border = BorderStroke(1.dp, NotificationGreen),
            contentPadding = PaddingValues(horizontal = 12.dp) // Adjust padding
        ) {
            Text(
                text = "Following",
                color = NotificationGreen,
                fontSize = 12.sp, // Adjust font size
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        // Follow Button (Filled)
        Button(
            onClick = onClick,
            modifier = Modifier.height(32.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NotificationGreen,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text(
                text = "Follow",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
