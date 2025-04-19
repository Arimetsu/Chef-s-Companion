package com.example.myapplication.front_end.userprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.components.recipegrids.Recipe
import com.example.myapplication.components.recipegrids.RecipeCard
import com.example.myapplication.components.recipegrids.RecipeTag
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Typography
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.myapplication.components.userprofile.LogoutConfirmationDialog
import com.example.myapplication.components.userprofile.MenuOption
import com.example.myapplication.components.userprofile.ProfileMenuButton
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.front_end.home.NavBar

data class UserProfile(
    val id: String,
    val name: String,
    val bio: String,
    val profilePictureUrl: String,
    val backgroundImageUrl: String,
    val links: List<UserLink>,
    val postsCount: Int,
    val followersCount: Int,
    val followingCount: Int,
    val username: String
)

data class UserLink(
    val title: String,
    val url: String
)

@Composable
fun UserProfileScreen(
    navController: NavController,
    userProfile: UserProfile,
    onSearchClick: () -> Unit = {
        navController.navigate(ScreenNavigation.Screen.SearchRecipe.route) {
            launchSingleTop = true
            restoreState = true
        }
    },
    onNotificationsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onLinkClick: (UserLink) -> Unit = {},
    recipes: List<Recipe> = emptyList(),
    onRecipeClick: (Recipe) -> Unit = {}


) {
    var selectedTab by remember { mutableStateOf(4) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    MyApplicationTheme {
        Scaffold(
            bottomBar = {
                NavBar(selectedItem = selectedTab, onItemSelected = {
                    selectedTab = it
                    if (it == 0) { //home
                        navController.navigate(ScreenNavigation.Screen.Home.route)
                    }
                    else if (it == 1) { //your recipe
                        navController.navigate(ScreenNavigation.Screen.YourRecipes.route)
                    }
                    else if (it == 2) { //your recipe

                    }
                    else if (it == 3) { //your recipe
                        navController.navigate(ScreenNavigation.Screen.MealPlan.route)
                    }
                    else if (it == 4) { //your recipe
                        navController.navigate(ScreenNavigation.Screen.UserProfile.route)
                    }
                })
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding)
            ) {
                // Profile Header
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        // Background with gradient
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp) // Increased height to give space for the profile picture
                        ) {
                            // Background Image
                            Image(
                                painter = painterResource(id = R.drawable.default_profile_bg),
                                contentDescription = "Profile background",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentScale = ContentScale.Crop
                            )

                            // Profile Picture
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(data = userProfile.profilePictureUrl)
                                        .error(R.drawable.user)
                                        .placeholder(R.drawable.user)
                                        .build()
                                ),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 16.dp)// Pulls the image upward
                                    .clip(CircleShape)
                                    .size(120.dp)
                                    .border(3.dp, Color.White, CircleShape)
                            )
                        }

                        // Top bar with action buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 2.dp, end = 2.dp, top = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { onSearchClick() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.search_img),
                                    contentDescription = "Search",
                                    tint = Color.Unspecified, // Use this if you want to preserve the original colors of the image
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onNotificationsClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.notif),
                                    contentDescription = "Notifications",
                                    tint = Color(0xFF2B5D2A) // Dark green color
                                )
                            }

                            ProfileMenuButton(
                                onMenuClick = onMenuClick,
                                menuOptions = listOf(
                                    MenuOption(
                                        title = "Edit Profile",
                                        onClick = { navController.navigate(ScreenNavigation.Screen.EditProfile.route) }
                                    ),
                                    MenuOption(
                                        title = "Privacy",
                                        onClick = { navController.navigate(ScreenNavigation.Screen.Privacy.route) }
                                    ),
                                    MenuOption(
                                        title = "Log out",
                                        onClick = { showLogoutDialog = true },
                                        isHighlighted = true
                                    )
                                )
                            )

                            if (showLogoutDialog) {
                                LogoutConfirmationDialog(
                                    onDismiss = { showLogoutDialog = false },
                                    onConfirmLogout = {
                                        // Handle logout logic here
                                        // For example: viewModel.logout()
                                        showLogoutDialog = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Profile content
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth() // Adjusted to position profile pic properly
                    ) {
                        // Profile Info
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp)
                                .padding(top = 6.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            // Name
                            Text(
                                text = userProfile.name,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )

                            // Username
                            Text(
                                text = "@${userProfile.username}",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp),
                                fontStyle = FontStyle.Italic
                            )

                            // Bio
                            Text(
                                text = userProfile.bio,
                                fontSize = 16.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(bottom = 8.dp),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start
                            )

                            // Links
                            if (userProfile.links.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    userProfile.links.forEach { link ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .padding(vertical = 4.dp)
                                                .clickable { onLinkClick(link) }
                                        ) {
                                            Image(
                                                painter = painterResource(R.drawable.icon_link),
                                                contentDescription = null,
                                                colorFilter = ColorFilter.tint(Color.Blue),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = link.url,
                                                color = Color.Blue,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Stats row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                StatItem(
                                    label = "Post",
                                    value = userProfile.postsCount,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                StatItem(
                                    label = "Followers",
                                    value = userProfile.followersCount,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                StatItem(
                                    label = "Following",
                                    value = userProfile.followingCount,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }

                        PostsTab(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )
                    }
                }

                item {
                    if (recipes.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((recipes.size * 120).dp.coerceAtMost(800.dp)) // Adjust based on number of items
                        ) {
                            items(recipes) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onRecipeClick = onRecipeClick,
                                    // Don't override any modifiers from the RecipeCard component
                                    modifier = Modifier
                                )
                            }
                        }
                    } else {
                        // Show a message when there are no recipes
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No posts yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun PostsTab(
    modifier: Modifier = Modifier
) {
    // Tab bar with Posts tab
    Column {
        // Top divider
        Divider(color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp))

        // Tab content - Posts with grid icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.grid_icon),
                contentDescription = "Posts Grid",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Posts",
                style = Typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Bottom divider
        Divider(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun StatItem(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    val sampleUser = UserProfile(
        id = "1",
        name = "John Smith",
        username = "johnsmith",
        bio = "I love cooking and I love planting 💚 follow me on yt too\n#Chef'sDaBest #JohnCooks",
        profilePictureUrl = "https://via.placeholder.com/100",
        backgroundImageUrl = "https://via.placeholder.com/600x200",
        links = listOf(
            UserLink("YouTube", "youtube.com/@johncooks")
        ),
        postsCount = 7,
        followersCount = 90030,
        followingCount = 100
    )

    val sampleRecipes = listOf(
        Recipe(
            id = 1,
            name = "Canned Tuna Pasta",
            imageRes = R.drawable.tryfood,
            tags = listOf(
                RecipeTag("Lunch"),
                RecipeTag("Italian")
            ),
            rating = 4.5
        ),
        Recipe(
            id = 2,
            name = "Vegetable Stir Fry",
            imageRes = R.drawable.tryfood,
            tags = listOf(
                RecipeTag("Dinner"),
                RecipeTag("Vegan")
            ),
            rating = 4.8
        )
    )

    MyApplicationTheme {
        UserProfileScreen(
            navController = NavController(LocalContext.current),
            userProfile = sampleUser,
            recipes = sampleRecipes,
            onRecipeClick = { recipe ->
                // Handle recipe click
            }
        )
    }
}