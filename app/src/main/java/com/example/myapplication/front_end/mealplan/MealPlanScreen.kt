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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Typography
import ui.components.addmealplan.mealPlanAddButton
import ui.components.datepicker.MealPlanDateCarouselAdvanced
import ui.components.plannedmeal.MealPlanContent
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MealPlanScreen() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    MyApplicationTheme {
        Scaffold (
            floatingActionButton = {
                mealPlanAddButton()
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
                    MealPlanContent()
                }

            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MealPlanScreenContentPreview() {
    MealPlanScreen()
}