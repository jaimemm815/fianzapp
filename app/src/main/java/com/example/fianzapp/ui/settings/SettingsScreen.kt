package com.example.fianzapp.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fianzapp.R
import com.example.fianzapp.domain.MAX_WARNING_DAYS
import com.example.fianzapp.reminder.canNotify

/** A3 and A7: the days of warning before the deadline, and the switch of the reminder. */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val savedDays by viewModel.warningDays.collectAsStateWithLifecycle()
    val reminderOn by viewModel.reminderOn.collectAsStateWithLifecycle()

    var daysText by rememberSaveable { mutableStateOf("") }
    var edited by rememberSaveable { mutableStateOf(false) }
    var daysError by rememberSaveable { mutableStateOf(false) }
    var daysSaved by rememberSaveable { mutableStateOf(false) }
    var permissionDenied by rememberSaveable { mutableStateOf(false) }

    // Show the saved value until the person types.
    LaunchedEffect(savedDays) { if (!edited) daysText = savedDays.toString() }

    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // Awkward case: a "no" keeps the reminder off and the app does not insist.
        permissionDenied = !granted
        viewModel.setReminder(granted)
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .widthIn(max = 640.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)

        Text(stringResource(R.string.settings_days_title), style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = daysText,
            onValueChange = {
                daysText = it
                edited = true
                daysError = false
                daysSaved = false
            },
            label = { Text(stringResource(R.string.settings_days_label)) },
            isError = daysError,
            supportingText = {
                Text(
                    when {
                        daysError -> stringResource(R.string.settings_days_error, MAX_WARNING_DAYS, savedDays)
                        daysSaved -> stringResource(R.string.settings_days_saved, savedDays)
                        else -> stringResource(R.string.settings_days_hint, MAX_WARNING_DAYS)
                    }
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            val ok = viewModel.saveWarningDays(daysText)
            daysError = !ok
            daysSaved = ok
            if (ok) edited = false
        }) { Text(stringResource(R.string.lease_save)) }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_reminder_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.settings_reminder_text), style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = reminderOn,
                onCheckedChange = { on ->
                    if (on && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !canNotify(context)) {
                        // Asked when the person turns the reminder on, not when the app starts.
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        permissionDenied = false
                        viewModel.setReminder(on)
                    }
                }
            )
        }
        if (permissionDenied) {
            Text(
                stringResource(R.string.settings_reminder_denied),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(stringResource(R.string.settings_reminder_not_exact), style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = viewModel::checkNow, enabled = reminderOn) {
            Text(stringResource(R.string.settings_check_now))
        }
    }
}
