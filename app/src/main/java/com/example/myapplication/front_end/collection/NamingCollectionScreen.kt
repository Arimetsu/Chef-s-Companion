package com.example.myapplication.front_end.collection

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close // Correct import for Close icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.R // Needed for placeholder/error drawables
import com.example.myapplication.data.Recipe // UI Model
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.front_end.home.PrimaryGreen // Import color
import com.example.myapplication.front_end.home.monte // Ensure font is accessible
import com.example.myapplication.front_end.recipe.add.TextFieldTextColor // Import color
import androidx.compose.foundation.lazy.grid.items
import com.example.myapplication.viewModel.CollectionCreationState
import com.example.myapplication.viewModel.SavedRecipeListState
import com.example.myapplication.viewModel.SavedRecipesViewModel


// Define colors if not imported from elsewhere
val TextFieldTextColor = Color.Black // Example Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamingCollectionScreen(
    navController: NavController,
    selectedRecipeIds: List<String>, // Recipe IDs passed from the previous screen
    savedRecipesViewModel: SavedRecipesViewModel = viewModel()
) {
    var collectionName by remember { mutableStateOf("") }
    val creationState by savedRecipesViewModel.collectionCreationState.collectAsStateWithLifecycle()
    // *** CORRECTED: Observe savedRecipeListState, NOT allRecipesState ***
    val savedRecipesListState by savedRecipesViewModel.savedRecipeListState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // *** REMOVED: LaunchedEffect calling fetchAllRecipes ***
    // The data should be loaded by the *previous* screen into savedRecipeListState

    Log.d("NamingScreen", "Composable Start/Recompose. Saved Recipes State: ${savedRecipesListState::class.simpleName}, Creation State: ${creationState::class.simpleName}, Received IDs: $selectedRecipeIds")

    // Filter recipes based on the *correct* state (savedRecipeListState)
    val selectedRecipeDetails = remember(savedRecipesListState, selectedRecipeIds) {
        val idsSet = selectedRecipeIds.toSet()
        when (val currentState = savedRecipesListState) {
            is SavedRecipeListState.Success -> {
                Log.d("NamingScreen", "Calculating details: Filtering ${idsSet.size} IDs from Success state (${currentState.recipes.size} total).")
                // Filter the recipes from the Success state based on the passed IDs
                currentState.recipes.filter { recipe ->
                    idsSet.contains(recipe.id)
                }.also {
                    Log.d("NamingScreen", "Filtering result size: ${it.size}")
                    // Log a warning if the number of found recipes doesn't match the number of IDs passed
                    if (it.size != selectedRecipeIds.size) {
                        Log.w("NamingScreen", "Mismatch! Expected ${selectedRecipeIds.size} recipes, found ${it.size}. Passed IDs: $selectedRecipeIds. Full list IDs: ${currentState.recipes.map { r -> r.id }}")
                    }
                }
            }
            else -> {
                // If the state is Loading, Error, or Empty, return an empty list for now
                Log.d("NamingScreen", "Calculating details: State is not Success (${currentState::class.simpleName}). Returning empty list.")
                emptyList<Recipe>()
            }
        }
    }

    // LaunchedEffect to handle creation state changes (Success/Error)
    LaunchedEffect(creationState) {
        when (val state = creationState) {
            is CollectionCreationState.Success -> {
                Toast.makeText(context, "'${collectionName.trim()}' created!", Toast.LENGTH_SHORT).show()
                savedRecipesViewModel.resetCollectionCreationState() // Reset state
                // Navigate back to the YourRecipes screen after successful creation
                navController.popBackStack(ScreenNavigation.Screen.YourRecipes.route, inclusive = false)
            }
            is CollectionCreationState.Error -> {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                savedRecipesViewModel.resetCollectionCreationState() // Reset state
            }
            else -> { /* Idle or Loading - No action needed here */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text( "Name Collection", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen, fontFamily = monte ) },
                navigationIcon = {
                    // Disable back button while loading to prevent issues
                    IconButton(
                        onClick = { if (creationState != CollectionCreationState.Loading) navController.popBackStack() },
                        enabled = creationState != CollectionCreationState.Loading
                    ) {
                        Icon( Icons.Filled.ArrowBack, contentDescription = "Back", tint = if (creationState != CollectionCreationState.Loading) PrimaryGreen else Color.Gray )
                    }
                },
                actions = {
                    // Checkmark button to create the collection
                    IconButton(
                        onClick = {
                            val finalName = collectionName.trim()
                            focusManager.clearFocus() // Hide keyboard
                            if (finalName.isNotBlank() && creationState != CollectionCreationState.Loading) {
                                Log.d("NamingScreen", "Create button clicked. Name: '$finalName', IDs: $selectedRecipeIds")
                                // Call ViewModel function to create the collection
                                savedRecipesViewModel.createNewCollection(finalName, selectedRecipeIds)
                            } else if (finalName.isBlank()){
                                Toast.makeText(context, "Please enter a collection name", Toast.LENGTH_SHORT).show()
                            }
                        },
                        // Disable button while loading or if name is blank
                        enabled = creationState != CollectionCreationState.Loading && collectionName.isNotBlank()
                    ) {
                        // Show progress indicator if loading, otherwise the icon
                        if (creationState == CollectionCreationState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = PrimaryGreen)
                        } else {
                            Icon( Icons.Filled.Check, contentDescription = "Create Collection", tint = if (collectionName.isNotBlank()) PrimaryGreen else Color.Gray )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues) // Apply Scaffold padding FIRST
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 16.dp) // Apply content padding AFTER background
                .imePadding(), // Add padding for the keyboard
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Enter a name for your new collection:",
                fontFamily = monte,
                modifier = Modifier.fillMaxWidth(),
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Collection Name TextField
            OutlinedTextField(
                value = collectionName,
                onValueChange = { collectionName = it },
                label = { Text("Collection Name", fontFamily = monte) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = creationState != CollectionCreationState.Loading, // Disable while loading
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen, unfocusedBorderColor = PrimaryGreen.copy(alpha = 0.5f),
                    focusedLabelColor = PrimaryGreen, unfocusedLabelColor = Color.Gray, cursorColor = PrimaryGreen,
                    focusedTextColor = TextFieldTextColor, unfocusedTextColor = TextFieldTextColor,
                    disabledBorderColor = Color.Gray.copy(alpha = 0.3f) // Optional: style when disabled
                ),
                textStyle = TextStyle(fontFamily = monte)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section Header for selected recipes
            Text(
                text = when (savedRecipesListState) {
                    is SavedRecipeListState.Success -> "Recipes in this collection (${selectedRecipeDetails.size}):"
                    is SavedRecipeListState.Loading -> "Loading selected recipes..."
                    else -> "Selected Recipes:" // Fallback for Error/Empty
                },
                fontFamily = monte,
                modifier = Modifier.fillMaxWidth(),
                color = Color.DarkGray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Display Area for Selected Recipes using a Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Grid takes available vertical space
                contentAlignment = Alignment.Center // Center loading/error messages within the box
            ) {
                // Use the correct state here
                when (val currentRecipeState = savedRecipesListState) {
                    is SavedRecipeListState.Loading -> {
                        // Show loading indicator if the recipe list is still loading
                        CircularProgressIndicator(color = PrimaryGreen)
                        Log.d("NamingScreen", "UI: Displaying loading indicator for recipe list.")
                    }
                    is SavedRecipeListState.Success -> {
                        // If recipes loaded successfully, check if we found the details
                        if (selectedRecipeDetails.isNotEmpty()) {
                            Log.d("NamingScreen", "UI: Displaying grid with ${selectedRecipeDetails.size} recipes.")
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2), // Adjust columns as needed
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(selectedRecipeDetails, key = { it.id }) { recipe ->
                                    // Assuming SelectableHomeScreenRecipeCard exists and takes these params
                                    SelectableHomeScreenRecipeCard(
                                        recipe = recipe,
                                        isSelected = true, // They are always selected on this screen
                                        onClick = { /* Maybe navigate to detail? Or do nothing */ }
                                    )
                                }
                            }
                        } else {
                            // This case means recipes loaded, but filtering failed (IDs didn't match)
                            Log.w("NamingScreen", "UI: Recipe list loaded, but filtering returned empty. Check passed IDs and list content.")
                            Text(
                                "Could not find details for the selected recipes.\nPlease go back and try again.",
                                fontFamily = monte, color = Color.Gray, textAlign = TextAlign.Center
                            )
                        }
                    }
                    is SavedRecipeListState.Error -> {
                        // Show error message if loading the recipe list failed
                        Log.e("NamingScreen", "UI: Displaying error message: ${currentRecipeState.message}")
                        Text(
                            "Error loading recipe details:\n${currentRecipeState.message}",
                            fontFamily = monte, color = Color.Red, textAlign = TextAlign.Center
                        )
                    }
                    is SavedRecipeListState.Empty -> {
                        // Show message if the saved recipe list was empty
                        Log.w("NamingScreen", "UI: Saved recipe list is empty.")
                        Text(
                            "No saved recipes found to select from.", // Or adjust message
                            fontFamily = monte, color = Color.Gray, textAlign = TextAlign.Center
                        )
                    }
                }
            } // End of Grid Display Area Box

            Spacer(modifier = Modifier.height(16.dp)) // Space before the bottom button

            // Create Collection Button (at the bottom)
            Button(
                onClick = {
                    val finalName = collectionName.trim()
                    focusManager.clearFocus()
                    if (finalName.isNotBlank() && creationState != CollectionCreationState.Loading) {
                        savedRecipesViewModel.createNewCollection(finalName, selectedRecipeIds)
                    } else if (finalName.isBlank()){
                        Toast.makeText(context, "Please enter a collection name", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = creationState != CollectionCreationState.Loading && collectionName.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen, contentColor = Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (creationState == CollectionCreationState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Create Collection", fontFamily = monte, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Bottom padding
        }
    }
}

// --- SelectedRecipeChip Composable (Ensure it's defined as before) ---
@Composable
fun SelectedRecipeChip(
    recipe: Recipe,
    onUnselect: () -> Unit,
    modifier: Modifier = Modifier,
    showRemoveButton: Boolean = true // Added parameter
) {
    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(40.dp)
                .background(Color(96,137,99), RoundedCornerShape(20.dp)) // Use a theme color or specific color
                .clip(RoundedCornerShape(20.dp))
                .padding(start = 6.dp, end = if (showRemoveButton) 4.dp else 10.dp) // Conditional end padding
        ) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = null, // Decorative in a chip list
                placeholder = painterResource(R.drawable.greenbackgroundlogo), // Use appropriate placeholder
                error = painterResource(R.drawable.greenbackgroundlogo), // Use appropriate error placeholder
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray) // Placeholder bg
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = recipe.name,
                fontSize = 12.sp,
                fontFamily = monte,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White, // Adjust text color for contrast
                modifier = Modifier.weight(1f, fill = false) // Prevent text push
            )
            // Conditionally show the remove button
            if (showRemoveButton) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onUnselect,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(0.dp) // Remove extra padding if needed
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove ${recipe.name}",
                        tint = Color.White.copy(alpha = 0.7f), // Adjust icon color/alpha
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}