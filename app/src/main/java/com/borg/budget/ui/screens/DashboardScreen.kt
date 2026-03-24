package com.borg.budget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.borg.budget.data.TransactionEntity
import com.borg.budget.data.SubscriptionEntity
import com.borg.budget.data.HolidayEntity
import com.borg.budget.ui.models.ExpenseCategory
import java.util.*

@Composable
fun DashboardScreen(
    totalIncome: Double,
    transactions: List<TransactionEntity>,
    subscriptions: List<SubscriptionEntity>,
    holidays: List<HolidayEntity>,
    totalHolidayQuota: Int,
    modifier: Modifier = Modifier
) {
    val totalSpent = transactions.sumOf { it.amount }
    val remainingBudget = totalIncome - totalSpent
    
    val calendar = Calendar.getInstance()
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val daysRemaining = (daysInMonth - currentDay + 1).coerceAtLeast(1)
    
    val dailyBudget = if (remainingBudget > 0) remainingBudget / daysRemaining else 0.0
    
    val holidaysTaken = holidays.sumOf { it.daysCount }
    val holidaysRemaining = totalHolidayQuota - holidaysTaken

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Gradient Header - Morning Sky for Dashboard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4FC3F7), // Light Blue
                            Color(0xFF81D4FA)
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Bonjour !",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Votre assistant budget est prêt.",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Recommended Daily Budget Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Budget quotidien conseillé",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "%.2f €".format(dailyBudget),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "pour les $daysRemaining jours restants",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
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
                Text(
                    text = "Vue d'ensemble",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Financial Grid
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardInfoCard(
                        modifier = Modifier.weight(1f),
                        label = "Revenus",
                        value = "%.0f€".format(totalIncome),
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFFE8F5E9),
                        onColor = Color(0xFF2E7D32)
                    )
                    DashboardInfoCard(
                        modifier = Modifier.weight(1f),
                        label = "Dépenses",
                        value = "%.0f€".format(totalSpent),
                        icon = Icons.Default.TrendingDown,
                        color = Color(0xFFFDECEA),
                        onColor = Color(0xFFC62828)
                    )
                }
            }

            // Budget Remaining Progress
            item {
                DashboardProgressCard(
                    label = "Budget restant",
                    current = remainingBudget,
                    total = totalIncome,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Holidays Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.BeachAccess, null, tint = Color(0xFF1976D2))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Congés", style = MaterialTheme.typography.titleSmall)
                            Text("$holidaysRemaining jours restants", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        }
                        Text(
                            text = "$holidaysTaken pris",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Upcoming Subscriptions
            if (subscriptions.isNotEmpty()) {
                item {
                    Text(
                        text = "Abonnements à venir",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // Show a few subscriptions
                items(subscriptions.take(3).size) { index ->
                    val sub = subscriptions[index]
                    SubscriptionPreviewItem(sub)
                }
            }
        }
    }
}

@Composable
fun DashboardInfoCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = onColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = onColor.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = onColor)
        }
    }
}

@Composable
fun DashboardProgressCard(
    label: String,
    current: Double,
    total: Double,
    color: Color
) {
    val progress = if (total > 0) (current / total).toFloat().coerceIn(0f, 1f) else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
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
fun SubscriptionPreviewItem(subscription: SubscriptionEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(4.dp)))
        Spacer(Modifier.width(12.dp))
        Text(subscription.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text("%.2f €".format(subscription.basePrice), fontWeight = FontWeight.SemiBold)
    }
}
