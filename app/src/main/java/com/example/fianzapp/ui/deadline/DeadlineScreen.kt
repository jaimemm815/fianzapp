package com.example.fianzapp.ui.deadline

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fianzapp.R
import com.example.fianzapp.domain.DeadlineStatus
import com.example.fianzapp.domain.deadlineStatus
import com.example.fianzapp.domain.depositDeadline
import com.example.fianzapp.domain.formatCents
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

private const val BOE_LAU = "https://www.boe.es/buscar/act.php?id=BOE-A-1994-26003"
private const val BOE_CIVIL_CODE = "https://www.boe.es/buscar/act.php?id=BOE-A-1889-4763"

/** A7: the deposit deadline of the saved lease, worked out by depositDeadline(). */
@Composable
fun DeadlineScreen(
    onOpenLease: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeadlineViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        DeadlineUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        DeadlineUiState.NoLease -> EmptyMessage(
            text = stringResource(R.string.deadline_no_lease),
            onOpenLease = onOpenLease,
            modifier = modifier
        )
        // A lease with no day of handing back the keys yet is not an error.
        is DeadlineUiState.KeysNotReturned -> EmptyMessage(
            text = stringResource(R.string.deadline_no_keys),
            onOpenLease = onOpenLease,
            modifier = modifier
        )
        is DeadlineUiState.Ready -> DeadlineContent(s, modifier)
    }
}

@Composable
private fun DeadlineContent(state: DeadlineUiState.Ready, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val deadline = depositDeadline(state.keysReturned)
    val today = LocalDate.now()
    val status = deadlineStatus(deadline, today)
    var showHowCounted by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.widthIn(max = 640.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(deadline, status)

            DetailsCard(state, deadline)

            Timeline(state.moveIn, state.keysReturned, deadline, today)

            val calendarTitle = stringResource(R.string.deadline_calendar_title)
            val calendarText = stringResource(R.string.deadline_calendar_description, state.lease.address)
            val noCalendar = stringResource(R.string.deadline_no_calendar)
            OutlinedButton(
                onClick = { addToCalendar(context, deadline, calendarTitle, calendarText, noCalendar) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.DateRange, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.deadline_add_calendar))
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    TextButton(onClick = { showHowCounted = !showHowCounted }) {
                        Text(
                            stringResource(R.string.deadline_how_counted),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            if (showHowCounted) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                    if (showHowCounted) {
                        Text(stringResource(R.string.deadline_how_counted_text), style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.deadline_example), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { openLink(context, BOE_LAU) }) {
                            Text(stringResource(R.string.deadline_link_lau))
                        }
                        TextButton(onClick = { openLink(context, BOE_CIVIL_CODE) }) {
                            Text(stringResource(R.string.deadline_link_civil_code))
                        }
                    }
                }
            }

            Text(
                stringResource(R.string.not_legal_advice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryCard(deadline: LocalDate, status: DeadlineStatus) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.deadline_summary_label), style = MaterialTheme.typography.labelLarge)
            Text(
                deadline.format(dateFormatter),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            val (text, colour) = when (status) {
                is DeadlineStatus.DaysLeft -> stringResource(R.string.deadline_days_left, status.days) to
                    MaterialTheme.colorScheme.primary
                DeadlineStatus.Today -> stringResource(R.string.deadline_today) to
                    MaterialTheme.colorScheme.tertiary
                is DeadlineStatus.Passed -> stringResource(R.string.deadline_passed, status.daysAgo) to
                    MaterialTheme.colorScheme.error
            }
            Text(
                text,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .background(colour, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun DetailsCard(state: DeadlineUiState.Ready, deadline: LocalDate) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            DetailRow(stringResource(R.string.deadline_detail_address), state.lease.address)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            DetailRow(
                stringResource(R.string.deadline_detail_deposit),
                stringResource(R.string.deadline_euros, formatCents(state.lease.depositCents))
            )
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            DetailRow(stringResource(R.string.deadline_detail_keys), state.keysReturned.format(dateFormatter))
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            DetailRow(stringResource(R.string.deadline_detail_deadline), deadline.format(dateFormatter))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f)
        )
    }
}

/** Move-in → keys handed back → deadline. A step is filled when its day has come. */
@Composable
private fun Timeline(moveIn: LocalDate?, keys: LocalDate, deadline: LocalDate, today: LocalDate) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.deadline_timeline), style = MaterialTheme.typography.titleMedium)
            moveIn?.let { TimelineStep(stringResource(R.string.deadline_step_move_in), it, !it.isAfter(today)) }
            TimelineStep(stringResource(R.string.deadline_step_keys), keys, !keys.isAfter(today))
            TimelineStep(stringResource(R.string.deadline_step_deadline), deadline, !deadline.isAfter(today))
        }
    }
}

@Composable
private fun TimelineStep(label: String, date: LocalDate, reached: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    CircleShape
                )
        )
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(date.format(dateFormatter), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun EmptyMessage(text: String, onOpenLease: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Button(onClick = onOpenLease) { Text(stringResource(R.string.deadline_open_lease)) }
        Text(
            stringResource(R.string.not_legal_advice),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

/** An all-day event on the deadline in the person's calendar app. Needs no permission. */
private fun addToCalendar(context: Context, deadline: LocalDate, title: String, description: String, noApp: String) {
    val start = deadline.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val intent = Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.Events.TITLE, title)
        .putExtra(CalendarContract.Events.DESCRIPTION, description)
        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, noApp, Toast.LENGTH_LONG).show()
    }
}

private fun openLink(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, url, Toast.LENGTH_LONG).show()
    }
}
