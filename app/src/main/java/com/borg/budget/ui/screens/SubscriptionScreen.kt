package com.borg.budget.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borg.budget.data.SubscriptionEntity
import com.borg.budget.ui.models.ExpenseCategory
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

    val upcoming = subscriptions
        .sortedBy { it.dayOfPayment }
        .filter { sub ->
            val daysUntil = sub.dayOfPayment - today
            daysUntil in 0..7
        }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { scaffoldPadding ->
        Column(
            modifier = modifier
                .padding(scaffoldPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF26A69A), Color(0xFF80CBC4))),
                        RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Abonnements", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showNotifyDialog = true }) {
                            Icon(Icons.Default.Notifications, null, tint = Color.White.copy(alpha = 0.8f))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SubscriptionStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Mensuel",
                            value = "%.2f €".format(totalMonthly)
                        )
                        SubscriptionStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Annuel",
                            value = "%.2f €".format(totalAnnual)
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (upcoming.isNotEmpty()) {
                    item {
                        Text(
                            "Prochains prélèvements",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                    }
                    items(upcoming) { sub ->
                        UpcomingSubscriptionCard(sub = sub, today = today)
                    }
                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                }

                item {
                    Text(
                        "Tous les abonnements (${subscriptions.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }

                if (subscriptions.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Aucun abonnement", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    items(subscriptions.sortedBy { it.dayOfPayment }) { sub ->
                        SubscriptionItem(sub = sub, onDelete = { onDeleteSubscription(sub) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubscriptionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount, day, freq, cat, billingMonth ->
                onAddSubscription(name, amount, day, freq, cat, billingMonth)
                showAddDialog = false
            }
        )
    }

    if (showNotifyDialog) {
        NotifyDaysDialog(
            currentDays = notifyDays,
            onDismiss = { showNotifyDialog = false },
            onConfirm = { days ->
                onNotifyDaysChange(days)
                showNotifyDialog = false
            }
        )
    }
}

@Composable
private fun SubscriptionStatCard(modifier: Modifier, label: String, value: String) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
            Text(value, color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun UpcomingSubscriptionCard(sub: SubscriptionEntity, today: Int) {
    val daysUntil = sub.dayOfPayment - today
    val urgencyColor = when {
        daysUntil == 0 -> Color(0xFFEF5350)
        daysUntil <= 2 -> Color(0xFFFF7043)
        else -> Color(0xFF26A69A)
    }
    val daysLabel = when (daysUntil) {
        0 -> "Aujourd'hui"
        1 -> "Demain"
        else -> "Dans $daysUntil jours"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = urgencyColor.copy(alpha = 0.08f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(urgencyColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("${sub.dayOfPayment}", fontWeight = FontWeight.Bold, color = urgencyColor, fontSize = 16.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sub.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                Text(daysLabel, style = MaterialTheme.typography.labelSmall, color = urgencyColor)
            }
            Text("- %.2f €".format(sub.basePrice), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = urgencyColor)
        }
    }
}

@Composable
private fun SubscriptionItem(sub: SubscriptionEntity, onDelete: () -> Unit) {
    val category = ExpenseCategory.entries.find { it.name == sub.category } ?: ExpenseCategory.OBLIGATION
    val freqLabel = if (sub.frequency == "ANNUAL") "Annuel" else "Mensuel"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Autorenew, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sub.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(category.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("•", color = MaterialTheme.colorScheme.outline, fontSize = 8.sp)
                    Text("le ${sub.dayOfPayment}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("•", color = MaterialTheme.colorScheme.outline, fontSize = 8.sp)
                    Box(
                        Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape).padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(freqLabel, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text("%.2f €".format(sub.basePrice), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun AddSubscriptionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int, String, ExpenseCategory, Int) -> Unit
) {
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    label = { Text("Montant") },
                    prefix = { Text("€ ") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = day, onValueChange = { if (it.length <= 2) day = it },
                    label = { Text("Jour de prélèvement") },
                    suffix = { Text("/ 31") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Fréquence :", style = MaterialTheme.typography.labelLarge)
                Row {
                    listOf("MONTHLY" to "Mensuel", "ANNUAL" to "Annuel").forEach { (key, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(frequency == key, { frequency = key })
                            Text(label)
                            Spacer(Modifier.width(12.dp))
                        }
                    }
                }
                if (frequency == "ANNUAL") {
                    OutlinedTextField(
                        value = billingMonth, onValueChange = { if (it.length <= 2) billingMonth = it },
                        label = { Text("Mois de prélèvement (1-12)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text("Catégorie :", style = MaterialTheme.typography.labelLarge)
                Column {
                    ExpenseCategory.entries.forEach { cat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(category == cat, { category = cat })
                            Text(cat.label)
                        }
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
    val options = listOf(1, 2, 3, 5, 7)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Délai de notification") },
        text = {
            Column {
                Text("Notifier X jours avant le prélèvement :", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                options.forEach { days ->
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
