package ui.screens.mealplan

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.components.addmealplan.RecipeSearchBar
import com.example.myapplication.data.Recipe
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.front_end.collection.RecipeItem
import com.example.myapplication.front_end.collection.SelectedRecipesBar
import com.example.myapplication.front_end.home.NavBar
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlin.collections.plus

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddMealsToMealPlanScreen(navController: NavController, mealType: String) {
    var rawSearchInput by remember { mutableStateOf("") }
    var debouncedSearchTerm by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRecipes by remember { mutableStateOf(listOf<Recipe>()) }
    var selectedTab by remember { mutableStateOf(3) }
    val allRecipes = remember {
        mutableStateListOf(
            Recipe("John Doe", "Canned Tuna Pasta", R.drawable.tryfood, "9.5", "Lunch", "15", 1, true),
            Recipe("Jane Smith", "Canned Tuna Pasta", R.drawable.tryfood, "9.5", "Lunch", "15", 1, true),
            Recipe("Peter Jones", "Canned Tuna Pasta", R.drawable.tryfood, "9.5", "Lunch", "15", 1, false),
            Recipe("Alice Brown", "Canned Tuna Pasta", R.drawable.tryfood, "9.5", "Lunch", "15", 1, false),
            Recipe("Bob Green", "Canned Tuna Pasta", R.drawable.tryfood, "9.5", "Lunch", "15", 1, false),
            Recipe("Charlie White", "Canned Tuna Pasta", R.drawable.tryfood, "9.5", "Lunch", "15", 1, true),
        )
    }

    LaunchedEffect(rawSearchInput) {
        delay(300) // Wait 300ms after last keystroke
        debouncedSearchTerm = rawSearchInput
    }

    val filteredRecipes = remember(debouncedSearchTerm) {
        if (debouncedSearchTerm.isBlank()) {
            allRecipes
        } else {
            allRecipes.filter { recipe ->
                recipe.name.contains(debouncedSearchTerm, ignoreCase = true)
            }
        }
    }

    MyApplicationTheme {
        Scaffold (
            bottomBar = {
                Column {
                    if (selectedRecipes.isNotEmpty()) {
                        SelectedRecipesBar(selectedRecipes = selectedRecipes)
                    }

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
            }
        ) { innerPadding ->
            Column (
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .fillMaxWidth()
                ) {
                    item(span = { GridItemSpan(2) }) {
                        MealPlanAddMealsHeader(
                            mealType = mealType,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    item(span = { GridItemSpan(2) }) {
                        RecipeSearchBar(
                            value = rawSearchInput,          // Pass raw input
                            onValueChange = { rawSearchInput = it } // Update raw input
                        )
                    }
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "Add Recipe",
                            modifier = Modifier
                                .padding(start = 14.dp, top = 16.dp, bottom = 10.dp),
                            style = Typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (filteredRecipes.isEmpty() && searchQuery.isNotBlank()) {
                        // Show "No results" message when filtered list is empty AND there's a search query
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No recipes found for '$searchQuery'",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Show recipes when there are results or no search query
                        items(filteredRecipes.size) { index ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                RecipeItem(
                                    recipe = filteredRecipes[index],
                                    isSelected = selectedRecipes.contains(filteredRecipes[index]),
                                    onRecipeClick = { recipe ->
                                        selectedRecipes = if (selectedRecipes.contains(recipe)) {
                                            selectedRecipes.minus(recipe)
                                        } else {
                                            selectedRecipes.plus(recipe)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MealPlanAddMealsHeader(
    mealType: String,
    onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {

        // Top Row: Back Button, Title, Confirm Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button (left)
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Title (center)
            Text(
                text = "Meal Plan",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = Typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            // Confirm button (right)
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row (modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 17.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                //For changes pa yung text since dapat depende sa ippick na category yan
                text = mealType,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
        }

    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun AddMealsToMealPlanScreenPreview() {
    MyApplicationTheme {
        AddMealsToMealPlanScreen(navController = rememberNavController(), mealType = "Breakfast")
    }
}

