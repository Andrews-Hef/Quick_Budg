package com.borg.budget.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borg.budget.data.HolidayEntity
import com.borg.budget.data.SubscriptionEntity
import com.borg.budget.data.TransactionEntity
import com.borg.budget.ui.models.ExpenseCategory
import com.borg.budget.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    totalIncome: Double,
    transactions: List<TransactionEntity>,
    subscriptions: List<SubscriptionEntity>,
    holidays: List<HolidayEntity>,
    totalHolidayQuota: Int,
    onNavigateToHolidays: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    val monthlyTransactions = remember(transactions, currentMonth, currentYear) {
        transactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == currentMonth
        }
    }

    val totalSpent = monthlyTransactions.sumOf { it.amount }
    val remaining = (totalIncome - totalSpent).coerceAtLeast(0.0)
    val daysRemaining = (calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH) + 1).coerceAtLeast(1)
    val dailyBudget = if (remaining > 0) remaining / daysRemaining else 0.0
    val globalProgress = if (totalIncome > 0) (totalSpent / totalIncome).toFloat().coerceIn(0f, 1f) else 0f

    val animatedProgress by animateFloatAsState(targetValue = globalProgress, animationSpec = tween(900, easing = EaseOutCubic), label = "global_progress")

    val holidaysTaken = holidays.sumOf { it.daysCount }
    val holidaysRemaining = totalHolidayQuota - holidaysTaken
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val upcomingSubscriptions = subscriptions.filter { it.dayOfPayment >= today }.sortedBy { it.dayOfPayment }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            // Header — aube / ciel du matin
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(DawnDeep, DawnMid, DawnLight)),
                        RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                    )
                    .padding(top = 28.dp, start = 24.dp, end = 24.dp, bottom = 28.dp)
            ) {
                Column {
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val greeting = when {
                        hour < 12 -> "Bonjour ☀️"
                        hour < 18 -> "Bon après-midi 🌤"
                        else -> "Bonsoir 🌙"
                    }
                    Text(greeting, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                    Text("Vue d'ensemble", color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

                    Spacer(Modifier.height(20.dp))

                    // Glass card — budget quotidien
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Budget quotidien conseillé", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(4.dp))
                            Text("%.2f €".format(dailyBudget), color = Color.White, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold))
                            Text("pour $daysRemaining jours restants", color = Color.White.copy(alpha = 0.65f), style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Stats row
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassStatChip(Modifier.weight(1f), "Revenus", "%.0f €".format(totalIncome), Icons.AutoMirrored.Filled.TrendingUp)
                        GlassStatChip(Modifier.weight(1f), "Dépensé", "%.0f €".format(totalSpent), Icons.AutoMirrored.Filled.TrendingDown)
                        GlassStatChip(Modifier.weight(1f), "Restant", "%.0f €".format(remaining), Icons.Default.AccountBalanceWallet)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Global progress
                    Column {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Budget consommé", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
                            Text("${(globalProgress * 100).toInt()}%", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = when {
                                globalProgress > 0.9f -> Color(0xFFEF5350)
                                globalProgress > 0.75f -> Color(0xFFFF9800)
                                else -> Color.White
                            },
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }

        // Category breakdown
        item {
            Text(
                "Répartition budgétaire",
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(12.dp))
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExpenseCategory.entries.forEach { cat ->
                    val allocated = totalIncome * cat.percentage
                    val spent = monthlyTransactions.filter { it.category == cat.name }.sumOf { it.amount }
                    val catProgress = if (allocated > 0) (spent / allocated).toFloat().coerceIn(0f, 1f) else 0f
                    val animCatProgress by animateFloatAsState(targetValue = catProgress, animationSpec = tween(800, easing = EaseOutCubic), label = "cat_${cat.name}")
                    val isOver = spent > allocated

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text(cat.label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("%.0f / %.0f €".format(spent, allocated), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    if (isOver) Box(Modifier.background(MaterialTheme.colorScheme.error.copy(0.12f), CircleShape).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                        Text("Dépassé", fontSize = 9.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { animCatProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }

        // Congés card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable { onNavigateToHolidays() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.BeachAccess, null, tint = HorizonMid)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Congés", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        Text("$holidaysRemaining jours restants sur $totalHolidayQuota", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Text("$holidaysTaken pris", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Upcoming subscriptions
        if (upcomingSubscriptions.isNotEmpty()) {
            item {
                Spacer(Modifier.height(20.dp))
                Text("Abonnements à venir", modifier = Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        upcomingSubscriptions.take(5).forEach { sub ->
                            SubscriptionPreviewItem(sub, today)
                        }
                    }
                }
            }
        }

        // Recent transactions
        if (monthlyTransactions.isNotEmpty()) {
            item {
                Spacer(Modifier.height(20.dp))
                Text("Dépenses récentes", modifier = Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        monthlyTransactions.take(5).forEach { tx ->
                            RecentTransactionItem(tx)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassStatChip(modifier: Modifier, label: String, value: String, icon: ImageVector) {
    Box(
        modifier = modifier
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
            Text(label, color = Color.White.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
fun DashboardInfoCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color, onColor: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = onColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = onColor.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = onColor)
        }
    }
}

@Composable
fun DashboardProgressCard(label: String, current: Double, total: Double, color: Color) {
    val progress = if (total > 0) (current / total).toFloat().coerceIn(0f, 1f) else 0f
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.titleSmall)
                Text("%.2f €".format(current), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = color, trackColor = color.copy(alpha = 0.2f))
        }
    }
}

@Composable
fun SubscriptionPreviewItem(subscription: SubscriptionEntity, today: Int) {
    val daysUntil = subscription.dayOfPayment - today
    val urgencyColor = when {
        daysUntil <= 0 -> MaterialTheme.colorScheme.error
        daysUntil <= 2 -> Color(0xFFFF7043)
        else -> MaterialTheme.colorScheme.outline
    }
    val dateLabel = when (daysUntil) {
        0 -> "Aujourd'hui"
        1 -> "Demain"
        else -> "Le ${subscription.dayOfPayment}"
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape))
        Spacer(Modifier.width(10.dp))
        Text(subscription.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(dateLabel, style = MaterialTheme.typography.labelSmall, color = urgencyColor)
        Spacer(Modifier.width(8.dp))
        Text("%.2f €".format(subscription.basePrice), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun RecentTransactionItem(transaction: TransactionEntity) {
    val category = ExpenseCategory.entries.find { it.name == transaction.category } ?: ExpenseCategory.OBLIGATION
    val date = SimpleDateFormat("dd/MM", Locale.FRENCH).format(Date(transaction.timestamp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary.copy(0.4f), CircleShape))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(transaction.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text("${category.label} • $date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        Text("- %.2f €".format(transaction.amount), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
    }
}
