package com.example.myapplication.front_end.collection

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items // Use items for LazyVerticalGrid
import androidx.compose.foundation.lazy.items // Use items for LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close // For removing items
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.* // Use M3 components
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.Recipe // UI Model
import com.example.myapplication.front_end.home.BadgeChip // Assuming BadgeChip is accessible or copied here
import com.example.myapplication.front_end.home.RatingStarColor // Assuming RatingStarColor is accessible or defined here
import com.example.myapplication.front_end.home.bb2 // Assuming fonts are accessible or defined here
import com.example.myapplication.front_end.home.monte // Assuming fonts are accessible or defined here
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.front_end.home.NavBar
import com.example.myapplication.front_end.home.monte
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewModel.SavedRecipeListState
import com.example.myapplication.viewModel.SavedRecipesViewModel
import androidx.compose.ui.text.TextStyle

// Define or import colors and fonts if not globally accessible
val PrimaryGreen = Color(26, 77, 46)
// val RatingStarColor = Color(0xFFFFC107)
// val monte = FontFamily( Font(R.font.montserrat_regular), Font(R.font.montserrat_bold, FontWeight.Bold), Font(R.font.montserrat_light, FontWeight.Light) )


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCollectionScreen(
    onNavigateToNaming: (List<String>) -> Unit, // Callback with selected IDs
    navController: NavController,
    savedRecipesViewModel: SavedRecipesViewModel = viewModel()
) {
    // --- State ---
    // Observe the state holding the user's SAVED recipes
    val savedListState by savedRecipesViewModel.savedRecipeListState.collectAsStateWithLifecycle()
    // Use rememberSaveable for selection persistence across recompositions/rotations
    var selectedRecipeIds by rememberSaveable { mutableStateOf(emptySet<String>()) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // --- Effect ---
    // Fetch the user's saved recipes ONCE when the screen loads
    LaunchedEffect(Unit) {
        Log.d("NewCollectionScreen", "LaunchedEffect: Fetching recipes for selection.")
        // Call the function that fetches ALL SAVED recipes for the user
        savedRecipesViewModel.fetchRecipesForSelection()
        // Or: savedRecipesViewModel.fetchRecipesForDisplay(RecipeFilters.ALL) - if that fits better
    }

    // --- Derived State ---
    // Calculate the list of selected Recipe objects based on the current state and selected IDs
    val selectedRecipes = remember(savedListState, selectedRecipeIds) {
        when (val currentState = savedListState) {
            is SavedRecipeListState.Success -> {
                // Filter the successfully loaded recipes
                currentState.recipes.filter { selectedRecipeIds.contains(it.id) }
            }
            else -> {
                // Return empty list if saved recipes are not loaded or in error state
                emptyList()
            }
        }
    }

    Log.d("NewCollectionScreen", "Recomposition: SavedListState=${savedListState::class.simpleName}, SelectedIDs=${selectedRecipeIds.size}")

    // --- UI ---
    Scaffold(
        topBar = {
            // Use CenterAlignedTopAppBar for better title centering
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "New Collection",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        fontFamily = monte
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon( Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryGreen )
                    }
                },
                actions = {
                    // Next button to proceed to naming screen
                    IconButton(
                        onClick = {
                            if (selectedRecipeIds.isNotEmpty()) {
                                Log.d("NewCollectionScreen", "Navigating to Naming with ${selectedRecipeIds.size} IDs.")
                                onNavigateToNaming(selectedRecipeIds.toList()) // Pass the list of selected IDs
                            } else {
                                Log.d("NewCollectionScreen", "Next clicked but no recipes selected.")
                                // Optionally show a toast message
                            }
                        },
                        enabled = selectedRecipeIds.isNotEmpty() // Enable only when recipes are selected
                    ) {
                        Icon(
                            Icons.Default.ArrowForward, // Use ArrowForward for "Next"
                            contentDescription = "Next",
                            tint = if (selectedRecipeIds.isNotEmpty()) PrimaryGreen else Color.Gray // Indicate enabled/disabled state
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White) // White background
            )
        },
        bottomBar = {
            // Show the selected recipes bar only if items are selected
            if (selectedRecipes.isNotEmpty()) {
                SelectedRecipesBar(
                    selectedRecipes = selectedRecipes,
                    onUnselect = { recipeId ->
                        // Update the selection state when an item is unselected from the bar
                        selectedRecipeIds = selectedRecipeIds - recipeId
                    }
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the available space
                .background(Color.White) // Set background color
                .padding(paddingValues) // Apply scaffold padding (for content area)
                .padding(horizontal = 16.dp) // Apply horizontal padding for content inside the column
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = Color.Gray) },
                placeholder = { Text("Search your saved recipes...", color = Color.Gray, fontFamily = monte) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(24.dp), // More rounded shape
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = PrimaryGreen
                ),
                textStyle = TextStyle(fontFamily = monte) // Apply font
            )

            Spacer(modifier = Modifier.height(8.dp)) // Space after search bar

            // Title Text
            Text(
                text = "Select Recipes to Add",
                fontSize = 18.sp,
                fontFamily = monte,
                fontWeight = FontWeight.SemiBold, // Use SemiBold
                color = Color.DarkGray, // Slightly softer color
                modifier = Modifier.fillMaxWidth() // Ensure it takes full width for alignment
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recipe Grid Area - takes remaining space
            Box(modifier = Modifier.weight(1f)) { // Use weight to fill available vertical space
                // Use the correct state here: savedListState
                when (val state = savedListState) {
                    is SavedRecipeListState.Loading -> {
                        // Show loading indicator centered
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryGreen)
                        }
                    }
                    is SavedRecipeListState.Success -> {
                        // Filter recipes based on search query
                        val recipesToDisplay = if (searchQuery.isBlank()) {
                            state.recipes // Show all saved recipes if search is empty
                        } else {
                            state.recipes.filter { recipe ->
                                // Case-insensitive search in name, author, or category
                                recipe.name.contains(searchQuery, ignoreCase = true) ||
                                        recipe.nameOfPerson.contains(searchQuery, ignoreCase = true) || // Search author
                                        recipe.category.contains(searchQuery, ignoreCase = true)    // Search category
                            }
                        }

                        if (recipesToDisplay.isEmpty()) {
                            // Show message if no recipes match search or if list is empty
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (searchQuery.isBlank()) "You haven't saved any recipes yet." else "No saved recipes match your search.",
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    fontFamily = monte,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            // Display the grid of selectable recipes
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2), // 2 columns
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize(), // Fill the Box area
                                contentPadding = PaddingValues(bottom = 8.dp) // Padding at the bottom inside the grid
                            ) {
                                items(recipesToDisplay, key = { it.id }) { recipe ->
                                    // Use the selectable card composable
                                    SelectableHomeScreenRecipeCard(
                                        recipe = recipe,
                                        isSelected = selectedRecipeIds.contains(recipe.id),
                                        onClick = { clickedRecipe ->
                                            // Toggle selection state
                                            selectedRecipeIds = if (selectedRecipeIds.contains(clickedRecipe.id)) {
                                                selectedRecipeIds - clickedRecipe.id
                                            } else {
                                                selectedRecipeIds + clickedRecipe.id
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is SavedRecipeListState.Error -> {
                        // Show error message centered
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Error loading your saved recipes:\n${state.message}",
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                                fontFamily = monte,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    is SavedRecipeListState.Empty -> {
                        // This state might be redundant if Success([]) is handled, but included for completeness
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "You haven't saved any recipes yet.",
                                color = Color.Gray,
                                fontFamily = monte,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } // End when(state)
            } // End Recipe Grid Area Box
        } // End Column
    } // End Scaffold
}


// --- New Composable for Selectable Card using HomeScreen Style ---
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class) // Needed for FlowRow, Card onClick
@Composable
fun SelectableHomeScreenRecipeCard(
    recipe: Recipe,
    isSelected: Boolean,
    onClick: (Recipe) -> Unit
) {
    Card(
        onClick = { onClick(recipe) }, // Make the whole card clickable
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            // .padding(4.dp) // Grid adds spacing
            .fillMaxWidth()
            .height(175.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isSelected) BorderStroke(2.dp, PrimaryGreen) else null // Highlight border if selected
    ) {
        Box { // Use Box to layer card content and selection indicator
            // --- Start: Copied Content from HomeScreen RecipeCard ---
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.name,
                placeholder = painterResource(R.drawable.greenbackgroundlogo), // Use appropriate placeholder
                error = painterResource(R.drawable.greenbackgroundlogo),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box( // Darkening Gradient
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 200f
                        )
                    )
            )
            Box( // Optional: Green Overlay Gradient (adjust or remove if needed)
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors = listOf(Color.Transparent, PrimaryGreen.copy(alpha = 0.6f)) ))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BadgeChip(text = recipe.nameOfPerson, iconResId = R.drawable.user, iconTint = Color.White, backgroundColor = PrimaryGreen.copy(alpha = 0.8f), textColor = Color.White)
                    BadgeChip(text = recipe.category, backgroundColor = Color.White.copy(alpha = 0.85f), textColor = Color.Black)
                    BadgeChip(text = String.format("%.1f", recipe.averageRating), iconResId = R.drawable.star, iconTint = RatingStarColor, backgroundColor = Color.White.copy(alpha = 0.85f), textColor = Color.Black)
                }

                Column {
                    Text(text = recipe.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp, fontFamily = monte, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.alarm), contentDescription = "Time", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = recipe.cookingTime.takeIf { it.isNotEmpty() } ?: "-", color = Color.White, fontSize = 10.sp, fontFamily = monte)
                        Spacer(modifier = Modifier.width(6.dp))
                        Divider(color = Color.White.copy(alpha = 0.5f), modifier = Modifier.height(12.dp).width(1.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(painter = painterResource(R.drawable.restaurant), contentDescription = "Serving", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "${recipe.serving} Serving${if (recipe.serving > 1) "s" else ""}", color = Color.White, fontSize = 10.sp, fontFamily = monte)
                    }
                }
            }

            // --- Selection Indicator ---
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .background(PrimaryGreen, CircleShape), // Use CircleShape
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            // --- End Selection Indicator ---
        } // End Outer Box
    } // End Card
}

@Composable
fun SelectedRecipesBar(
    selectedRecipes: List<Recipe>,
    onUnselect: (recipeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface( // Use Surface for elevation and background
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 4.dp, // Add some shadow to lift it
        color = Color(96,137,99) // Use a theme color
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
            Text(
                text = "Selected Recipes (${selectedRecipes.size})",
                fontFamily = monte,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(selectedRecipes, key = { it.id }) { recipe ->
                    SelectedRecipeChip(recipe = recipe, onUnselect = { onUnselect(recipe.id) })
                }
            }
        }
    }
}

// --- Composable for a single item in the SelectedRecipesBar ---
@Composable
fun SelectedRecipeChip(
    recipe: Recipe,
    onUnselect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(40.dp) // Fixed height for chips
                .background(Color(32,137,99), RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .padding(start = 6.dp, end = 4.dp) // Adjust padding
        ) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = null, // Decorative
                placeholder = painterResource(R.drawable.greenbackgroundlogo),
                error = painterResource(R.drawable.greenbackgroundlogo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(28.dp) // Smaller image size
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = recipe.name,
                fontSize = 12.sp,
                fontFamily = monte,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f, fill = false) // Allow text to shrink if needed but don't force fill
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onUnselect,
                modifier = Modifier.size(24.dp) // Smaller icon button
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove ${recipe.name}",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp) // Smaller icon
                )
            }
        }
    }
}

