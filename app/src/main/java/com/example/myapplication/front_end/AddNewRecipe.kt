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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.R
import com.example.yourapp.Screen

@Preview
@Composable
fun NewRecipeScreen() {
    val primaryColor = Color(0xFF1B5E20) // Replace with your actual color
    var recipeName by remember { mutableStateOf("") }
    var selectedCuisine by remember { mutableStateOf("") }
    val cuisineOptions = listOf("Filipino", "Italian", "Chinese", "Mexican", "Indian", "Other")
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
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) } // For image picking (requires more setup)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryColor.copy(alpha = 0.8f)) // Apply background color
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Distribute space
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(onClick = { }) {
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
                    label = { Text("Cuisine", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCuisineDropdownExpanded = true }, // Use clickable here
                    readOnly = true,
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White
                    ),
                    trailingIcon = {
                        Icon(Icons.Filled.ArrowDropDown, "Dropdown", tint = Color.White)
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
                            Text(option) // Correctly using Text as a composable
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Column {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = { /* Do nothing, it's a dropdown */ },
                    label = { Text("Category", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCategoryDropdownExpanded = true }, // Use clickable here
                    readOnly = true,
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White
                    ),
                    trailingIcon = {
                        Icon(Icons.Filled.ArrowDropDown, "Dropdown", tint = Color.White)
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
                            Text(option, color = primaryColor)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            OutlinedTextField(
                value = servings,
                onValueChange = { servings = it.filter { it.isDigit() } },
                label = { Text("Servings", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.White),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = preparationHours,
                    onValueChange = {
                        if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                            preparationHours = it
                        }
                    },
                    label = { Text("HH", color = Color.White) },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White
                    )
                )
                Text(":", color = Color.White, fontSize = 18.sp)
                OutlinedTextField(
                    value = preparationMinutes,
                    onValueChange = {
                        if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                            preparationMinutes = it
                        }
                    },
                    label = { Text("MM", color = Color.White) },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = cookingTime,
                    onValueChange = { it.filter { char -> char.isDigit() }.let { cookingTime = it } },
                    label = { Text("Cooking Time (mins)", color = Color.White) },
                    modifier = Modifier.weight(2f),
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Ingredients", color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp), // Adjust height as needed
                textStyle = TextStyle(color = Color.White),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions", color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp), // Adjust height as needed
                textStyle = TextStyle(color = Color.White),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Column {
                OutlinedTextField(
                    value = selectedCollection,
                    onValueChange = { /* Do nothing, it's a dropdown */ },
                    label = { Text("Save to Collection", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCollectionDropdownExpanded = true }, // Use clickable here
                    readOnly = true,
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White
                    ),
                    trailingIcon = {
                        Icon(Icons.Filled.ArrowDropDown, "Dropdown", tint = Color.White)
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
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save")
                }
                OutlinedButton(
                    onClick = {
                        // Implement cancel logic here
                        println("Cancel clicked")
                        // You might want to navigate back or clear the form
                    },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color.White), // Use BorderStroke here
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}