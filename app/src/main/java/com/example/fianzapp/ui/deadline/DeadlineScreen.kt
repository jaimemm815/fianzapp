package com.example.fianzapp.ui.deadline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fianzapp.R
import com.example.fianzapp.domain.depositDeadline
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * A7, first part: pick the day you handed back the keys and see the deadline.
 * The reminder with WorkManager is still to do.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeadlineScreen(modifier: Modifier = Modifier) {
    // Kept as epoch day, so that it survives a rotation.
    var keysEpochDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.deadline_intro))
        Button(onClick = { showPicker = true }) {
            Text(stringResource(R.string.deadline_pick_date))
        }
        keysEpochDay?.let { epochDay ->
            val keys = LocalDate.ofEpochDay(epochDay)
            Text(stringResource(R.string.deadline_keys_returned, keys.format(formatter)))
            Text(
                stringResource(R.string.deadline_result, depositDeadline(keys).format(formatter)),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            stringResource(R.string.not_legal_advice),
            style = MaterialTheme.typography.bodySmall
        )
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // The picker gives midnight UTC of the chosen day.
                    pickerState.selectedDateMillis?.let { millis ->
                        keysEpochDay = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                    }
                    showPicker = false
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
