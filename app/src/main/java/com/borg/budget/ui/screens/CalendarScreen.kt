package com.borg.budget.ui.screens

import androidx.compose.animation.core.*
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
import com.borg.budget.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    var displayedCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
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
            cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month && cal.get(Calendar.DAY_OF_MONTH) == selectedDay
        }
    }

    val monthLabel = remember(year, month) {
        SimpleDateFormat("MMMM yyyy", Locale.FRENCH).format(displayedCalendar.time).replaceFirstChar { it.uppercase() }
    }
    val totalMonthExpenses = expensesByDay.values.sum()

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header — crépuscule / violet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(TwilightDeep, TwilightViolet, TwilightPurple, TwilightLight)),
                    RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
                .padding(top = 28.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            Column {
                Text("Agenda", color = Color.White.copy(0.85f), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))

                // Glass total
                Box(
                    Modifier.fillMaxWidth()
                        .border(1.dp, Color.White.copy(0.25f), RoundedCornerShape(18.dp))
                        .background(Color.White.copy(0.12f), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column {
                            Text("Total du mois", color = Color.White.copy(0.75f), style = MaterialTheme.typography.labelMedium)
                            Text("%.2f €".format(totalMonthExpenses), color = Color.White, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Text("${expensesByDay.size} jours", color = Color.White.copy(0.7f), style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    IconButton(onClick = {
                        selectedDay = null
                        displayedCalendar = (displayedCalendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                    Text(monthLabel, color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = {
                        selectedDay = null
                        displayedCalendar = (displayedCalendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White) }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Calendar grid
        Column(Modifier.padding(horizontal = 12.dp)) {
            Row(Modifier.fillMaxWidth()) {
                listOf("L", "M", "M", "J", "V", "S", "D").forEach { label ->
                    Text(label, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(6.dp))

            val firstDayOfMonth = (displayedCalendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
            var startOffset = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY
            if (startOffset < 0) startOffset += 7
            val daysInMonth = firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            val today = Calendar.getInstance()
            val isCurrentMonth = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month
            val todayDay = if (isCurrentMonth) today.get(Calendar.DAY_OF_MONTH) else -1
            val rows = (startOffset + daysInMonth + 6) / 7

            for (row in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val day = row * 7 + col - startOffset + 1
                        if (day < 1 || day > daysInMonth) {
                            Box(Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            CalendarDayCell(
                                day = day,
                                amount = expensesByDay[day],
                                isSelected = selectedDay == day,
                                isToday = day == todayDay,
                                modifier = Modifier.weight(1f).aspectRatio(1f),
                                onClick = { selectedDay = if (selectedDay == day) null else day }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))

        if (selectedDay != null) {
            Text("Dépenses du $selectedDay $monthLabel", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(8.dp))
            if (transactionsForSelectedDay.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                    Text("Aucune dépense ce jour", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(transactionsForSelectedDay) { tx -> CalendarTransactionItem(tx) }
                }
            }
        } else {
            Text("Résumé du mois", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(8.dp))
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ExpenseCategory.entries) { cat ->
                    val spent = transactions.filter { tx ->
                        val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                        cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month && tx.category == cat.name
                    }.sumOf { it.amount }
                    if (spent > 0) CalendarCategorySummaryItem(cat, spent)
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(day: Int, amount: Double?, isSelected: Boolean, isToday: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val hasExpense = amount != null && amount > 0
    val bgColor = when { isSelected -> TwilightPurple; isToday -> TwilightPurple.copy(0.15f); else -> Color.Transparent }
    val textColor = when { isSelected -> Color.White; isToday -> TwilightPurple; else -> MaterialTheme.colorScheme.onBackground }

    Box(
        modifier = modifier.padding(2.dp).clip(CircleShape).background(bgColor)
            .then(if (isToday && !isSelected) Modifier.border(1.5.dp, TwilightPurple, CircleShape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$day", style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal), color = textColor, fontSize = 13.sp)
            if (hasExpense) Box(Modifier.size(5.dp).background(if (isSelected) Color.White.copy(0.85f) else TwilightLight, CircleShape))
        }
    }
}

@Composable
fun CalendarTransactionItem(transaction: TransactionEntity) {
    val category = ExpenseCategory.entries.find { it.name == transaction.category } ?: ExpenseCategory.OBLIGATION
    val time = SimpleDateFormat("HH:mm", Locale.FRENCH).format(Date(transaction.timestamp))
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surface), CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(TwilightPurple, CircleShape))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(transaction.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Text("${category.label} • $time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Text("- %.2f €".format(transaction.amount), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun CalendarCategorySummaryItem(category: ExpenseCategory, spent: Double) {
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surface), CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(TwilightPurple.copy(0.6f), CircleShape))
            Spacer(Modifier.width(12.dp))
            Text(category.label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text("%.2f €".format(spent), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
        }
    }
}
