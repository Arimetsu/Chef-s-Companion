package com.example.myapplication.front_end // Adjust package as needed

import androidx.compose.animation.*
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

// Define colors if not already global
val PrimaryGreen = Color(0xFF1A4D2E)
val RatingStarColor = Color(0xFFFFC107)

@OptIn(ExperimentalAnimationApi::class) // For AnimatedVisibility
@Composable
fun YourRecipeScreen(
    navController: NavHostController,
    savedRecipesViewModel: SavedRecipesViewModel = viewModel() // Inject ViewModel
) {
    // Observe states from ViewModel
    val collectionState by savedRecipesViewModel.userCollectionsState.collectAsStateWithLifecycle()
    val recipeListState by savedRecipesViewModel.savedRecipeListState.collectAsStateWithLifecycle()

    // State for UI interaction
    var selectedCollectionFilter by remember { mutableStateOf<String>(RecipeFilters.ALL) } // Start with "All"
    var selectedTab by remember { mutableStateOf(1) } // Saved recipes tab is index 1
    var isFabExpanded by remember { mutableStateOf(false) }


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
                modifier = Modifier.padding(start = 16.dp, top = 56.dp, end = 16.dp) // Adjust padding
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
                    4 -> navController.navigate(ScreenNavigation.Screen.UserProfile.route)
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
                FloatingActionButton(modifier = Modifier.background(Color.White), onClick = { isFabExpanded = !isFabExpanded }, backgroundColor = PrimaryGreen, contentColor = Color.White ) { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White) }
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

            // Collection/Filter Chips Row
            when (val state = collectionState) {
                is UserCollectionState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
                is UserCollectionState.Success -> {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // "All" Chip
                        item {
                            CollectionChip(
                                displayText = RecipeFilters.ALL,
                                filterValue = RecipeFilters.ALL,
                                isSelected = selectedCollectionFilter == RecipeFilters.ALL,
                                onSelected = { filter ->
                                    selectedCollectionFilter = filter
                                    savedRecipesViewModel.fetchRecipesForDisplay(filter)
                                }
                            )
                        }
                        // "Favorites" Chip
                        item {
                            CollectionChip(
                                displayText = RecipeFilters.FAVORITES,
                                filterValue = RecipeFilters.FAVORITES,
                                isSelected = selectedCollectionFilter == RecipeFilters.FAVORITES,
                                onSelected = { filter ->
                                    selectedCollectionFilter = filter
                                    savedRecipesViewModel.fetchRecipesForDisplay(filter)
                                }
                            )
                        }
                        // Dynamic Collection Chips
                        items(state.collections.filterNot { it.name.equals(RecipeFilters.FAVORITES, true) }) { collection ->
                            CollectionChip(
                                displayText = collection.name,
                                filterValue = collection.id, // Use ID for filtering
                                isSelected = selectedCollectionFilter == collection.id,
                                onSelected = { filterId ->
                                    selectedCollectionFilter = filterId
                                    savedRecipesViewModel.fetchRecipesForDisplay(filterId)
                                }
                            )
                        }
                    }
                }
                is UserCollectionState.Error -> {
                    Text("Error loading collections", color = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recipe Grid - Display based on savedRecipeListState
            when (val state = recipeListState) {
                is SavedRecipeListState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryGreen) }
                }
                is SavedRecipeListState.Success -> {
                    if (state.recipes.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                            Text(when(selectedCollectionFilter){
                                RecipeFilters.ALL -> "You haven't saved any recipes yet."
                                RecipeFilters.FAVORITES -> "You haven't favorited any recipes yet."
                                else -> "No recipes in this collection."
                            }, color = Color.Gray, fontFamily = monte, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyVerticalGrid( columns = Fixed(2), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize() ) {
                            items(state.recipes, key = { it.id }) { recipe ->
                                SavedRecipeCard( recipe = recipe, onCardClick = { navController.navigate(ScreenNavigation.Screen.RecipeDetail.createRoute(recipe.id)) }, onFavoriteClick = { savedRecipesViewModel.toggleFavoriteStatus(recipe.id, recipe.favorite) } )
                            }
                        }
                    }
                }
                is SavedRecipeListState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error loading recipes: ${state.message}", color = Color.Red, textAlign = TextAlign.Center) }
                }

                SavedRecipeListState.Empty -> TODO()
            }
        }
    }
}

// --- Filter/Collection Chip (Keep Existing) ---
@Composable
fun CollectionChip(
    displayText: String,
    filterValue: String,
    isSelected: Boolean,
    onSelected: (String) -> Unit
) {
    Button(
        onClick = { onSelected(filterValue) },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isSelected) PrimaryGreen else PrimaryGreen.copy(alpha = 0.1f),
            contentColor = if (isSelected) Color.White else PrimaryGreen
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        elevation = ButtonDefaults.elevation(0.dp),
        border = if (!isSelected) BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.5f)) else null
    ) {
        Text(displayText, fontSize = 12.sp, fontFamily = monte)
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