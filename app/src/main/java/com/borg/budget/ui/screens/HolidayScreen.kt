package com.borg.budget.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.borg.budget.ui.theme.*
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

    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(900, easing = EaseOutCubic), label = "holiday_progress")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = HorizonMid, contentColor = Color.White) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = modifier.padding(scaffoldPadding).fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item {
                // Header — Horizon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(HorizonDeep, HorizonMid, HorizonLight)),
                            RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                        )
                        .padding(top = 28.dp, start = 24.dp, end = 24.dp, bottom = 28.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BeachAccess, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Congés", color = Color.White.copy(0.85f), style = MaterialTheme.typography.bodyLarge)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Suivi annuel", color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

                        Spacer(Modifier.height(20.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            HorizonStatCard(Modifier.weight(1f), "$holidaysTaken", "jours pris")
                            HorizonStatCard(
                                Modifier.weight(1f), "$holidaysRemaining", "restants",
                                highlight = true,
                                overQuota = isOverQuota
                            )
                            HorizonStatCard(Modifier.weight(1f), "$totalQuota", "quota total")
                        }

                        Spacer(Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = if (isOverQuota) Color(0xFFEF5350) else Color.White,
                            trackColor = Color.White.copy(0.25f)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "$holidaysTaken / $totalQuota jours · ${(progress * 100).toInt()}%",
                            color = Color.White.copy(0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            if (holidays.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxWidth().padding(vertical = 64.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.BeachAccess, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(0.4f))
                            Spacer(Modifier.height(16.dp))
                            Text("Aucun congé posé", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(4.dp))
                            Text("Appuyez sur + pour en ajouter", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline.copy(0.6f), textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                item {
                    Spacer(Modifier.height(20.dp))
                    Text("Congés posés (${holidays.size})", Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(12.dp))
                }
                items(holidays.sortedByDescending { it.startDate }) { holiday ->
                    HolidayCard(holiday = holiday, onDelete = { onDeleteHoliday(holiday) }, modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddHolidayDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, start, end, days -> onAddHoliday(title, start, end, days); showAddDialog = false }
        )
    }
}

@Composable
private fun HorizonStatCard(modifier: Modifier, value: String, label: String, highlight: Boolean = false, overQuota: Boolean = false) {
    val bg = when { overQuota && highlight -> Color(0xFFEF5350).copy(0.3f); highlight -> Color.White.copy(0.25f); else -> Color.White.copy(0.12f) }
    Box(
        modifier = modifier
            .border(1.dp, Color.White.copy(if (highlight) 0.4f else 0.2f), RoundedCornerShape(16.dp))
            .background(bg, RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.Bold), fontSize = if (highlight) 26.sp else 22.sp)
            Text(label, color = Color.White.copy(0.8f), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun HolidayCard(holiday: HolidayEntity, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val fmt = SimpleDateFormat("dd MMM", Locale.FRENCH)
    val start = fmt.format(Date(holiday.startDate))
    val end = fmt.format(Date(holiday.endDate))

    Card(modifier.fillMaxWidth(), RoundedCornerShape(18.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surface), CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).background(HorizonLight.copy(0.15f), RoundedCornerShape(14.dp)), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${holiday.daysCount}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = HorizonMid)
                    Text("j.", fontSize = 10.sp, color = HorizonMid)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(holiday.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.width(4.dp))
                    Text("$start  →  $end", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Box(Modifier.background(HorizonMid.copy(0.1f), CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text("${holiday.daysCount} j", fontSize = 11.sp, color = HorizonMid, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDelete, Modifier.size(36.dp)) {
                Icon(Icons.Default.DeleteOutline, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error.copy(0.6f))
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
    val formattedDate = remember(selectedMillis) { SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(Date(selectedMillis)) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Annuler") } }
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BeachAccess, null, tint = HorizonMid, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Poser des congés")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Motif") }, placeholder = { Text("Vacances, RTT…") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(
                    value = formattedDate, onValueChange = {}, label = { Text("Date de début") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.EditCalendar, null) } },
                    modifier = Modifier.fillMaxWidth(), readOnly = true, singleLine = true
                )
                OutlinedTextField(value = daysCount, onValueChange = { if (it.length <= 3) daysCount = it }, label = { Text("Nombre de jours") }, leadingIcon = { Icon(Icons.Default.Schedule, null) }, suffix = { Text("jours") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)

                val days = daysCount.toIntOrNull() ?: 0
                if (days > 0) {
                    val endMillis = selectedMillis + (86_400_000L * days)
                    val endFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).format(Date(endMillis))
                    Surface(shape = RoundedCornerShape(10.dp), color = HorizonMid.copy(0.08f)) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, Modifier.size(14.dp), tint = HorizonMid)
                            Spacer(Modifier.width(8.dp))
                            Text("Du $formattedDate au $endFormatted", style = MaterialTheme.typography.labelSmall, color = HorizonDeep)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysCount.toIntOrNull() ?: 0
                    if (title.isNotBlank() && days > 0) onConfirm(title, selectedMillis, selectedMillis + (86_400_000L * days), days)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HorizonMid)
            ) { Text("Confirmer") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Annuler") } }
    )
}
