package com.borg.budget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borg.budget.data.HolidayEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayScreen(
    holidays: List<HolidayEntity>,
    totalQuota: Int,
    onAddHoliday: (String, Long, Long, Int) -> Unit,
    onDeleteHoliday: (HolidayEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val holidaysTaken = holidays.sumOf { it.daysCount }
    val holidaysRemaining = (totalQuota - holidaysTaken).coerceAtLeast(0)
    val progress = if (totalQuota > 0) (holidaysTaken.toFloat() / totalQuota).coerceIn(0f, 1f) else 0f
    val isOverQuota = holidaysTaken > totalQuota

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF00897B), Color(0xFF4DB6AC))),
                        RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BeachAccess, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Mes Congés", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        HolidayStatCard(Modifier.weight(1f), "$holidaysTaken", "jours pris", Color.White.copy(alpha = 0.15f))
                        HolidayStatCard(Modifier.weight(1f), "$holidaysRemaining", "restants",
                            if (isOverQuota) Color(0xFFEF5350).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.25f),
                            highlight = true
                        )
                        HolidayStatCard(Modifier.weight(1f), "$totalQuota", "quota total", Color.White.copy(alpha = 0.15f))
                    }

                    Spacer(Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = if (isOverQuota) Color(0xFFEF5350) else Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "$holidaysTaken / $totalQuota jours utilisés · ${(progress * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            if (holidays.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.BeachAccess,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Aucun congé posé",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Appuyez sur + pour en ajouter",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Congés posés (${holidays.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(holidays.sortedByDescending { it.startDate }) { holiday ->
                        HolidayCard(holiday = holiday, onDelete = { onDeleteHoliday(holiday) })
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = Color(0xFF00897B),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ajouter des congés")
        }
    }

    if (showAddDialog) {
        AddHolidayDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, start, end, days ->
                onAddHoliday(title, start, end, days)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun HolidayStatCard(modifier: Modifier, value: String, label: String, bgColor: Color, highlight: Boolean = false) {
    Box(
        modifier = modifier.background(bgColor, RoundedCornerShape(14.dp)).padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                value,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.Bold),
                fontSize = if (highlight) 26.sp else 22.sp
            )
            Text(label, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun HolidayCard(holiday: HolidayEntity, onDelete: () -> Unit) {
    val fmt = SimpleDateFormat("dd MMM", Locale.FRENCH)
    val start = fmt.format(Date(holiday.startDate))
    val end = fmt.format(Date(holiday.endDate))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(52.dp).background(Color(0xFFE0F2F1), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${holiday.daysCount}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF00897B)
                    )
                    Text("j.", fontSize = 10.sp, color = Color(0xFF00897B))
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(holiday.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "$start  →  $end",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Box(
                modifier = Modifier.background(Color(0xFFE0F2F1), CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("${holiday.daysCount} jours", fontSize = 11.sp, color = Color(0xFF00897B), fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHolidayDialog(onDismiss: () -> Unit, onConfirm: (String, Long, Long, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var daysCount by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    val selectedMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
    val formattedDate = remember(selectedMillis) {
        SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(Date(selectedMillis))
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BeachAccess, null, tint = Color(0xFF00897B), modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Poser des congés")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Motif") },
                    placeholder = { Text("Vacances d'été, RTT…") },
                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = formattedDate,
                    onValueChange = {},
                    label = { Text("Date de début") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.EditCalendar, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true
                )

                OutlinedTextField(
                    value = daysCount,
                    onValueChange = { if (it.length <= 3) daysCount = it },
                    label = { Text("Nombre de jours") },
                    leadingIcon = { Icon(Icons.Default.Schedule, null) },
                    suffix = { Text("jours") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                val days = daysCount.toIntOrNull() ?: 0
                if (days > 0) {
                    val endMillis = selectedMillis + (86_400_000L * days)
                    val endFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(Date(endMillis))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE0F2F1)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(14.dp), tint = Color(0xFF00897B))
                            Spacer(Modifier.width(8.dp))
                            Text("Du $formattedDate au $endFormatted", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00695C))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysCount.toIntOrNull() ?: 0
                    if (title.isNotBlank() && days > 0) {
                        val endMillis = selectedMillis + (86_400_000L * days)
                        onConfirm(title, selectedMillis, endMillis, days)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
            ) { Text("Confirmer") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Annuler") } }
    )
}
