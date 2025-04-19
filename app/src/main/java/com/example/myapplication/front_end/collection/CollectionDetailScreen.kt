package com.example.myapplication.front_end // Or your actual package

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Use auto-mirrored for LTR/RTL
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.Recipe // Your UI Model
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.front_end.home.RecipeCard // Assuming reusable RecipeCard
import com.example.myapplication.front_end.home.monte // Your font
import com.example.myapplication.viewModel.CollectionDetailState
import com.example.myapplication.viewModel.CollectionUpdateState
import com.example.myapplication.viewModel.DEFAULT_SAVED_COLLECTION_NAME // Import the constant
import com.example.myapplication.viewModel.RecipeFilters // Import the constant
import com.example.myapplication.viewModel.SavedRecipesViewModel
import kotlinx.coroutines.launch
import java.net.URLDecoder


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    navController: NavController,
    collectionId: String,
    initialCollectionName: String, // Renamed parameter for clarity
    savedRecipesViewModel: SavedRecipesViewModel = viewModel()
) {
    val detailState by savedRecipesViewModel.collectionDetailState.collectAsStateWithLifecycle()
    val updateState by savedRecipesViewModel.collectionUpdateState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // State for showing dialogs
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }

    // Decode the initial collection name in case it was URL encoded
    val decodedInitialName = remember(initialCollectionName) {
        try {
            URLDecoder.decode(initialCollectionName, "UTF-8")
        } catch (e: Exception) {
            Log.e("CollectionDetailScreen", "Error decoding initial collection name", e)
            initialCollectionName // Fallback to original if decoding fails
        }
    }


    // Fetch details when the screen is composed or collectionId changes
    LaunchedEffect(collectionId) {
        Log.d("CollectionDetailScreen", "LaunchedEffect: Fetching details for collection ID: $collectionId")
        savedRecipesViewModel.fetchRecipesForCollection(collectionId)
    }

    // Handle navigation when collection is deleted
    LaunchedEffect(detailState) {
        if (detailState is CollectionDetailState.Deleted) {
            Log.d("CollectionDetailScreen", "Detail state is Deleted, navigating back.")
            Toast.makeText(context, "Collection deleted", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            // Consider resetting the state in VM if needed, though fetch handles loading again
        }
    }

    // Show snackbar feedback for update operations (rename, add/remove recipes, delete failure)
    LaunchedEffect(updateState) {
        // ... (snackbar logic remains the same) ...
        when (val uState = updateState) {
            is CollectionUpdateState.Success -> {
                // Optional: Show success message for operations other than delete
                // snackbarHostState.showSnackbar("Update successful!")
                Log.d("CollectionDetailScreen", "Update successful, resetting state.")
                savedRecipesViewModel.resetCollectionUpdateState() // Reset after success
            }
            is CollectionUpdateState.Error -> {
                Log.e("CollectionDetailScreen", "Update error: ${uState.message}")
                scope.launch { // Launch within coroutine scope
                    snackbarHostState.showSnackbar(
                        message = uState.message, // Show specific error from ViewModel
                        duration = SnackbarDuration.Long
                    )
                }
                savedRecipesViewModel.resetCollectionUpdateState() // Reset after showing error
            }
            else -> { /* Idle or Loading - No Snackbar needed */ }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Display loading/error or the actual collection details
            when (val state = detailState) {
                is CollectionDetailState.Success -> {
                    val collection = state.collection
                    val isProtectedCollection = remember(collection.name) {
                        collection.name.equals(RecipeFilters.FAVORITES, ignoreCase = true) ||
                                collection.name.equals(DEFAULT_SAVED_COLLECTION_NAME, ignoreCase = true)
                    }
                    Log.d("CollectionDetailScreen", "TopBar: Collection='${collection.name}', Protected=$isProtectedCollection")

                    TopAppBar(
                        // Use the name from the loaded state for accuracy
                        title = { Text(collection.name, fontWeight = FontWeight.Bold, fontFamily = monte) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
                            }
                        },
                        actions = {
                            // --- Edit, Delete, Add Icons (Conditional based on isProtectedCollection) ---
                            // --- Edit Name Icon ---
                            if (!isProtectedCollection) {
                                IconButton(onClick = {
                                    newNameInput = collection.name // Pre-fill dialog
                                    showRenameDialog = true
                                    Log.d("CollectionDetailScreen", "Edit clicked for '${collection.name}'")
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Rename Collection", tint = PrimaryGreen)
                                }
                            }
                            // --- Delete Icon ---
                            if (!isProtectedCollection) {
                                IconButton(onClick = {
                                    showDeleteDialog = true
                                    Log.d("CollectionDetailScreen", "Delete clicked for '${collection.name}'")
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Collection", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            // --- Add Recipes Icon ---
                            IconButton(onClick = {
                                navController.navigate(
                                    ScreenNavigation.Screen.AddRecipeToCollection.createRoute(collection.id)
                                )
                                Log.d("CollectionDetailScreen", "Add Recipes clicked for '${collection.name}'")
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Recipes", tint = PrimaryGreen)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
                // --- Loading State: Use decodedInitialName ---
                is CollectionDetailState.Loading -> {
                    TopAppBar(
                        // *** Use the decoded initial name while loading ***
                        title = { Text(decodedInitialName, fontFamily = monte) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
                            }
                        },
                        // Add a progress indicator maybe?
                        actions = { Spacer(Modifier.width(48.dp)) }, // Placeholder for actions alignment
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
                // --- Error State ---
                is CollectionDetailState.Error -> {
                    TopAppBar(
                        title = { Text("Error", fontFamily = monte) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
                // --- Deleted State ---
                is CollectionDetailState.Deleted -> {
                    TopAppBar(
                        title = { Text("", fontFamily = monte) }, // Blank title while navigating away
                        navigationIcon = {
                            IconButton(onClick = { /* May already be navigating */ }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Gray)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
            }
        }
    ) { paddingValues ->
        // --- Content Area (Box + When statement) ---
        Box(modifier = Modifier.padding(paddingValues)
            .background(Color.White)
            .fillMaxSize()) {
            when (val state = detailState) {
                is CollectionDetailState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is CollectionDetailState.Success -> {
                    // ... (Recipe grid or empty message display logic remains the same) ...
                    val recipes = state.recipes
                    if (recipes.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text( "This collection is empty.\nTap '+' to add recipes.", color = Color.Gray, textAlign = TextAlign.Center, fontFamily = monte )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(recipes, key = { it.id }) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = { navController.navigate(ScreenNavigation.Screen.RecipeDetail.createRoute(recipe.id)) }
                                )
                            }
                        }
                    }
                }
                is CollectionDetailState.Error -> {
                    // ... (Error message display logic remains the same) ...
                    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text( "Error loading collection details:\n${state.message}", color = Color.Red, textAlign = TextAlign.Center, fontFamily = monte )
                    }
                }
                is CollectionDetailState.Deleted -> {
                    // ... (Content while deleted state is active - likely brief) ...
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { }
                }
            }
        } // End Content Area Box
    }

    // --- Rename Dialog ---
    if (showRenameDialog && detailState is CollectionDetailState.Success) {
        val currentCollection = (detailState as CollectionDetailState.Success).collection
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Collection", fontFamily = monte) },
            text = {
                OutlinedTextField(
                    value = newNameInput,
                    onValueChange = { newNameInput = it },
                    label = { Text("New collection name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalNewName = newNameInput.trim()
                        if (finalNewName.isNotEmpty() && finalNewName != currentCollection.name) {
                            Log.d("CollectionDetailScreen", "Confirm Rename: '${currentCollection.name}' -> '$finalNewName'")
                            savedRecipesViewModel.updateCollectionName(currentCollection.id, finalNewName)
                        } else {
                            Log.d("CollectionDetailScreen", "Rename skipped: Name empty or unchanged.")
                        }
                        showRenameDialog = false
                    },
                    // Disable if name is empty or unchanged, or if an update is already loading
                    enabled = newNameInput.trim().isNotEmpty() &&
                            newNameInput.trim() != currentCollection.name &&
                            updateState != CollectionUpdateState.Loading
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Delete Confirmation Dialog ---
    if (showDeleteDialog && detailState is CollectionDetailState.Success) {
        val currentCollection = (detailState as CollectionDetailState.Success).collection
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Collection?", fontFamily = monte) },
            text = { Text("Are you sure you want to delete the collection '${currentCollection.name}'? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d("CollectionDetailScreen", "Confirm Delete: '${currentCollection.name}' (ID: ${currentCollection.id})")
                        savedRecipesViewModel.deleteCollection(currentCollection.id)
                        showDeleteDialog = false // Close dialog immediately
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = updateState != CollectionUpdateState.Loading // Disable if already processing an update/delete
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = updateState != CollectionUpdateState.Loading // Also disable dismiss during loading
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}