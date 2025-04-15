package com.example.myapplication.front_end.recipe.add

// --- Keep necessary imports ---
import android.net.Uri
import android.widget.Toast // Import Toast for validation feedback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.FirebaseRecipe
// Import the data class for Firebase
import com.example.myapplication.front_end.home.monte
import com.example.myapplication.ui.theme.MyApplicationTheme


// Define primary color for consistency
val PrimaryGreen = Color(0xFF1A4D2E) // Same as Color(26, 77, 46)
val TextFieldFocusedBorderColor = PrimaryGreen
val TextFieldUnfocusedBorderColor = PrimaryGreen.copy(alpha = 0.7f)
val TextFieldTextColor = PrimaryGreen
val DropdownMenuBackgroundColor = Color.White // Background for the dropdown list itself
val DropdownMenuItemTextColor = PrimaryGreen // Text color for items in the dropdown list


// --- Ingredient Input Item Data Class (Keep as defined previously) ---
data class IngredientInputItem(
    var quantity: String = "",
    var unit: String = "",
    var name: String = ""
)

val commonUnits = listOf(
    "", // Option for no unit (e.g., for items like "1 egg")
    "g", "kg", "mg",
    "ml", "l",
    "tsp", "tbsp",
    "cup", "oz", "lb",
    "pinch", "dash",
    "pcs", // Pieces
    // Add any other units you commonly use
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRecipeScreen(navController: NavHostController /* Add ViewModel parameter later */) {

    // --- State Variables ---
    var recipeName by rememberSaveable { mutableStateOf("") }
    var selectedCuisine by rememberSaveable { mutableStateOf("") }
    val cuisineOptions = listOf("Filipino", "Italian", "Chinese", "Mexican", "Indian", "Japanese", "Other")
    var isCuisineDropdownExpanded by remember { mutableStateOf(false) }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    val categoryOptions = listOf("Main Dish", "Appetizer", "Dessert", "Side Dish", "Breakfast", "Snack", "Beverage")
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var servings by rememberSaveable { mutableStateOf("") }
    var preparationHours by rememberSaveable { mutableStateOf("") }
    var preparationMinutes by rememberSaveable { mutableStateOf("") }
    var cookingHours by rememberSaveable { mutableStateOf("") }
    var cookingMinutes by rememberSaveable { mutableStateOf("") }
    var personalNote by rememberSaveable { mutableStateOf("") }
    var selectedCollection by rememberSaveable { mutableStateOf("") }
    val collectionOptions = listOf("Favorites", "To Try", "Quick Meals", "Family Recipes", "None") // Load dynamically later
    var isCollectionDropdownExpanded by remember { mutableStateOf(false) }
    val ingredients = remember { mutableStateListOf(IngredientInputItem()) }
    val instructions = remember { mutableStateListOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current // Get context for Toasts

    // State for validation errors (optional, for showing messages)
    var validationError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text( "New Recipe", fontFamily = monte, fontWeight = FontWeight.Bold, color = PrimaryGreen )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon( Icons.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGreen )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors( containerColor = Color.White )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Validation Error Display ---
            if (validationError != null) {
                item {
                    Text(
                        text = validationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // --- Recipe Name ---
            item {
                OutlinedTextField(
                    value = recipeName,
                    onValueChange = { recipeName = it; validationError = null }, // Clear error on change
                    label = { Text("Recipe Name", color = TextFieldTextColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte),
                    colors = recipeTextFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }

            // --- Image Section ---
            item {
                ImageInputSection(
                    imageUri = imageUri,
                    onAddImageClick = { imagePickerLauncher.launch("image/*"); validationError = null }
                )
            }

            // --- Cuisine Dropdown ---
            item {
                DropdownInput(
                    label = "Cuisine",
                    options = cuisineOptions,
                    selectedOption = selectedCuisine,
                    onOptionSelected = { selectedCuisine = it; validationError = null },
                    isExpanded = isCuisineDropdownExpanded,
                    onExpandedChange = { isCuisineDropdownExpanded = it }
                )
            }

            // --- Category Dropdown ---
            item {
                DropdownInput(
                    label = "Category",
                    options = categoryOptions,
                    selectedOption = selectedCategory,
                    onOptionSelected = { selectedCategory = it; validationError = null },
                    isExpanded = isCategoryDropdownExpanded,
                    onExpandedChange = { isCategoryDropdownExpanded = it }
                )
            }

            // --- Servings ---
            item {
                OutlinedTextField(
                    value = servings,
                    onValueChange = { newValue ->
                        servings = newValue.filter { it.isDigit() }.take(3)
                        validationError = null
                    },
                    label = { Text("Servings (Person)", color = TextFieldTextColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte),
                    colors = recipeTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    singleLine = true,
                )
            }

            // --- Time Inputs ---
            item {
                TimeInputRow(
                    prepHours = preparationHours, onPrepHoursChange = { preparationHours = it; validationError = null },
                    prepMinutes = preparationMinutes, onPrepMinutesChange = { preparationMinutes = it; validationError = null },
                    cookHours = cookingHours, onCookHoursChange = { cookingHours = it; validationError = null },
                    cookMinutes = cookingMinutes, onCookMinutesChange = { cookingMinutes = it; validationError = null }
                )
            }

            // --- Ingredients Section ---
            item {
                IngredientListInput(
                    title = "Ingredients",
                    items = ingredients,
                    onAddItem = { ingredients.add(IngredientInputItem()); validationError = null },
                    onRemoveItem = { index -> if (ingredients.size > 1) ingredients.removeAt(index); validationError = null }
                )
            }

            // --- Instructions Section ---
            item {
                DynamicListInput(
                    title = "Instructions",
                    items = instructions,
                    onItemChange = { index, value -> instructions[index] = value; validationError = null },
                    onAddItem = { instructions.add(""); validationError = null },
                    onRemoveItem = { index -> if (instructions.size > 1) instructions.removeAt(index); validationError = null },
                    keyboardAction = ImeAction.Default // Allow multi-line, remove specific action
                )
            }

            // --- Personal Note ---
            item {
                OutlinedTextField(
                    value = personalNote,
                    onValueChange = { personalNote = it },
                    label = { Text("Personal Note (Optional)", color = TextFieldTextColor.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte),
                    colors = recipeTextFieldColors()
                )
            }

            // --- Collection Dropdown ---
            item {
                DropdownInput(
                    label = "Save to Collection (Optional)",
                    options = collectionOptions,
                    selectedOption = selectedCollection,
                    onOptionSelected = { selectedCollection = it },
                    isExpanded = isCollectionDropdownExpanded,
                    onExpandedChange = { isCollectionDropdownExpanded = it }
                )
            }

            // --- Save/Cancel Buttons ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button( // SAVE BUTTON
                        onClick = {
                            focusManager.clearFocus()
                            validationError = null // Clear previous error

                            // --- Simple Validation ---
                            if (recipeName.isBlank()) {
                                validationError = "Recipe Name cannot be empty."
                                Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
                                return@Button // Stop execution
                            }
                            if (imageUri == null) {
                                validationError = "Please add a recipe photo."
                                Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedCuisine.isBlank()) {
                                validationError = "Please select a cuisine."
                                Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedCategory.isBlank()) {
                                validationError = "Please select a category."
                                Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (servings.isBlank() || servings.toIntOrNull() == null || servings.toInt() <= 0) {
                                validationError = "Please enter a valid number of servings."
                                Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val prepTimeFormatted = formatTime(preparationHours, preparationMinutes)
                            val cookingTimeFormatted = formatTime(cookingHours, cookingMinutes)
                            if (prepTimeFormatted.isBlank() && cookingTimeFormatted.isBlank()) {
                                validationError = "Please enter Preparation or Cooking Time."
                                Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val finalIngredients = ingredients.filter { it.name.isNotBlank() }
                            if (finalIngredients.isEmpty()) {
                                validationError = "Please add at least one ingredient."
                                Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val finalInstructions = instructions.filter { it.isNotBlank() }
                            if (finalInstructions.isEmpty()) {
                                validationError = "Please add at least one instruction step."
                                Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
                                return@Button
                            }


                            // --- If Validation Passes ---
                            println("Validation Passed! Preparing to save...")

                            // Format Ingredients for saving
                            val formattedIngredients = finalIngredients.map {
                                // Create a clean string, handling cases where quantity or unit might be empty
                                buildString {
                                    if (it.quantity.isNotBlank()) append("${it.quantity} ")
                                    if (it.unit.isNotBlank()) append("${it.unit} ")
                                    append(it.name)
                                }.trim()
                            }

                            // Placeholder for image upload and getting URL
                            // In real app, call ViewModel function here to upload imageUri
                            // val imageUrl = viewModel.uploadImageAndGetUrl(imageUri!!)
                            val placeholderImageUrl = "PLACEHOLDER_IMAGE_URL" // Replace after upload

                            // TODO: Get current user ID and Name from AuthViewModel
                            val currentUserId = "TEMP_USER_ID"
                            val currentUsername = "TEMP_USER_NAME"

                            // Construct the FirebaseRecipe object
                            val newRecipe = FirebaseRecipe(
                                name = recipeName.trim(),
                                imageUrl = placeholderImageUrl, // Use the actual URL after upload
                                cuisine = selectedCuisine,
                                category = selectedCategory,
                                servings = servings.toInt(), // Already validated it's an Int > 0
                                prepTime = prepTimeFormatted,
                                cookingTime = cookingTimeFormatted,
                                ingredients = formattedIngredients, // Use formatted list
                                instructions = finalInstructions,
                                personalNote = personalNote.trim(),
                                collectionId = if (selectedCollection == "None") null else selectedCollection, // Handle "None" case, potentially map name to ID
                                authorId = currentUserId,
                                authorName = currentUsername
                                // createdAt will be set by Firestore @ServerTimestamp
                            )

                            // TODO: Call ViewModel to save the 'newRecipe' object
                            println("Recipe Object to Save: $newRecipe")
                            // viewModel.saveRecipe(newRecipe)

                            // TODO: Navigate on successful save (e.g., back to list or detail)
                            Toast.makeText(context, "Recipe Saved (Placeholder)", Toast.LENGTH_LONG).show()
                            navController.popBackStack() // Go back for now


                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Recipe", fontFamily = monte, color = Color.White)
                    }
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        border = BorderStroke(1.dp, PrimaryGreen),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
                    ) {
                        Text("Cancel", fontFamily = monte, color = PrimaryGreen)
                    }
                }
            }
        } // End LazyColumn
    } // End Scaffold
}

// --- Helper Composables (Keep as defined previously) ---

@Composable
fun ImageInputSection(imageUri: Uri?, onAddImageClick: () -> Unit) {
    // ... (Keep implementation from previous answer) ...
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .border(1.dp, PrimaryGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clickable(onClick = onAddImageClick),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage( model = imageUri, contentDescription = "Selected Recipe Image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon( painter = painterResource(R.drawable.add), contentDescription = "Add Image Placeholder", tint = PrimaryGreen, modifier = Modifier.size(48.dp) )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Add Recipe Photo", color = PrimaryGreen, fontFamily = monte)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class) // Needed for ExposedDropdownMenuBox
@Composable
fun DropdownInput(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = { /* Read Only */ },
            label = { Text(label, color = TextFieldTextColor.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            readOnly = true,
            textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte),
            // Apply themed colors
            colors = recipeTextFieldColors(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            }
        )
        // Apply theme to the dropdown menu itself
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(DropdownMenuBackgroundColor) // Set background color
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontFamily = monte, color = DropdownMenuItemTextColor) }, // Themed text
                    onClick = {
                        onOptionSelected(option)
                        onExpandedChange(false)
                    },
                    // Apply theme to menu items if needed (e.g., content color)
                    // colors = MenuDefaults.itemColors(textColor = DropdownMenuItemTextColor)
                )
            }
        }
    }
}

@Composable
fun TimeInputRow( /* ... keep implementation ... */
                  prepHours: String, onPrepHoursChange: (String) -> Unit,
                  prepMinutes: String, onPrepMinutesChange: (String) -> Unit,
                  cookHours: String, onCookHoursChange: (String) -> Unit,
                  cookMinutes: String, onCookMinutesChange: (String) -> Unit
) {
    Column {
        Row( modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Preparation Time:", style = MaterialTheme.typography.labelMedium, color = PrimaryGreen, fontFamily = monte)
            Text("Cooking Time:", style = MaterialTheme.typography.labelMedium, color = PrimaryGreen, fontFamily = monte)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row( modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically ) {
            TimeInputUnit(value = prepHours, onValueChange = onPrepHoursChange, label = "HH", modifier = Modifier.weight(1f))
            Text(":", color = PrimaryGreen, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 2.dp))
            TimeInputUnit(value = prepMinutes, onValueChange = onPrepMinutesChange, label = "MM", modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            TimeInputUnit(value = cookHours, onValueChange = onCookHoursChange, label = "HH", modifier = Modifier.weight(1f))
            Text(":", color = PrimaryGreen, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 2.dp))
            TimeInputUnit(value = cookMinutes, onValueChange = onCookMinutesChange, label = "MM", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun TimeInputUnit( /* ... keep implementation ... */
                   value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() }.take(2)
            val isValid = when (label) {
                "HH" -> filtered.toIntOrNull()?.let { it <= 24 } ?: (filtered.isEmpty())
                "MM" -> filtered.toIntOrNull()?.let { it <= 59 } ?: (filtered.isEmpty())
                else -> true
            }
            if (isValid) { onValueChange(filtered) }
        },
        label = { Text(label, color = TextFieldTextColor.copy(alpha = 0.7f)) },
        modifier = modifier,
        textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte, textAlign = TextAlign.Center),
        colors = recipeTextFieldColors(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        singleLine = true,
        shape = RoundedCornerShape(8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientListInput(
    title: String,
    items: MutableList<IngredientInputItem>,
    onAddItem: () -> Unit,
    onRemoveItem: (index: Int) -> Unit
) {
    var expandedDropdownIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text( /* ... Title ... */
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = monte,
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onAddItem, modifier = Modifier.size(32.dp)) {
                Icon( Icons.Default.Add, contentDescription = "Add $title", tint = PrimaryGreen )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                // --- Back to CenterVertically now heights are fixed ---
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp) // Space between elements
            ) {
                // --- Quantity Field ---
                Column(
                    modifier = Modifier
                        .weight(0.25f)
                ) {
                    Text(
                        text = "Qty",
                        fontSize = 12.sp,
                        color = TextFieldTextColor,
                        fontFamily = monte,
                        modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                    )
                    OutlinedTextField(
                        value = item.quantity,
                        onValueChange = { items[index] = item.copy(quantity = it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte, fontSize = 12.sp),
                        colors = recipeTextFieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        label = null // Make sure label is null or removed
                    )
                }

                // --- Unit Dropdown ---
                Box(modifier = Modifier
                    // ---> Adjust Weight <---
                    .weight(0.4f) // Medium weight
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdownIndex == index,
                        onExpandedChange = { isExpanding ->
                            expandedDropdownIndex = if (isExpanding) index else null
                        },
                    ) {
                        Column(

                        ) {
                            Text(
                                text = "Unit",
                                fontSize = 12.sp,
                                color = TextFieldTextColor,
                                fontFamily = monte,
                                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                            )

                        OutlinedTextField(
                            value = item.unit,
                            onValueChange = { /* Read Only */ },
                            label = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                // ---> Fixed Height <---
                                .height(56.dp), // Standard height
                            readOnly = true,
                            textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte, fontSize = 12.sp),
                            colors = recipeTextFieldColors().copy(
                                unfocusedLabelColor = TextFieldTextColor.copy(alpha = 0.7f),
                                focusedLabelColor = TextFieldFocusedBorderColor
                            ),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdownIndex == index)
                            },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                            }

                        ExposedDropdownMenu(
                            expanded = expandedDropdownIndex == index,
                            onDismissRequest = { expandedDropdownIndex = null },
                            modifier = Modifier.background(DropdownMenuBackgroundColor)
                        ) {
                            commonUnits.forEach { unitOption ->
                                DropdownMenuItem( /* ... */
                                    text = { Text(unitOption.ifEmpty { "-" }, fontFamily = monte, color = DropdownMenuItemTextColor, fontSize = 12.sp) },
                                    onClick = {
                                        items[index] = item.copy(unit = unitOption)
                                        expandedDropdownIndex = null
                                    }
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(0.45f)
                ) {
                    // --- Ingredient Name Field ---
                    Text(
                        text = "Ingredient",
                        fontSize = 12.sp,
                        color = TextFieldTextColor,
                        fontFamily = monte,
                        modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                    )
                    OutlinedTextField(
                        value = item.name,
                        onValueChange = { items[index] = item.copy(name = it) },
                        label = null,
                        modifier = Modifier
                            // ---> Adjust Weight <---
                            .fillMaxWidth() // Largest weight
                            // ---> Fixed Height <---
                            .height(56.dp), // Standard height
                        textStyle = TextStyle(
                            color = TextFieldTextColor,
                            fontFamily = monte,
                            fontSize = 12.sp
                        ),
                        colors = recipeTextFieldColors().copy(
                            unfocusedLabelColor = TextFieldTextColor.copy(alpha = 0.7f),
                            focusedLabelColor = TextFieldFocusedBorderColor
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(0.2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("")
                    // --- Remove Button ---
                    Box(
                        modifier = Modifier
                            // ---> Fixed Height & Define Width <---
                            .height(56.dp)  // Match TextField height
                            .width(48.dp),  // Explicit width for the button area
                        contentAlignment = Alignment.Center // Center icon within the Box
                    ) {
                        if (items.size > 1) {
                            IconButton(
                                onClick = { onRemoveItem(index) },
                                modifier = Modifier.size(32.dp)
                            ) { // Slightly smaller icon button
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove Ingredient",
                                    tint = Color.Gray
                                )
                            }
                        } else {
                            // Spacer takes up the same defined width when button is hidden
                            Spacer(modifier = Modifier.width(40.dp))
                        }
                    }
                }
            } // End Row for single ingredient
        } // End forEachIndexed
    } // End Column for Ingredients section
}


@Composable
fun DynamicListInput( /* ... keep implementation ... */
                      title: String, items: MutableList<String>, onItemChange: (index: Int, value: String) -> Unit, onAddItem: () -> Unit, onRemoveItem: (index: Int) -> Unit, keyboardAction: ImeAction = ImeAction.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row( modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween ) {
            Text( text = title, style = MaterialTheme.typography.titleMedium, fontFamily = monte, color = PrimaryGreen, fontWeight = FontWeight.Bold )
            IconButton(onClick = onAddItem, modifier = Modifier.size(32.dp)) { Icon( Icons.Default.Add, contentDescription = "Add $title", tint = PrimaryGreen ) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        items.forEachIndexed { index, item ->
            Row( modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically ) {
                OutlinedTextField( value = item, onValueChange = { onItemChange(index, it) }, placeholder = { Text("${title.dropLast(1)} ${index + 1}", color = TextFieldTextColor.copy(alpha = 0.5f)) }, modifier = Modifier.weight(1f), textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte), colors = recipeTextFieldColors(), keyboardOptions = KeyboardOptions.Default.copy(imeAction = keyboardAction), keyboardActions = KeyboardActions( onNext = { /* Could move focus */ }, onDone = { /* Could dismiss keyboard */ } ) )
                if (items.size > 1) {
                    IconButton(onClick = { onRemoveItem(index) }, modifier = Modifier.padding(start = 8.dp)) { Icon( Icons.Default.Close, contentDescription = "Remove ${title.dropLast(1)}", tint = Color.Gray ) }
                } else { Spacer(modifier = Modifier.width(48.dp)) }
            }
        }
    }
}

@Composable
fun recipeTextFieldColors(): TextFieldColors = /* ... keep implementation ... */
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = TextFieldFocusedBorderColor,
        unfocusedBorderColor = TextFieldUnfocusedBorderColor,
        focusedLabelColor = TextFieldFocusedBorderColor, // Label color when focused
        unfocusedLabelColor = TextFieldUnfocusedBorderColor, // Label color when unfocused
        cursorColor = TextFieldFocusedBorderColor,
        focusedTextColor = TextFieldTextColor,
        unfocusedTextColor = TextFieldTextColor
    )

fun formatTime(hoursStr: String, minutesStr: String): String {
    val hours = hoursStr.toIntOrNull() ?: 0
    val minutes = minutesStr.toIntOrNull() ?: 0
    return when {
        hours > 0 && minutes > 0 -> "$hours hr $minutes min"
        hours > 0 -> "$hours hr"
        minutes > 0 -> "$minutes min"
        else -> "" // Return empty if no time specified
    }
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
fun NewRecipeScreenPreview() {
    MyApplicationTheme {
        // Provide a NavHostController instance for the preview
        NewRecipeScreen(navController = NavHostController(LocalContext.current))
    }
}