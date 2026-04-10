package com.borg.budget.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BudgetScreen(
    totalIncome: Double,
    onIncomeChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val obligations = totalIncome * 0.50
    val pleasures = totalIncome * 0.30
    val savings = totalIncome * 0.20

    var incomeInput by remember(totalIncome) { mutableStateOf(if(totalIncome > 0) totalIncome.toString() else "") }

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
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Répartition du Budget",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DonutChart(
                        modifier = Modifier.size(120.dp),
                        proportions = listOf(0.5f, 0.3f, 0.2f),
                        colors = listOf(Color(0xFFE3F2FD), Color(0xFFF3E5F5), Color(0xFFE8F5E9)),
                        totalLabel = "%.0f€".format(totalIncome)
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Total Revenus",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "%.2f €".format(totalIncome),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BudgetSummaryItem(Modifier.weight(1f), "Fixes", obligations, Icons.Default.Wallet, Color(0xFFE3F2FD), Color(0xFF1976D2))
                    BudgetSummaryItem(Modifier.weight(1f), "Envies", pleasures, Icons.Default.ShoppingBag, Color(0xFFF3E5F5), Color(0xFF7B1FA2))
                    BudgetSummaryItem(Modifier.weight(1f), "Épargne", savings, Icons.Default.Savings, Color(0xFFE8F5E9), Color(0xFF388E3C))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "Définir mon revenu mensuel",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                OutlinedTextField(
                    value = incomeInput,
                    onValueChange = { 
                        incomeInput = it
                        it.toDoubleOrNull()?.let { amt -> onIncomeChange(amt) }
                    },
                    label = { Text("Somme totale disponible") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    prefix = { Icon(Icons.Default.EuroSymbol, null, Modifier.size(18.dp)) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
fun DonutChart(modifier: Modifier = Modifier, proportions: List<Float>, colors: List<Color>, totalLabel: String) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            proportions.forEachIndexed { index, proportion ->
                val sweepAngle = proportion * 360f
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 25f, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }
        Text(text = totalLabel, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}

@Composable
fun BudgetSummaryItem(modifier: Modifier, label: String, amount: Double, icon: ImageVector, color: Color, onColor: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = onColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = onColor.copy(alpha = 0.7f), maxLines = 1)
            Text("%.0f€".format(amount), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = onColor)
        }
    }
}
