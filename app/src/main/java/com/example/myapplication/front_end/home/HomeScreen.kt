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
import androidx.compose.material.Text // Material 2 Text potentially used
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.CircularProgressIndicator // Import for loading state
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.data.Recipe // UI Model for list
import com.example.myapplication.data.User
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.viewModel.AuthViewModel
import com.example.myapplication.viewModel.HomeViewModel // Import HomeViewModel
import com.example.myapplication.viewModel.RecipeListState // Import HomeViewModel State

// Remove this if recipes are fetched from ViewModel
// import recipes // Assuming this was a local dummy data source

val bb2 = FontFamily( Font(R.font.bb2_regular), Font(R.font.bb2_bold, FontWeight.Bold) )
val monte = FontFamily( Font(R.font.montserrat_regular), Font(R.font.montserrat_bold, FontWeight.Bold), Font(R.font.montserrat_light, FontWeight.Light) )
val PrimaryGreen = Color(26, 77, 46) // Define color consistently
val RatingStarColor = Color(0xFFFFC107) // Gold/Yellow for selected stars

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel() // Inject HomeViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    // Category state remains, triggers fetching
    var selectedCategory by remember { mutableStateOf("All") }

    // Observe user profile from AuthViewModel
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    // Observe recipe list state from HomeViewModel
    val recipeListState by homeViewModel.recipeListState.collectAsStateWithLifecycle()

    // Effect to re-fetch recipes when selectedCategory changes
    LaunchedEffect(selectedCategory) {
        homeViewModel.fetchRecipes(selectedCategory)
    }

    Scaffold(
        bottomBar = {
            NavBar(selectedItem = selectedTab, onItemSelected = { index ->
                selectedTab = index
                when (index) {
                    0 -> { /* Stay on Home */ }
                    1 -> navController.navigate(ScreenNavigation.Screen.YourRecipes.route)
                    2 -> navController.navigate(ScreenNavigation.Screen.AddRecipe.route) // Navigate to Add Recipe
                    3 -> navController.navigate(ScreenNavigation.Screen.MealPlan.route)
                    4 -> navController.navigate(ScreenNavigation.Screen.UserProfile.route)
                }
            })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply Scaffold padding
                .background(Color.White) // Set background for the Column if needed
        ) {
            TopSection(navController, userProfile)
            CategorySection(
                selectedCategory = selectedCategory, // Pass current selection
                onCategorySelected = { category -> selectedCategory = category } // Update state on selection
            )
            // Pass recipe list state to RecipeList composable
            RecipeListContent(
                recipeListState = recipeListState,
                onRecipeClick = { recipeId ->
                    // Navigate to detail screen, passing the recipe ID
                    navController.navigate(ScreenNavigation.Screen.RecipeDetail.createRoute(recipeId))
                }
            )
        }
    }
}

@Composable
fun TopSection(navController: NavHostController, userProfile: User?) {
    Column(modifier = Modifier.padding(14.dp)) {
        Row( modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically ) {
            Text( text = "Chef's Companion", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = bb2, color = PrimaryGreen )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { navController.navigate(ScreenNavigation.Screen.Notification.route) }) {
                Box( modifier = Modifier.size(30.dp).border( 0.2.dp, PrimaryGreen, RoundedCornerShape(50.dp) ).clip(RoundedCornerShape(50.dp)) ) {
                    Image( painter = painterResource(R.drawable.notif), contentDescription = "Notification", modifier = Modifier.size(20.dp).align(Alignment.Center) )
                }
            }
        }

        Row( modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically ) {
            Box( modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.LightGray) ) {
                AsyncImage(
                    model = userProfile?.profileImageUrl,
                    contentDescription = "User Profile Picture",
                    placeholder = painterResource(id = R.drawable.user),
                    error = painterResource(id = R.drawable.user),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text( text = userProfile?.username?.takeIf { it.isNotEmpty() } ?: "Welcome!", style = MaterialTheme.typography.bodyLarge, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis )
                Text( text = "What are you making today?", fontFamily = monte, fontSize = 10.sp, fontWeight = FontWeight.Light, color = Color(118, 118, 118) )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp) // Adjust padding if needed
                .height(48.dp) // Standard height for search bar
                .border(BorderStroke(1.dp, Color(0xFFD9D9D9)), RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp)) // Clip background/click effect
                .clickable(onClick = { navController.navigate(ScreenNavigation.Screen.SearchRecipe.route) }) // Use correct route string
                .padding(horizontal = 16.dp), // Inner padding for content
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon( imageVector = Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray )
            Spacer(modifier = Modifier.width(8.dp))
            Text( text = "Search for a recipe", color = Color.Gray, fontFamily = monte )
        }
    }
}


@Composable
fun CategorySection(
    selectedCategory: String, // Receive current selection
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("All", "Breakfast", "Lunch", "Dinner", "Dessert")
    // No internal state needed here, driven by HomeScreen's state

    LazyRow(
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            val isSelected = selectedCategory == category // Check against the passed state
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background( if (isSelected) PrimaryGreen else Color.LightGray )
                    .clickable { onCategorySelected(category) } // Call callback on click
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    color = if (isSelected) Color.White else Color(0, 0, 0, 64),
                    fontFamily = monte
                )
            }
        }
    }
}

@Composable
fun RecipeListContent(
    recipeListState: RecipeListState,
    onRecipeClick: (recipeId: String) -> Unit
) {
    // Handle different states from the ViewModel
    when (recipeListState) {
        is RecipeListState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        }
        is RecipeListState.Success -> {
            val recipes = recipeListState.recipes
            if (recipes.isEmpty()) {
                // Display message when the list is successfully loaded but empty
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No recipes found.", fontFamily = monte, color = Color.Gray) // Simplified message
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize() // Allow grid to fill available space
                ) {
                    // Use items extension that takes a list and optional key
                    items(
                        count = recipes.size,
                        key = { index -> recipes[index].id } // Use recipe ID as stable key
                    ) { index ->
                        val recipe = recipes[index]
                        RecipeCard(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe.id) }
                        )
                    }
                    // Alternative if you don't need index but have keys:
                    // items(items = recipes, key = { it.id }) { recipe ->
                    //    RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
                    // }
                }
            }
        }
        is RecipeListState.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "Error: ${recipeListState.message}", // Simplified error message
                    color = Color.Red, // Or MaterialTheme.colorScheme.error
                    fontFamily = monte,
                    textAlign = TextAlign.Center
                )
            }
        }
        // Explicitly handle the Empty state added to RecipeListState
        is RecipeListState.Empty -> {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No recipes found.", fontFamily = monte, color = Color.Gray) // Consistent message
            }
        }
    } // End when
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(4.dp) // Reduced padding slightly
            .fillMaxWidth()
            .height(175.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Add subtle elevation
    ) {
        Box {
            AsyncImage( // Use AsyncImage to load URL
                model = recipe.imageUrl,
                contentDescription = recipe.name,
                placeholder = painterResource(R.drawable.greenbackgroundlogo), // Add your placeholder
                error = painterResource(R.drawable.greenbackgroundlogo), // Add your error placeholder
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box( // Darkening Gradient
                modifier = Modifier.fillMaxSize().background( Brush.verticalGradient( colors = listOf( Color.Transparent, Color.Black.copy(alpha = 0.8f) ), startY = 200f ))
            )
            Box( // Green Overlay Gradient
                modifier = Modifier.fillMaxSize().background( Brush.verticalGradient( colors = listOf( Color.Transparent, Color(26, 77, 46, 255) ) ))
            )

            Column( // Content Area
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp), // Adjusted padding
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                FlowRow( modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp) ) {
                    BadgeChip( text = recipe.nameOfPerson, iconResId = R.drawable.user, iconTint = Color.White, backgroundColor = PrimaryGreen.copy(alpha = 0.8f), textColor = Color.White )
                    BadgeChip( text = recipe.category, backgroundColor = Color.White.copy(alpha = 0.85f), textColor = Color.Black )
                    // Display formatted average rating
                    BadgeChip( text = String.format("%.1f", recipe.averageRating), iconResId = R.drawable.star, iconTint = RatingStarColor, backgroundColor = Color.White.copy(alpha = 0.85f), textColor = Color.Black )
                }

                Column { // Bottom Content Area
                    Text( text = recipe.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp, fontFamily = monte, maxLines = 2, overflow = TextOverflow.Ellipsis )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon( painter = painterResource(R.drawable.alarm), contentDescription = "Time", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(12.dp) )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text( text = recipe.cookingTime.takeIf { it.isNotEmpty() } ?: "-", color = Color.White, fontSize = 10.sp, fontFamily = monte ) // Show "-" if time is empty
                        Spacer(modifier = Modifier.width(6.dp))
                        Divider( color = Color.White.copy(alpha = 0.5f), modifier = Modifier.height(12.dp).width(1.dp) )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon( painter = painterResource(R.drawable.restaurant), contentDescription = "Serving", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(12.dp) )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text( text = "${recipe.serving} Serving${if (recipe.serving > 1) "s" else ""}", color = Color.White, fontSize = 10.sp, fontFamily = monte )
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeChip( text: String, backgroundColor: Color, textColor: Color, iconResId: Int? = null, iconTint: Color? = null ) {
    Box( modifier = Modifier.background(backgroundColor, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).padding(horizontal = 6.dp, vertical = 3.dp) ) { // Slightly smaller padding
        Row( verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp) ) {
            if (iconResId != null) {
                Icon( painter = painterResource(id = iconResId), contentDescription = null, tint = iconTint ?: LocalContentColor.current, modifier = Modifier.size(11.dp) ) // Slightly smaller icon
            }
            Text( text = text, color = textColor, fontSize = 9.sp, fontFamily = monte, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis )
        }
    }
}

@Composable
fun NavBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    userProfileImageUrl: String? = null // Optional: Add parameter for user profile image
) {
    val items = listOf("Home", "Saved", "Add", "Calendar", "User")
    val icons = listOf(
        R.drawable.home,
        R.drawable.book,
        R.drawable.add,
        R.drawable.calendar,
        R.drawable.user
    )

    NavigationBar(
        containerColor = Color.White,
        contentColor = Color.Gray,
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    if (index == 4) { // User icon special case
                        Box(modifier = Modifier.size(28.dp)) {
                            if (userProfileImageUrl != null) {
                                // Use AsyncImage for remote URL
                                AsyncImage(
                                    model = userProfileImageUrl,
                                    contentDescription = "User Profile",
                                    placeholder = painterResource(id = icons[index]),
                                    error = painterResource(id = icons[index]),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Use regular Image for local drawable
                                Image(
                                    painter = painterResource(id = icons[index]),
                                    contentDescription = "User",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else {
                        Icon(
                            painter = painterResource(id = icons[index]),
                            contentDescription = item,
                            modifier = Modifier.size(
                                if (index == 2) 32.dp else 26.dp
                            )
                        )
                    }
                },
                selected = selectedItem == index,
                onClick = { onItemSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}