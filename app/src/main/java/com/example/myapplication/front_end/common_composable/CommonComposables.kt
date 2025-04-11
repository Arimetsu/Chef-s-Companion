package com.example.myapplication.front_end.common_composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Define or import these colors here if they aren't defined globally
val TagBackgroundColor = Color.White // Example
val TagBorderColor = Color(0xFFBDBDBD) // Example

@Composable
fun RecipeTag(text: String) {
    val tagShape = RoundedCornerShape(50)

    Box(
        modifier = Modifier
            .clip(tagShape)
            .background(TagBackgroundColor) // Make sure this color is defined/accessible
            .border(BorderStroke(1.dp, TagBorderColor), tagShape) // Make sure this color is defined/accessible
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = LocalContentColor.current
        )
    }
}

// Add any other common composables here...