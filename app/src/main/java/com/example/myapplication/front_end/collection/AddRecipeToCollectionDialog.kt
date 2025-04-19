package com.example.myapplication.front_end.collection

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.data.Recipe
import com.example.myapplication.front_end.home.monte // Your font

@Composable
fun AddRecipeToCollectionDialog(
    availableRecipes: List<Recipe>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onAddSelected: (List<String>) -> Unit // Pass back selected IDs
) {
    var selectedRecipeIdsToAdd by remember { mutableStateOf<Set<String>>(emptySet()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colors.surface, // Or Color.White
            modifier = Modifier
                .fillMaxWidth(0.95f) // Take up most of width
                .fillMaxHeight(0.8f) // Take up most of height
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Add Recipes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = monte,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                } else if (availableRecipes.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            "No other saved recipes available to add.",
                            fontFamily = monte,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2), // Or 3 if space allows
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f) // Fill available space
                    ) {
                        items(availableRecipes, key = { it.id }) { recipe ->
                            SelectableRecipeCard( // Reuse the card from edit screen
                                recipe = recipe,
                                isSelected = selectedRecipeIdsToAdd.contains(recipe.id),
                                onToggleSelection = {
                                    selectedRecipeIdsToAdd = if (selectedRecipeIdsToAdd.contains(recipe.id)) {
                                        selectedRecipeIdsToAdd - recipe.id
                                    } else {
                                        selectedRecipeIdsToAdd + recipe.id
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontFamily = monte, color = PrimaryGreen)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAddSelected(selectedRecipeIdsToAdd.toList()) },
                        enabled = selectedRecipeIdsToAdd.isNotEmpty() && !isLoading,
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryGreen)
                    ) {
                        Text("Add Selected", fontFamily = monte, color = Color.White)
                    }
                }
            }
        }
    }
}