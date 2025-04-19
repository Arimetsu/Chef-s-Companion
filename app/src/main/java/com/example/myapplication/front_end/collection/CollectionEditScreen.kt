package com.example.myapplication.front_end.collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.* // M2 components
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage // Import AsyncImage
import com.example.myapplication.R // For placeholder drawable
import com.example.myapplication.data.Recipe
import com.example.myapplication.data.UserCollection
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.front_end.home.bb2
import com.example.myapplication.front_end.home.monte
import com.example.myapplication.viewModel.CollectionDetailState
import com.example.myapplication.viewModel.CollectionUpdateState
import com.example.myapplication.viewModel.SavedRecipesViewModel
import com.example.myapplication.viewModel.SavedRecipeListState // Needed for fetching recipes to add
import kotlinx.coroutines.launch

@Composable
fun CollectionEditScreen(
    navController: NavHostController,
    collectionId: String,
    savedRecipesViewModel: SavedRecipesViewModel = viewModel()
) {
    val detailState by savedRecipesViewModel.collectionDetailState.collectAsStateWithLifecycle()
    val updateState by savedRecipesViewModel.collectionUpdateState.collectAsStateWithLifecycle()
    // State to hold all potentially addable recipes (fetched once)
    val allSavedRecipesState by savedRecipesViewModel.savedRecipeListState.collectAsStateWithLifecycle()

    var collectionData by remember { mutableStateOf<UserCollection?>(null) }
    var recipesInCollection by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var editedName by remember { mutableStateOf(TextFieldValue("")) }
    var selectedRecipeIdsToRemove by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showAddRecipeDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    // Fetch initial collection details
    LaunchedEffect(collectionId) {
        savedRecipesViewModel.fetchRecipesForCollection(collectionId)
        // Also ensure all saved recipes are loaded for the 'Add' dialog
        if (allSavedRecipesState !is SavedRecipeListState.Success) {
            savedRecipesViewModel.fetchRecipesForSelection()
        }
    }

    // Update local state when detailState loads successfully
    LaunchedEffect(detailState) {
        if (detailState is CollectionDetailState.Success) {
            val successState = detailState as CollectionDetailState.Success
            collectionData = successState.collection
            recipesInCollection = successState.recipes
            editedName = TextFieldValue(successState.collection.name)
            selectedRecipeIdsToRemove = emptySet() // Reset selection on data load
        }
    }

    // Handle feedback from update operations (Save, Remove, Add, Delete)
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is CollectionUpdateState.Success -> {
                scope.launch { scaffoldState.snackbarHostState.showSnackbar("Changes saved successfully") }
                savedRecipesViewModel.resetCollectionUpdateState() // Reset state after showing message
                // Name change is handled by LaunchedEffect(detailState) reacting to re-fetch
            }
            is CollectionUpdateState.Error -> {
                scope.launch { scaffoldState.snackbarHostState.showSnackbar("Error: ${state.message}") }
                savedRecipesViewModel.resetCollectionUpdateState()
            }
            is CollectionUpdateState.Loading -> { /* Maybe show indicator */ }
            else -> {} // Idle
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text( "Editing", fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = bb2, color = PrimaryGreen )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
                    }
                },
                actions = {
                    // Save Button (Checkmark)
                    IconButton(
                        onClick = {
                            val nameChanged = collectionData?.name != editedName.text.trim()
                            val itemsToRemove = selectedRecipeIdsToRemove.isNotEmpty()

                            if (nameChanged) {
                                savedRecipesViewModel.updateCollectionName(collectionId, editedName.text)
                                // The updateState LaunchedEffect will show snackbar on success/error
                            }
                            if (itemsToRemove) {
                                savedRecipesViewModel.removeRecipesFromCollection(collectionId, selectedRecipeIdsToRemove.toList())
                                selectedRecipeIdsToRemove = emptySet() // Clear selection after initiating removal
                                // updateState LaunchedEffect handles feedback
                            }
                            if (!nameChanged && !itemsToRemove) {
                                // If nothing changed, maybe show a message or just do nothing
                                scope.launch { scaffoldState.snackbarHostState.showSnackbar("No changes to save") }
                            }
                            // Adding is handled separately via the dialog's confirmation button
                        },
                        enabled = updateState !is CollectionUpdateState.Loading // Disable while any operation is loading
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save Changes", tint = PrimaryGreen)
                    }
                },
                backgroundColor = Color.White,
                elevation = 0.dp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    ) { paddingValues ->
        when (val state = detailState) {
            is CollectionDetailState.Loading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            is CollectionDetailState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .background(Color.White)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Collection Name ---
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Collection Name", fontFamily = monte, color = PrimaryGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = PrimaryGreen
                        ),
                        singleLine = true,
                        trailingIcon = {
                            if (editedName.text.isNotEmpty()) {
                                IconButton(onClick = { editedName = TextFieldValue("") }) {
                                    Icon(Icons.Filled.Close, "Clear name", tint = Color.Gray)
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Delete Button ---
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.White,
                            contentColor = Color.Red
                        ),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                        elevation = ButtonDefaults.elevation(0.dp)
                    ) {
                        Text("Delete Collection", fontFamily = monte)
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // --- Recipe Selection Area ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Selecting", fontSize = 20.sp, fontFamily = monte, fontWeight = FontWeight.SemiBold)
                        // Optional: Remove button (could be combined with Save/Checkmark)
                        // TextButton(onClick = { /* Handle Remove */ }, enabled = selectedRecipeIdsToRemove.isNotEmpty()) {
                        //     Text("Remove", color = if (selectedRecipeIdsToRemove.isNotEmpty()) Color.Red else Color.Gray)
                        // }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid of recipes to add/remove
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2), // Or Adaptive
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f) // Takes remaining space
                    ) {
                        // Add Recipe Button
                        item {
                            AddRecipeCardButton(onClick = { showAddRecipeDialog = true })
                        }

                        // Recipes currently in the collection
                        items(recipesInCollection, key = { it.id }) { recipe ->
                            SelectableRecipeCard(
                                recipe = recipe,
                                isSelected = selectedRecipeIdsToRemove.contains(recipe.id),
                                onToggleSelection = {
                                    selectedRecipeIdsToRemove = if (selectedRecipeIdsToRemove.contains(recipe.id)) {
                                        selectedRecipeIdsToRemove - recipe.id
                                    } else {
                                        selectedRecipeIdsToRemove + recipe.id
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // Padding at bottom
                } // End Column
            } // End Success State
            is CollectionDetailState.Error -> {
                Box(Modifier.fillMaxSize().padding(paddingValues).padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error loading collection for editing: ${state.message}",
                        color = Color.Red, fontFamily = monte, textAlign = TextAlign.Center
                    )
                }
            }
            is CollectionDetailState.Deleted -> {
                Box(Modifier.fillMaxSize().padding(paddingValues).padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Collection not found.", color = Color.Gray, fontFamily = monte)
                }
            }
        } // End When
    } // End Scaffold

    // --- Dialogs ---
    if (showAddRecipeDialog) {
        val availableRecipes = remember(allSavedRecipesState, recipesInCollection) {
            if (allSavedRecipesState is SavedRecipeListState.Success) {
                val currentIds = recipesInCollection.map { it.id }.toSet()
                (allSavedRecipesState as SavedRecipeListState.Success).recipes.filter { it.id !in currentIds }
            } else {
                emptyList()
            }
        }

        AddRecipeToCollectionDialog(
            availableRecipes = availableRecipes,
            isLoading = allSavedRecipesState is SavedRecipeListState.Loading,
            onDismiss = { showAddRecipeDialog = false },
            onAddSelected = { recipeIdsToAdd ->
                savedRecipesViewModel.addRecipesToCollection(collectionId, recipeIdsToAdd)
                showAddRecipeDialog = false // Close dialog after initiating add
                // Update state feedback will handle snackbar etc.
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Collection?", fontFamily = monte, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete the '${collectionData?.name ?: "this"}' collection? This cannot be undone.", fontFamily = monte) },
            confirmButton = {
                Button(
                    onClick = {
                        savedRecipesViewModel.deleteCollection(collectionId)
                        showDeleteConfirmDialog = false
                        // Navigation back happens via LaunchedEffect on detailState becoming Deleted
                        navController.navigate(ScreenNavigation.Screen.YourRecipes.route)
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red, contentColor = Color.White)
                ) { Text("Delete", fontFamily = monte) }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White, // white background
                        contentColor = Color(0xFF1A4D2E) // text color: rgba(26, 77, 46, 1)
                    ),
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(6.dp) // optional: for slight rounding
                ) {
                    Text("Cancel", fontFamily = monte)
                }
            }
        )
    }
}


// Simple Card with a "+" Icon
@Composable
fun AddRecipeCardButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .aspectRatio(1f) // Square shape like recipe cards
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        elevation = 0.dp,
        backgroundColor = PrimaryGreen.copy(alpha = 0.05f) // Light background tint
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Recipe to Collection",
                tint = PrimaryGreen,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}


// Recipe Card with a Checkbox overlay for selection
@Composable
fun SelectableRecipeCard(
    recipe: Recipe,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f) // Maintain aspect ratio
            .clickable(onClick = onToggleSelection), // Click anywhere on card toggles
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Recipe Image and Name (similar to RecipeCard)
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.name,
                placeholder = painterResource(R.drawable.greenbackgroundlogo), // Replace with your placeholder
                error = painterResource(R.drawable.greenbackgroundlogo),       // Replace with your error placeholder
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient( colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)), startY = 200f )) // Adjust gradient as needed
            )
            // Recipe Name at bottom
            Text(
                text = recipe.name,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                fontFamily = monte,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )

            // --- Selection Checkbox ---
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape), // Background for visibility
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryGreen,
                    uncheckedColor = Color.White,
                    checkmarkColor = Color.White
                )
            )
        }
    }
}