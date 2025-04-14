package com.example.myapplication.front_end.recipe.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.data.IngredientItem
import com.example.myapplication.data.NutritionItem
import com.example.myapplication.data.RecipeDetail
import com.example.myapplication.front_end.home.monte
import com.example.myapplication.front_end.recipe.find.DarkGreen
import com.example.myapplication.front_end.recipe.find.LightGreenBackground

// --- Reusable Colors (define these centrally if used elsewhere) ---
val MutedGray = Color.Gray // Or Color(0xFF757575)
val SectionBackgroundColor = LightGreenBackground // Background for ingredient/procedure boxes
val FavoriteColor = Color.Red // Or MaterialTheme.colorScheme.primary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailScreen(
    recipeDetail: RecipeDetail,
    onBackClicked: () -> Unit,
    onSaveClicked: (RecipeDetail) -> Unit, // Pass updated state back if needed
    // Add callbacks for favorite/bookmark persistence if needed
    // onFavoriteToggled: (Boolean) -> Unit,
    // onBookmarkToggled: (Boolean) -> Unit
) {
    // Local state for toggles (update actual data via callbacks/ViewModel)
    var isFavorite by rememberSaveable { mutableStateOf(recipeDetail.isFavorite) }
    var isBookmarked by rememberSaveable { mutableStateOf(recipeDetail.isBookmarked) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center // Center the Text inside
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                            Text(
                                text = "Gen Recipe",
                                style = TextStyle(
                                    fontSize = 32.sp,
                                    fontFamily = monte,
                                    fontWeight = FontWeight(700),
                                    color = Color(0xFF1A4D2E),
                                    textAlign = TextAlign.Center
                                )
                            )
                            Text(
                                "AI Generated Recipe",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedGray
                            )
                        }

                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkGreen)
                    }
                },
                actions = { Spacer(Modifier.width(48.dp))}, // Balance nav icon for centering
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        // --- Floating Save Button ---
        // Use floatingActionButton if you want it overlaid, otherwise place Button in Column
        /*
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Save") },
                icon = { Icon(Icons.Filled.Save, contentDescription = "Save Recipe") },
                onClick = { onSaveClicked(recipeDetail.copy(isFavorite = isFavorite, isBookmarked = isBookmarked)) },
                containerColor = DarkGreen,
                contentColor = Color.White
            )
        },
        floatingActionButtonPosition = FabPosition.Center
        */
        // --- Button at the bottom ---
        // If button should be at the absolute bottom, handle Scaffold padding carefully
        // Or put it as the last item in LazyColumn as done below
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)// Apply scaffold padding
                .background(color = Color.White)
                .padding(horizontal = 16.dp), // Add horizontal padding for content
            verticalArrangement = Arrangement.spacedBy(16.dp), // Space between sections

        ) {

            // --- Header Section ---
            item {
                RecipeDetailHeader(
                    title = recipeDetail.title,
                    tags = recipeDetail.tags,
                    isFavorite = isFavorite,
                    isBookmarked = isBookmarked,
                    onFavoriteClick = { isFavorite = !isFavorite /* Call ViewModel/callback */ },
                    onBookmarkClick = { isBookmarked = !isBookmarked /* Call ViewModel/callback */ }
                )
            }

            // --- Time & Serving Section ---
            item {
                RecipeTimeServingInfo(
                    servingSize = recipeDetail.servingSize,
                    cookingTime = recipeDetail.cookingTime,
                    prepTime = recipeDetail.prepTime
                )
            }

            // --- Ingredients Section ---
            item {
                RecipeSection(title = "Ingredients") { // Use helper for consistency
                    IngredientList(ingredients = recipeDetail.ingredients)
                }
            }

            // --- Procedure Section ---
            item {
                RecipeSection(title = "Procedure") {
                    Text(
                        text = recipeDetail.procedure,
                        style = MaterialTheme.typography.bodyLarge, // Use appropriate style
                        modifier = Modifier.padding(12.dp) // Padding inside the procedure box
                    )
                }
            }


            // --- Nutrition Facts (Optional) ---
            if (!recipeDetail.nutritionFacts.isNullOrEmpty()) {
                item {
                    RecipeSection(title = "Nutrition Facts") {
                        NutritionTable(facts = recipeDetail.nutritionFacts)
                    }
                }
            }

            // --- Save Button (as last item in list) ---
            item {
                Spacer(Modifier.height(8.dp)) // Space before button
                Button(
                    onClick = {
                        // Update the recipe object with current toggle states before saving
                        val updatedRecipe = recipeDetail.copy(
                            isFavorite = isFavorite,
                            isBookmarked = isBookmarked
                        )
                        onSaveClicked(updatedRecipe)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(50), // Pill shape
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("Save", fontSize = 16.sp)
                }
                Spacer(Modifier.height(16.dp)) // Space at the very bottom
            }
        }
    }
}

// --- Helper Composables ---

@Composable
fun RecipeDetailHeader(
    title: String,
    tags: List<String>,
    isFavorite: Boolean,
    isBookmarked: Boolean,
    onFavoriteClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall, // Larger title
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(end = 8.dp) // Allow title to wrap
            )
            Row { // Group icons together
                IconToggleButton(
                    checked = isFavorite,
                    onCheckedChange = { onFavoriteClick() },
                    modifier = Modifier.size(40.dp) // Adjust size
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) FavoriteColor else LocalContentColor.current
                    )
                }
                IconToggleButton(
                    checked = isBookmarked,
                    onCheckedChange = { onBookmarkClick() },
                    modifier = Modifier.size(40.dp) // Adjust size
                ) {
                    Image(
                        painter = painterResource(if (isBookmarked) R.drawable.bookmark else R.drawable.bookmark_filled),
                        contentDescription = "Bookmark"
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        // Use FlowRow for tags if they might wrap
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag -> RecipeTag(text = tag) } // Reuse RecipeTag from previous screen
        }
    }
}

@Composable
fun RecipeTimeServingInfo(
    servingSize: Int,
    cookingTime: String,
    prepTime: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround, // Distribute space
        verticalAlignment = Alignment.Top
    ) {
        InfoColumn(label = "Serving", value = servingSize.toString(), unit = "(Person)")
        InfoColumn(
            label = "Cooking Time",
            value = cookingTime,
            iconPainter = painterResource(R.drawable.alarm)
        )
        InfoColumn(label = "Preparation Time", value = prepTime,
            iconPainter = painterResource(R.drawable.restaurant))
    }
}

@Composable
fun InfoColumn(label: String, value: String, unit: String? = null, iconPainter: Painter? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium, // Smaller label
                color = MutedGray
            )
            if (iconPainter != null) {
                Spacer(Modifier.width(4.dp))
                Icon(iconPainter, contentDescription = null, tint = MutedGray, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge, // Value text
            fontWeight = FontWeight.Medium
        )
        if (unit != null) {
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall, // Smaller unit
                color = MutedGray
            )
        }
    }
}


// Helper for consistent section styling (Title + Background Box)
@Composable
fun RecipeSection(
    title: String,
    hint: String? = null,
    content: @Composable ColumnScope.() -> Unit // Content goes inside the box
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (hint != null) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedGray
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface( // Use Surface for background and shape
            shape = RoundedCornerShape(12.dp),
            color = SectionBackgroundColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content) // Place the provided content inside
        }
    }
}


@Composable
fun IngredientList(ingredients: List<IngredientItem>) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp) // Space between ingredient rows
    ) {
        ingredients.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.quantity} ${item.unit ?: ""}".trim(), // Combine quantity and unit
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(80.dp) // Fixed width for quantity/unit part
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f) // Ingredient name takes remaining space
                )
            }
        }
    }
}


@Composable
fun NutritionTable(facts: List<NutritionItem>) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        // Optional Header Row
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            Text(
                "Nutrition Per Serving", // Example Header
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            // You could add serving size text here if needed
            // Text("100g", style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.End)
        }
        Divider(color = MutedGray.copy(alpha = 0.5f)) // Separator line

        facts.forEach { fact ->
            NutritionRow(fact = fact)
            Divider(color = MutedGray.copy(alpha = 0.3f)) // Lighter divider between rows
        }
    }
}

@Composable
fun NutritionRow(fact: NutritionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp), // Padding for each nutrition row
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = fact.nutrient,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f) // Nutrient name takes most space
        )
        if (fact.amount != null) {
            Text(
                text = fact.amount,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.widthIn(min = 50.dp) // Ensure minimum space for amount
                    .padding(horizontal = 8.dp)
            )
        }
        if (fact.dailyValue != null) {
            Text(
                text = fact.dailyValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold, // Make DV bold maybe?
                textAlign = TextAlign.End,
                modifier = Modifier.width(40.dp) // Fixed width for daily value %
            )
        } else if (fact.amount != null) {
            // Add spacer if only amount is present, to align with rows that have DV
            Spacer(Modifier.width(40.dp))
        }
    }
}


// Reuse RecipeTag from your other screen or define it here
@Composable
fun RecipeTag(text: String) {
    val tagShape = RoundedCornerShape(50)
    val tagBorderColor = Color(0xFFBDBDBD)
    val tagBackgroundColor = Color.White

    Box(
        modifier = Modifier
            .clip(tagShape)
            .background(tagBackgroundColor)
            .border(BorderStroke(1.dp, tagBorderColor), tagShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = LocalContentColor.current // Adapts to theme
        )
    }
}


// --- Preview ---
@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview() {
    val sampleRecipe = RecipeDetail(
        id = "tuna123",
        title = "Canned Tuna Pasta",
        tags = listOf("Dinner", "Italian"),
        isFavorite = true,
        isBookmarked = false,
        servingSize = 1,
        cookingTime = "1 hr: 00 mins",
        prepTime = "1 hr: 00 mins",
        ingredients = listOf(
            IngredientItem("1", "can", "Tuna in oil, drained"),
            IngredientItem("200", "grams", "Spaghetti"),
            IngredientItem("2", "cloves", "Garlic, minced"),
            IngredientItem("1/4", "cup", "Olive Oil"),
            IngredientItem("1", "tsp", "Chili Flakes (optional)"),
            IngredientItem(" ", null, "Salt and Pepper to taste"),
            IngredientItem("Some", null, "Parsley, chopped")
        ),
        procedure = "1. Cook spaghetti according to package directions. Drain, reserving some pasta water.\n2. While pasta cooks, heat olive oil in a pan over medium heat. Add garlic and chili flakes, cook until fragrant (about 1 min).\n3. Add drained tuna to the pan, break it up. Cook for 2-3 minutes.\n4. Add cooked spaghetti to the pan. Toss well. Add reserved pasta water if needed to create a light sauce.\n5. Season with salt and pepper. Stir in chopped parsley before serving.",
        nutritionFacts = listOf(
            NutritionItem("Calories", "450kcal", null),
            NutritionItem("Total Fat", "15g", "20%"),
            NutritionItem("Saturated Fat", "3g", "15%"),
            NutritionItem("Trans Fat", "0g", null),
            NutritionItem("Cholesterol", "30mg", "10%"),
            NutritionItem("Sodium", "600mg", "26%"),
            NutritionItem("Total Carbohydrate", "55g", "20%"),
            NutritionItem("Dietary Fiber", "4g", "14%"),
            NutritionItem("Total Sugars", "3g", null),
            NutritionItem("Protein", "25g", "50%")
        )
    )

    MaterialTheme { // Wrap preview in MaterialTheme
        RecipeDetailScreen(
            recipeDetail = sampleRecipe,
            onBackClicked = {},
            onSaveClicked = {}
        )
    }
}