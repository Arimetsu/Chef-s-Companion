package com.example.myapplication.components.datepicker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Typography
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isFirstDayOfMonth: Boolean = false,
    val isLastDayOfPrevMonth: Boolean = false,
    val isFirstDayOfNextMonth: Boolean = false
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthCalendar(
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate) -> Unit = {}
) {
    // State variables
    val today = remember { LocalDate.now() }
    val currentYearMonth = remember { YearMonth.from(today) }
    var selectedDate by remember { mutableStateOf(today) }

    // Generate calendar days
    val calendarDays = remember(currentYearMonth) {
        generateCalendarDays(currentYearMonth, today)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Add any header content here if needed

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp), // Add constrained height
            contentPadding = PaddingValues(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(calendarDays) { calendarDay ->
                Box(
                    modifier = Modifier.height(70.dp) // Fixed height for each item
                ) {
                    // Month label above specific days
                    if (calendarDay.isLastDayOfPrevMonth ||
                        calendarDay.isFirstDayOfMonth ||
                        calendarDay.isFirstDayOfNextMonth) {

                        val monthName = when {
                            calendarDay.isLastDayOfPrevMonth ->
                                calendarDay.date.month.previous().getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
                            calendarDay.isFirstDayOfMonth ->
                                calendarDay.date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
                            else ->
                                calendarDay.date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
                        }

                        Text(
                            text = monthName,
                            style = Typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-12).dp)
                                .zIndex(1f)
                        )
                    }

                    DayItem(
                        calendarDay = calendarDay,
                        isSelected = selectedDate == calendarDay.date,
                        onDateClick = {
                            selectedDate = calendarDay.date
                            onDateSelected(calendarDay.date)
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayItem(
    calendarDay: CalendarDay,
    isSelected: Boolean,
    onDateClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary  // Dark green for selected date  // Dark green for today
        calendarDay.isCurrentMonth -> MaterialTheme.colorScheme.primaryContainer // Light green for current month
        else -> MaterialTheme.colorScheme.secondaryContainer  // Light green for other months
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        calendarDay.isToday -> MaterialTheme.colorScheme.onPrimary
        calendarDay.isCurrentMonth -> Color(0xFF1E5631)
        else -> Color(0xFF9EB3A4)  // Lighter green for out-of-month days
    }

    val borderColor = when {
        isSelected -> Color(0xFF1E5631)
        else -> Color.Transparent
    }

    // Get day of week (S, M, T, W, T, F, S)
    val dayOfWeekLabel = calendarDay.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())

    Card(
        modifier = Modifier
            .aspectRatio(.65f)
            .fillMaxWidth()
            .clickable { onDateClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (isSelected) BorderStroke(1.dp, borderColor) else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Day of week label (S, M, T, W, T, F, S)
            Text(
                text = dayOfWeekLabel,
                fontSize = 12.sp,
                color = textColor,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Day number
            Text(
                text = calendarDay.date.dayOfMonth.toString(),
                fontSize = 16.sp,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun generateCalendarDays(yearMonth: YearMonth, today: LocalDate): List<CalendarDay> {
    val days = mutableListOf<CalendarDay>()

    // Get the first day of the month
    val firstOfMonth = yearMonth.atDay(1)

    // Get the day of week for the first day of month (Monday, Tuesday, etc.)
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

    // Calculate how many days from the previous month we need to show
    var start = firstOfMonth
    while (start.dayOfWeek != firstDayOfWeek) {
        start = start.minusDays(1)
    }

    // Previous month details
    val prevMonth = yearMonth.minusMonths(1)
    val lastDayOfPrevMonth = prevMonth.atEndOfMonth()

    // Previous month days
    while (start.isBefore(firstOfMonth)) {
        days.add(
            CalendarDay(
                date = start,
                isCurrentMonth = false,
                isToday = start.isEqual(today),
                isLastDayOfPrevMonth = start.isEqual(lastDayOfPrevMonth)
            )
        )
        start = start.plusDays(1)
    }

    // Current month days
    val lastOfMonth = yearMonth.atEndOfMonth()
    while (!start.isAfter(lastOfMonth)) {
        days.add(
            CalendarDay(
                date = start,
                isCurrentMonth = true,
                isToday = start.isEqual(today),
                isFirstDayOfMonth = start.dayOfMonth == 1
            )
        )
        start = start.plusDays(1)
    }

    // Next month days
    val nextMonth = yearMonth.plusMonths(1)
    val firstDayOfNextMonth = nextMonth.atDay(1)

    // Ensure we have 6 complete weeks (42 days)
    while (days.size < 42) {
        days.add(
            CalendarDay(
                date = start,
                isCurrentMonth = false,
                isToday = start.isEqual(today),
                isFirstDayOfNextMonth = start.isEqual(firstDayOfNextMonth)
            )
        )
        start = start.plusDays(1)
    }

    return days
}

// Helper extension to get previous month
@RequiresApi(Build.VERSION_CODES.O)
fun java.time.Month.previous(): java.time.Month {
    return if (this.value == 1) java.time.Month.DECEMBER else java.time.Month.of(this.value - 1)
}


@Preview(showBackground = true)
@Composable
fun MonthCalendarPreview() {
    // Wrap in a Surface to see the preview better
    MyApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // For preview purposes, we can use a conditional check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                MonthCalendar(
                    modifier = Modifier.padding(16.dp),
                    onDateSelected = { date ->
                        println("Selected date: ${date.format(DateTimeFormatter.ISO_DATE)}")
                    }
                )
            } else {
                // Fallback for older APIs in preview
                Text(
                    text = "Calendar requires API 26+",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
