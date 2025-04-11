package com.example.myapplication.front_end.recipe.find

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.RecipeResult
import com.example.myapplication.front_end.recipe.detail.RecipeTag

// --- Custom Colors (Approximated - Unchanged) ---
val DarkGreenTitle = Color(0xFF1B5E20)
val CardBackground = Color(0xFFE8F5E9)
val CardBorderColor = Color(0xFFB0BEC5)
val DetailLabelColor = Color.Gray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeResultsScreen(
    recipes: List<RecipeResult>,
    onBackClicked: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center // Center the Text inside
                    ) {
                        Text(
                            "Gen Recipe",
                            fontWeight = FontWeight.Bold,
                            color = DarkGreenTitle,
                            fontSize = 32.sp, // Increased size
                            textAlign = TextAlign.Center // Explicitly center text
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkGreenTitle
                        )
                    }
                },
                actions = { Spacer(Modifier.width(48.dp))}, // Approx width of IconButton
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp)) // Reduced space after AppBar

            Text(
                "Based On Available Ingredients",
                style = MaterialTheme.typography.titleMedium,
                // No explicit color = default text color
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .align(Alignment.CenterHorizontally) // Center this text as well
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recipes, key = { it.id }) { recipe ->
                    RecipeResultItem(recipe = recipe) // Use redesigned item
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RecipeResultItem(recipe: RecipeResult) {
    val cardShape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(CardBackground)
            .border(BorderStroke(1.dp, CardBorderColor), cardShape)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // --- Title ---
            Text(
                text = recipe.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp, // Slightly smaller title
                modifier = Modifier.fillMaxWidth() // Allow text wrapping
            )

            Spacer(modifier = Modifier.height(10.dp)) // Space between title and details

            // --- Details Row (Serving, Prep, Cook) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                // Distribute space evenly between details
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                RecipeDetailColumn(label = "Serving", value = recipe.serving.toString())
                RecipeDetailColumn(label = "Preparation Time", value = recipe.prepTime)
                RecipeDetailColumn(label = "Cooking Time", value = recipe.cookingTime)
            }

            Spacer(modifier = Modifier.height(10.dp)) // Space between details and tags

            // --- Tags Row (Unchanged structure) ---
            Row(
                modifier = Modifier.fillMaxWidth(), // Ensure tags can wrap if needed
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Wrap tags using FlowRow if they might exceed one line often
                // For simplicity, keeping Row for now, assuming few tags per recipe
                recipe.tags.forEach { tag ->
                    RecipeTag(text = tag)
                }
                // If you expect many tags, replace the Row above with:
                /*
                 FlowRow(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.spacedBy(8.dp),
                     verticalArrangement = Arrangement.spacedBy(4.dp) // If tags wrap
                 ) {
                     recipe.tags.forEach { tag ->
                         RecipeTag(text = tag)
                     }
                 }
                 */
            }
        }
    }
}

// --- Helper Composables (Unchanged) ---

@Composable
fun RecipeDetailColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = DetailLabelColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            // Use LocalContentColor to adapt to light/dark theme text color
            color = LocalContentColor.current.copy(alpha = 0.8f), // Slightly less prominent than title
            textAlign = TextAlign.Center
        )
    }
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
fun RecipeResultsScreenPreview() {
    val sampleRecipes = listOf(
        RecipeResult(1, "Canned Tuna Pasta", 1, "1 hr:59 mins", "1 hr:59 mins", listOf("Filipino", "Breakfast")), // Shortened time format slightly for preview
        RecipeResult(2, "Another Pasta Dish", 2, "30 mins", "45 mins", listOf("Italian")),
        RecipeResult(3, "Chicken Adobo", 4, "20 mins", "1 hr 15 mins", listOf("Filipino", "Dinner", "Lunch")),
        RecipeResult(4, "Very Long Recipe Title That Might Wrap Around To Test Layout", 1, "10 mins", "5 mins", listOf("Quick", "Snack", "Easy", "Beginner")),
        RecipeResult(5, "Chicken Adobo", 4, "20 mins", "1 hr 15 mins", listOf("Filipino", "Dinner", "Lunch")),
    )
    MaterialTheme {
        RecipeResultsScreen(recipes = sampleRecipes)
    }
}