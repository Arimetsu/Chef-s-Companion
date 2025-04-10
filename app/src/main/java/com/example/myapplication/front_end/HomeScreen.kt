package com.example.myapplication.front_end

import Recipe
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
import androidx.navigation.NavHostController
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
fun HomeScreen(navController: NavHostController) { // Receive NavController
    var selectedTab by remember { mutableStateOf(0) }
    // This state correctly drives the recipe filtering
    var selectedCategory by remember { mutableStateOf("All") }


    Scaffold(
        bottomBar = {
            NavBar(selectedItem = selectedTab, onItemSelected = {
                selectedTab = it
                if (it == 1) { // Index 1 corresponds to "Saved" (book icon)
                    navController.navigate("yourRecipes") // Navigate to YourRecipeScreen
                }
            })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TopSection(navController)
            CategorySection(onCategorySelected = { category -> selectedCategory = category })
            RecipeList(selectedCategory = selectedCategory)
        }
    }
}

@Composable
fun TopSection(navController: NavHostController){
    Column(modifier = Modifier.padding(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(1.dp),
            verticalAlignment = Alignment.CenterVertically) {
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

        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically){
            Box(
                modifier = Modifier
                    .size(60.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.user), // Your colored Google icon
                    contentDescription = "User",
                    modifier = Modifier
                        .size(60.dp) // Set the size of the image

                )
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "Juan Lopez", // name
                    fontFamily = latoFontLI,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
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
        Spacer(modifier = Modifier.height(10.dp))

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


@Composable
fun RecipeCard(recipe: Recipe) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(175.dp)
            .clickable { /* Handle click */ }
    ) {
        Box() {
            // Background Image
            Image(
                painter = rememberImagePainter(recipe.imageResId),
                contentDescription = recipe.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(60, 179, 107, 0), Color(26, 77, 46))
                        )
                    )
            )

            // Content (Name, Time, Serving, etc.)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = recipe.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = monte
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.alarm), contentDescription = "Time", tint = Color(255, 255, 255, 132), modifier = Modifier.size(10.dp))
                    Text(text = " ${recipe.cookingTime}", color = Color.White, fontSize = 8.sp, fontFamily = monte)
                    Column{
                    Divider(
                        color = Color.Gray,
                        modifier = Modifier.height(8.dp).width(1.dp), // This works because it's in a Row
                        thickness = 1.dp
                    )
                        }
                    Icon(painter = painterResource(R.drawable.restaurant), contentDescription = "Serving", tint = Color(255, 255, 255, 132), modifier = Modifier.size(10.dp))
                    Text(text = " ${recipe.serving} Serving" , color = Color.White, fontSize = 8.sp, fontFamily = monte)
                }
            }

            // Floating Rating Badge
        Row(
            modifier = Modifier.fillMaxWidth(), // Ensure the Row takes the full width
            horizontalArrangement = Arrangement.SpaceEvenly, // Distribute space evenly between items
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier
                    .padding(1.dp, top = 9.dp)
                    .background(Color(26, 77, 46, 140), RoundedCornerShape(12.dp))
                    .padding(2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(1.dp)) {
                    Image(
                        painter = painterResource(R.drawable.user),
                        contentDescription = "User",
                        modifier = Modifier
                            .size(18.dp)
                    )
                    Text(
                        text = "${recipe.nameOfPerson}",
                        fontSize = 8.sp,
                        fontFamily = latoFontLI,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp),
                        color = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(1.dp, top = 9.dp)
                    .background(Color(255,255,255, 209), RoundedCornerShape(12.dp))
                    .padding(6.dp)
            ) {
                Text(text = recipe.category,
                    fontSize = 8.sp,
                    fontFamily = monte,
                )
            }


            Box(
                modifier = Modifier
                    .padding(1.dp, top = 9.dp)
                    .background(Color(255,255,255, 209), RoundedCornerShape(12.dp))
                    .padding(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.star), contentDescription = "Serving", tint = Color(255, 185, 0), modifier = Modifier.size(10.dp))
                    Text(text = " ${recipe.rating}", fontSize = 8.sp, fontFamily = monte)
                }

            }

        }
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