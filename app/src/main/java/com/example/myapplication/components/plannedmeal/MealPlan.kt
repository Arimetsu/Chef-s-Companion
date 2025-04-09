package ui.components.plannedmeal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme

// Data class to represent a meal item
data class MealItem(
    val name: String,
    val cuisineType: String,
    val mealType: String,
    val servings: Int,
    val prepTime: String,
    val cookingTime: String
)

@Composable
fun MealPlanContent() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .padding(end = 16.dp)
    ) {
        // Example data
        val breakfastMeals = listOf(
            MealItem("Canned Tuna Pasta", "Filipino", "Breakfast", 1, "1 hr : 59 mins", "1 hr : 59 mins"),
            MealItem("Canned Tuna Pasta", "Filipino", "Breakfast", 1, "1 hr : 59 mins", "1 hr : 59 mins")
        )

        val lunchMeals = listOf(
            MealItem("Grilled Chicken", "Italian", "Lunch", 1, "30 mins", "45 mins")
        )

        val dinnerMeals = listOf(
            MealItem("Beef Stir Fry", "Chinese", "Dinner", 1, "20 mins", "15 mins")
        )

        HorizontalDivider(
            modifier = Modifier.padding(bottom = 10.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.secondary
        )

        // Breakfast section
        MealTypeSection(
            mealType = "Breakfast",
            iconResource = R.drawable.breakfast_vector,
            dishes = breakfastMeals.size.toString(),
            nutritionInfo = NutritionInfo(100, 50, 50, 50),
            meals = breakfastMeals
        )

        HorizontalDivider(
            modifier = Modifier
                .padding(bottom = 10.dp)
                .padding(top = 5.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.secondary
        )

        // Lunch section
        MealTypeSection(
            mealType = "Lunch",
            iconResource = R.drawable.lunch_vector,
            dishes = lunchMeals.size.toString(),
            nutritionInfo = NutritionInfo(100, 50, 50, 50),
            meals = lunchMeals
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.secondary
        )

        // Dinner section
        MealTypeSection(
            mealType = "Dinner",
            iconResource = R.drawable.dinner_vector,
            dishes = dinnerMeals.size.toString(),
            nutritionInfo = NutritionInfo(100, 50, 50, 50),
            meals = dinnerMeals
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

data class NutritionInfo(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int
)

@Composable
fun MealTypeSection(
    mealType: String,
    iconResource: Int,
    dishes: String,
    nutritionInfo: NutritionInfo,
    meals: List<MealItem>
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotationDegree by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f
    )
    MyApplicationTheme {
        Column {
            // Header section that can be clicked to expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon and meal type
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Replace with your actual icon resource
                    Image(
                        painter = painterResource(id = iconResource),
                        contentDescription = mealType,
                        modifier = Modifier.size(40.dp),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = mealType,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Dishes: $dishes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Nutrition information
                Row(
                    modifier = Modifier.weight(1.5f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Box(modifier = Modifier
                        .height(40.dp) // Fixed height
                        .width(1.dp)) {
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            thickness = 0.5.dp
                        )
                    }
                    NutritionItem("Calories", "${nutritionInfo.calories} kcal")
                    Box(modifier = Modifier
                        .height(40.dp) // Fixed height
                        .width(1.dp)) {
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            thickness = 0.5.dp
                        )
                    }
                    NutritionItem("Protein", "${nutritionInfo.protein} g")
                    Box(modifier = Modifier
                        .height(40.dp) // Fixed height
                        .width(1.dp)) {
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            thickness = 0.5.dp
                        )
                    }
                    NutritionItem("Carbs", "${nutritionInfo.carbs} g")
                    Box(modifier = Modifier
                        .height(40.dp) // Fixed height
                        .width(1.dp)) {
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            thickness = 0.5.dp
                        )
                    }
                    NutritionItem("Fats", "${nutritionInfo.fats} g")
                }

                // Expand/collapse icon
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .padding(8.dp)
                        .rotate(arrowRotationDegree),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Expandable content
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    meals.forEach { meal ->
                        MealItemCard(meal = meal)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Add button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { /* Handle add meal */ },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color(0xFF19602A), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add meal",
                                tint = Color(0xFF19602A)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun MealItemCard(meal: MealItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE6F0E6) // Light green background
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Meal name
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Close button
                IconButton(
                    onClick = { /* Handle close/delete */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove meal",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tags row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cuisine tag
                TagChip(text = meal.cuisineType)

                Spacer(modifier = Modifier.width(8.dp))

                // Meal type tag
                TagChip(text = meal.mealType)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time and servings info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    label = "Serving",
                    value = meal.servings.toString(),
                    modifier = Modifier.weight(1f)
                )

                InfoItem(
                    label = "Preparation Time",
                    value = meal.prepTime,
                    modifier = Modifier.weight(2f)
                )

                InfoItem(
                    label = "Cooking Time",
                    value = meal.cookingTime,
                    modifier = Modifier.weight(2f)
                )
            }
        }
    }
}

@Composable
fun TagChip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFFD9E6D9))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
