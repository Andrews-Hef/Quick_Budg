package com.borg.budget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borg.budget.data.TransactionEntity
import com.borg.budget.ui.models.ExpenseCategory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    var displayedCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        })
    }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val year = displayedCalendar.get(Calendar.YEAR)
    val month = displayedCalendar.get(Calendar.MONTH)

    val expensesByDay = remember(transactions, year, month) {
        val map = mutableMapOf<Int, Double>()
        transactions.forEach { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month) {
                val day = cal.get(Calendar.DAY_OF_MONTH)
                map[day] = (map[day] ?: 0.0) + tx.amount
            }
        }
        map
    }

    val transactionsForSelectedDay = remember(transactions, selectedDay, year, month) {
        if (selectedDay == null) emptyList()
        else transactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            cal.get(Calendar.YEAR) == year &&
                    cal.get(Calendar.MONTH) == month &&
                    cal.get(Calendar.DAY_OF_MONTH) == selectedDay
        }
    }

    val monthLabel = remember(year, month) {
        SimpleDateFormat("MMMM yyyy", Locale.FRENCH).format(displayedCalendar.time)
            .replaceFirstChar { it.uppercase() }
    }

    val totalMonthExpenses = expensesByDay.values.sum()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF7E57C2),
                            Color(0xFFB39DDB)
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "Calendrier des Dépenses",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Total du mois : %.2f €".format(totalMonthExpenses),
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(Modifier.height(16.dp))

                // Month navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        selectedDay = null
                        displayedCalendar = (displayedCalendar.clone() as Calendar).apply {
                            add(Calendar.MONTH, -1)
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Mois précédent",
                            tint = Color.White
                        )
                    }
                    Text(
                        monthLabel,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = {
                        selectedDay = null
                        displayedCalendar = (displayedCalendar.clone() as Calendar).apply {
                            add(Calendar.MONTH, 1)
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Mois suivant",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Calendar grid
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Day headers
            val dayHeaders = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
            Row(modifier = Modifier.fillMaxWidth()) {
                dayHeaders.forEach { label ->
                    Text(
                        label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Build day grid
            val firstDayOfMonth = (displayedCalendar.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
            }
            // Monday = 0 offset
            var startOffset = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY
            if (startOffset < 0) startOffset += 7

            val daysInMonth = firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            val today = Calendar.getInstance()
            val isCurrentMonth = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month
            val todayDay = if (isCurrentMonth) today.get(Calendar.DAY_OF_MONTH) else -1

            val totalCells = startOffset + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - startOffset + 1
                        if (day < 1 || day > daysInMonth) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val dayAmount = expensesByDay[day]
                            val isSelected = selectedDay == day
                            val isToday = day == todayDay
                            CalendarDayCell(
                                day = day,
                                amount = dayAmount,
                                isSelected = isSelected,
                                isToday = isToday,
                                modifier = Modifier.weight(1f).aspectRatio(1f),
                                onClick = { selectedDay = if (isSelected) null else day }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))

        // Transaction list for selected day
        if (selectedDay != null) {
            Text(
                "Dépenses du ${selectedDay} ${monthLabel}",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))

            if (transactionsForSelectedDay.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Aucune dépense ce jour",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactionsForSelectedDay) { tx ->
                        CalendarTransactionItem(tx)
                    }
                }
            }
        } else {
            // Summary by category for the month
            Text(
                "Résumé du mois",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ExpenseCategory.entries) { cat ->
                    val spent = transactions.filter { tx ->
                        val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                        cal.get(Calendar.YEAR) == year &&
                                cal.get(Calendar.MONTH) == month &&
                                tx.category == cat.name
                    }.sumOf { it.amount }
                    if (spent > 0) {
                        CalendarCategorySummaryItem(cat, spent)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    day: Int,
    amount: Double?,
    isSelected: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val hasExpense = amount != null && amount > 0
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onBackground
    }
    val dotColor = when {
        isSelected -> Color.White.copy(alpha = 0.8f)
        else -> Color(0xFFEF5350)
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .then(
                if (isToday && !isSelected)
                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$day",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal),
                color = textColor,
                fontSize = 13.sp
            )
            if (hasExpense) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(dotColor, CircleShape)
                )
            }
        }
    }
}

@Composable
fun CalendarTransactionItem(transaction: TransactionEntity) {
    val category = ExpenseCategory.entries.find { it.name == transaction.category } ?: ExpenseCategory.OBLIGATION
    val time = SimpleDateFormat("HH:mm", Locale.FRENCH).format(Date(transaction.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Text(
                    "${category.label} • $time",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                "- %.2f €".format(transaction.amount),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun CalendarCategorySummaryItem(category: ExpenseCategory, spent: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                category.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "%.2f €".format(spent),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
