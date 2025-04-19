package com.example.myapplication.front_end.recipe.add

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
import androidx.compose.material.icons.filled.Add
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
import com.example.myapplication.data.UserCollection // Import UserCollection
import com.example.myapplication.front_end.home.monte
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewModel.IngredientInput
import com.example.myapplication.viewModel.RecipeViewModel
import com.example.myapplication.viewModel.SavedRecipesViewModel // Import SavedRecipesViewModel
import com.example.myapplication.viewModel.UserCollectionState // Import State

val PrimaryGreen = Color(0xFF1A4D2E)
val TextFieldFocusedBorderColor = PrimaryGreen
val TextFieldUnfocusedBorderColor = PrimaryGreen.copy(alpha = 0.7f)
val TextFieldTextColor = PrimaryGreen
val DropdownMenuBackgroundColor = Color.White
val DropdownMenuItemTextColor = PrimaryGreen

data class IngredientInputItem(
    var quantity: String = "",
    var unit: String = "",
    var name: String = ""
)

val commonUnits = listOf(
    "", "g", "kg", "mg", "ml", "l", "tsp", "tbsp",
    "cup", "oz", "lb", "pinch", "dash", "pcs"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRecipeScreen(
    navController: NavHostController,
    recipeViewModel: RecipeViewModel = viewModel(),
    savedRecipesViewModel: SavedRecipesViewModel = viewModel() // Inject SavedRecipesViewModel
) {
    // --- Existing State Variables ---
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
    val ingredients = remember { mutableStateListOf(IngredientInputItem()) }
    val instructions = remember { mutableStateListOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var caloriesInput by rememberSaveable { mutableStateOf("") }
    var proteinInput by rememberSaveable { mutableStateOf("") }
    var fatInput by rememberSaveable { mutableStateOf("") }
    var carbsInput by rememberSaveable { mutableStateOf("") }

    // --- Collection Dropdown State ---
    var selectedCollectionId by rememberSaveable { mutableStateOf<String?>(null) } // Store the ID, null means "None"
    var selectedCollectionName by rememberSaveable { mutableStateOf("None") } // Store the Name for display
    var isCollectionDropdownExpanded by remember { mutableStateOf(false) }

    // --- Observe ViewModel States ---
    val saveState by recipeViewModel.recipeSaveState.collectAsStateWithLifecycle()
    val collectionState by savedRecipesViewModel.userCollectionsState.collectAsStateWithLifecycle() // Observe collection state

    // --- Other Setup ---
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // --- Effects ---
    LaunchedEffect(saveState) { // Reaction to Save Recipe State
        when (val state = saveState) {
            is RecipeViewModel.RecipeSaveState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                recipeViewModel.resetRecipeSaveState()
                navController.popBackStack() // Go back after saving
            }
            is RecipeViewModel.RecipeSaveState.Error -> {
                Toast.makeText(context, "Save Error: ${state.message}", Toast.LENGTH_LONG).show()
                recipeViewModel.resetRecipeSaveState()
            }
            else -> {} // Loading/Idle handled by button state
        }
    }

    // --- UI ---
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Recipe", fontFamily = monte, fontWeight = FontWeight.Bold, color = PrimaryGreen) },
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
            // --- Recipe Name, Image, Cuisine, Category, Servings, Time, Ingredients, Instructions, Note, Nutrition ---
            // (Keep all these items as they were in the previous complete code)
            item { OutlinedTextField( value = recipeName, onValueChange = { recipeName = it }, label = { Text("Recipe Name", color = TextFieldTextColor.copy(alpha = 0.7f)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte), colors = recipeTextFieldColors(), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next) ) }
            item { ImageInputSection( imageUri = imageUri, onAddImageClick = { imagePickerLauncher.launch("image/*") } ) }
            item { DropdownInput( label = "Cuisine", options = cuisineOptions, selectedOption = selectedCuisine, onOptionSelected = { selectedCuisine = it }, isExpanded = isCuisineDropdownExpanded, onExpandedChange = { isCuisineDropdownExpanded = it } ) }
            item { DropdownInput( label = "Category", options = categoryOptions, selectedOption = selectedCategory, onOptionSelected = { selectedCategory = it }, isExpanded = isCategoryDropdownExpanded, onExpandedChange = { isCategoryDropdownExpanded = it } ) }
            item { OutlinedTextField( value = servings, onValueChange = { servings = it.filter { it.isDigit() }.take(3) }, label = { Text("Servings (Person)", color = TextFieldTextColor.copy(alpha = 0.7f)) }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte), colors = recipeTextFieldColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), singleLine = true ) }
            item { TimeInputRow( prepHours = preparationHours, onPrepHoursChange = { preparationHours = it }, prepMinutes = preparationMinutes, onPrepMinutesChange = { preparationMinutes = it }, cookHours = cookingHours, onCookHoursChange = { cookingHours = it }, cookMinutes = cookingMinutes, onCookMinutesChange = { cookingMinutes = it } ) }
            item { IngredientListInput( title = "Ingredients", items = ingredients, onAddItem = { ingredients.add(IngredientInputItem()) }, onRemoveItem = { index -> if (ingredients.size > 1) ingredients.removeAt(index) } ) }
            item { DynamicListInput( title = "Instructions", items = instructions, onItemChange = { index, value -> instructions[index] = value }, onAddItem = { instructions.add("") }, onRemoveItem = { index -> if (instructions.size > 1) instructions.removeAt(index) }, keyboardAction = ImeAction.Default ) }
            item { OutlinedTextField( value = personalNote, onValueChange = { personalNote = it }, label = { Text("Personal Note (Optional)", color = TextFieldTextColor.copy(alpha = 0.7f)) }, modifier = Modifier.fillMaxWidth().height(120.dp), textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte), colors = recipeTextFieldColors() ) }
            item { // Nutritional Info Section
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text( "Nutritional Information (Optional)", style = MaterialTheme.typography.titleMedium, fontFamily = monte, color = PrimaryGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField( value = caloriesInput, onValueChange = { caloriesInput = it }, label = { Text("Calories (e.g., 350 kcal)", color = TextFieldTextColor.copy(alpha = 0.7f)) }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte), colors = recipeTextFieldColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), singleLine = true )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField( value = proteinInput, onValueChange = { proteinInput = it }, label = { Text("Protein (e.g., 20g)", color = TextFieldTextColor.copy(alpha = 0.7f)) }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte), colors = recipeTextFieldColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next), singleLine = true )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField( value = fatInput, onValueChange = { fatInput = it }, label = { Text("Fat (e.g., 15g)", color = TextFieldTextColor.copy(alpha = 0.7f)) }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte), colors = recipeTextFieldColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next), singleLine = true )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField( value = carbsInput, onValueChange = { carbsInput = it }, label = { Text("Carbohydrates (e.g., 30g)", color = TextFieldTextColor.copy(alpha = 0.7f)) }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte), colors = recipeTextFieldColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done), singleLine = true, keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }) )
                }
            }


            // --- ★★★ Collection Dropdown (Dynamic) ★★★ ---
            item {
                var collectionNameOptions by remember { mutableStateOf(listOf("None")) }
                var collectionIdMap by remember { mutableStateOf(mapOf("None" to null as String?)) } // Map Name to ID

                // Update options when collectionState changes
                LaunchedEffect(collectionState) {
                    if (collectionState is UserCollectionState.Success) {
                        val collections = (collectionState as UserCollectionState.Success).collections
                        collectionNameOptions = listOf("None") + collections.map { it.name }
                        collectionIdMap = mapOf("None" to null) + collections.associateBy({ it.name }, { it.id })

                        // Ensure selected name matches selected ID after options load/change
                        selectedCollectionName = collectionIdMap.entries.find { it.value == selectedCollectionId }?.key ?: "None"
                    }
                }

                // The DropdownInput composable itself
                DropdownInput(
                    label = "Save to Collection (Optional)",
                    options = collectionNameOptions, // Pass dynamic names
                    selectedOption = selectedCollectionName, // Show the selected name
                    onOptionSelected = { selectedName ->
                        // Update both name and ID based on selection
                        selectedCollectionName = selectedName
                        selectedCollectionId = collectionIdMap[selectedName] // Look up ID from map
                    },
                    isExpanded = isCollectionDropdownExpanded,
                    onExpandedChange = { isCollectionDropdownExpanded = it },
                    // Optional: Disable dropdown while collections are loading
                    enabled = collectionState !is UserCollectionState.Loading
                )
                // Optional: Show loading or error state for collections
                if(collectionState is UserCollectionState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp).padding(start = 8.dp))
                } else if (collectionState is UserCollectionState.Error) {
                    Text("Failed to load collections", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }
            // --- ★★★ End Collection Dropdown ★★★ ---


            // --- Save/Cancel Buttons ---
            item {
                Row( modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val prepTimeFormatted = formatTime(preparationHours, preparationMinutes)
                            val cookingTimeFormatted = formatTime(cookingHours, cookingMinutes)
                            val finalInstructions = instructions.filter { it.isNotBlank() }
                            val ingredientsToSave = ingredients
                                .filter { it.name.isNotBlank() }
                                .map { uiItem -> IngredientInput( name = uiItem.name, quantity = uiItem.quantity, unit = uiItem.unit.takeIf { it.isNotEmpty() } ) }

                            // ★ Pass the selectedCollectionId (String?) to the ViewModel ★
                            recipeViewModel.saveNewRecipe(
                                context = context,
                                recipeName = recipeName,
                                imageUri = imageUri,
                                selectedCuisine = selectedCuisine,
                                selectedCategory = selectedCategory,
                                servings = servings,
                                prepTimeFormatted = prepTimeFormatted,
                                cookingTimeFormatted = cookingTimeFormatted,
                                finalIngredients = ingredientsToSave,
                                finalInstructions = finalInstructions,
                                personalNote = personalNote,
                                // Pass the ID, which can be null if "None" is selected
                                selectedCollectionId = selectedCollectionId,
                                caloriesInput = caloriesInput,
                                proteinInput = proteinInput,
                                fatInput = fatInput,
                                carbsInput = carbsInput
                            )
                        },
                        enabled = saveState != RecipeViewModel.RecipeSaveState.Loading,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (saveState == RecipeViewModel.RecipeSaveState.Loading) { CircularProgressIndicator( modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp ) }
                        else { Text("Save Recipe", fontFamily = monte, color = Color.White) }
                    }
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        enabled = saveState != RecipeViewModel.RecipeSaveState.Loading,
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

// --- Helper Composables (ImageInputSection, DropdownInput, TimeInputRow, TimeInputUnit, IngredientListInput, DynamicListInput, recipeTextFieldColors, formatTime) ---
// (Keep these exactly as they were in the previous complete code block, unless DropdownInput needs 'enabled' parameter)

@Composable
fun ImageInputSection(imageUri: Uri?, onAddImageClick: () -> Unit) { /* ... implementation ... */
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box( modifier = Modifier.size(200.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray.copy(alpha = 0.3f)).border(1.dp, PrimaryGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).clickable(onClick = onAddImageClick), contentAlignment = Alignment.Center ) {
            if (imageUri != null) { AsyncImage( model = imageUri, contentDescription = "Selected Recipe Image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop ) }
            else { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon( painter = painterResource(R.drawable.add), contentDescription = "Add Image Placeholder", tint = PrimaryGreen, modifier = Modifier.size(48.dp) ); Spacer(modifier = Modifier.height(8.dp)); Text("Add Recipe Photo", color = PrimaryGreen, fontFamily = monte) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownInput( label: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit, isExpanded: Boolean, onExpandedChange: (Boolean) -> Unit, enabled: Boolean = true ) { // Add enabled parameter
    ExposedDropdownMenuBox( expanded = isExpanded, onExpandedChange = { if(enabled) onExpandedChange(it) } ) { // Only change expansion if enabled
        OutlinedTextField(
            value = selectedOption,
            onValueChange = { /* Read Only */ },
            label = { Text(label, color = if(enabled) TextFieldTextColor.copy(alpha = 0.7f) else Color.Gray) }, // Adjust label color when disabled
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            readOnly = true,
            enabled = enabled, // Pass enabled state
            textStyle = TextStyle(color = if(enabled) TextFieldTextColor else Color.Gray, fontFamily = monte), // Adjust text color when disabled
            colors = recipeTextFieldColors(), // Colors might need adjustment for disabled state too
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) }
        )
        ExposedDropdownMenu( expanded = isExpanded && enabled, onDismissRequest = { onExpandedChange(false) }, modifier = Modifier.background(DropdownMenuBackgroundColor) ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontFamily = monte, color = DropdownMenuItemTextColor) },
                    onClick = { onOptionSelected(option); onExpandedChange(false) }
                )
            }
        }
    }
}

@Composable
fun TimeInputRow( prepHours: String, onPrepHoursChange: (String) -> Unit, prepMinutes: String, onPrepMinutesChange: (String) -> Unit, cookHours: String, onCookHoursChange: (String) -> Unit, cookMinutes: String, onCookMinutesChange: (String) -> Unit ) { /* ... implementation ... */
    Column {
        Row( modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Preparation Time:", style = MaterialTheme.typography.labelMedium, color = PrimaryGreen, fontFamily = monte); Text("Cooking Time:", style = MaterialTheme.typography.labelMedium, color = PrimaryGreen, fontFamily = monte) }
        Spacer(modifier = Modifier.height(4.dp))
        Row( modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically ) { TimeInputUnit(value = prepHours, onValueChange = onPrepHoursChange, label = "HH", modifier = Modifier.weight(1f)); Text(":", color = PrimaryGreen, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 2.dp)); TimeInputUnit(value = prepMinutes, onValueChange = onPrepMinutesChange, label = "MM", modifier = Modifier.weight(1f)); Spacer(modifier = Modifier.width(16.dp)); TimeInputUnit(value = cookHours, onValueChange = onCookHoursChange, label = "HH", modifier = Modifier.weight(1f)); Text(":", color = PrimaryGreen, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 2.dp)); TimeInputUnit(value = cookMinutes, onValueChange = onCookMinutesChange, label = "MM", modifier = Modifier.weight(1f)) }
    }
}

@Composable
fun TimeInputUnit( value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier ) { /* ... implementation ... */
    OutlinedTextField( value = value, onValueChange = { newValue -> val filtered = newValue.filter { it.isDigit() }.take(2); val isValid = when (label) { "HH" -> filtered.toIntOrNull()?.let { it <= 24 } ?: (filtered.isEmpty()); "MM" -> filtered.toIntOrNull()?.let { it <= 59 } ?: (filtered.isEmpty()); else -> true }; if (isValid) { onValueChange(filtered) } }, label = { Text(label, color = TextFieldTextColor.copy(alpha = 0.7f)) }, modifier = modifier, textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte, textAlign = TextAlign.Center), colors = recipeTextFieldColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), singleLine = true, shape = RoundedCornerShape(8.dp) )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientListInput( title: String, items: MutableList<IngredientInputItem>, onAddItem: () -> Unit, onRemoveItem: (index: Int) -> Unit ) { /* ... implementation ... */
    var expandedDropdownIndex by remember { mutableStateOf<Int?>(null) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row( modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween ) { Text( text = title, style = MaterialTheme.typography.titleMedium, fontFamily = monte, color = PrimaryGreen, fontWeight = FontWeight.Bold ); IconButton(onClick = onAddItem, modifier = Modifier.size(32.dp)) { Icon( Icons.Default.Add, contentDescription = "Add $title", tint = PrimaryGreen ) } }
        Spacer(modifier = Modifier.height(8.dp))
        items.forEachIndexed { index, item ->
            Row( modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp) ) {
                Column( modifier = Modifier.weight(0.25f) ) { Text( text = "Qty", fontSize = 12.sp, color = TextFieldTextColor, fontFamily = monte, modifier = Modifier.padding(start = 8.dp, bottom = 2.dp) ); OutlinedTextField( value = item.quantity, onValueChange = { items[index] = item.copy(quantity = it) }, modifier = Modifier.fillMaxWidth().height(56.dp), textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte, fontSize = 12.sp), colors = recipeTextFieldColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), singleLine = true, shape = RoundedCornerShape(8.dp), label = null ) }
                Box(modifier = Modifier.weight(0.4f) ) { ExposedDropdownMenuBox( expanded = expandedDropdownIndex == index, onExpandedChange = { isExpanding -> expandedDropdownIndex = if (isExpanding) index else null }, ) { Column { Text( text = "Unit", fontSize = 12.sp, color = TextFieldTextColor, fontFamily = monte, modifier = Modifier.padding(start = 8.dp, bottom = 2.dp) ); OutlinedTextField( value = item.unit, onValueChange = { /* Read Only */ }, label = null, modifier = Modifier.fillMaxWidth().menuAnchor().height(56.dp), readOnly = true, textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte, fontSize = 12.sp), colors = recipeTextFieldColors().copy( unfocusedLabelColor = TextFieldTextColor.copy(alpha = 0.7f), focusedLabelColor = TextFieldFocusedBorderColor ), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdownIndex == index) }, shape = RoundedCornerShape(8.dp), singleLine = true ) }
                ExposedDropdownMenu( expanded = expandedDropdownIndex == index, onDismissRequest = { expandedDropdownIndex = null }, modifier = Modifier.background(DropdownMenuBackgroundColor) ) { commonUnits.forEach { unitOption -> DropdownMenuItem( text = { Text(unitOption.ifEmpty { "-" }, fontFamily = monte, color = DropdownMenuItemTextColor, fontSize = 12.sp) }, onClick = { items[index] = item.copy(unit = unitOption); expandedDropdownIndex = null } ) } } } }
                Column( modifier = Modifier.weight(0.45f) ) { Text( text = "Ingredient", fontSize = 12.sp, color = TextFieldTextColor, fontFamily = monte, modifier = Modifier.padding(start = 8.dp, bottom = 2.dp) ); OutlinedTextField( value = item.name, onValueChange = { items[index] = item.copy(name = it) }, label = null, modifier = Modifier.fillMaxWidth().height(56.dp), textStyle = TextStyle( color = TextFieldTextColor, fontFamily = monte, fontSize = 12.sp ), colors = recipeTextFieldColors().copy( unfocusedLabelColor = TextFieldTextColor.copy(alpha = 0.7f), focusedLabelColor = TextFieldFocusedBorderColor ), keyboardOptions = KeyboardOptions( capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next ), singleLine = true, shape = RoundedCornerShape(8.dp) ) }
                Column( modifier = Modifier.weight(0.2f), horizontalAlignment = Alignment.CenterHorizontally ) { Text(""); Box( modifier = Modifier.height(56.dp).width(48.dp), contentAlignment = Alignment.Center ) { if (items.size > 1) { IconButton( onClick = { onRemoveItem(index) }, modifier = Modifier.size(32.dp) ) { Icon( Icons.Default.Close, contentDescription = "Remove Ingredient", tint = Color.Gray ) } } else { Spacer(modifier = Modifier.width(40.dp)) } } }
            }
        }
    }
}

@Composable
fun DynamicListInput( title: String, items: MutableList<String>, onItemChange: (index: Int, value: String) -> Unit, onAddItem: () -> Unit, onRemoveItem: (index: Int) -> Unit, keyboardAction: ImeAction = ImeAction.Default ) { /* ... implementation ... */
    Column(modifier = Modifier.fillMaxWidth()) {
        Row( modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween ) { Text( text = title, style = MaterialTheme.typography.titleMedium, fontFamily = monte, color = PrimaryGreen, fontWeight = FontWeight.Bold ); IconButton(onClick = onAddItem, modifier = Modifier.size(32.dp)) { Icon( Icons.Default.Add, contentDescription = "Add $title", tint = PrimaryGreen ) } }
        Spacer(modifier = Modifier.height(8.dp))
        items.forEachIndexed { index, item ->
            Row( modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically ) { OutlinedTextField( value = item, onValueChange = { onItemChange(index, it) }, placeholder = { Text("${title.dropLast(1)} ${index + 1}", color = TextFieldTextColor.copy(alpha = 0.5f)) }, modifier = Modifier.weight(1f), textStyle = TextStyle(color = TextFieldTextColor, fontFamily = monte), colors = recipeTextFieldColors(), keyboardOptions = KeyboardOptions.Default.copy(imeAction = keyboardAction), keyboardActions = KeyboardActions( onNext = { /* Optional */ }, onDone = { /* Optional */ } ) ); if (items.size > 1) { IconButton(onClick = { onRemoveItem(index) }, modifier = Modifier.padding(start = 8.dp)) { Icon( Icons.Default.Close, contentDescription = "Remove ${title.dropLast(1)}", tint = Color.Gray ) } } else { Spacer(modifier = Modifier.width(48.dp)) } }
        }
    }
}

@Composable
fun recipeTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors( focusedBorderColor = TextFieldFocusedBorderColor, unfocusedBorderColor = TextFieldUnfocusedBorderColor, focusedLabelColor = TextFieldFocusedBorderColor, unfocusedLabelColor = TextFieldUnfocusedBorderColor, cursorColor = TextFieldFocusedBorderColor, focusedTextColor = TextFieldTextColor, unfocusedTextColor = TextFieldTextColor )
fun formatTime(hoursStr: String, minutesStr: String): String { val hours = hoursStr.toIntOrNull() ?: 0; val minutes = minutesStr.toIntOrNull() ?: 0; return when { hours > 0 && minutes > 0 -> "$hours hr $minutes min"; hours > 0 -> "$hours hr"; minutes > 0 -> "$minutes min"; else -> "" } }

@Preview(showBackground = true)
@Composable
fun NewRecipeScreenPreview() { MyApplicationTheme { NewRecipeScreen(navController = NavHostController(LocalContext.current)) } }