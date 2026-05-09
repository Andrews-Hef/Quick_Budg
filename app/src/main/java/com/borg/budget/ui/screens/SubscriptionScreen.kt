package com.borg.budget.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borg.budget.data.SubscriptionEntity
import com.borg.budget.ui.models.ExpenseCategory
import com.borg.budget.ui.theme.*
import java.util.Calendar

@Composable
fun SubscriptionScreen(
    subscriptions: List<SubscriptionEntity>,
    notifyDays: Int,
    onAddSubscription: (String, Double, Int, String, ExpenseCategory, Int) -> Unit,
    onDeleteSubscription: (SubscriptionEntity) -> Unit,
    onNotifyDaysChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showNotifyDialog by remember { mutableStateOf(false) }

    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val totalMonthly = subscriptions.filter { it.frequency == "MONTHLY" }.sumOf { it.basePrice }
    val totalAnnual = subscriptions.filter { it.frequency == "ANNUAL" }.sumOf { it.basePrice }
    val upcoming = subscriptions.sortedBy { it.dayOfPayment }.filter { (it.dayOfPayment - today) in 0..7 }

    val quotaProgress = if (subscriptions.isNotEmpty()) (upcoming.size.toFloat() / subscriptions.size).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = quotaProgress, animationSpec = tween(800, easing = EaseOutCubic), label = "sub_progress")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = CosmicBlue, contentColor = Color.White) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = modifier.padding(scaffoldPadding).fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item {
                // Header — nuit cosmique
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(CosmicDeep, CosmicNavy, CosmicBlue, CosmicPurple)),
                            RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                        )
                        .padding(top = 28.dp, start = 24.dp, end = 24.dp, bottom = 28.dp)
                ) {
                    Column {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Column {
                                Text("Abonnements", color = Color.White.copy(0.85f), style = MaterialTheme.typography.bodyLarge)
                                Text("${subscriptions.size} actifs", color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            }
                            IconButton(onClick = { showNotifyDialog = true }) {
                                Box(
                                    Modifier.size(40.dp).border(1.dp, Color.White.copy(0.3f), CircleShape).background(Color.White.copy(0.12f), CircleShape),
                                    Alignment.Center
                                ) {
                                    Icon(Icons.Default.NotificationsActive, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CosmicStatCard(Modifier.weight(1f), "Mensuel", "%.2f €".format(totalMonthly))
                            CosmicStatCard(Modifier.weight(1f), "Annuel", "%.2f €".format(totalAnnual))
                            CosmicStatCard(Modifier.weight(1f), "Imminent", "${upcoming.size} abos", highlight = upcoming.isNotEmpty())
                        }
                    }
                }
            }

            if (upcoming.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(20.dp))
                    Text("Prochains prélèvements", modifier = Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(12.dp))
                }
                items(upcoming) { sub ->
                    UpcomingSubCard(sub = sub, today = today, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                }
                item {
                    HorizontalDivider(Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
                }
            }

            item {
                Spacer(Modifier.height(if (upcoming.isEmpty()) 20.dp else 4.dp))
                Text("Tous les abonnements", modifier = Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(12.dp))
            }

            if (subscriptions.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Autorenew, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline.copy(0.4f))
                            Spacer(Modifier.height(12.dp))
                            Text("Aucun abonnement", color = MaterialTheme.colorScheme.outline)
                            Text("Appuyez sur + pour en ajouter", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline.copy(0.6f))
                        }
                    }
                }
            } else {
                items(subscriptions.sortedBy { it.dayOfPayment }) { sub ->
                    SubscriptionItem(sub = sub, onDelete = { onDeleteSubscription(sub) }, modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubscriptionDialog(onDismiss = { showAddDialog = false }, onConfirm = { name, amount, day, freq, cat, bm -> onAddSubscription(name, amount, day, freq, cat, bm); showAddDialog = false })
    }
    if (showNotifyDialog) {
        NotifyDaysDialog(currentDays = notifyDays, onDismiss = { showNotifyDialog = false }, onConfirm = { days -> onNotifyDaysChange(days); showNotifyDialog = false })
    }
}

@Composable
private fun CosmicStatCard(modifier: Modifier, label: String, value: String, highlight: Boolean = false) {
    Box(
        modifier = modifier
            .border(1.dp, Color.White.copy(if (highlight) 0.5f else 0.2f), RoundedCornerShape(16.dp))
            .background(if (highlight) Color.White.copy(0.22f) else Color.White.copy(0.1f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
            Text(label, color = Color.White.copy(0.65f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun UpcomingSubCard(sub: SubscriptionEntity, today: Int, modifier: Modifier = Modifier) {
    val daysUntil = sub.dayOfPayment - today
    val urgencyColor = when { daysUntil == 0 -> Color(0xFFEF5350); daysUntil <= 2 -> Color(0xFFFF7043); else -> CosmicBlue }
    val daysLabel = when (daysUntil) { 0 -> "Aujourd'hui"; 1 -> "Demain"; else -> "Dans $daysUntil jours" }

    Card(modifier.fillMaxWidth(), RoundedCornerShape(18.dp), CardDefaults.cardColors(urgencyColor.copy(0.08f)), CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).background(urgencyColor.copy(0.15f), CircleShape), Alignment.Center) {
                Text("${sub.dayOfPayment}", fontWeight = FontWeight.ExtraBold, color = urgencyColor, fontSize = 17.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(sub.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                Text(daysLabel, style = MaterialTheme.typography.labelSmall, color = urgencyColor)
            }
            Text("- %.2f €".format(sub.basePrice), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = urgencyColor)
        }
    }
}

@Composable
private fun SubscriptionItem(sub: SubscriptionEntity, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val category = ExpenseCategory.entries.find { it.name == sub.category } ?: ExpenseCategory.OBLIGATION
    val freqLabel = if (sub.frequency == "ANNUAL") "Annuel" else "Mensuel"
    Card(modifier.fillMaxWidth(), RoundedCornerShape(18.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surface), CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(CosmicBlue.copy(0.1f), CircleShape), Alignment.Center) {
                Icon(Icons.Default.Autorenew, null, Modifier.size(20.dp), tint = CosmicBlue)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(sub.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(category.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("• le ${sub.dayOfPayment}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Box(Modifier.background(CosmicBlue.copy(0.12f), CircleShape).padding(horizontal = 6.dp, vertical = 1.dp)) {
                        Text(freqLabel, fontSize = 9.sp, color = CosmicBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text("%.2f €".format(sub.basePrice), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            IconButton(onClick = onDelete, Modifier.size(36.dp)) {
                Icon(Icons.Default.DeleteOutline, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error.copy(0.6f))
            }
        }
    }
}

@Composable
private fun AddSubscriptionDialog(onDismiss: () -> Unit, onConfirm: (String, Double, Int, String, ExpenseCategory, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("MONTHLY") }
    var billingMonth by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.OBLIGATION) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvel abonnement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Montant") }, prefix = { Text("€ ") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = day, onValueChange = { if (it.length <= 2) day = it }, label = { Text("Jour de prélèvement") }, suffix = { Text("/ 31") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Fréquence :", style = MaterialTheme.typography.labelLarge)
                Row {
                    listOf("MONTHLY" to "Mensuel", "ANNUAL" to "Annuel").forEach { (key, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(frequency == key, { frequency = key }); Text(label); Spacer(Modifier.width(12.dp)) }
                    }
                }
                if (frequency == "ANNUAL") {
                    OutlinedTextField(value = billingMonth, onValueChange = { if (it.length <= 2) billingMonth = it }, label = { Text("Mois (1-12)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                Text("Catégorie :", style = MaterialTheme.typography.labelLarge)
                Column {
                    ExpenseCategory.entries.forEach { cat ->
                        Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(category == cat, { category = cat }); Text(cat.label) }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                val dayInt = day.toIntOrNull()?.coerceIn(1, 31) ?: 1
                val bmInt = if (frequency == "ANNUAL") billingMonth.toIntOrNull()?.coerceIn(1, 12) ?: 0 else 0
                if (name.isNotBlank() && amt > 0) onConfirm(name, amt, dayInt, frequency, category, bmInt)
            }) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Annuler") } }
    )
}

@Composable
private fun NotifyDaysDialog(currentDays: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var selected by remember { mutableStateOf(currentDays) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Délai de notification") },
        text = {
            Column {
                Text("Notifier X jours avant le prélèvement :", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                listOf(1, 2, 3, 5, 7).forEach { days ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected == days, { selected = days })
                        Text(if (days == 1) "1 jour avant" else "$days jours avant")
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(selected) }) { Text("Confirmer") } },
        dismissButton = { TextButton(onDismiss) { Text("Annuler") } }
    )
}
