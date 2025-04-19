package com.example.myapplication.front_end.recipe.edit // Adjust package name if needed

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add // Keep for consistency if used in helpers
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.RecipeDetail // Used for initial data
import com.example.myapplication.front_end.home.monte // Ensure import is correct
// Assuming helpers are structured similarly or copied/adapted:
import com.example.myapplication.front_end.recipe.add.IngredientInputItem // Import data class if shared
import com.example.myapplication.front_end.recipe.add.commonUnits // Import if shared
import com.example.myapplication.front_end.recipe.add.DropdownInput // Import helper
import com.example.myapplication.front_end.recipe.add.TimeInputRow // Import helper
import com.example.myapplication.front_end.recipe.add.TimeInputUnit // Import helper
import com.example.myapplication.front_end.recipe.add.IngredientListInput // Import helper
import com.example.myapplication.front_end.recipe.add.DynamicListInput // Import helper
import com.example.myapplication.front_end.recipe.add.recipeTextFieldColors // Import helper
import com.example.myapplication.front_end.recipe.add.formatTime // Import helper
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewModel.IngredientInput // ViewModel's input class
import com.example.myapplication.viewModel.RecipeViewModel
import com.example.myapplication.viewModel.RecipeDetailState // VM State for loading
import com.example.myapplication.viewModel.RecipeDetailViewModel // VM for loading
import com.example.myapplication.viewModel.RecipeViewModel.RecipeModifyState
import kotlinx.coroutines.delay // For error message display

// --- Constants (Define or import from a shared location) ---
val PrimaryGreen = Color(0xFF1A4D2E)
val TextFieldFocusedBorderColor = PrimaryGreen
val TextFieldUnfocusedBorderColor = PrimaryGreen.copy(alpha = 0.7f)
val TextFieldTextColor = PrimaryGreen
val DropdownMenuBackgroundColor = Color.White
val DropdownMenuItemTextColor = PrimaryGreen

// --- Top Level Composable ---
@Composable
fun EditRecipeScreen(
    navController: NavHostController,
    recipeId: String,
    detailViewModel: RecipeDetailViewModel = viewModel(), // Inject Detail VM
    recipeViewModel: RecipeViewModel = viewModel()      // Inject Recipe VM for saving
) {
    // Fetch recipe details when the screen launches or recipeId changes
    LaunchedEffect(recipeId) {
        detailViewModel.fetchRecipeById(recipeId)
    }

    val detailState by detailViewModel.recipeDetailState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle Loading/Error/Success states for fetching the recipe
    when (val state = detailState) {
        is RecipeDetailState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
                Text("Loading Recipe...", modifier = Modifier.padding(top = 60.dp), fontFamily = monte)
            }
        }
        is RecipeDetailState.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "Error loading recipe: ${state.message}\nTap to retry.",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    fontFamily = monte,
                    modifier = Modifier.clickable { detailViewModel.fetchRecipeById(recipeId) }
                )
            }
        }
        is RecipeDetailState.Success -> {
            // Check if the current user is the owner before showing edit content
            if (!state.isOwner) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "You do not have permission to edit this recipe.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontFamily = monte
                    )
                }
                // Navigate back automatically after a delay
                LaunchedEffect(Unit) {
                    delay(3000) // Show message for 3 seconds
                    // Prevent navigating back if the user already navigated away manually
                    if (navController.currentBackStackEntry?.destination?.route?.startsWith("editRecipe") == true) {
                        navController.popBackStack()
                    }
                }
            } else {
                // Pass the loaded data to the content composable
                EditRecipeContent(
                    navController = navController,
                    initialRecipeDetail = state.recipeDetail, // Pass the loaded data
                    recipeViewModel = recipeViewModel
                )
            }
        }
        is RecipeDetailState.Idle -> {
            // Show loading indicator while idle before first fetch attempt
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        }
    }
}

// --- Content Composable (Holds the actual form UI) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditRecipeContent(
    navController: NavHostController,
    initialRecipeDetail: RecipeDetail, // Receive initial data
    recipeViewModel: RecipeViewModel
) {
    // --- State Variables - Initialized from initialRecipeDetail ---
    var recipeName by rememberSaveable(initialRecipeDetail.id) { mutableStateOf(initialRecipeDetail.title) }
    var selectedCuisine by rememberSaveable(initialRecipeDetail.id) { mutableStateOf(initialRecipeDetail.cuisine) }
    val cuisineOptions = listOf("Filipino", "Italian", "Chinese", "Mexican", "Indian", "Japanese", "Other")
    var isCuisineDropdownExpanded by remember { mutableStateOf(false) }

    // ★ Corrected: Declare categoryOptions BEFORE using it ★
    val categoryOptions = listOf("Main Dish", "Appetizer", "Dessert", "Side Dish", "Breakfast", "Snack", "Beverage")

    var selectedCategory by rememberSaveable(initialRecipeDetail.id) {
        mutableStateOf(
            // Try to find the category from the recipe's tags
            initialRecipeDetail.tags.intersect(categoryOptions.toSet()).firstOrNull()
            // If not found in tags, try matching the dedicated category field if it exists in RecipeDetail (adjust if needed)
            // ?: categoryOptions.find { it.equals(initialRecipeDetail.category, ignoreCase = true) }
            // Default to the first option if still not found
                ?: categoryOptions.first()
        )
    }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    var servings by rememberSaveable(initialRecipeDetail.id) { mutableStateOf(initialRecipeDetail.servingSize.toString()) }

    val initialPrepTime = parseTime(initialRecipeDetail.prepTime)
    var preparationHours by rememberSaveable(initialRecipeDetail.id) { mutableStateOf(initialPrepTime.first) }
    var preparationMinutes by rememberSaveable(initialRecipeDetail.id) { mutableStateOf(initialPrepTime.second) }

    val initialCookTime = parseTime(initialRecipeDetail.cookingTime)
    var cookingHours by rememberSaveable(initialRecipeDetail.id) { mutableStateOf(initialCookTime.first) }
    var cookingMinutes by rememberSaveable(initialRecipeDetail.id) { mutableStateOf(initialCookTime.second) }

    var personalNote by rememberSaveable(initialRecipeDetail.id) { mutableStateOf(initialRecipeDetail.personalNote ?: "") }

    val ingredients = remember {
        mutableStateListOf<IngredientInputItem>().apply {
            addAll(initialRecipeDetail.ingredients.map { IngredientInputItem(it.quantity, it.unit ?: "", it.name) })
            if (isEmpty()) add(IngredientInputItem()) // Ensure at least one item for editing
        }
    }

    val instructions = remember {
        mutableStateListOf<String>().apply {
            addAll(initialRecipeDetail.procedureSteps)
            if (isEmpty()) add("") // Ensure at least one item for editing
        }
    }

    val originalImageUrl by remember(initialRecipeDetail.id) { mutableStateOf(initialRecipeDetail.imageUrl) }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    val displayImageSource: Any? = newImageUri ?: originalImageUrl // Prioritize new URI for display

    var caloriesInput by rememberSaveable(initialRecipeDetail.id) { mutableStateOf(findNutritionValue(initialRecipeDetail.nutritionFacts, "Calories")) }
    var proteinInput by rememberSaveable(initialRecipeDetail.id) { mutableStateOf(findNutritionValue(initialRecipeDetail.nutritionFacts, "Protein")) }
    var fatInput by rememberSaveable(initialRecipeDetail.id) { mutableStateOf(findNutritionValue(initialRecipeDetail.nutritionFacts, "Fat")) }
    var carbsInput by rememberSaveable(initialRecipeDetail.id) { mutableStateOf(findNutritionValue(initialRecipeDetail.nutritionFacts, "Carbohydrates")) } // Or "Carbs"


    // --- Observe Modify State from RecipeViewModel ---
    val modifyState by recipeViewModel.recipeModifyState.collectAsStateWithLifecycle()

    // --- Other Setup ---
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> newImageUri = uri }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // --- Effects ---
    LaunchedEffect(modifyState) { // Reaction to Update Recipe State
        when (val state = modifyState) {
            is RecipeModifyState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show() // Short toast for success
                recipeViewModel.resetRecipeModifyState()
                // Navigate back to the detail screen implicitly by popping the stack
                navController.popBackStack()
            }
            is RecipeModifyState.Error -> {
                Toast.makeText(context, "Update Error: ${state.message}", Toast.LENGTH_LONG).show()
                recipeViewModel.resetRecipeModifyState() // Allow retry
            }
            else -> {} // Loading/Idle handled by button state
        }
    }

    // --- UI ---
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Recipe", fontFamily = monte, fontWeight = FontWeight.Bold, color = PrimaryGreen) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGreen) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // --- Recipe Name ---
            item {
                OutlinedTextField(
                    value = recipeName,
                    onValueChange = { recipeName = it },
                    label = { Text("Recipe Name", color = TextFieldTextColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte),
                    colors = recipeTextFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }
            // --- Image Input ---
            item {
                EditImageInputSection(
                    displaySource = displayImageSource,
                    onEditImageClick = { imagePickerLauncher.launch("image/*") }
                )
            }
            // --- Cuisine ---
            item {
                DropdownInput(
                    label = "Cuisine",
                    options = cuisineOptions,
                    selectedOption = selectedCuisine,
                    onOptionSelected = { selectedCuisine = it },
                    isExpanded = isCuisineDropdownExpanded,
                    onExpandedChange = { isCuisineDropdownExpanded = it }
                )
            }
            // --- Category ---
            item {
                DropdownInput(
                    label = "Category",
                    options = categoryOptions,
                    selectedOption = selectedCategory,
                    onOptionSelected = { selectedCategory = it },
                    isExpanded = isCategoryDropdownExpanded,
                    onExpandedChange = { isCategoryDropdownExpanded = it }
                )
            }
            // --- Servings ---
            item {
                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it.filter { it.isDigit() }.take(3) }, // Allow up to 3 digits
                    label = { Text("Servings (Person)", color = TextFieldTextColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte),
                    colors = recipeTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    singleLine = true
                )
            }
            // --- Time Inputs ---
            item {
                TimeInputRow(
                    prepHours = preparationHours,
                    onPrepHoursChange = { preparationHours = it },
                    prepMinutes = preparationMinutes,
                    onPrepMinutesChange = { preparationMinutes = it },
                    cookHours = cookingHours,
                    onCookHoursChange = { cookingHours = it },
                    cookMinutes = cookingMinutes,
                    onCookMinutesChange = { cookingMinutes = it }
                )
            }
            // --- Ingredients ---
            item {
                IngredientListInput(
                    title = "Ingredients",
                    items = ingredients,
                    onAddItem = { ingredients.add(IngredientInputItem()) },
                    onRemoveItem = { index -> if (ingredients.size > 1) ingredients.removeAt(index) }
                )
            }
            // --- Instructions ---
            item {
                DynamicListInput(
                    title = "Instructions",
                    items = instructions,
                    onItemChange = { index, value -> instructions[index] = value },
                    onAddItem = { instructions.add("") },
                    onRemoveItem = { index -> if (instructions.size > 1) instructions.removeAt(index) },
                    keyboardAction = ImeAction.Default // Allow multiline instructions
                )
            }
            // --- Personal Note ---
            item {
                OutlinedTextField(
                    value = personalNote,
                    onValueChange = { personalNote = it },
                    label = { Text("Personal Note (Optional)", color = TextFieldTextColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp), // Allow multiple lines
                    textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte),
                    colors = recipeTextFieldColors(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences) // Default keyboard
                )
            }
            // --- Nutrition ---
            item { // Nutritional Info Section
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text( "Nutritional Information (Optional)", style = MaterialTheme.typography.titleMedium, fontFamily = monte, color = PrimaryGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    // Use helper function to ensure label consistency
                    NutritionInputField(label = "Calories", hint= "(e.g., 350 kcal)", value = caloriesInput, onValueChange = {caloriesInput = it}, imeAction = ImeAction.Next)
                    Spacer(modifier = Modifier.height(8.dp))
                    NutritionInputField(label = "Protein", hint= "(e.g., 20g)", value = proteinInput, onValueChange = {proteinInput = it}, imeAction = ImeAction.Next)
                    Spacer(modifier = Modifier.height(8.dp))
                    NutritionInputField(label = "Fat", hint= "(e.g., 15g)", value = fatInput, onValueChange = {fatInput = it}, imeAction = ImeAction.Next)
                    Spacer(modifier = Modifier.height(8.dp))
                    NutritionInputField(label = "Carbohydrates", hint= "(e.g., 30g)", value = carbsInput, onValueChange = {carbsInput = it}, imeAction = ImeAction.Done, onDone = { focusManager.clearFocus() })
                }
            }

            // --- Update/Cancel Buttons ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            focusManager.clearFocus() // Hide keyboard
                            val prepTimeFormatted = formatTime(preparationHours, preparationMinutes)
                            val cookingTimeFormatted = formatTime(cookingHours, cookingMinutes)
                            // Filter out empty instructions/ingredients before saving
                            val finalInstructions = instructions.filter { it.isNotBlank() }
                            val ingredientsToSave = ingredients
                                .filter { it.name.isNotBlank() } // Ensure name is not blank
                                .map { uiItem ->
                                    IngredientInput(
                                        name = uiItem.name,
                                        quantity = uiItem.quantity, // Pass as String
                                        unit = uiItem.unit.takeIf { it.isNotEmpty() }
                                    )
                                }

                            // Call the update function in the ViewModel
                            recipeViewModel.updateRecipe(
                                context = context,
                                recipeId = initialRecipeDetail.id,
                                recipeName = recipeName,
                                newImageUri = newImageUri, // Pass the *new* URI (if any)
                                originalImageUrl = originalImageUrl, // Pass original URL for deletion check
                                selectedCuisine = selectedCuisine,
                                selectedCategory = selectedCategory, // Pass the selected category string
                                servings = servings, // Pass as String
                                prepTimeFormatted = prepTimeFormatted,
                                cookingTimeFormatted = cookingTimeFormatted,
                                finalIngredients = ingredientsToSave,
                                finalInstructions = finalInstructions,
                                personalNote = personalNote,
                                caloriesInput = caloriesInput,
                                proteinInput = proteinInput,
                                fatInput = fatInput,
                                carbsInput = carbsInput
                            )
                        },
                        enabled = modifyState != RecipeModifyState.Loading,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (modifyState == RecipeModifyState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Update Recipe", fontFamily = monte, color = Color.White)
                        }
                    }
                    OutlinedButton(
                        onClick = { navController.popBackStack() }, // Simply go back on cancel
                        enabled = modifyState != RecipeModifyState.Loading,
                        modifier = Modifier.weight(1f).height(48.dp),
                        border = BorderStroke(1.dp, PrimaryGreen),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
                    ) {
                        Text("Cancel", fontFamily = monte, color = PrimaryGreen)
                    }
                }
                // Add some bottom padding
                Spacer(modifier = Modifier.height(16.dp))
            }
        } // End LazyColumn
    } // End Scaffold
}


// --- Helper Functions & Composables ---

// Helper to find nutrition value from the list safely
fun findNutritionValue(facts: List<com.example.myapplication.data.NutritionItem>?, nutrientName: String): String {
    return facts?.find { it.nutrient.equals(nutrientName, ignoreCase = true) }?.amount ?: ""
}

// Helper for consistent Nutrition Input Fields
@Composable
fun NutritionInputField(
    label: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction,
    onDone: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("$label $hint", color = TextFieldTextColor.copy(alpha = 0.7f)) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte),
        colors = recipeTextFieldColors(),
        // Use Text keyboard generally, number might restrict symbols like 'g' or 'kcal'
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = imeAction),
        singleLine = true,
        keyboardActions = if (onDone != null) KeyboardActions(onDone = { onDone() }) else KeyboardActions.Default
    )
}


// Helper to parse time string "X hr Y min" back to Pair("X", "Y")
fun parseTime(timeString: String?): Pair<String, String> {
    if (timeString.isNullOrBlank()) return Pair("", "")
    var hours = ""
    var minutes = ""
    val parts = timeString.split(" ")
    try {
        var foundHours = false
        var foundMinutes = false
        parts.forEachIndexed { index, part ->
            if (!foundHours && part.equals("hr", ignoreCase = true) && index > 0) {
                hours = parts[index - 1].filter { it.isDigit() }
                foundHours = true
            } else if (!foundMinutes && part.equals("min", ignoreCase = true) && index > 0) {
                minutes = parts[index - 1].filter { it.isDigit() }
                foundMinutes = true
            }
        }
        // Handle cases like "30 min" or "1 hr" if only one part was found
        if (hours.isEmpty() && minutes.isEmpty() && parts.size >= 2) {
            if (parts[1].equals("hr", ignoreCase = true)) {
                hours = parts[0].filter { it.isDigit() }
            } else if (parts[1].equals("min", ignoreCase = true)) {
                minutes = parts[0].filter { it.isDigit() }
            }
        }
        // Handle cases like "30min" or "1hr" (no space) - less robust
        if (hours.isEmpty() && minutes.isEmpty() && parts.size == 1) {
            val part = parts[0]
            if (part.endsWith("hr", ignoreCase = true)) {
                hours = part.dropLast(2).filter { it.isDigit() }
            } else if (part.endsWith("min", ignoreCase = true)) {
                minutes = part.dropLast(3).filter { it.isDigit() }
            }
        }

    } catch (e: Exception) {
        // Log error or handle parsing failure
        println("Error parsing time string: $timeString - $e")
        return Pair("", "") // Return empty on error
    }
    return Pair(hours, minutes)
}


// Adapt ImageInputSection to handle String URL or Uri for Edit
@Composable
fun EditImageInputSection(displaySource: Any?, onEditImageClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .border(1.dp, PrimaryGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clickable(onClick = onEditImageClick), // Allow clicking to change
            contentAlignment = Alignment.Center
        ) {
            if (displaySource != null && displaySource.toString().isNotEmpty()) { // Check not null or empty string
                // Use AsyncImage for both URL (String) and URI
                AsyncImage(
                    model = displaySource, // Can be String URL or Uri
                    contentDescription = "Recipe Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.add), // Use a generic placeholder
                    error = painterResource(R.drawable.broken_image) // Use a broken image indicator
                )
            } else {
                // Placeholder if no image exists and none selected yet
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.add), // Use a more specific icon if available
                        contentDescription = "Add/Edit Image Placeholder",
                        tint = PrimaryGreen.copy(alpha = 0.8f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Change Recipe Photo", color = PrimaryGreen.copy(alpha = 0.9f), fontFamily = monte)
                }
            }
        }
    }
}

// --- Include or Import other necessary helpers ---
// Make sure DropdownInput, TimeInputRow, TimeInputUnit, IngredientListInput,
// DynamicListInput, recipeTextFieldColors, formatTime are accessible.
// Example (if they are identical to add screen and imported):
// import com.example.myapplication.front_end.recipe.add.DropdownInput
// ... etc ...


// --- Preview ---
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun EditRecipeScreenPreview() {
    // Dummy data for previewing EditRecipeContent
    val previewRecipe = RecipeDetail(
        id = "previewEdit1",
        title = "Editable Fluffy Pancakes with Berries",
        imageUrl = null, // Test placeholder display
        cuisine = "Breakfast",
        tags = listOf("Sweet", "Breakfast", "Quick"),
        isFavorite = false,
        isBookmarked = false,
        servingSize = 2,
        cookingTime = "15 min",
        prepTime = "10 min",
        ingredients = listOf(
            com.example.myapplication.data.IngredientItem("1.5", "cup", "All-purpose Flour"),
            com.example.myapplication.data.IngredientItem("2", "tsp", "Baking Powder"),
            com.example.myapplication.data.IngredientItem("1", "tbsp", "Sugar"),
            com.example.myapplication.data.IngredientItem("1", "", "Large Egg"),
            com.example.myapplication.data.IngredientItem("1", "cup", "Milk"),
            com.example.myapplication.data.IngredientItem("2", "tbsp", "Melted Butter")
        ),
        procedureSteps = listOf(
            "Whisk dry ingredients (flour, baking powder, sugar, salt).",
            "In another bowl, whisk egg and milk.",
            "Pour wet into dry, add melted butter, mix until just combined (lumps okay).",
            "Heat lightly oiled griddle or pan over medium heat.",
            "Pour or scoop batter onto griddle.",
            "Cook until bubbles form, flip, cook until golden.",
            "Serve immediately with desired toppings."
        ),
        nutritionFacts = listOf(
            com.example.myapplication.data.NutritionItem("Calories", "310 kcal", null),
            com.example.myapplication.data.NutritionItem("Protein", "8g", null),
            com.example.myapplication.data.NutritionItem("Fat", "12g", null),
            com.example.myapplication.data.NutritionItem("Carbohydrates", "40g", null)
        ),
        authorName = "Preview Chef",
        personalNote = "Make sure not to overmix the batter for fluffy pancakes!",
        userRating = 0,
        authorId = "chef123",
        ratingCount = 15,
        averageRating = 4.2
    )

    MyApplicationTheme {
        // Preview the content part directly
        EditRecipeContent(
            navController = NavHostController(LocalContext.current), // Mock NavController
            initialRecipeDetail = previewRecipe,
            recipeViewModel = viewModel() // Provide a mock or real ViewModel for preview
        )
    }
}