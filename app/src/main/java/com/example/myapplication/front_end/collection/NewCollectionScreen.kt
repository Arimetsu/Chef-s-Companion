package com.example.myapplication.front_end.collection

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close // For removing items
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.* // Use M3 components
import androidx.compose.runtime.*
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
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewModel.SavedRecipeListState
import com.example.myapplication.viewModel.SavedRecipesViewModel

// Define or import colors and fonts if not globally accessible
val PrimaryGreen = Color(26, 77, 46)
// val RatingStarColor = Color(0xFFFFC107)
// val monte = FontFamily( Font(R.font.montserrat_regular), Font(R.font.montserrat_bold, FontWeight.Bold), Font(R.font.montserrat_light, FontWeight.Light) )


@Composable
fun NewCollectionScreen(
    onNavigateToNaming: (List<String>) -> Unit,
    navController: NavController,
    savedRecipesViewModel: SavedRecipesViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        savedRecipesViewModel.fetchAllRecipes()
    }

    val allRecipesState by savedRecipesViewModel.allRecipesState.collectAsStateWithLifecycle()
    var selectedRecipeIds by remember { mutableStateOf(emptySet<String>()) }
    var searchQuery by remember { mutableStateOf("") }

    // Derived state to get the actual Recipe objects for selected IDs
    val selectedRecipes = remember(allRecipesState, selectedRecipeIds) {
        if (allRecipesState is SavedRecipeListState.Success) {
            (allRecipesState as SavedRecipeListState.Success).recipes.filter {
                selectedRecipeIds.contains(it.id)
            }
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon( Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryGreen )
                }
                Text(
                    text = "New Collection",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    fontFamily = monte
                )
                IconButton(
                    onClick = {
                        if (selectedRecipeIds.isNotEmpty()) {
                            onNavigateToNaming(selectedRecipeIds.toList())
                        }
                    },
                    enabled = selectedRecipeIds.isNotEmpty()
                ) {
                    IconButton(
                        onClick = {
                            if (selectedRecipeIds.isNotEmpty()) {
                                onNavigateToNaming(selectedRecipeIds.toList())
                            }
                        },
                        enabled = selectedRecipeIds.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Next",
                            tint = if (selectedRecipeIds.isNotEmpty()) PrimaryGreen else Color.Gray
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Show the selected recipes bar only if items are selected
            if (selectedRecipes.isNotEmpty()) {
                SelectedRecipesBar(
                    selectedRecipes = selectedRecipes,
                    onUnselect = { recipeId ->
                        selectedRecipeIds = selectedRecipeIds - recipeId
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues) // Apply scaffold padding
                .padding(horizontal = 16.dp) // Apply horizontal padding for content
                .fillMaxSize()
                .background(Color.White)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                placeholder = { Text("Search all recipes by name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Box( modifier = Modifier.fillMaxWidth() ) {
                Text(
                    text = "Select Recipes to Add",
                    fontSize = 18.sp,
                    fontFamily = monte,
                    fontWeight = FontWeight(600),
                    color = PrimaryGreen,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Recipe Grid Area - takes remaining space
            Box(modifier = Modifier.weight(1f)) {
                when (val state = allRecipesState) {
                    is SavedRecipeListState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryGreen)
                        }
                    }
                    is SavedRecipeListState.Success -> {
                        val recipesToDisplay = if (searchQuery.isBlank()) {
                            state.recipes
                        } else {
                            state.recipes.filter {
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                        it.category.contains(searchQuery, ignoreCase = true) // Example: search category too
                            }
                        }

                        if (recipesToDisplay.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (searchQuery.isBlank()) "No recipes found." else "No recipes match your search.",
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 8.dp) // Padding at the bottom of the grid
                            ) {
                                items(recipesToDisplay, key = { it.id }) { recipe ->
                                    SelectableHomeScreenRecipeCard( // Use the new composable
                                        recipe = recipe,
                                        isSelected = selectedRecipeIds.contains(recipe.id),
                                        onClick = { clickedRecipe ->
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
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error loading recipes: ${state.message}", color = Color.Red, textAlign = TextAlign.Center)
                        }
                    }
                    is SavedRecipeListState.Empty -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No recipes available.", color = Color.Gray)
                        }
                    }
                }
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
            // --- End: Copied Content ---

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

// --- New Composable for the Bottom Bar showing selected recipes ---
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

