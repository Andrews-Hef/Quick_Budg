package com.borg.budget.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.borg.budget.data.TransactionEntity
import com.borg.budget.ui.models.ExpenseCategory

@Composable
fun ExpenseScreen(
    totalIncome: Double,
    transactions: List<TransactionEntity>,
    onAddTransaction: (String, Double, ExpenseCategory, Boolean, Boolean) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showScanner = true
    }

    if (showScanner) {
        ScannerScreen(
            onReceiptScanned = { merchant, _, amount ->
                onAddTransaction(merchant, amount, ExpenseCategory.PLEASURE, false, false)
                showScanner = false
            },
            onClose = { showScanner = false }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text("Suivi des Dépenses", color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    
                    ExpenseCategory.entries.forEach { cat ->
                        val allocated = totalIncome * cat.percentage
                        val spent = transactions.filter { it.category == cat.name }.sumOf { it.amount }
                        ProfessionalProgressRow(cat, spent, allocated)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transactions récentes", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                
                // OCR Button
                FilledIconButton(
                    onClick = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                                showScanner = true
                            }
                            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(Icons.Default.PhotoCamera, "Scanner un ticket")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                items(transactions) { transaction ->
                    ProfessionalExpenseItem(
                        transaction = transaction,
                        onDelete = { onDeleteTransaction(transaction) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nouvelle transaction", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amt, cat, isSub, isDebt ->
                onAddTransaction(title, amt, cat, isSub, isDebt)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProfessionalProgressRow(category: ExpenseCategory, spent: Double, allocated: Double) {
    val progress = if (allocated > 0) (spent / allocated).toFloat() else 0f
    val isOver = spent > allocated
    val statusColor = if (isOver) Color(0xFFEF5350) else Color.White
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(category.label, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
            Text("%.2f / %.0f €".format(spent, allocated), color = statusColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = if (isOver) Color(0xFFFFCDD2) else Color.White,
            trackColor = Color.White.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun ProfessionalExpenseItem(transaction: TransactionEntity, onDelete: () -> Unit) {
    val category = ExpenseCategory.entries.find { it.name == transaction.category } ?: ExpenseCategory.OBLIGATION
    val categoryIcon = when(category) {
        ExpenseCategory.OBLIGATION -> Icons.Default.ReceiptLong
        ExpenseCategory.PLEASURE -> Icons.Default.Celebration
        ExpenseCategory.SAVING_DEBT -> Icons.Default.AccountBalance
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(categoryIcon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(category.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    if (transaction.isSubscription) {
                        Box(Modifier.padding(start = 8.dp).background(Color(0xFFE8F5E9), CircleShape).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("ABO", fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Text("- %.2f €".format(transaction.amount), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
            IconButton(onClick = onDelete) { Icon(Icons.Default.Close, null, tint = Color.LightGray, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
fun AddExpenseDialog(onDismiss: () -> Unit, onConfirm: (String, Double, ExpenseCategory, Boolean, Boolean) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.OBLIGATION) }
    var isSub by remember { mutableStateOf(false) }
    var isDebt by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle dépense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Montant") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), prefix = { Text("€ ") })
                Text("Catégorie :", style = MaterialTheme.typography.labelLarge)
                Column {
                    ExpenseCategory.entries.forEach { cat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(category == cat, { category = cat })
                            Text(cat.label)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(isSub, { isSub = it }); Text("Abonnement") }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(isDebt, { isDebt = it }); Text("Dette/Remboursement") }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (title.isNotBlank() && amt > 0) onConfirm(title, amt, category, isSub, isDebt)
            }) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Annuler") } }
    )
}
