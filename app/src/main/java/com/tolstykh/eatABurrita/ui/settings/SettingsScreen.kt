package com.tolstykh.eatABurrita.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tolstykh.eatABurrita.data.BurritoEntry
import com.tolstykh.eatABurrita.dateFromMilliseconds
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var showResetConfirm by rememberSaveable { mutableStateOf(false) }
    var isAddingEntry by rememberSaveable { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<BurritoEntry?>(null) }
    // editorStep: 1 = date picker, 2 = time picker
    var editorStep by rememberSaveable { mutableIntStateOf(1) }
    // selectedDateMillis holds UTC-midnight date millis after date picker confirms
    var selectedDateMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }

    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    val pageSize = 30
    val totalPages = maxOf(1, (entries.size + pageSize - 1) / pageSize)
    val safePage = currentPage.coerceIn(0, totalPages - 1)
    if (currentPage != safePage) currentPage = safePage
    val pagedEntries = entries.drop(safePage * pageSize).take(pageSize)

    Surface(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(Modifier.height(16.dp))

        // Appearance
        Text("Appearance", style = MaterialTheme.typography.titleMedium, color = colorScheme.primary)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Dark mode", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = isDarkMode, onCheckedChange = viewModel::toggleDarkMode)
        }

        Spacer(Modifier.height(24.dp))

        // Entries
        Text("Entries", style = MaterialTheme.typography.titleMedium, color = colorScheme.primary)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Rounded card with elevated tonal background signals a scrollable region
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // 48.dp per row × 10 rows; inner scroll lets the rest be reached
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp),
            ) {
                pagedEntries.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = dateFromMilliseconds(entry.timestamp),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = {
                            editingEntry = entry
                            selectedDateMillis = entry.timestamp
                            editorStep = 1
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { viewModel.deleteEntry(entry) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = colorScheme.error)
                        }
                    }
                }
            }
        }

        if (totalPages > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { currentPage-- },
                    enabled = safePage > 0,
                ) { Text("← Prev") }
                Text(
                    "${safePage + 1} / $totalPages",
                    style = MaterialTheme.typography.bodySmall,
                )
                TextButton(
                    onClick = { currentPage++ },
                    enabled = safePage < totalPages - 1,
                ) { Text("Next →") }
            }
        }

        OutlinedButton(
            onClick = {
                isAddingEntry = true
                selectedDateMillis = System.currentTimeMillis()
                editorStep = 1
            },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text("+ Add Entry")
        }

        Spacer(Modifier.height(32.dp))

        // Danger zone
        Text("Danger Zone", style = MaterialTheme.typography.titleMedium, color = colorScheme.error)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Button(
            onClick = { showResetConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Reset All Stats")
        }

        Spacer(Modifier.height(32.dp))
    }
    } // end Surface

    // Reset confirmation
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset all stats?") },
            text = { Text("This will permanently delete all burrito entries.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAll()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error),
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            },
        )
    }

    // Date picker (step 1) — for both add and edit
    val showDatePicker = (isAddingEntry || editingEntry != null) && editorStep == 1
    if (showDatePicker) {
        val initialUtcMillis = normalizeToUtcMidnight(selectedDateMillis)
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialUtcMillis)
        DatePickerDialog(
            onDismissRequest = {
                isAddingEntry = false
                editingEntry = null
            },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    editorStep = 2
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = {
                    isAddingEntry = false
                    editingEntry = null
                }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time picker (step 2)
    val showTimePicker = (isAddingEntry || editingEntry != null) && editorStep == 2
    if (showTimePicker) {
        val originalTimestamp = editingEntry?.timestamp ?: System.currentTimeMillis()
        val originalCal = Calendar.getInstance().apply { timeInMillis = originalTimestamp }
        val timePickerState = rememberTimePickerState(
            initialHour = if (editingEntry != null) originalCal.get(Calendar.HOUR_OF_DAY)
                          else Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            initialMinute = if (editingEntry != null) originalCal.get(Calendar.MINUTE)
                            else Calendar.getInstance().get(Calendar.MINUTE),
        )
        // Capture before lambda to avoid stale closure issues
        val isAdding = isAddingEntry
        val entryToEdit = editingEntry
        AlertDialog(
            onDismissRequest = {
                isAddingEntry = false
                editingEntry = null
                editorStep = 1
            },
            confirmButton = {
                TextButton(onClick = {
                    val newTimestamp = combineDateAndTime(
                        utcMidnightMillis = selectedDateMillis,
                        hour = timePickerState.hour,
                        minute = timePickerState.minute,
                    )
                    if (isAdding) {
                        viewModel.addEntry(newTimestamp)
                    } else {
                        entryToEdit?.let { viewModel.updateEntry(it.copy(timestamp = newTimestamp)) }
                    }
                    isAddingEntry = false
                    editingEntry = null
                    editorStep = 1
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    isAddingEntry = false
                    editingEntry = null
                    editorStep = 1
                }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) },
        )
    }
}

private fun normalizeToUtcMidnight(millis: Long): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = millis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun combineDateAndTime(utcMidnightMillis: Long, hour: Int, minute: Int): Long {
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = utcMidnightMillis
    }
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
        set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
