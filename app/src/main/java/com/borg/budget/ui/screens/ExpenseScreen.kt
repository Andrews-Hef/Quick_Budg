package com.borg.budget.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.borg.budget.data.TransactionEntity
import com.borg.budget.ui.models.ExpenseCategory
import com.borg.budget.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseScreen(
    totalIncome: Double,
    transactions: List<TransactionEntity>,
    onAddTransaction: (String, Double, ExpenseCategory, Boolean, Boolean) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var preselectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var displayedCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        })
    }
    val context = LocalContext.current
    val year = displayedCalendar.get(Calendar.YEAR)
    val month = displayedCalendar.get(Calendar.MONTH)
    val monthLabel = remember(year, month) {
        SimpleDateFormat("MMMM yyyy", Locale.FRENCH).format(displayedCalendar.time).replaceFirstChar { it.uppercase() }
    }
    val monthlyTransactions = remember(transactions, year, month) {
        transactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
        }
    }
    val totalSpent = monthlyTransactions.sumOf { it.amount }
    val globalProgress = if (totalIncome > 0) (totalSpent / totalIncome).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = globalProgress, animationSpec = tween(900, easing = EaseOutCubic), label = "progress")

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) showScanner = true }

    if (showScanner) {
        ScannerScreen(
            onReceiptScanned = { merchant, _, amount -> onAddTransaction(merchant, amount, ExpenseCategory.PLEASURE, false, false); showScanner = false },
            onClose = { showScanner = false }
        )
        return
    }

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())
    ) {
        // Header — coucher de soleil
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(SunsetDeep, SunsetMid, SunsetOrange, SunsetGold)),
                    RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
                .padding(top = 28.dp, start = 24.dp, end = 24.dp, bottom = 28.dp)
        ) {
            Column {
                Text("Mes dépenses", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    IconButton(onClick = { displayedCalendar = (displayedCalendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Text(monthLabel, color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = { displayedCalendar = (displayedCalendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Glass total card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column {
                            Text("Dépensé", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
                            Text("%.2f €".format(totalSpent), color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Budget", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
                            Text("%.0f €".format(totalIncome), color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = when { animatedProgress > 0.9f -> Color(0xFFFFCDD2); animatedProgress > 0.75f -> Color(0xFFFFE082); else -> Color.White },
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Spacer(Modifier.height(4.dp))
                Text("%.0f € restant • ${(globalProgress * 100).toInt()}% utilisé".format(totalIncome - totalSpent), color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Quick actions
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SunsetActionButton(Modifier.weight(1f), Icons.Default.AddCircle, "Ajouter", onClick = { preselectedCategory = null; showAddDialog = true })
            SunsetActionButton(
                Modifier.weight(1f), Icons.Default.PhotoCamera, "Scanner ticket",
                secondary = true,
                onClick = {
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> showScanner = true
                        else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        Text("Santé budgétaire", modifier = Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(12.dp))

        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ExpenseCategory.entries.forEach { cat ->
                val allocated = totalIncome * cat.percentage
                val spent = monthlyTransactions.filter { it.category == cat.name }.sumOf { it.amount }
                CategoryBudgetCard(category = cat, spent = spent, allocated = allocated, onAddClick = { preselectedCategory = cat; showAddDialog = true })
            }
        }

        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().padding(bottom = 12.dp), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, Modifier.size(13.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.width(4.dp))
                Text("Historique détaillé dans l'Agenda", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(preselectedCategory = preselectedCategory, onDismiss = { showAddDialog = false }, onConfirm = { title, amt, cat -> onAddTransaction(title, amt, cat, false, false); showAddDialog = false })
    }
}

@Composable
private fun SunsetActionButton(modifier: Modifier, icon: ImageVector, label: String, secondary: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (secondary) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.22f),
            contentColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(22.dp))
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CategoryBudgetCard(category: ExpenseCategory, spent: Double, allocated: Double, onAddClick: () -> Unit) {
    val progress = if (allocated > 0) (spent / allocated).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(800, easing = EaseOutCubic), label = "cat_progress")
    val ratio = if (allocated > 0) spent / allocated else 0.0
    val (statusLabel, statusColor, barColor) = when {
        ratio > 1.0 -> Triple("Dépassé", Color(0xFFB71C1C), Color(0xFFEF5350))
        ratio > 0.85 -> Triple("Attention", Color(0xFFE65100), Color(0xFFFF7043))
        ratio > 0.5 -> Triple("En cours", Color(0xFF1B5E20), Color(0xFF4CAF50))
        else -> Triple("Disponible", Color(0xFF1B5E20), Color(0xFF81C784))
    }
    val (icon, bgColor) = when (category) {
        ExpenseCategory.OBLIGATION -> Icons.AutoMirrored.Filled.ReceiptLong to Color(0xFFF3E5F5)
        ExpenseCategory.PLEASURE -> Icons.Default.Celebration to Color(0xFFFFF3E0)
        ExpenseCategory.SAVING_DEBT -> Icons.Default.AccountBalance to Color(0xFFE3F2FD)
    }
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surface), CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).background(bgColor, RoundedCornerShape(12.dp)), Alignment.Center) {
                    Icon(icon, null, Modifier.size(22.dp), tint = SunsetMid)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(category.label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Text("${(category.percentage * 100).toInt()}% du budget", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                Box(Modifier.background(statusColor.copy(alpha = 0.12f), CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(statusLabel, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                FilledIconButton(onClick = onAddClick, modifier = Modifier.size(36.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = SunsetMid.copy(0.15f))) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp), tint = SunsetMid)
                }
            }
            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = barColor, trackColor = barColor.copy(alpha = 0.12f))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("%.2f €".format(spent), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text("sur %.0f €".format(allocated), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun AddExpenseDialog(preselectedCategory: ExpenseCategory?, onDismiss: () -> Unit, onConfirm: (String, Double, ExpenseCategory) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember(preselectedCategory) { mutableStateOf(preselectedCategory ?: ExpenseCategory.OBLIGATION) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle dépense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Description") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Montant") }, prefix = { Text("€ ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Catégorie", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExpenseCategory.entries.forEach { cat ->
                        val selected = category == cat
                        Surface(onClick = { category = cat }, shape = RoundedCornerShape(12.dp), color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selected, onClick = { category = cat })
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(cat.label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal))
                                    Text("${(cat.percentage * 100).toInt()}% du budget", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { val amt = amount.replace(",", ".").toDoubleOrNull() ?: 0.0; if (title.isNotBlank() && amt > 0) onConfirm(title, amt, category) }, shape = RoundedCornerShape(12.dp)) { Text("Ajouter") } },
        dismissButton = { TextButton(onDismiss) { Text("Annuler") } }
    )
}

@Composable
fun ProfessionalExpenseItem(transaction: TransactionEntity, onDelete: () -> Unit) {
    val category = ExpenseCategory.entries.find { it.name == transaction.category } ?: ExpenseCategory.OBLIGATION
    val icon = when (category) { ExpenseCategory.OBLIGATION -> Icons.AutoMirrored.Filled.ReceiptLong; ExpenseCategory.PLEASURE -> Icons.Default.Celebration; ExpenseCategory.SAVING_DEBT -> Icons.Default.AccountBalance }
    val date = SimpleDateFormat("dd/MM", Locale.FRENCH).format(Date(transaction.timestamp))
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surface), CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(0.4f), CircleShape), Alignment.Center) { Icon(icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(transaction.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Text("${category.label} • $date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Text("- %.2f €".format(transaction.amount), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
            IconButton(onClick = onDelete) { Icon(Icons.Default.Close, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outline) }
        }
    }
}
