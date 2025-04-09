package ui.components.datepicker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.Typography
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MealPlanDateCarousel(
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }

    // Generate the 7 dates (-4 to +2 from today)
    val dateRange = remember {
        (-4..2).map { today.plusDays(it.toLong()) }
    }

    // Group dates by month - this will help us display month headers
    val datesByMonth = remember(dateRange) {
        dateRange.groupBy { it.month }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Display month labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            datesByMonth.keys.forEach { month ->
                Text(
                    text = month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Display the date carousel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dateRange.forEach { date ->
                DateCell(
                    date = date,
                    isSelected = date == selectedDate,
                    isToday = date == today,
                    onClick = {
                        selectedDate = date
                        onDateSelected(date)
                    }
                )
            }
        }
    }
}


//ALT Date Carousel
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MealPlanDateCarouselAdvanced(
    modifier: Modifier = Modifier
        .padding(start = 12.dp)
        .padding(end = 12.dp)
        .padding(bottom = 68.dp),
    onDateSelected: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }

    // Calculate the start of the current week (Sunday)
    val startOfWeek = remember(today) {
        // Adjust to get Sunday as the first day of week
        // DayOfWeek values: MONDAY(1), TUESDAY(2)... SUNDAY(7)
        val daysToSubtract = when (today.dayOfWeek.value % 7) {
            0 -> 0 // It's Sunday already
            else -> today.dayOfWeek.value % 7
        }
        today.minusDays(daysToSubtract.toLong())
    }

    // Generate the 7 days of the week starting from Sunday
    val weekDates = remember(startOfWeek) {
        (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // First row: Month labels or empty space
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDates.forEach { date ->
                // Show month label for first day of month or first date in range
                val showMonthLabel = date.dayOfMonth == 1 ||
                        weekDates.indexOf(date) == 0 ||
                        weekDates.getOrNull(weekDates.indexOf(date) - 1)?.month != date.month

                Box(modifier = Modifier.width(50.dp)) {
                    if (showMonthLabel) {
                        Text(
                            text = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF19602A),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Second row: Date cells
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDates.forEach { date ->
                DateCell(
                    date = date,
                    isSelected = date == selectedDate,
                    isToday = date == today,
                    onClick = {
                        selectedDate = date
                        onDateSelected(date)
                    }
                )
            }
        }
    }
}

//WARNING private fun DateCell
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DateCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary // Dark green for selected (like in your image)
        else -> MaterialTheme.colorScheme.primaryContainer       // Light green for non-selected
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.background // Better contrast for selected state
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer // For unselected state
    }

    Box(
        modifier = Modifier
            .sizeIn(minWidth = 40.dp, minHeight = 64.dp)  // Minimum touch target
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Day of week (S, M, T, W, T, F, S)
            Text(
                text = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                style = Typography.titleSmall,
                color = textColor
            )

            // Day of month (1, 2, 3, etc.)
            Text(
                text = date.dayOfMonth.toString(),
                style = Typography.labelMedium,
                color = textColor
            )
        }
    }
}