package com.example.myapplication.front_end.recipe.find

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.SolidColor


val DarkGreen = Color(0xFF1B5E20) // Button color
val LightGreenBackground = Color(0xFFE8F5E9) // Text field background (approx)
val ChipBorderColor = Color(0xFFBDBDBD) // Chip border color

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FindRecipeScreen(
    onBackClicked: () -> Unit = {},
    onGenerateClicked: (RecipePreferences) -> Unit = {},
    onCancelClicked: () -> Unit = {}
) {
    // --- State Variables ---
    var ingredients by rememberSaveable { mutableStateOf("") }
    var serving by rememberSaveable { mutableStateOf("") }
    var prepTime by rememberSaveable { mutableStateOf("") }
    var cookingTime by rememberSaveable { mutableStateOf("") }
    var selectedCuisines by rememberSaveable { mutableStateOf(setOf<String>()) }
    var selectedCategories by rememberSaveable { mutableStateOf(setOf<String>()) }

    val cuisineOptions = listOf("Filipino", "American", "Italian", "Indian", "Chinese", "Japanese")
    val categoryOptions = listOf("Breakfast", "Lunch", "Dinner", "Dessert")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Find Recipe", fontWeight = FontWeight.Bold)
                        Text(
                            "AI Generated Recipe",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent // Or MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()) // Make content scrollable
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // --- Available Ingredients ---
            Text("Available Ingredients", style = MaterialTheme.typography.titleMedium)
            Text(
                "(Add measure for greater accuracy)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp), // Adjust height as needed
                placeholder = { Text("Enter ingredients, separated by commas...") },
                trailingIcon = {
                    if (ingredients.isNotEmpty()) {
                        IconButton(onClick = { ingredients = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Ingredients")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors( // Custom background attempt
                    focusedContainerColor = LightGreenBackground,
                    unfocusedContainerColor = LightGreenBackground,
                    disabledContainerColor = LightGreenBackground,
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- Serving ---
            InputRow(
                label = "Serving",
                hint = "(Person)",
                value = serving,
                onValueChange = { serving = it },
                placeholder = "e.g., 4"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Preparation Time ---
            InputRow(
                label = "Preparation Time",
                value = prepTime,
                onValueChange = { prepTime = it },
                placeholder = "e.g., 30 mins" // Placeholder updated
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Cooking Time ---
            InputRow(
                label = "Cooking Time",
                value = cookingTime,
                onValueChange = { cookingTime = it },
                placeholder = "e.g., 1 hr 15 mins" // Placeholder updated
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- Preferred Cuisine ---
            ChipSelectionGroup(
                label = "Preferred Cuisine",
                hint = "(Optional)",
                options = cuisineOptions,
                selectedOptions = selectedCuisines,
                onSelectionChanged = { selectedCuisines = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- Categories ---
            ChipSelectionGroup(
                label = "Categories",
                options = categoryOptions,
                selectedOptions = selectedCategories,
                onSelectionChanged = { selectedCategories = it }
            )

            Spacer(modifier = Modifier.height(32.dp)) // More space before buttons

            // --- Action Buttons ---
            Button(
                onClick = {
                    // Collect data and call the generate function
                    val preferences = RecipePreferences(
                        ingredients = ingredients.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        serving = serving.toIntOrNull(),
                        prepTime = prepTime,
                        cookingTime = cookingTime,
                        cuisines = selectedCuisines,
                        categories = selectedCategories
                    )
                    onGenerateClicked(preferences)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
            ) {
                Text("Generate", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onCancelClicked,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(DarkGreen) // Use border color consistent with Generate button
                )
            ) {
                Text("Cancel", color = DarkGreen, fontSize = 16.sp) // Text color matches border
            }

            Spacer(modifier = Modifier.height(16.dp)) // Space at the bottom
        }
    }
}

// Helper Composable for Text Field Rows
@Composable
fun InputRow(
    label: String,
    hint: String? = null,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        if (hint != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        singleLine = true,
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear ${label}")
                }
            }
        }
    )
}

// Helper Composable for Chip Selection Groups
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChipSelectionGroup(
    label: String,
    hint: String? = null,
    options: List<String>,
    selectedOptions: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        if (hint != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow( // Use FlowRow for automatic wrapping
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp), // Spacing between chips
        verticalArrangement = Arrangement.spacedBy(4.dp) // Spacing between rows if wraps
    ) {
        options.forEach { option ->
            val isSelected = selectedOptions.contains(option)
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newSelection = selectedOptions.toMutableSet()
                    if (isSelected) {
                        newSelection.remove(option)
                    } else {
                        newSelection.add(option)
                    }
                    onSelectionChanged(newSelection)
                },
                label = { Text(option) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DarkGreen.copy(alpha = 0.8f),
                    selectedLabelColor = Color.White
                ),
                // --- CORRECTED BORDER ---
                border = if (isSelected) null else BorderStroke(1.dp, ChipBorderColor)
            )
        }
    }
}

// Data class to hold the collected preferences
data class RecipePreferences(
    val ingredients: List<String>,
    val serving: Int?,
    val prepTime: String,
    val cookingTime: String,
    val cuisines: Set<String>,
    val categories: Set<String>
)


@Preview(showBackground = true)
@Composable
fun FindRecipeScreenPreview() {
    MaterialTheme { // Wrap preview in MaterialTheme
        FindRecipeScreen()
    }
}