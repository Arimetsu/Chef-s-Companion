package ui.screens.mealplan

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.front_end.home.NavBar
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Typography
import ui.components.addmealplan.mealPlanAddButton
import ui.components.datepicker.MealPlanDateCarouselAdvanced
import ui.components.plannedmeal.MealPlanContent
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MealPlanScreen(
    navController: NavController,
    onAddMealsToMealPlanClick: (String) -> Unit = { mealType ->
        navController.navigate(ScreenNavigation.Screen.AddMealsToMealPlan.createRoute(mealType)) },
    onAddMealPlanClick: () -> Unit = { navController.navigate(ScreenNavigation.Screen.AddMealPlan.route) }
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTab by remember { mutableStateOf(3) }

    MyApplicationTheme {
        Scaffold (
            floatingActionButton = {
                mealPlanAddButton(onAddMealPlanClick)
            },
            bottomBar = {
                NavBar(selectedItem = selectedTab, onItemSelected = {
                    selectedTab = it
                    if (it == 0) { //home
                        navController.navigate(ScreenNavigation.Screen.Home.route)
                    }
                    else if (it == 1) { //your recipe
                        navController.navigate(ScreenNavigation.Screen.YourRecipes.route)
                    }
                })
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding)
            ) {
                item {
                    Text(
                        text = "Meal Plan",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .padding(bottom = 48.dp)
                            .padding(start = 16.dp),
                        style = Typography.titleLarge
                    )
                }

                item {
                    MealPlanDateCarouselAdvanced(
                        onDateSelected = { date ->
                            selectedDate = date
                        }
                    )
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
