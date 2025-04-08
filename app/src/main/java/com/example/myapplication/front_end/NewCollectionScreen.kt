package com.example.myapplication.front_end

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.myapplication.R

@Composable
fun NewCollectionScreen() {
    val recipes = remember {
        mutableStateListOf(
            Recipe("John Doe", "Canned Tuna Pasta", R.drawable.tryfood, "9.5", "Lunch", "15", 1),
            Recipe("Jane Smith", "Canned Tuna Pasta", R.drawable.tryfood, "9.5", "Lunch", "15", 1),
            Recipe("Peter Jones", "Canned Tuna Pasta", R.drawable.tryfood, "9.5", "Lunch", "15", 1),
            Recipe("Alice Brown", "Canned Tuna Pasta", R.drawable.tryfood, "9.5", "Lunch", "15", 1),
            Recipe("Bob Green", "Canned Tuna Pasta", R.drawable.tryfood, "9.5", "Lunch", "15", 1),
            Recipe("Charlie White", "Canned Tuna Pasta", R.drawable.tryfood, "9.5", "Lunch", "15", 1),
        )
    }

    var selectedRecipes by remember { mutableStateOf(listOf<Recipe>()) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(onClick = {  }) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1B5E20) // Dark Green
                    )
                }

                // Title
                Text(
                    text = "New Collection",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20), // Dark Green
                    fontFamily = monte
                )

                // Check Button
                IconButton(onClick = {  }) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Check",
                        tint = Color(0xFF1B5E20) // Dark Green
                    )
                }
            }
        },
        bottomBar = {
            if (selectedRecipes.isNotEmpty()) {
                SelectedRecipesBar(selectedRecipes = selectedRecipes)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = { /* TODO: Handle search query */ },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                placeholder = { Text("Search for a recipe") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
             Box(
                 modifier = Modifier.fillMaxWidth()
             ) {
                 Text(
                     text = "Add Recipe",
                         fontSize = 20.sp,
                         fontFamily = monte,
                         fontWeight = FontWeight(600),
                         color = Color(0xFF1A4D2E),
                 )
             }
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recipes.size) { index ->
                    RecipeItem(
                        recipe = recipes[index],
                        isSelected = selectedRecipes.contains(recipes[index]),
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

@Composable
fun RecipeItem(
    recipe: Recipe,
    isSelected: Boolean,
    onRecipeClick: (Recipe) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFF0FFF0),
        elevation = 1.dp,
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .height(175.dp)
            .clickable { onRecipeClick(recipe) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberImagePainter(recipe.imageResId),
                contentDescription = recipe.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
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
                        fontFamily = latoFontLI,
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

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp) // Apply size before background
                        .background(Color(0xFF4CAF50), RoundedCornerShape(percent = 50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedRecipesBar(selectedRecipes: List<Recipe>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface)
            .padding(vertical = 8.dp)
    ) {
        Text(
            "Selected",
            fontWeight = FontWeight.SemiBold,
            fontFamily = monte,
            color = Color(0xFF1A4D2E),
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(selectedRecipes.size) { index ->
                Image(
                    painter = painterResource(id = selectedRecipes[index].imageResId),
                    contentDescription = selectedRecipes[index].name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NewCollectionScreen()
}