package ui.screens.mealplan

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items // Correct items import
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.* // Use Material 3 components
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
// import com.example.myapplication.R // Only needed if BadgeChip etc. are defined locally
import com.example.myapplication.components.addmealplan.RecipeSearchBar // Assuming this exists and is compatible
import com.example.myapplication.data.Recipe // UI Model
// import com.example.myapplication.front_end.ScreenNavigation // Needed if NavBar handles navigation internally
import com.example.myapplication.front_end.collection.SelectableHomeScreenRecipeCard // *** USE THIS CARD ***
import com.example.myapplication.front_end.collection.SelectedRecipesBar // *** USE THIS BAR ***
import com.example.myapplication.front_end.collection.SelectedRecipeChip // Needed by SelectedRecipesBar
import com.example.myapplication.front_end.home.BadgeChip // Needed by SelectableHomeScreenRecipeCard
import com.example.myapplication.front_end.home.NavBar // Reusing NavBar
import com.example.myapplication.front_end.home.RatingStarColor // Needed by BadgeChip/Card
import com.example.myapplication.front_end.home.monte // Needed by Card/Bar
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Typography // Assuming Typography object exists
import com.example.myapplication.viewModel.MealPlanUpdateState
import com.example.myapplication.viewModel.MealPlanViewModel // Import ViewModel
import com.example.myapplication.viewModel.SavedRecipeListState // Import State
import kotlinx.coroutines.delay
import java.time.LocalDate // Import LocalDate

// Define colors/fonts if not globally accessible
val PrimaryGreen = Color(26, 77, 46)
// val RatingStarColor = Color(0xFFFFC107)
// val monte = FontFamily(...) // Make sure fonts are accessible

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddMealsToMealPlanScreen(
    navController: NavController,
    mealType: String,
    selectedDate: LocalDate = LocalDate.now(), // Ensure date is passed correctly
    mealPlanViewModel: MealPlanViewModel = viewModel()
) {
    var rawSearchInput by remember { mutableStateOf("") }
    var debouncedSearchTerm by remember { mutableStateOf("") }
    var selectedRecipeIds by remember { mutableStateOf(emptySet<String>()) }
    var selectedTab by remember { mutableStateOf(3) } // Default to Meal Plan tab

    val availableRecipesState by mealPlanViewModel.availableRecipesState.collectAsStateWithLifecycle()
    val updateState by mealPlanViewModel.updateState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Derived state to get actual Recipe objects for selected IDs (needed for bottom bar)
    val selectedRecipes = remember(availableRecipesState, selectedRecipeIds) {
        if (availableRecipesState is SavedRecipeListState.Success) {
            (availableRecipesState as SavedRecipeListState.Success).recipes.filter {
                selectedRecipeIds.contains(it.id)
            }
        } else {
            emptyList()
        }
    }

    // Effect for debouncing search input
    LaunchedEffect(rawSearchInput) {
        delay(300) // Debounce delay
        debouncedSearchTerm = rawSearchInput
        mealPlanViewModel.fetchAvailableRecipesForAdding(debouncedSearchTerm)
    }

    // Effect to fetch initial available recipes (without search term)
    LaunchedEffect(Unit) {
        mealPlanViewModel.fetchAvailableRecipesForAdding()
    }

    // Effect to handle update state feedback and navigation
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is MealPlanUpdateState.Success -> {
                Toast.makeText(context, "$mealType meals updated!", Toast.LENGTH_SHORT).show()
                mealPlanViewModel.resetUpdateState()
                navController.popBackStack() // Go back after success
            }
            is MealPlanUpdateState.Error -> {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                mealPlanViewModel.resetUpdateState() // Reset state after showing error
            }
            else -> { /* Idle or Loading - Do nothing here */ }
        }
    }


    MyApplicationTheme {
        Scaffold (
            topBar = {
                // Header moved into LazyVerticalGrid for better scroll behavior
            },
            bottomBar = {
                Column { // Stack SelectedRecipesBar and NavBar vertically
                    // Show the selected recipes bar only if items are selected
                    if (selectedRecipes.isNotEmpty()) {
                        SelectedRecipesBar( // *** USE THE ACTUAL BAR ***
                            selectedRecipes = selectedRecipes,
                            onUnselect = { recipeId ->
                                selectedRecipeIds = selectedRecipeIds - recipeId // Update state on unselect
                            }
                        )
                    }
                    NavBar(
                        selectedItem = selectedTab,
                        onItemSelected = { index ->
                            selectedTab = index
                            // Handle navigation based on NavBar selection if needed
                            // e.g., navigate home, to saved recipes, etc.
                            // Consider if navigating away should clear selections or prompt user.
                        }
                        // Pass user profile image if needed by NavBar:
                        // userProfileImageUrl = ...
                    )
                }
            }
        ) { innerPadding ->
            // Apply innerPadding (from Scaffold) to the LazyVerticalGrid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // *** APPLY SCAFFOLD PADDING HERE ***
                    .padding(horizontal = 16.dp), // Apply horizontal padding for grid content
                contentPadding = PaddingValues(bottom = 16.dp) // Padding at the very bottom of the scrollable content
            ) {
                // Span the header across both columns
                item(span = { GridItemSpan(2) }) {
                    MealPlanAddMealsHeader(
                        mealType = mealType,
                        onBackClick = { navController.popBackStack() },
                        onConfirmClick = {
                            if (selectedRecipeIds.isNotEmpty()) {
                                mealPlanViewModel.addRecipesToMealSlot(
                                    date = selectedDate,
                                    mealType = mealType,
                                    recipeIdsToAdd = selectedRecipeIds.toList()
                                )
                            } else {
                                Toast.makeText(context, "Select at least one recipe", Toast.LENGTH_SHORT).show()
                            }
                        },
                        // Disable confirm button while an update is in progress
                        isConfirmEnabled = updateState != MealPlanUpdateState.Loading
                    )
                }

                // Span the search bar across both columns
                item(span = { GridItemSpan(2) }) {
                    RecipeSearchBar(
                        value = rawSearchInput,
                        onValueChange = { rawSearchInput = it },
                        modifier = Modifier.padding(top = 8.dp) // Add some top padding
                    )
                }

                // Span the "Add Recipe" title across both columns
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "Select Recipes to Add", // Changed title for clarity
                        modifier = Modifier.padding(top = 16.dp, bottom = 10.dp), // Adjusted padding
                        style = Typography.titleMedium, // Use defined Typography
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold // Make title bold
                    )
                }

                // Handle Recipe List States
                when (val state = availableRecipesState) {
                    is SavedRecipeListState.Loading -> {
                        item(span = { GridItemSpan(2) }) {
                            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { // Give loading box height
                                CircularProgressIndicator(color = PrimaryGreen) // Use consistent color
                            }
                        }
                    }
                    is SavedRecipeListState.Success -> {
                        if (state.recipes.isEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) { // Add vertical padding
                                    Text(
                                        text = if (debouncedSearchTerm.isBlank()) "No recipes found." else "No recipes match '$debouncedSearchTerm'",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Display recipe items using the correct card
                            items(state.recipes, key = { it.id }) { recipe ->
                                SelectableHomeScreenRecipeCard( // *** USE THE CORRECT CARD ***
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
                    is SavedRecipeListState.Error -> {
                        item(span = { GridItemSpan(2) }) {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Error loading recipes: ${state.message}",
                                    color = MaterialTheme.colorScheme.error, // Use theme error color
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    // Handle potential Empty state explicitly if defined in SavedRecipeListState
                    is SavedRecipeListState.Empty -> {
                        item(span = { GridItemSpan(2) }) {
                            Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                                Text("No recipes available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } // End When
            } // End LazyVerticalGrid
        } // End Scaffold
    } // End MyApplicationTheme
}

// Header Composable (minor adjustments for padding/style)
@Composable
fun MealPlanAddMealsHeader(
    mealType: String,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit,
    isConfirmEnabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), // Adjust vertical padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Use SpaceBetween for better alignment
        ) {
            IconButton(onClick = onBackClick) {
                Icon( Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary )
            }
            // Centered Title (if desired, otherwise remove textAlign)
            Text(
                text = "Meal Plan",
                // modifier = Modifier.weight(1f), // Remove weight if using SpaceBetween
                textAlign = TextAlign.Center,
                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold), // Make title bold
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onConfirmClick, enabled = isConfirmEnabled) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Confirm",
                    tint = if(isConfirmEnabled) MaterialTheme.colorScheme.primary else Color.Gray // Use theme color or Gray
                )
            }
        }
        // Subtitle below the main header row
        Text(
            text = "Add to $mealType",
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), // Adjust padding
            textAlign = TextAlign.Center,
            style = Typography.titleMedium, // Use appropriate style
            color = MaterialTheme.colorScheme.secondary // Use secondary color for subtitle
        )
    }
}
