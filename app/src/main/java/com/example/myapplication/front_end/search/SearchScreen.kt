package com.example.myapplication.front_end.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.R
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Brush
import coil.compose.rememberImagePainter
import com.example.myapplication.data.Recipe
import com.example.myapplication.front_end.home.NavBar
import com.example.myapplication.front_end.home.monte
import recipes


data class RecentSearchItem(
    val type: SearchItemType,
    val label: String,
    val secondaryLabel: String? = null,
    val imageResId: Int? = null
)

enum class SearchItemType {
    USER, QUERY, RECIPE
}

var search by mutableStateOf("")


@Composable
fun InteractionSearchScreen(navController: NavHostController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            SearchBar(navController)
            RecentSearches(
                recentItems = listOf(
                    RecentSearchItem(SearchItemType.USER, "xampleeuser", "sample name\n20.2M followers", R.drawable.user),
                    RecentSearchItem(SearchItemType.QUERY, "meowmeow"),
                    RecentSearchItem(SearchItemType.RECIPE, "Canned Tuna Pasta", "xampleeuser\nitalian",  R.drawable.tryfood),
                    RecentSearchItem(SearchItemType.USER, "xampleeuser", "sample name\n20.2M followers", R.drawable.user),
                    RecentSearchItem(SearchItemType.QUERY, "meowmeow"),
                    RecentSearchItem(SearchItemType.RECIPE, "Canned Tuna Pasta", "xampleeuser\nitalian",  R.drawable.tryfood),
                    RecentSearchItem(SearchItemType.USER, "xampleeuser", "sample name\n20.2M followers", R.drawable.user),
                    RecentSearchItem(SearchItemType.QUERY, "meowmeow"),
                    RecentSearchItem(SearchItemType.RECIPE, "Canned Tuna Pasta", "xampleeuser\nitalian",  R.drawable.tryfood),
                    RecentSearchItem(SearchItemType.USER, "xampleeuser", "sample name\n20.2M followers", R.drawable.user),
                    RecentSearchItem(SearchItemType.QUERY, "meowmeow"),
                    RecentSearchItem(SearchItemType.RECIPE, "Canned Tuna Pasta", "xampleeuser\nitalian", R.drawable.tryfood),
                )
            )
        }
    }


@Composable
fun SearchBar(navController: NavHostController) {
    Column(
        horizontalAlignment = Alignment.End,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = { navController.navigate("home") }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back",
                    tint = Color(26, 77, 46))
            }
            OutlinedTextField(
                value = search,
                onValueChange = { search = it /*TODO: Handle text input*/ },
                placeholder = { Text("Search for a recipe") },
                modifier = Modifier
                    .weight(1f),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(26, 77, 46),  // Border color when focused
                    unfocusedBorderColor = Color.Gray, // Border color when unfocused
                    errorBorderColor = Color.Red       // Border color when there's an error
                )
            )
            Spacer(modifier = Modifier.width(8.dp))

        }
    }
}

@Composable
fun RecentSearches(recentItems: List<RecentSearchItem>) {
    Text(
        text = "Recent",
        fontSize = 16.sp,
        fontFamily = monte,
        fontWeight = FontWeight(600)
    )
    Spacer(modifier = Modifier.height(8.dp))
    LazyColumn {
        items(recentItems) { item ->
            RecentSearchItemRow(item = item)
            Divider()
        }
    }
}

@Composable
fun RecentSearchItemRow(item: RecentSearchItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (item.type) {
            SearchItemType.USER -> {
                Image(
                    painter = painterResource(R.drawable.user), // Use placeholder if no image
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(item.label,
                        fontWeight = FontWeight(500),
                        fontFamily = monte,
                        fontSize = 12.sp,
                        )
                    item.secondaryLabel?.let {
                        Text(it, fontSize = 12.sp, color = Color.Gray,
                            fontWeight = FontWeight(500))
                    }
                }
            }
            SearchItemType.QUERY -> {
                Icon(Icons.Filled.Search, contentDescription = "Search Query", tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.label,
                    fontFamily = monte,
                    fontWeight = FontWeight(500))
            }
            SearchItemType.RECIPE -> {
                Image(
                    painter = painterResource(R.drawable.tryfood), // Use placeholder if no image
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RectangleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(item.label, fontWeight = FontWeight(500),
                        fontFamily = monte, )
                    item.secondaryLabel?.let {
                        Text(it, fontSize = 12.sp,
                            fontFamily = monte,
                            color = Color.Gray,
                            fontWeight = FontWeight(500))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { /*TODO: Handle item removal*/ }) {
            Icon(Icons.Filled.Close, contentDescription = "Remove")
        }
    }
}

@Composable
fun SearchResult(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) } // State for the selected tab

    Scaffold(
        bottomBar = {
            NavBar(selectedItem = selectedTab, onItemSelected = {
                selectedTab = it
                if (it == 1) { // Index 1 corresponds to "Saved" (book icon)
                    navController.navigate("yourRecipes") // Navigate to YourRecipeScreen
                }
            })
        }
    ) { paddingValues -> // Add paddingValues for proper content placement
        Column(
            modifier = Modifier
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(8.dp)
        ) {
            SearchBar(navController)

            Row(
                modifier = Modifier.padding(8.dp)
            ) {
                Button(
                    onClick = { /* TODO: Handle All click */ },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF1A4D2E), // Dark Green
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(50.dp) // Set a specific width
                        .height(30.dp) // Set a specific height
                ) {
                    Text(
                        text = "All",
                        fontSize = 10.sp,
                        fontFamily = monte,
                        color = Color.White
                    )
                }
                Button(
                    onClick = { /* TODO: Handle Popular click */ },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF8FBC8F), // Light Green-Grayish
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(81.dp) // Set a specific width
                        .height(30.dp) // Set a specific height
                ) {
                    Text(
                        text = "Popular",
                        fontSize = 10.sp,
                        fontFamily = monte,
                        color = Color.White
                    )
                }
                Button(
                    onClick = { /* TODO: Handle Newest click */ },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF1A4D2E), // Dark Green
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(77.dp) // Set a specific width
                        .height(30.dp) // Set a specific height
                ) {
                    Text(
                        text = "Newest",
                        fontSize = 10.sp,
                        fontFamily = monte,
                        color = Color.White
                    )
                }
                Button(
                    onClick = { /* TODO: Handle Newest click */ },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF1A4D2E), // Dark Green
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(85.dp) // Set a specific width
                        .height(30.dp) // Set a specific height
                ) {
                    Icon(
                        painter = painterResource(R.drawable.filter), // Replace with your filter icon resource
                        contentDescription = "Filter",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp) // Adjust icon size for smaller button
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Filter", fontSize = 10.sp,
                        fontFamily = monte,
                        color = Color.White

                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Result",
                fontFamily = monte,
                fontWeight = FontWeight(600),
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(10.dp))


            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // Set the number of columns to 2
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recipes.size) { index ->
                    val recipe = recipes[index]
                    QueryResult(recipe)
                }
            }
        }
    }
}

@Composable
fun QueryResult(recipe: Recipe) {
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
                            colors = listOf(
                                Color.Transparent,
                                Color(60, 179, 107, 0),
                                Color(26, 77, 46)
                            )
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
                    Icon(
                        painter = painterResource(R.drawable.alarm),
                        contentDescription = "Time",
                        tint = Color(255, 255, 255, 132),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = " ${recipe.cookingTime}",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontFamily = monte
                    )
                    Column {
                        Divider(
                            color = Color.Gray,
                            modifier = Modifier.height(8.dp)
                                .width(1.dp), // This works because it's in a Row
                            thickness = 1.dp
                        )
                    }
                    Icon(
                        painter = painterResource(R.drawable.restaurant),
                        contentDescription = "Serving",
                        tint = Color(255, 255, 255, 132),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = " ${recipe.serving} Serving",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontFamily = monte
                    )
                }
            }

            // Floating Rating Badge
            Row(
                modifier = Modifier.fillMaxWidth(), // Ensure the Row takes the full width
                horizontalArrangement = Arrangement.SpaceEvenly, // Distribute space evenly between items
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(1.dp, top = 9.dp)
                        .background(Color(26, 77, 46, 140), RoundedCornerShape(12.dp))
                        .padding(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(1.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.user),
                            contentDescription = "User",
                            modifier = Modifier
                                .size(18.dp)
                        )
                        Text(
                            text = "${recipe.nameOfPerson}",
                            fontSize = 8.sp,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp),
                            color = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(1.dp, top = 9.dp)
                        .background(Color(255, 255, 255, 209), RoundedCornerShape(12.dp))
                        .padding(6.dp)
                ) {
                    Text(
                        text = recipe.category,
                        fontSize = 8.sp,
                        fontFamily = monte,
                    )
                }


                Box(
                    modifier = Modifier
                        .padding(1.dp, top = 9.dp)
                        .background(Color(255, 255, 255, 209), RoundedCornerShape(12.dp))
                        .padding(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.star),
                            contentDescription = "Serving",
                            tint = Color(255, 185, 0),
                            modifier = Modifier.size(10.dp)
                        )
                        Text(text = " ${recipe.rating}", fontSize = 8.sp, fontFamily = monte)
                    }

                }

            }
        }
    }
}