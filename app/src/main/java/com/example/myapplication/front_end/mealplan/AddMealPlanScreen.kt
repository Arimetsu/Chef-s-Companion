package ui.screens.mealplan

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.components.datepicker.MonthCalendar
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.front_end.home.NavBar
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Typography
import ui.components.plannedmeal.MealPlanContent

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun addMealPlanScreen(navController: NavController, onAddMealPlanClick: () -> Unit, onAddMealsToMealPlanClick: (String) -> Unit = { mealType ->
    navController.navigate(ScreenNavigation.Screen.AddMealsToMealPlan.createRoute(mealType)) }) {
    var selectedTab by remember { mutableStateOf(3) }

    MyApplicationTheme {
        Scaffold(
            bottomBar = {
                NavBar(
                    selectedItem = selectedTab,
                    onItemSelected = { index ->
                        selectedTab = index
                        when (index) {
                            0 -> navController.navigate(ScreenNavigation.Screen.Home.route)
                            1 -> navController.navigate(ScreenNavigation.Screen.YourRecipes.route)
                            2 -> {} // Placeholder for other tab
                            3 -> navController.navigate(ScreenNavigation.Screen.MealPlan.route)
                            4 -> navController.navigate(ScreenNavigation.Screen.UserProfile.route)
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding)
            ) {
                item {
                    addMealPlanHeader(
                        onBackClick = { navController.popBackStack() },
                        onConfirmClick = onAddMealPlanClick
                    )
                }

                item {
                    // Add fixed height to MonthCalendar
                    Column(modifier = Modifier
                        .height(550.dp)
                        .padding(start = 16.dp, end = 16.dp)) {
                        MonthCalendar(
                            modifier = Modifier.fillMaxWidth(),
                            onDateSelected = { date ->
                                // Handle date selection
                            }
                        )
                    }
                }

                item {
                    Text(
                        text = "Planned Meals",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                            .padding(bottom = 15.dp),
                        style = Typography.titleMedium
                    )
                }

                item {
                    MealPlanContent(
                        onNavigateToAddMealsToMealPlanScreen = { mealType ->
                            // Pass the mealType to navigate
                            onAddMealsToMealPlanClick(mealType)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun addMealPlanHeader(
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Top Row: Back Button, Title, Confirm Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button (left)
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Title (center)
            Text(
                text = "Add Meal Plan",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = Typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            // Confirm button (right)
            IconButton(onClick = onConfirmClick) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Date selection row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Date",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                style = Typography.titleMedium
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun AddMealPlanScreenPreview() {
    MyApplicationTheme {
        addMealPlanScreen(
            navController = rememberNavController(),
            onAddMealPlanClick = {}
        )
    }
}