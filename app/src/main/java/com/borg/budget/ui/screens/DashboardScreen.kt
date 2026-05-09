package com.borg.budget.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val monthlyTransactions = transactions.filter { tx ->
        val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
        cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == currentMonth
    }

    val totalSpent = monthlyTransactions.sumOf { it.amount }
    val remainingBudget = totalIncome - totalSpent

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val daysRemaining = (daysInMonth - currentDay + 1).coerceAtLeast(1)
    val dailyBudget = if (remainingBudget > 0) remainingBudget / daysRemaining else 0.0

    val holidaysTaken = holidays.sumOf { it.daysCount }
    val holidaysRemaining = totalHolidayQuota - holidaysTaken

    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val upcomingSubscriptions = subscriptions
        .filter { it.dayOfPayment >= today }
        .sortedBy { it.dayOfPayment }

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF4FC3F7), Color(0xFF81D4FA))),
                    RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text("Bonjour !", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Votre assistant budget est prêt.", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Budget quotidien conseillé", color = Color.White, style = MaterialTheme.typography.labelLarge)
                        Text("%.2f €".format(dailyBudget), color = Color.White, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
                        Text("pour les $daysRemaining jours restants", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Vue d'ensemble du mois", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardInfoCard(
                        modifier = Modifier.weight(1f),
                        label = "Revenus",
                        value = "%.0f €".format(totalIncome),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = Color(0xFFE8F5E9),
                        onColor = Color(0xFF2E7D32)
                    )
                    DashboardInfoCard(
                        modifier = Modifier.weight(1f),
                        label = "Dépensé",
                        value = "%.0f €".format(totalSpent),
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        color = Color(0xFFFDECEA),
                        onColor = Color(0xFFC62828)
                    )
                }
            }

            item {
                DashboardProgressCard(label = "Budget restant", current = remainingBudget, total = totalIncome, color = MaterialTheme.colorScheme.primary)
            }

            item {
                Text("Répartition par catégorie", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExpenseCategory.entries.forEach { cat ->
                            val allocated = totalIncome * cat.percentage
                            val spent = monthlyTransactions.filter { it.category == cat.name }.sumOf { it.amount }
                            val progress = if (allocated > 0) (spent / allocated).toFloat().coerceIn(0f, 1f) else 0f
                            val isOver = spent > allocated
                            Column {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text(cat.label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text(
                                        "%.0f / %.0f €".format(spent, allocated),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                        fontWeight = if (isOver) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                    color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToHolidays() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.BeachAccess, null, tint = Color(0xFF1976D2))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Congés", style = MaterialTheme.typography.titleSmall)
                            Text("$holidaysRemaining jours restants", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        }
                        Text("$holidaysTaken pris", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (upcomingSubscriptions.isNotEmpty()) {
                item {
                    Text("Abonnements à venir", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 4.dp))
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            upcomingSubscriptions.take(5).forEach { sub ->
                                SubscriptionPreviewItem(sub, today)
                            }
                            if (subscriptions.size > 5) {
                                Text(
                                    "+${subscriptions.size - 5} autres abonnements",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            if (monthlyTransactions.isNotEmpty()) {
                item {
                    Text("Dépenses récentes", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 4.dp))
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
fun DashboardInfoCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color, onColor: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(modifier = Modifier.padding(16.dp)) {
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
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.titleSmall)
                Text("%.2f €".format(current), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun SubscriptionPreviewItem(subscription: SubscriptionEntity, today: Int) {
    val daysUntil = subscription.dayOfPayment - today
    val dateLabel = when {
        daysUntil == 0 -> "Aujourd'hui"
        daysUntil == 1 -> "Demain"
        daysUntil > 1 -> "Le ${subscription.dayOfPayment}"
        else -> "Le ${subscription.dayOfPayment}"
    }
    val urgencyColor = when {
        daysUntil <= 0 -> MaterialTheme.colorScheme.error
        daysUntil <= 2 -> Color(0xFFFF7043)
        else -> MaterialTheme.colorScheme.outline
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.secondary, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.width(12.dp))
        Text(subscription.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(dateLabel, style = MaterialTheme.typography.labelSmall, color = urgencyColor)
        Spacer(Modifier.width(8.dp))
        Text("%.2f €".format(subscription.basePrice), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun RecentTransactionItem(transaction: TransactionEntity) {
    val category = ExpenseCategory.entries.find { it.name == transaction.category } ?: ExpenseCategory.OBLIGATION
    val date = java.text.SimpleDateFormat("dd/MM", Locale.FRENCH).format(java.util.Date(transaction.timestamp))

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text("${category.label} • $date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        Text("- %.2f €".format(transaction.amount), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
    }
}
