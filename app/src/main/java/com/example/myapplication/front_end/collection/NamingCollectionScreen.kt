package com.example.myapplication.front_end.collection

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.R // Needed for placeholder/error drawables
import com.example.myapplication.data.Recipe // UI Model
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.front_end.home.monte // Ensure font is accessible
import com.example.myapplication.front_end.recipe.add.TextFieldTextColor

import com.example.myapplication.viewModel.CollectionCreationState
import com.example.myapplication.viewModel.SavedRecipeListState
import com.example.myapplication.viewModel.SavedRecipesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamingCollectionScreen(
    navController: NavController,
    selectedRecipeIds: List<String>, // Received from navigation
    savedRecipesViewModel: SavedRecipesViewModel = viewModel()
) {
    var collectionName by remember { mutableStateOf("") }
    val creationState by savedRecipesViewModel.collectionCreationState.collectAsStateWithLifecycle()
    val allRecipesState by savedRecipesViewModel.allRecipesState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Log.d("NamingScreen", "Composable Start/Recompose. State: ${allRecipesState::class.simpleName}, Received IDs: $selectedRecipeIds")

    // Derive the actual Recipe objects for display based on passed IDs
    val selectedRecipeDetails = remember(allRecipesState, selectedRecipeIds) {
        val idsSet = selectedRecipeIds.toSet()
        if (allRecipesState is SavedRecipeListState.Success) {
            Log.d("NamingScreen", "Calculating details: State is Success. Filtering ${idsSet.size} IDs.")
            (allRecipesState as SavedRecipeListState.Success).recipes.filter { recipe ->
                idsSet.contains(recipe.id)
            }.also {
                Log.d("NamingScreen", "Filtering result size: ${it.size}")
            }
        } else {
            Log.d("NamingScreen", "Calculating details: State is ${allRecipesState::class.simpleName}. Returning empty list.")
            emptyList()
        }
    }

    // LaunchedEffect for handling creation state results
    LaunchedEffect(creationState) {
        when (val state = creationState) {
            is CollectionCreationState.Success -> {
                Toast.makeText(context, "'${collectionName.trim()}' created!", Toast.LENGTH_SHORT).show()
                savedRecipesViewModel.resetCollectionCreationState()
                // Navigate back to YourRecipes screen after success, clear intermediate screens
                navController.popBackStack(ScreenNavigation.Screen.YourRecipes.route, inclusive = false)
            }
            is CollectionCreationState.Error -> {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                savedRecipesViewModel.resetCollectionCreationState()
            }
            else -> { /* Idle or Loading */ }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text( "Name Collection", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen, fontFamily = monte ) },
                navigationIcon = {
                    IconButton(onClick = {
                        // Prevent back navigation while loading if desired
                        if (creationState != CollectionCreationState.Loading) {
                            navController.popBackStack()
                        }
                    }) {
                        Icon( Icons.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGreen )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val finalName = collectionName.trim()
                            focusManager.clearFocus()
                            if (finalName.isNotBlank() && creationState != CollectionCreationState.Loading) {
                                savedRecipesViewModel.createNewCollection(finalName, selectedRecipeIds)
                            } else if (finalName.isBlank()){
                                Toast.makeText(context, "Please enter a collection name", Toast.LENGTH_SHORT).show()
                            }
                        },
                        // Disable button while loading or if name is blank
                        enabled = creationState != CollectionCreationState.Loading && collectionName.isNotBlank()
                    ) {
                        if (creationState == CollectionCreationState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = PrimaryGreen)
                        } else {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Create Collection",
                                // Change tint based on enabled state for better UX
                                tint = if (collectionName.isNotBlank()) PrimaryGreen else Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize() // Takes full size
                .background(Color.White) // Explicit background
                .padding(horizontal = 16.dp), // Horizontal padding for content
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Top spacing

            Text(
                "Enter a name for your new collection:",
                fontFamily = monte,
                modifier = Modifier.fillMaxWidth(), // Align text to start
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = collectionName,
                onValueChange = { collectionName = it },
                label = { Text("Collection Name", fontFamily = monte) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = creationState != CollectionCreationState.Loading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = PrimaryGreen.copy(alpha = 0.5f),
                    focusedLabelColor = PrimaryGreen,
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = PrimaryGreen,
                    focusedTextColor = TextFieldTextColor,
                    unfocusedTextColor = TextFieldTextColor
                ),
                textStyle = TextStyle(fontFamily = monte)
            )

            Spacer(modifier = Modifier.height(24.dp)) // Space before recipe list

            // *** Section to display the selected recipes ***
            Text(
                "Recipes in this collection (${selectedRecipeDetails.size}):",
                fontFamily = monte,
                modifier = Modifier.fillMaxWidth(), // Align text to start
                color = Color.DarkGray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // --- Display Area for Selected Recipes ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp, max= 150.dp), // Allow slightly more height if needed
                contentAlignment = Alignment.TopStart // Align LazyRow to top-start
            ) {
                when (allRecipesState) {
                    is SavedRecipeListState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                            CircularProgressIndicator(color = PrimaryGreen)
                        }
                    }
                    is SavedRecipeListState.Success -> {
                        if (selectedRecipeDetails.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(selectedRecipeDetails, key = { it.id }) { recipe ->
                                    SelectedRecipeChip( // Reusable chip
                                        recipe = recipe,
                                        onUnselect = { /* No action */ },
                                        showRemoveButton = false // Hide the 'X' button
                                    )
                                }
                            }
                        } else {
                            // State is Success, but filtering found nothing (ID mismatch?)
                            Text(
                                "Could not load details for selected recipes.",
                                fontFamily = monte,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp).align(Alignment.Center)
                            )
                        }
                    }
                    is SavedRecipeListState.Error -> {
                        Text(
                            "Error loading recipe details: ${(allRecipesState as SavedRecipeListState.Error).message}",
                            fontFamily = monte,
                            color = Color.Red,
                            modifier = Modifier.padding(vertical = 8.dp).align(Alignment.Center)
                        )
                    }
                    is SavedRecipeListState.Empty -> {
                        Text(
                            "No recipes available to display.",
                            fontFamily = monte,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp).align(Alignment.Center)
                        )
                    }
                }
            }
            // *** End of selected recipes display section ***

            Spacer(modifier = Modifier.weight(1f)) // Pushes button to bottom

            // Create Collection Button (Material 3)
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
                // Disable button while loading or if name is blank
                enabled = creationState != CollectionCreationState.Loading && collectionName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors( // M3 ButtonDefaults
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f), // More distinct disabled color
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

// Ensure SelectedRecipeChip exists and has the showRemoveButton parameter
// This composable should ideally be in a shared file if used elsewhere
@Composable
fun SelectedRecipeChip(
    recipe: Recipe,
    onUnselect: () -> Unit,
    modifier: Modifier = Modifier,
    showRemoveButton: Boolean = true // Parameter to control the 'X' button
) {
    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(40.dp) // Fixed height
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                // Adjust end padding based on whether the button is shown
                .padding(start = 6.dp, end = if (showRemoveButton) 4.dp else 10.dp)
        ) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.name, // Use recipe name for accessibility
                placeholder = painterResource(R.drawable.greenbackgroundlogo), // Use a neutral placeholder
                error = painterResource(R.drawable.greenbackgroundlogo), // Use a neutral error placeholder
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray) // Background if image fails
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = recipe.name,
                fontSize = 12.sp,
                fontFamily = monte,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f, fill = false) // Prevent text from pushing button away
            )
            // Conditionally display the remove button
            if (showRemoveButton) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onUnselect,
                    modifier = Modifier.size(24.dp) // Ensure sufficient touch target
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove ${recipe.name}",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

