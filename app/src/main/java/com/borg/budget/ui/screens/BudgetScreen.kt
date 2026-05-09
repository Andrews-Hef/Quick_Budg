package com.borg.budget.ui.screens

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
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.borg.budget.ui.theme.*

@Composable
fun BudgetScreen(
    totalIncome: Double,
    onIncomeChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val obligations = totalIncome * 0.50
    val pleasures = totalIncome * 0.30
    val savings = totalIncome * 0.20

    var incomeInput by remember(totalIncome) { mutableStateOf(if (totalIncome > 0) totalIncome.toString() else "") }

    val animatedObl by animateFloatAsState(targetValue = if (totalIncome > 0) 0.5f else 0f, animationSpec = tween(900, easing = EaseOutCubic), label = "obl")
    val animatedPls by animateFloatAsState(targetValue = if (totalIncome > 0) 0.3f else 0f, animationSpec = tween(900, 100, EaseOutCubic), label = "pls")
    val animatedSav by animateFloatAsState(targetValue = if (totalIncome > 0) 0.2f else 0f, animationSpec = tween(900, 200, EaseOutCubic), label = "sav")

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        // Header — Emerald
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(EmeraldDeep, EmeraldMid, EmeraldLight)),
                    RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
                .padding(top = 28.dp, start = 24.dp, end = 24.dp, bottom = 28.dp)
        ) {
            Column {
                Text("Budget", color = Color.White.copy(0.85f), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(4.dp))
                Text("Répartition 50/30/20", color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

                Spacer(Modifier.height(20.dp))

                // Glass income card
                Box(
                    Modifier.fillMaxWidth()
                        .border(1.dp, Color.White.copy(0.25f), RoundedCornerShape(18.dp))
                        .background(Color.White.copy(0.12f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column {
                            Text("Revenus mensuels", color = Color.White.copy(0.75f), style = MaterialTheme.typography.labelMedium)
                            Text("%.2f €".format(totalIncome), color = Color.White, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Icon(Icons.Default.EuroSymbol, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EmeraldStatChip(Modifier.weight(1f), "Fixes", "%.0f €".format(obligations), Icons.Default.Wallet)
                    EmeraldStatChip(Modifier.weight(1f), "Envies", "%.0f €".format(pleasures), Icons.Default.ShoppingBag)
                    EmeraldStatChip(Modifier.weight(1f), "Épargne", "%.0f €".format(savings), Icons.Default.Savings)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Income input
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text("Définir mon revenu", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = incomeInput,
                onValueChange = { v ->
                    incomeInput = v
                    v.toDoubleOrNull()?.let { onIncomeChange(it) }
                },
                label = { Text("Revenu mensuel net") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Icon(Icons.Default.EuroSymbol, null, Modifier.size(18.dp)) },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        Spacer(Modifier.height(28.dp))

        // Progress bars
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Détail", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(4.dp))
            BudgetRuleCard("Charges fixes", "50 %", "%.2f €".format(obligations), animatedObl, EmeraldMid, Icons.Default.Wallet)
            BudgetRuleCard("Envies / loisirs", "30 %", "%.2f €".format(pleasures), animatedPls, EmeraldLight, Icons.Default.ShoppingBag)
            BudgetRuleCard("Épargne", "20 %", "%.2f €".format(savings), animatedSav, EmeraldDeep, Icons.Default.Savings)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun EmeraldStatChip(modifier: Modifier, label: String, value: String, icon: ImageVector) {
    Box(
        modifier = modifier
            .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(14.dp))
            .background(Color.White.copy(0.1f), RoundedCornerShape(14.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, color = Color.White, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), maxLines = 1)
            Text(label, color = Color.White.copy(0.65f), style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
private fun BudgetRuleCard(label: String, pct: String, amount: String, progress: Float, color: Color, icon: ImageVector) {
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surface), CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(36.dp).background(color.copy(0.12f), CircleShape), Alignment.Center) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                    }
                    Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(amount, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = color)
                    Text(pct, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = color,
                trackColor = color.copy(0.15f)
            )
        }
    }
}
