package com.example.myapplication.front_end // Adjust package as needed

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridCells.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items // Use items extension for LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.* // Using Material 2 components here
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon // Using Material 3 Icon
import androidx.compose.material3.IconButton // Using Material 3 IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage // Import AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.Recipe // UI Model
import com.example.myapplication.data.UserCollection // Import collection data class
import com.example.myapplication.front_end.home.monte // Assuming font is here
import com.example.myapplication.front_end.home.NavBar // Reusing NavBar? Or define locally
import com.example.myapplication.front_end.home.bb2
import com.example.myapplication.viewModel.RecipeFilters
import com.example.myapplication.viewModel.SavedRecipeListState
import com.example.myapplication.viewModel.SavedRecipesViewModel // Import the new ViewModel
import com.example.myapplication.viewModel.UserCollectionState
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import com.example.myapplication.data.RecipeViewMode
import com.example.myapplication.front_end.home.RecipeCard

// Define colors if not already global
val PrimaryGreen = Color(0xFF1A4D2E)
val RatingStarColor = Color(0xFFFFC107)
val SelectedButtonColor = PrimaryGreen
val UnselectedButtonColor = Color.White
val SelectedContentColor = Color.White
val UnselectedContentColor = PrimaryGreen

@OptIn(ExperimentalAnimationApi::class) // For AnimatedVisibility
@Composable
fun YourRecipeScreen(
    navController: NavHostController,
    savedRecipesViewModel: SavedRecipesViewModel = viewModel() // Inject ViewModel
) {
    // Observe states from ViewModel
    val collectionState by savedRecipesViewModel.userCollectionsState.collectAsStateWithLifecycle()
    val recipeListState by savedRecipesViewModel.savedRecipeListState.collectAsStateWithLifecycle()

    var selectedViewMode by remember { mutableStateOf(RecipeViewMode.COLLECTIONS) } // Default to Collections view
    var selectedTab by remember { mutableStateOf(1) } // Bottom nav state
    var isFabExpanded by remember { mutableStateOf(false) } // FAB state

    LaunchedEffect(selectedViewMode) {
        when (selectedViewMode) {
            RecipeViewMode.ALL_RECIPES -> savedRecipesViewModel.fetchRecipesForDisplay(RecipeFilters.ALL)
            RecipeViewMode.FAVORITES -> savedRecipesViewModel.fetchRecipesForDisplay(RecipeFilters.FAVORITES)
            RecipeViewMode.COLLECTIONS -> {

            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.material.Text(
                        text = "Your Recipe",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bb2,
                        color = PrimaryGreen
                    )
                },
                backgroundColor = Color.White,
                elevation = 0.dp,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp) // Adjust padding
            )
        },
        bottomBar = {
            // Assuming NavBar is reusable and takes selectedItem/onItemSelected
            NavBar(selectedItem = selectedTab, onItemSelected = { index ->
                selectedTab = index
                when (index) {
                    0 -> navController.navigate(ScreenNavigation.Screen.Home.route) { popUpTo(ScreenNavigation.Screen.Home.route) { inclusive = true } } // Navigate Home
                    1 -> { /* Already on Saved Recipes */ }
                    2 -> navController.navigate(ScreenNavigation.Screen.AddRecipe.route)
                    3 -> navController.navigate(ScreenNavigation.Screen.MealPlan.route)
                    4 -> { /* Handle User Profile navigation */ }
                }
            })
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = { // Keep FAB logic
            Row(verticalAlignment = Alignment.Bottom) {
                AnimatedVisibility( visible = isFabExpanded, enter = slideInVertically { -it } + fadeIn(), exit = slideOutVertically { -it } + fadeOut() ) {
                    Column( horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(end = 8.dp, bottom = 16.dp) ) {
                        Button( onClick = { isFabExpanded = false; /* TODO: Handle "Recipe Finder" */ }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50), contentColor = Color.White), elevation = ButtonDefaults.elevation(defaultElevation = 4.dp) ) { Text("Recipe Finder", fontFamily = monte) }
                        Button( onClick = { isFabExpanded = false; navController.navigate(ScreenNavigation.Screen.AddRecipe.route) }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50), contentColor = Color.White), elevation = ButtonDefaults.elevation(defaultElevation = 4.dp) ) { Text("Add Recipe", fontFamily = monte) }
                        Button( onClick = { isFabExpanded = false; navController.navigate(ScreenNavigation.Screen.NewCollection.route) }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3), contentColor = Color.White), elevation = ButtonDefaults.elevation(defaultElevation = 4.dp) ) { Text("Add Collection", fontFamily = monte) }
                    }
                }
                FloatingActionButton(modifier = Modifier.background(Color.White), onClick = { isFabExpanded = !isFabExpanded }, backgroundColor = PrimaryGreen, contentColor = Color.White ) { Icon(Icons.Default.Add, contentDescription = "Add") }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply scaffold padding
                .padding(horizontal = 16.dp) // Apply horizontal padding for content
                .background(Color.White)
        ) {
            // Search Bar (Keep existing)
            Row( modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp) .border(BorderStroke(1.dp, Color(0xFFD9D9D9)), RoundedCornerShape(24.dp)).clickable { /* TODO: Implement Search Navigation/UI */ }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically ) {
                Icon( Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray )
                Spacer(modifier = Modifier.width(8.dp))
                Text( "Search your saved recipes", color = Color.Gray, fontFamily = monte )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text( "Collections", fontSize = 20.sp, fontFamily = monte, fontWeight = FontWeight(600) )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewModeButton(
                    text = "Collection",
                    isSelected = selectedViewMode == RecipeViewMode.COLLECTIONS,
                    onClick = { selectedViewMode = RecipeViewMode.COLLECTIONS },
                    modifier = Modifier.weight(1f)
                )
                ViewModeButton(
                    text = "All",
                    isSelected = selectedViewMode == RecipeViewMode.ALL_RECIPES,
                    onClick = { selectedViewMode = RecipeViewMode.ALL_RECIPES },
                    modifier = Modifier.weight(1f)
                )
                ViewModeButton(
                    text = "Favorites",
                    isSelected = selectedViewMode == RecipeViewMode.FAVORITES,
                    onClick = { selectedViewMode = RecipeViewMode.FAVORITES },
                    modifier = Modifier.weight(1f)
                )
            }

            AnimatedContent(
                targetState = selectedViewMode,
                transitionSpec = {
                    // Define animations (optional)
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                }
            ) { mode ->
                when (mode) {
                    RecipeViewMode.COLLECTIONS -> {
                        CollectionsView(collectionState = collectionState,
                            navController = navController) { collectionId ->

                            // Collection View
                        }
                    }
                    RecipeViewMode.ALL_RECIPES -> {
                        RecipesGridView(
                            recipeListState = recipeListState,
                            viewMode = RecipeViewMode.ALL_RECIPES, // Pass mode for empty text
                            navController = navController,
                            savedRecipesViewModel = savedRecipesViewModel
                        )
                    }
                    RecipeViewMode.FAVORITES -> {
                        RecipesGridView(
                            recipeListState = recipeListState,
                            viewMode = RecipeViewMode.FAVORITES, // Pass mode for empty text
                            navController = navController,
                            savedRecipesViewModel = savedRecipesViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ViewModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button( // Use M2 Button
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp), // More rounded corners
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isSelected) SelectedButtonColor else UnselectedButtonColor,
            contentColor = if (isSelected) SelectedContentColor else UnselectedContentColor
        ),
        contentPadding = PaddingValues(vertical = 10.dp), // Adjust padding
        elevation = ButtonDefaults.elevation(0.dp), // No elevation
        border = if (!isSelected) BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.5f)) else null
    ) {
        Text(text, fontSize = 13.sp, fontFamily = monte, fontWeight = FontWeight.SemiBold) // Use M2 Text
    }
}

@Composable
fun CollectionsView(
    collectionState: UserCollectionState,
    navController: NavController, // <-- Add this line
    onCollectionClick: (String) -> Unit // Pass collection ID on click
) {
    when (collectionState) {
        is UserCollectionState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        }
        is UserCollectionState.Success -> {
            // Filter out the "Favorites" collection from display
            val collectionsToShow = collectionState.collectionsWithPreviews.filterNot {
                it.collection.name.equals(RecipeFilters.FAVORITES, ignoreCase = true)
            }

            if (collectionsToShow.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.TopCenter) {
                    Text(
                        text = "You haven't created any collections yet.\nTap the '+' button to add one!",
                        color = Color.Gray,
                        fontFamily = monte,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(collectionsToShow, key = { it.collection.id }) { collectionWithPreview ->
                        CollectionCard(
                            collectionName = collectionWithPreview.collection.name,
                            previewImageUrls = collectionWithPreview.previewImageUrls, // Pass the URLs
                            onClick = { navController.navigate(
                                ScreenNavigation.Screen.CollectionDetail.createRoute(
                                    collectionId = collectionWithPreview.collection.id,
                                    collectionName = collectionWithPreview.collection.name // Pass name here
                                )) } // Pass ID
                        )
                    }
                }
            }
        }
        is UserCollectionState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error loading collections: ${collectionState.message}", color = Color.Red, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun RecipesGridView(
    recipeListState: SavedRecipeListState,
    viewMode: RecipeViewMode, // To customize empty message
    navController: NavHostController,
    savedRecipesViewModel: SavedRecipesViewModel // ViewModel is still needed for potential future actions or if favorite status comes from detail screen
) {
    when (recipeListState) {
        is SavedRecipeListState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        }
        is SavedRecipeListState.Success -> {
            if (recipeListState.recipes.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.TopCenter) {
                    val emptyText = when (viewMode) {
                        RecipeViewMode.ALL_RECIPES -> "You haven't saved any recipes yet."
                        RecipeViewMode.FAVORITES -> "You haven't favorited any recipes yet."
                        else -> "No recipes to display." // Fallback
                    }
                    Text( // Use M2 Text
                        text = emptyText,
                        color = Color.Gray,
                        fontFamily = monte,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp), // Matches RecipeCard padding? Adjust if needed
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // Matches RecipeCard padding? Adjust if needed
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp) // Consistent padding around grid
                ) {
                    items(recipeListState.recipes, key = { it.id }) { recipe ->
                        // *** Use RecipeCard from home package ***
                        RecipeCard( // <-- Using the imported card
                            recipe = recipe,
                            // Pass the navigation logic to the onClick parameter
                            onClick = {
                                navController.navigate(ScreenNavigation.Screen.RecipeDetail.createRoute(recipe.id))
                            }
                        )
                    }
                }
            }
        }
        is SavedRecipeListState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error loading recipes: ${recipeListState.message}", color = Color.Red, textAlign = TextAlign.Center) // Use M2 Text
            }
        }
        is SavedRecipeListState.Empty -> { // Handle Empty state if used
            Box(Modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.TopCenter) {
                Text(
                    text = "No recipes found.", // Generic empty message
                    color = Color.Gray,
                    fontFamily = monte,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}



// --- Recipe Card specific for Saved Recipes Screen ---
@Composable
fun SavedRecipeCard(
    recipe: Recipe,
    onCardClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFF5F5F5), // Slightly different background maybe
        elevation = 2.dp,
        modifier = Modifier
            .padding(4.dp)
            // .aspectRatio(0.8f) // Adjust aspect ratio if needed
            .height(200.dp) // Slightly taller card?
            .clickable(onClick = onCardClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.name,
                placeholder = painterResource(R.drawable.greenbackgroundlogo),
                error = painterResource(R.drawable.greenbackgroundlogo), // Use consistent placeholder on error
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box( // Gradient Overlay
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient( colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)), startY = 300f )) // Darker gradient?
            )
            // Favorite button top right
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape) // Semi-transparent background
            ) {
                Icon(
                    imageVector = if (recipe.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (recipe.favorite) Color.Red else Color.White, // Red when favorite
                    modifier = Modifier.size(20.dp)
                )
            }

            // Content bottom left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = recipe.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp, // Adjusted size
                    fontFamily = monte,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon( Icons.Filled.Star, contentDescription = "Rating", tint = RatingStarColor, modifier = Modifier.size(14.dp) )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text( text = String.format("%.1f", recipe.averageRating), color = Color.White, fontSize = 11.sp, fontFamily = monte )
                    // Add other info like time if desired
                }
            }
        }
    }
}

@Composable
fun CollectionCard(
    collectionName: String,
    previewImageUrls: List<String>, // Expecting a list of image URLs
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Maintain square-ish shape
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        backgroundColor = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp), // Padding only at the bottom for the name
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image Grid Area (2x2)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Take available space above the name
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)) // Clip top corners
                    .background(Color.Gray) // Background if no images load
            ) {
                Row(Modifier.fillMaxSize()) {
                    // Column 1
                    Column(Modifier.fillMaxHeight().weight(1f)) {
                        PreviewImage(
                            url = previewImageUrls.getOrNull(0), // Top-left
                            modifier = Modifier.fillMaxSize().weight(1f)
                        )
                        PreviewImage(
                            url = previewImageUrls.getOrNull(2), // Bottom-left
                            modifier = Modifier.fillMaxSize().weight(1f)
                        )
                    }
                    // Column 2
                    Column(Modifier.fillMaxHeight().weight(1f)) {
                        PreviewImage(
                            url = previewImageUrls.getOrNull(1), // Top-right
                            modifier = Modifier.fillMaxSize().weight(1f)
                        )
                        PreviewImage(
                            url = previewImageUrls.getOrNull(3), // Bottom-right
                            modifier = Modifier.fillMaxSize().weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Space between grid and text

            // Collection Name
            Text(
                text = collectionName,
                fontFamily = monte,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.DarkGray, // Or PrimaryGreen
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp) // Padding for the text
            )
        }
    }
}

// Helper composable for individual preview images
@Composable
fun PreviewImage(url: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            // Add a subtle border between images if desired
            // .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f)))
            .background(Color.Transparent) // Background shown if URL is null or loading fails severely
    ) {
        if (!url.isNullOrEmpty()) {
            AsyncImage(
                model = url,
                contentDescription = null, // Decorative image
                placeholder = painterResource(R.drawable.greenbackgroundlogo), // Your placeholder drawable
                error = painterResource(R.drawable.greenbackgroundlogo), // Your error drawable
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Crop to fill the space
            )
        }
        // else: the Box with PlaceholderColor background is shown
    }
}