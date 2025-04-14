package ui.components.addmealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun mealPlanAddButton(onAddMealPlanClick: () -> Unit){
    MyApplicationTheme {
        IconButton(
            onClick = onAddMealPlanClick,
            modifier = Modifier
                .size(69.dp)
                .background(Color.White, CircleShape)
                .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(
                modifier = Modifier
                    .size(48.dp),
                imageVector = Icons.Default.Add,
                contentDescription = "Add meal",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
