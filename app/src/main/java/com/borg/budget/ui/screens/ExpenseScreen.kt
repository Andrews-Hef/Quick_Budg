package com.borg.budget.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.borg.budget.data.TransactionEntity
import com.borg.budget.ui.models.ExpenseCategory
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
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        })
    }
    val context = LocalContext.current

    val year = displayedCalendar.get(Calendar.YEAR)
    val month = displayedCalendar.get(Calendar.MONTH)
    val monthLabel = remember(year, month) {
        SimpleDateFormat("MMMM yyyy", Locale.FRENCH).format(displayedCalendar.time)
            .replaceFirstChar { it.uppercase() }
    }

    val monthlyTransactions = remember(transactions, year, month) {
        transactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
        }
    }
    val totalSpent = monthlyTransactions.sumOf { it.amount }
    val globalProgress = if (totalIncome > 0) (totalSpent / totalIncome).toFloat().coerceIn(0f, 1f) else 0f

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) showScanner = true }

    if (showScanner) {
        ScannerScreen(
            onReceiptScanned = { merchant, _, amount ->
                onAddTransaction(merchant, amount, ExpenseCategory.PLEASURE, false, false)
                showScanner = false
            },
            onClose = { showScanner = false }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF5C6BC0), Color(0xFF9FA8DA))),
                    RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text("Mes dépenses", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        displayedCalendar = (displayedCalendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Text(monthLabel, color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = {
                        displayedCalendar = (displayedCalendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("Dépensé", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
                        Text("%.2f €".format(totalSpent), color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Budget", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
                        Text("%.0f €".format(totalIncome), color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                    }
                }

                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { globalProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = when {
                        globalProgress > 1f -> Color(0xFFEF5350)
                        globalProgress > 0.85f -> Color(0xFFFF7043)
                        else -> Color.White
                    },
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "%.0f €".format((totalIncome - totalSpent).coerceAtLeast(0.0)) + " restant",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Quick actions
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.AddCircle,
                label = "Ajouter",
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = { preselectedCategory = null; showAddDialog = true }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.PhotoCamera,
                label = "Scanner ticket",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = {
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> showScanner = true
                        else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        // Category budget cards
        Text(
            "Santé budgétaire",
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExpenseCategory.entries.forEach { cat ->
                val allocated = totalIncome * cat.percentage
                val spent = monthlyTransactions.filter { it.category == cat.name }.sumOf { it.amount }
                CategoryBudgetCard(
                    category = cat,
                    spent = spent,
                    allocated = allocated,
                    onAddClick = { preselectedCategory = cat; showAddDialog = true }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Redirect hint
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.width(4.dp))
                Text(
                    "Consultez l'historique détaillé dans le Calendrier",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            preselectedCategory = preselectedCategory,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amt, cat ->
                onAddTransaction(title, amt, cat, false, false)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CategoryBudgetCard(
    category: ExpenseCategory,
    spent: Double,
    allocated: Double,
    onAddClick: () -> Unit
) {
    val progress = if (allocated > 0) (spent / allocated).toFloat().coerceIn(0f, 1f) else 0f
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).background(bgColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(category.label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Text("${(category.percentage * 100).toInt()}% du budget", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(statusLabel, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = onAddClick,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = barColor,
                trackColor = barColor.copy(alpha = 0.15f)
            )

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("%.2f €".format(spent), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text("sur %.0f €".format(allocated), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    preselectedCategory: ExpenseCategory?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, ExpenseCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember(preselectedCategory) { mutableStateOf(preselectedCategory ?: ExpenseCategory.OBLIGATION) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle dépense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Description") },
                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Montant") },
                    prefix = { Text("€ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("Catégorie", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExpenseCategory.entries.forEach { cat ->
                        val selected = category == cat
                        Surface(
                            onClick = { category = cat },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) onConfirm(title, amt, category)
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Annuler") } }
    )
}

// Keep for backward compatibility with CalendarScreen
@Composable
fun ProfessionalExpenseItem(transaction: TransactionEntity, onDelete: () -> Unit) {
    val category = ExpenseCategory.entries.find { it.name == transaction.category } ?: ExpenseCategory.OBLIGATION
    val categoryIcon = when (category) {
        ExpenseCategory.OBLIGATION -> Icons.AutoMirrored.Filled.ReceiptLong
        ExpenseCategory.PLEASURE -> Icons.Default.Celebration
        ExpenseCategory.SAVING_DEBT -> Icons.Default.AccountBalance
    }
    val date = SimpleDateFormat("dd/MM", Locale.FRENCH).format(Date(transaction.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIcon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(category.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("• $date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Text("- %.2f €".format(transaction.amount), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
        }
    }
}
