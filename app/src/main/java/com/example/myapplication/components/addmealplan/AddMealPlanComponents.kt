package com.example.myapplication.components.addmealplan

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.Typography
import com.example.myapplication.ui.theme.latoFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeSearchBar(
    value: String,                   // Controlled value from parent
    onValueChange: (String) -> Unit, // Update callback
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,  // Use the value from parent, not local state
        onValueChange = { newValue ->
            onValueChange(newValue) // Propagate changes to parent
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 2.dp),
        placeholder = {
            Text(
                text = "Search for a recipe",
                style = Typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") } // Clear search
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        textStyle = TextStyle(
            fontSize = 16.sp,
            fontFamily = latoFont
        )
    )
}
