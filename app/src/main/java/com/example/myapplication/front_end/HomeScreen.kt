package com.example.myapplication.front_end

import android.graphics.PointF.length
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.FontFamily

import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.Font
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import androidx.compose.material3.CardElevation
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavHostController
import kotlin.contracts.contract

@Composable
fun HomeScreen(navController: NavHostController) { // Receive NavController
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavBar(selectedItem = selectedTab, onItemSelected = {
                selectedTab = it
                if (it == 1) { // Index 1 corresponds to "Saved" (book icon)
                    navController.navigate("yourRecipes") // Navigate to YourRecipeScreen
                }
                else if (it == 2) {

                }
                else if (it == 3) {
                    navController.navigate(ScreenNavigation.Screen.MealPlan.route)
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
            CategorySection()
            RecipeList()
        }
    }
}

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
                        painter = painterResource(R.drawable.notif), // Your colored Google icon
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
fun CategorySection() {

    val categories = listOf("All", "Breakfast", "Lunch", "Dinner", "Dessert")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    Box(modifier = Modifier.padding(start = 16.dp)) {
        Text(
            text = "Categories", // name
            fontFamily = monte,
            fontSize = 20.sp,
            fontWeight = FontWeight(600),
        )
    }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)) {
        items(
            count = categories.size, // Number of items
            key = { index -> categories[index] }, // Unique key for each item
            contentType = { index -> categories[index] }, // Content type for each item
            itemContent = { index -> // Composable for each item
                val category = categories[index]
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selectedCategory == category) Color(26, 77, 46) else Color.LightGray // Change color based on selection
                        )
                        .clickable { selectedCategory = category } // Update selected category on click
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category,
                        color = if (selectedCategory == category) Color.White else Color(0, 0, 0, 64), // Change text color based on selection
                        fontFamily = monte
                    )
                }
            }
        )
    }
}

@Composable
fun RecipeList() {
    val recipes = listOf(
        Recipe("Veni", "Spicy Firecracker Beef", R.drawable.tryfood, "9.5", "Lunch", "1 Hour", 5),
        Recipe("Vennidict", "Chicken Fajitas", R.drawable.tryfood, "9.2", "Dinner", "30 min", 1),
        Recipe("Vennidict", "Canned Tuna Pasta", R.drawable.tryfood, "9.3", "Breakfast", "30 min", 1),
        Recipe("Vennidict", "Canned Tuna Pasta", R.drawable.tryfood, "9.3", "Lunch", "30 min", 1),
        Recipe("Vennidict", "Canned Tuna Pasta", R.drawable.tryfood, "9.3", "Lunch", "30 min", 1),
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Set the number of columns to 2
        contentPadding = PaddingValues(8.dp) // Optional: Add padding around the grid
    ) {
        items(recipes.size) { index ->
            val recipe = recipes[index]
            RecipeCard(recipe)
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


// dagdagan nalang
data class Recipe(val nameOfPerson: String, val name: String, val imageResId: Int, val rating: String, val category: String, val cookingTime: String, val serving: Int)

