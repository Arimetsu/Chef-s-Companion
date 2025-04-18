import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.myapplication.R
import com.example.myapplication.front_end.home.monte
import androidx.navigation.NavHostController
import com.example.myapplication.data.Recipe
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.front_end.home.NavBar

val recipes = listOf(
    Recipe("Veni", "Spicy Firecracker Beef", R.drawable.tryfood, "9.5", "Lunch", "1 Hour", 5, false),
    Recipe("Vennidicto Cruz Aack", "Chicken Fajitas", R.drawable.tryfood, "9.2", "Dinner", "30 min", 1, false),
    Recipe("Vennidict", "Canned Tuna Pasta", R.drawable.tryfood, "9.3", "Breakfast", "30 min", 1, false),
    Recipe("Vennidict", "Canned Tuna Pasta", R.drawable.tryfood, "9.3", "Lunch", "30 min", 1, true),
    Recipe("Vennidict", "Canned Tuna Pasta", R.drawable.tryfood, "9.3", "Lunch", "30 min", 1, true),
    Recipe("Vennidict", "Canned Tuna Pasta", R.drawable.tryfood, "9.3", "Lunch", "30 min", 1, true),
    Recipe("Vennidict", "Canned Tuna Pasta", R.drawable.tryfood, "9.3", "Lunch", "30 min", 1, true)
)



@Composable
fun YourRecipeScreen(navController: NavHostController) {
    val savedRecipes = remember {
        mutableStateListOf(
            Recipe("John Doe", "Delicious Pasta", R.drawable.tryfood, "4.5", "Lunch", "30 mins", 2, true),
            Recipe("Jane Smith", "Easy Salad", R.drawable.tryfood, "4.8", "Lunch", "15 mins", 1, false),
            Recipe("Peter Jones", "Morning Toast", R.drawable.tryfood, "4.2", "Breakfast", "5 mins", 1, true),
            Recipe("Alice Brown", "Sweet Pancakes", R.drawable.tryfood, "4.9", "Breakfast", "20 mins", 2, true),
            Recipe("Charlie Green", "Roasted Chicken", R.drawable.tryfood, "4.7", "Dinner", "60 mins", 4, false),
            Recipe("Diana White", "Simple Soup", R.drawable.tryfood, "4.6", "Dinner", "45 mins", 3, false),
            Recipe("Eve Black", "Fruity Smoothie", R.drawable.tryfood, "4.4", "Breakfast", "10 mins", 1, false),
            Recipe("Frank Gray", "Grilled Salmon", R.drawable.tryfood, "4.8", "Dinner", "25 mins", 2, true),
            Recipe("Grace Blue", "Quick Noodles", R.drawable.tryfood, "4.3", "Lunch", "12 mins", 1, false),
            Recipe("Henry Red", "Baked Potatoes", R.drawable.tryfood, "4.5", "Dinner", "50 mins", 3, true),
            Recipe("Ivy Gold", "Berry Yogurt", R.drawable.tryfood, "4.7", "Breakfast", "8 mins", 1, false),
            Recipe("Jack Silver", "Vegetable Curry", R.drawable.tryfood, "4.6", "Lunch", "40 mins", 2, true),
            Recipe("Jack Silver", "Vegetable Curry", R.drawable.tryfood, "4.6", "Secret", "40 mins", 2, false),
        )
    }

    var selectedCollectionFilter by remember { mutableStateOf("Collection") }
    var search by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(1) }
    var isFabExpanded by remember { mutableStateOf(false) }

    val groupedRecipes = remember(savedRecipes) {
        savedRecipes.groupBy { it.category }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.padding(16.dp)
                    .padding(top = 34.dp)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Your Recipe",
                            color = Color(0xFF1A4D2E),
                            fontWeight = FontWeight(700),
                            fontSize = 32.sp,
                            fontFamily = monte
                        )
                    },
                    backgroundColor = Color.White,
                    elevation = 0.dp
                )
            }
        },
        bottomBar = {
            NavBar(selectedItem = selectedTab, onItemSelected = {
                selectedTab = it
                if (it == 1) { // Index 1 corresponds to "Saved" (book icon)
                    navController.navigate(ScreenNavigation.Screen.YourRecipes.route)
                }
                else if (it == 2) {
                    //TODO USER POST RECIPE
                }
                else if (it == 3) {
                    navController.navigate(ScreenNavigation.Screen.MealPlan.route)
                }
                else if (it == 4) {
                    navController.navigate(ScreenNavigation.Screen.UserProfile.route)
                }
            })
        },
        floatingActionButtonPosition = FabPosition.End, // Ensure FAB is at the end (right)
        floatingActionButton = {
            Row(verticalAlignment = Alignment.Bottom) {
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(), // Slide in from above
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut() // Slide out upwards
                ) {
                    Column(
                        horizontalAlignment = Alignment.End, // Keep alignment for the buttons
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 8.dp, bottom = 16.dp) // Adjust end padding
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Button(
                                onClick = {
                                    isFabExpanded = false
                                    // TODO: Handle "Recipe" click
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF4CAF50),
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.elevation(defaultElevation = 4.dp)
                            ) {
                                Text("Recipe Finder", fontFamily = monte)
                            }
                        }
                        Button(
                            onClick = {
                                isFabExpanded = false
                                // TODO: Handle "Recipe" click
                                navController.navigate("addRecipe")
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.elevation(defaultElevation = 4.dp)
                        ) {
                            Text("Recipe", fontFamily = monte)
                        }
                        Button(
                            onClick = {
                                isFabExpanded = false
                                // TODO: Handle "Collection" click
                                navController.navigate(ScreenNavigation.Screen.NewCollection.route)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF2196F3),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.elevation(defaultElevation = 4.dp)
                        ) {
                            Text("Collection", fontFamily = monte)
                        }
                    }
                }
                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    backgroundColor = Color(0xFF1A4D2E),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(horizontal = 16.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(BorderStroke(2.dp, Color(0xFFD9D9D9)), RoundedCornerShape(24.dp))
                    .clickable(onClick = { navController.navigate("searchRecipe")})
                    .padding(8.dp)
                    .background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Collection",
                fontSize = 20.sp,
                fontFamily = monte,
                fontWeight = FontWeight(600),
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(text = "Filter", isSelected = false, onSelected = { /* TODO: Handle filter */ })
                }
                item {
                    CollectionChip(
                        text = "Collection",
                        isSelected = selectedCollectionFilter == "Collection",
                        onSelected = { selectedCollectionFilter = "Collection" })
                }
                item {
                    CollectionChip(
                        text = "All",
                        isSelected = selectedCollectionFilter == "All",
                        onSelected = { selectedCollectionFilter = "All" })
                }
                item {
                    CollectionChip(
                        text = "Favorites",
                        isSelected = selectedCollectionFilter == "Favorites",
                        onSelected = { selectedCollectionFilter = "Favorites" })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedCollectionFilter == "Collection") {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // Two folders per row
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedRecipes.forEach { (category, recipes) ->
                        item {
                            RecipeCategoryFolder(category = category, recipes = recipes.take(4)) // Pass only the first 4
                        }
                    }
                }
            } else if(selectedCollectionFilter == "All") {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val recipesToDisplay = if (selectedCollectionFilter == "All") {
                        savedRecipes
                    } else {
                        savedRecipes.filter { it.category.equals(selectedCollectionFilter, ignoreCase = true) }
                    }
                    items(recipesToDisplay) { recipe ->
                        RecipeCard(recipe = recipe)
                    }
                }
            }
            else if (selectedCollectionFilter == "Favorites") {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val favoriteRecipes = savedRecipes.filter { it.favorite } // This line checks the boolean
                    items(favoriteRecipes) { recipe ->
                        RecipeCard(recipe = recipe)
                    }
                }
            }

        }
    }
}

@Composable
fun RecipeCategoryFolder(category: String, recipes: List<Recipe>) {
    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFF0FFF0),
        elevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Make the folder card square
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = category,
                fontWeight = FontWeight(600),
                fontSize = 16.sp,
                fontFamily = monte,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (recipes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (recipes.size >= 1) SmallRecipeCard(recipe = recipes[0])
                        if (recipes.size >= 2) SmallRecipeCard(recipe = recipes[1])
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (recipes.size >= 3) SmallRecipeCard(recipe = recipes[2])
                        if (recipes.size >= 4) SmallRecipeCard(recipe = recipes[3])
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No recipes in $category")
                }
            }
        }
    }
}

@Composable
fun SmallRecipeCard(recipe: Recipe) {

    Card(
        shape = RoundedCornerShape(4.dp),
        backgroundColor = Color(0xFFE0EEE0),
        elevation = 1.dp,
        modifier = Modifier
            .width(65.dp)  // Ensures it fills available width
            .aspectRatio(1f) // Adjusts the aspect ratio (2:1, width:height)
            .clickable { /* Handle click on individual preview */ }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Image inside the Box with full size and rounded corners
            Image(
                painter = rememberImagePainter(recipe.imageResId),
                contentDescription = recipe.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )

            // Box overlay for the gradient and text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF698B69).copy(alpha = 0.3f),
                                Color(0xFF1A4D2E).copy(alpha = 0.5f)
                            )
                        )
                    )
            ) {
                Text(
                    text = recipe.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 10.sp, // Adjusted font size for sneak peek
                    fontFamily = monte,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(start = 4.dp, bottom = 4.dp)
                        .align(Alignment.BottomStart)  // Positioned at the bottom start
                )
            }
        }
    }
}



@Composable
fun FilterChip(text: String, isSelected: Boolean, onSelected: (Boolean) -> Unit) {
    val backgroundColor = if (isSelected) Color(26, 77, 46) else Color(0x801A4D2E)
    val textColor = Color.White
    Button(
        onClick = { onSelected(!isSelected) },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor, contentColor = textColor),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Icon(Icons.Filled.Person, contentDescription = "Filter Icon", tint = textColor, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp)
    }
}

@Composable
fun CollectionChip(text: String, isSelected: Boolean, onSelected: (String) -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFF1A4D2E) else Color(0x801A4D2E)
    val textColor = Color.White
    Button(
        onClick = { onSelected(text) },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor, contentColor = textColor),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Text(text, fontSize = 12.sp, fontFamily = monte)
    }
}

@Composable
fun RecipeCard(recipe: Recipe) {
    var isFavorite by remember { mutableStateOf(recipe.favorite) }
    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFF0FFF0),
        elevation = 1.dp,
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .height(175.dp)
            .clickable { /* Handle click */ }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberImagePainter(recipe.imageResId),
                contentDescription = recipe.name,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(60, 179, 107, 0), Color(26, 77, 46))
                        )
                    )
            )
            Text(
                text = recipe.name,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = monte,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp, bottom = 8.dp)
            )
            IconButton(
                onClick = {
                    isFavorite = !isFavorite
                    // TODO: Implement logic to update the recipe's favorite status
                    println("Favorite clicked for ${recipe.name}, new state: $isFavorite")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)

            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(178, 49, 49) else Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(
                modifier = Modifier.padding(top = 8.dp, start = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0x801A4D2E), RoundedCornerShape(8.dp))
                        .padding(vertical = 2.dp, horizontal = 6.dp)
                ) {
                    Text(
                        text = recipe.nameOfPerson,
                        fontSize = 8.sp,
                        fontFamily = monte,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xA0FFFFFF), RoundedCornerShape(8.dp))
                        .padding(vertical = 2.dp, horizontal = 6.dp)
                ) {
                    Text(
                        text = recipe.category,
                        fontSize = 8.sp,
                        fontFamily = monte,
                        color = Color.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xA0FFFFFF), RoundedCornerShape(8.dp))
                        .padding(vertical = 2.dp, horizontal = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = recipe.rating,
                            fontSize = 8.sp,
                            fontFamily = monte,
                            modifier = Modifier.padding(start = 2.dp),
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}