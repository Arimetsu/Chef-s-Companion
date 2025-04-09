package com.example.myapplication.front_end

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.example.myapplication.R

@Composable
fun NewRecipeScreen(navController: NavHostController) {
    val primaryColor = Color(0xFF1B5E20) // Replace with your actual color
    var recipeName by remember { mutableStateOf("") }
    var selectedCuisine by remember { mutableStateOf("") }
    val cuisineOptions = listOf("Filipino", "Italian", "Chinese", "Mexican", "Indian", "Japanese")
    var isCuisineDropdownExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    val categoryOptions = listOf("Main Dish", "Appetizer", "Dessert", "Side Dish", "Breakfast", "Snack")
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var servings by remember { mutableStateOf("") }
    var preparationHours by remember { mutableStateOf("") }
    var preparationMinutes by remember { mutableStateOf("") }
    var cookingTime by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var selectedCollection by remember { mutableStateOf("") }
    val collectionOptions = listOf("Favorites", "To Try", "Quick Meals", "Family Recipes") // Example collections
    var isCollectionDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize() // Apply background color
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Distribute space
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(onClick = {
                    navController.navigate("yourRecipes")
                }) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(26, 77, 46)
                    )
                }
                Box(
                    modifier = Modifier.weight(1f), // Allow the Box to take up available space
                    contentAlignment = Alignment.Center // Center the text within the Box
                ) {
                    // Title
                    Text(
                        text = "New Recipe",
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontFamily = monte,
                            fontWeight = FontWeight(700),
                            color = Color(0xFF1A4D2E),
                            )
                    )
                }
                Spacer(modifier = Modifier.width(48.dp)) // Add space for potential trailing icons or to balance the back button
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { // Use a Column to stack the label and the text field

                OutlinedTextField(
                    value = recipeName,
                    onValueChange = { recipeName = it },
                    modifier = Modifier.width(300.dp).height(50.dp), // Set your desired width here
                    textStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center), // Center text
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.Black,
                        cursorColor = Color.White,
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Recipe Name",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontFamily = monte,
                        fontWeight = FontWeight(400),
                        color = Color(0xFF1A4D2E),
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Logo here (you'll place your Image composable here)
                Text(text = "Image",
                        style = TextStyle(
                        fontSize = 15.sp,
                    fontFamily = monte,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF1A4D2E),

                    ))

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {  },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1A4D2E)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    elevation = ButtonDefaults.elevation(0.dp)
                ){
                    Text (text = "Add", style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = monte,
                        fontWeight = FontWeight(500),
                        color = Color(0xFFFFFFFF),

                        textAlign = TextAlign.Center,
                    ))
                }

            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // images
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.greenbackgroundlogo), // Replace with your actual drawable resource ID
                    contentDescription = "Picture",
                    modifier = Modifier
                        .size(250.dp) // Adjust the size as needed to match the screenshot
                        .background(Color(0xFF1B5E20)) // Dark green background (adjust if needed)
                        .clip(RoundedCornerShape(8.dp)) // Apply rounded corners like in the screenshot
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            Column {
                OutlinedTextField(
                    value = selectedCuisine,
                    onValueChange = { /* Do nothing, it's a dropdown */ },
                    label = { Text("Cuisine", color = Color(26, 77, 46)) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    readOnly = true,
                    textStyle = TextStyle(color = Color(26, 77, 46)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(26, 77, 46),
                        unfocusedBorderColor = Color(26, 77, 46).copy(alpha = 0.7f),
                        focusedLabelColor = Color(26, 77, 46),
                        unfocusedLabelColor = Color(26, 77, 46).copy(alpha = 0.7f),
                        cursorColor = Color(26, 77, 46)
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = { isCuisineDropdownExpanded = true }
                        ) {
                            Icon(Icons.Filled.ArrowDropDown, "Dropdown", tint = Color(26, 77, 46))
                        }
                    }
                )

                // Make sure the dropdown is below the OutlinedTextField
                DropdownMenu(
                    expanded = isCuisineDropdownExpanded,
                    onDismissRequest = { isCuisineDropdownExpanded = false }
                ) {
                    cuisineOptions.forEach { option ->
                        DropdownMenuItem(
                            onClick = {
                                selectedCuisine = option
                                isCuisineDropdownExpanded = false // Close the menu after selection
                            }
                        ) {
                            Text(option,
                                fontFamily = monte,
                                color = Color(26, 77, 46)) // Correctly using Text as a composable
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Column {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { /* Do nothing, it's a dropdown */ },
                        label = { Text("Category", color = Color(26, 77, 46)) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        readOnly = true,
                        textStyle = TextStyle(color = Color(26, 77, 46)),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(26, 77, 46),
                            unfocusedBorderColor = Color(26, 77, 46).copy(alpha = 0.7f),
                            focusedLabelColor = Color(26, 77, 46),
                            unfocusedLabelColor = Color(26, 77, 46).copy(alpha = 0.7f),
                            cursorColor = Color(26, 77, 46)
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { isCategoryDropdownExpanded = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    tint = Color(26, 77, 46)
                                )
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categoryOptions.forEach { option ->
                            DropdownMenuItem(onClick = {
                                selectedCategory = option
                                isCategoryDropdownExpanded = false
                            }) {
                                Text(option, color = Color(26, 77, 46)) // Change color as needed
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            OutlinedTextField(
                value = servings,
                onValueChange = {newText ->
                    val filteredText = newText.filter { it.isDigit() }
                    if (filteredText.isNotBlank()) {
                        val intValue = filteredText.toInt()
                        if (intValue <= 100) {
                            servings = filteredText
                        }
                    } else {
                        servings = "" // Allow clearing the field
                    } },
                label = { Text("Servings (Person)", color = Color(26, 77, 46)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color(26, 77, 46)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(26, 77, 46),
                    unfocusedBorderColor = Color(26, 77, 46).copy(alpha = 0.7f),
                    focusedLabelColor = Color(26, 77, 46),
                    unfocusedLabelColor = Color(26, 77, 46).copy(alpha = 0.7f),
                    cursorColor = Color(26, 77, 46)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) // Set keyboard type to Number
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Column{
                Text (
                    text = "Cooking Time: ",
                    fontFamily = monte,
                    fontSize = 12.sp,
                    color = Color(26, 77, 46)
                )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = preparationHours,
                    onValueChange = { newText ->
                        val filteredText = newText.filter { it.isDigit() }
                        if (filteredText.length <= 2) {
                            if (filteredText.isNotBlank()) {
                                val intValue = filteredText.toInt()
                                if (intValue <= 24) { // Changed to 24 for hours (0-24)
                                    preparationHours = filteredText
                                }
                            } else {
                                preparationHours = "" // Allow clearing
                            }
                        }
                    },
                    label = { Text("HH", color = Color(26, 77, 46)) },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = Color(26, 77, 46)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(26, 77, 46),
                        unfocusedBorderColor = Color(26, 77, 46).copy(alpha = 0.7f),
                        focusedLabelColor = Color(26, 77, 46),
                        unfocusedLabelColor = Color(26, 77, 46).copy(alpha = 0.7f),
                        cursorColor = Color(26, 77, 46)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(":", color = Color(26, 77, 46), fontSize = 18.sp)
                OutlinedTextField(
                    value = preparationMinutes,
                    onValueChange = { newText ->
                        val filteredText = newText.filter { it.isDigit() }
                        if (filteredText.length <= 2) {
                            if (filteredText.isNotBlank()) {
                                val intValue = filteredText.toInt()
                                if (intValue <= 59) { // Changed to 59 for minutes (0-59)
                                    preparationMinutes = filteredText
                                }
                            } else {
                                preparationMinutes = "" // Allow clearing
                            }
                        }
                    },
                    label = { Text("MM", color = Color(26, 77, 46)) },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = Color(26, 77, 46)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(26, 77, 46),
                        unfocusedBorderColor = Color(26, 77, 46).copy(alpha = 0.7f),
                        focusedLabelColor = Color(26, 77, 46),
                        unfocusedLabelColor = Color(26, 77, 46).copy(alpha = 0.7f),
                        cursorColor = Color(26, 77, 46)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Ingredients", color = Color(26, 77, 46)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp), // Adjust height as needed
                textStyle = TextStyle(color = Color(26, 77, 46)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(26, 77, 46),
                    unfocusedBorderColor = Color(26, 77, 46).copy(alpha = 0.7f),
                    focusedLabelColor = Color(26, 77, 46),
                    unfocusedLabelColor = Color(26, 77, 46).copy(alpha = 0.7f),
                    cursorColor = Color(26, 77, 46)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions", color = Color(26, 77, 46)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp), // Adjust height as needed
                textStyle = TextStyle(color = Color(26, 77, 46)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(26, 77, 46),
                    unfocusedBorderColor = Color(26, 77, 46).copy(alpha = 0.7f),
                    focusedLabelColor = Color(26, 77, 46),
                    unfocusedLabelColor = Color(26, 77, 46).copy(alpha = 0.7f),
                    cursorColor = Color(26, 77, 46)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Column {
                OutlinedTextField(
                    value = selectedCollection,
                    onValueChange = { /* Do nothing, it's a dropdown */ },
                    label = { Text("Save to Collection", color = Color(26, 77, 46)) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    readOnly = true,
                    textStyle = TextStyle(color = Color(26, 77, 46)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(26, 77, 46),
                        unfocusedBorderColor = Color(26, 77, 46).copy(alpha = 0.7f),
                        focusedLabelColor = Color(26, 77, 46),
                        unfocusedLabelColor = Color(26, 77, 46).copy(alpha = 0.7f),
                        cursorColor = Color(26, 77, 46)
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = { isCollectionDropdownExpanded = true }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = Color(26, 77, 46)
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = isCollectionDropdownExpanded,
                    onDismissRequest = { isCollectionDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    collectionOptions.forEach { option ->
                        DropdownMenuItem(onClick = {
                            selectedCollection = option
                            isCollectionDropdownExpanded = false
                        }) {
                            Text(option, color = primaryColor)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        // Implement save logic here
                        println("Save clicked")
                        // You would typically collect all the state variables
                        // (recipeName, selectedCuisine, ingredients, etc.) here
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1A4D2E), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Save",
                        fontFamily = monte,
                        color = Color.White
                    )
                }
                OutlinedButton(
                    onClick = {
                        // Implement cancel logic here
                        // You might want to navigate back or clear the form
                        navController.navigate("yourRecipes")
                    },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color(26, 77, 46)), // Use BorderStroke here
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = monte,
                        color = Color(26, 77, 46)
                    )
                }
            }
        }
    }
}