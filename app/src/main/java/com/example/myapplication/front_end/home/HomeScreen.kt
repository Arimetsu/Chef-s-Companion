package com.example.myapplication.front_end.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication.R
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.Font
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.data.Recipe
import com.example.myapplication.data.User
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.viewModel.AuthViewModel
import recipes


val bb2 = FontFamily(
    Font(R.font.bb2_regular),
    Font(R.font.bb2_bold, FontWeight.Bold)
)
val monte = FontFamily(
    Font(R.font.montserrat_regular),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_light, FontWeight.Light)
)

@Composable
fun HomeScreen(navController: NavHostController,
               authViewModel: AuthViewModel = viewModel()) { // Receive NavController
    var selectedTab by remember { mutableStateOf(0) }
    // This state correctly drives the recipe filtering
    var selectedCategory by remember { mutableStateOf("All") }

    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()


    Scaffold(
        bottomBar = {
            NavBar(
                selectedItem = selectedTab,
                onItemSelected = { index ->
                    selectedTab = index
                    when (index) {
                        0 -> navController.navigate(ScreenNavigation.Screen.Home.route)
                        1 -> navController.navigate(ScreenNavigation.Screen.YourRecipes.route)
                        2 -> {} // Placeholder for other tab
                        3 -> navController.navigate(ScreenNavigation.Screen.MealPlan.route)
                        4 -> navController.navigate(ScreenNavigation.Screen.UserProfile.route)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TopSection(navController, userProfile)
            CategorySection(onCategorySelected = { category -> selectedCategory = category })
            RecipeList(selectedCategory = selectedCategory)
        }
    }
}

@Composable
fun TopSection(navController: NavHostController,
               userProfile: User?){
    Column(modifier = Modifier.padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chef's Companion",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = bb2,
                color = Color(26, 77, 46)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* Notif Sign-In */ }) {
                Box(
                    modifier = Modifier
                        .size(30.dp) // Size of the border
                        .border(
                            0.2.dp,
                            Color(26, 77, 46),
                            RoundedCornerShape(50.dp)
                        ) // Light border with rounded corners
                        .clip(RoundedCornerShape(50.dp)) // Clip the border to rounded corners
                ) {
                    Image(
                        painter = painterResource(R.drawable.notif),
                        contentDescription = "Notification",
                        modifier = Modifier
                            .size(20.dp) // Set the size of the image
                            .align(Alignment.Center)

                    )
                }
            }

        }

        // --- Dynamic Profile Picture ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape) // Clip the image to a circle
                    .background(Color.LightGray) // Background while loading/error
            ) {
                AsyncImage(
                    model = userProfile?.profileImageUrl, // Use URL from userProfile, or null
                    contentDescription = "User Profile Picture",
                    // Provide a fallback/placeholder drawable
                    placeholder = painterResource(id = R.drawable.user), // Your default user icon
                    error = painterResource(id = R.drawable.user), // Show same default on error
                    contentScale = ContentScale.Crop, // Crop to fit the circle
                    modifier = Modifier.fillMaxSize() // Fill the Box
                )
            }
            // --- End Dynamic Profile Picture ---

            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(
                    text = if (userProfile?.username.isNullOrEmpty()) "Welcome!" else userProfile!!.username!!,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1, // Prevent wrapping
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "What are you making today?",
                    fontFamily = monte,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(118, 118, 118)
                )
            }
        }
    }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(BorderStroke(2.dp, Color(0xFFD9D9D9)), RoundedCornerShape(24.dp))
                .clickable(onClick = { navController.navigate("searchRecipe") })
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Search for a recipe",
                color = Color.Gray,
                fontFamily = monte
            )
        }
    }


@Composable
fun CategorySection(onCategorySelected: (String) -> Unit) {
    // Categories list
    val categories = listOf("All", "Breakfast", "Lunch", "Dinner", "Dessert")
    // Internal state for highlighting the selected button
    var selectedCategoryState by remember { mutableStateOf("All") }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 14.dp), // Padding for the whole row
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp) // Vertical padding for the section
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        // Use internal state for background color
                        if (selectedCategoryState == category) Color(26, 77, 46) else Color.LightGray
                    )
                    .clickable {
                        // Update internal state
                        selectedCategoryState = category
                        // Call the callback to notify the parent (HomeScreen)
                        onCategorySelected(category)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Padding inside the box
            ) {
                Text(
                    text = category,
                    // Use internal state for text color
                    color = if (selectedCategoryState == category) Color.White else Color(0, 0, 0, 64),
                    fontFamily = monte // Use appropriate font
                )
            }
        }
    }
}

@Composable
fun RecipeList(selectedCategory: String) {
    // This remember block ensures filtering only happens when selectedCategory changes
    val filteredRecipes = remember(selectedCategory) {
        recipes.filter { recipe ->
            // Correct filtering logic
            selectedCategory == "All" || recipe.category.equals(selectedCategory, ignoreCase = true) // Make comparison case-insensitive
        }
    }

    // Use LazyVerticalGrid to display the filtered recipes
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Display two columns
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp), // Padding around the grid
        verticalArrangement = Arrangement.spacedBy(8.dp), // Spacing between rows
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Spacing between columns
    ) {
        items(filteredRecipes.size) { index ->
            val recipe = filteredRecipes[index]
            RecipeCard(recipe) // Display each recipe using RecipeCard
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit = {}) { // Added onClick parameter
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(175.dp)
            .clickable(onClick = onClick)
    ) {
        Box { // Main container for stacking image, gradient, and content
            // Background Image
            Image(
                // painter = rememberAsyncImagePainter(recipe.imageResId), // Use for URLs
                painter = painterResource(R.drawable.tryfood), // Placeholder
                contentDescription = recipe.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // --- Stacked Gradients ---
            // 1. Base darkening gradient for general readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.0f),
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 200f
                        )
                    )
            )

            // 2. *** ADDED BACK: Your Specific Green Gradient Overlay ***
            // Applied ON TOP of the darkening gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent, // Start transparent at the top
                                Color(60, 179, 107, 0), // Transparent Green (as in your original)
                                // Opaque Green at the bottom (using alpha value explicitly)
                                Color(26, 77, 46, 255) // Your original green, fully opaque
                                // Or use Color(26, 77, 46) if you prefer the shorthand
                            )
                            // Adjust startY/endY if needed to control where the green starts/ends
                        )
                    )
            )
            // --- End Stacked Gradients ---


            // Content Area (Positioned on top of gradients)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween // Push badges top, details bottom
            ) {

                // --- Top Badges Area (Using FlowRow) ---
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // User Badge (using the helper)
                    BadgeChip(
                        text = recipe.nameOfPerson,
                        iconResId = R.drawable.user,
                        iconTint = Color.White,
                        // Keep the green background for the user badge as before
                        backgroundColor = Color(26, 77, 46, 200), // Semi-transparent green
                        textColor = Color.White
                    )

                    // Category Badge (using the helper)
                    BadgeChip(
                        text = recipe.category,
                        backgroundColor = Color(255, 255, 255, 209),
                        textColor = Color.Black
                    )

                    // Rating Badge (using the helper)
                    BadgeChip(
                        text = " ${recipe.rating}",
                        iconResId = R.drawable.star,
                        iconTint = Color(255, 185, 0),
                        backgroundColor = Color(255, 255, 255, 209),
                        textColor = Color.Black
                    )
                }

                // --- Bottom Content Area ---
                Column {
                    Text(
                        text = recipe.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp, // Slightly larger for title
                        fontFamily = monte,
                        maxLines = 2, // Allow wrapping but limit lines
                        overflow = TextOverflow.Ellipsis // Add ellipsis if too long
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.alarm),
                            contentDescription = "Time",
                            tint = Color.White.copy(alpha = 0.8f), // Slightly transparent white
                            modifier = Modifier.size(12.dp) // Slightly larger icon
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = recipe.cookingTime,
                            color = Color.White,
                            fontSize = 10.sp, // Slightly larger
                            fontFamily = monte
                        )

                        // Vertical Divider - Correct placement inside Row
                        Spacer(modifier = Modifier.width(6.dp))
                        Divider(
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier
                                .height(12.dp) // Height of the divider
                                .width(1.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            painter = painterResource(R.drawable.restaurant),
                            contentDescription = "Serving",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${recipe.serving} Serving${if (recipe.serving > 1) "s" else ""}", // Handle plural
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = monte
                        )
                    }
                }
            } // End Bottom Content Column
        } // End Main Box
    } // End Card
}

// --- Reusable Badge Composable ---
@Composable
fun BadgeChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    iconResId: Int? = null, // Optional icon
    iconTint: Color? = null  // Optional icon tint
) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            // Clip the content just in case text is extremely long and overflows padding
            .clip(RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp) // Adjust padding as needed
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp) // Space between icon and text
        ) {
            if (iconResId != null) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null, // Decorative icon
                    tint = iconTint ?: LocalContentColor.current, // Use provided tint or default
                    modifier = Modifier.size(12.dp) // Consistent icon size
                )
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 9.sp, // Consistent badge text size
                fontFamily = monte,
                fontWeight = FontWeight.Medium,
                maxLines = 1, // Prevent badge text from wrapping
                overflow = TextOverflow.Ellipsis // Add ellipsis if badge text is too long
            )
        }
    }
}

@Composable
fun NavBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    val items = listOf("Home", "Saved", "Add", "Calendar", "User")
    val icons = listOf(
        R.drawable.home,
        R.drawable.book,
        R.drawable.add,
        R.drawable.calendar,
        R.drawable.user // User icon (will be handled separately)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.Transparent),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onItemSelected(index) }
            ) {
                if (index == 4) { // User icon (last item)
                    Image(
                        painter = painterResource(id = icons[index]),
                        contentDescription = item,
                        modifier = Modifier
                            .size(39.dp)
                            .clip(CircleShape) // Optional for rounded user image
                    )
                } else {
                    Icon(
                        painter = painterResource(id = icons[index]),
                        contentDescription = item,
                        tint = if (selectedItem == index) Color(26, 77, 46) else Color.Gray,
                        modifier = Modifier.size(if (index == 2) 44.dp else 34.dp)
                    )
                }

                if (selectedItem == index) {
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(20.dp)
                            .background(Color(26, 77, 46), shape = RoundedCornerShape(1.dp))
                    )
                }
            }
        }
    }
}