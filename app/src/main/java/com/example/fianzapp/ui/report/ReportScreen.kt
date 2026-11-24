package com.example.fianzapp.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fianzapp.R
import com.example.fianzapp.SignaturePad
import com.example.fianzapp.data.db.Phase
import com.example.fianzapp.decodeSignature
import com.example.fianzapp.encodeSignature
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val signedAtFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

/** The move-in and move-out reports, signed on the phone by the tenant and the landlord. */
@Composable
fun ReportScreen(
    onOpenLease: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        ReportUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        ReportUiState.NoLease -> Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.report_no_lease),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Button(onClick = onOpenLease) { Text(stringResource(R.string.deadline_open_lease)) }
        }
        is ReportUiState.Ready -> ReportContent(s, viewModel, modifier)
    }
}

@Composable
private fun ReportContent(state: ReportUiState.Ready, viewModel: ReportViewModel, modifier: Modifier = Modifier) {
    var phaseIndex by rememberSaveable { mutableStateOf(0) }
    val phase = if (phaseIndex == 0) Phase.MOVE_IN else Phase.MOVE_OUT
    val summary = state.summary(phase)

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = phaseIndex) {
            Tab(
                selected = phaseIndex == 0,
                onClick = { phaseIndex = 0 },
                text = { Text(stringResource(R.string.report_tab_move_in)) }
            )
            Tab(
                selected = phaseIndex == 1,
                onClick = { phaseIndex = 1 },
                text = { Text(stringResource(R.string.report_tab_move_out)) }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .widthIn(max = 640.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(state, summary)

            Text(
                stringResource(
                    if (phase == Phase.MOVE_IN) R.string.report_declaration_move_in
                    else R.string.report_declaration_move_out,
                    state.lease.address
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            when {
                summary.isSigned -> SignedReport(summary)
                // Awkward case: nothing to sign yet.
                !summary.hasPhotos -> Text(
                    stringResource(R.string.report_no_photos),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                else -> SigningForm(
                    phase = phase,
                    onSign = { tenant, landlord -> viewModel.sign(state.lease.id, phase, tenant, landlord) }
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(state: ReportUiState.Ready, summary: PhaseSummary) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.report_summary_title), style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SummaryRow(stringResource(R.string.deadline_detail_address), state.lease.address)
            SummaryRow(stringResource(R.string.report_landlord), state.lease.landlordName)
            SummaryRow(stringResource(R.string.report_move_in_date), state.lease.moveInDate)
            SummaryRow(stringResource(R.string.report_rooms), state.rooms.toString())
            SummaryRow(stringResource(R.string.report_item_photos), summary.itemPhotos.toString())
            SummaryRow(stringResource(R.string.report_damages), summary.damages.toString())
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f)
        )
    }
}

/** Two pads, one for each person. The drafts are text, so they survive a rotation. */
@Composable
private fun SigningForm(phase: Phase, onSign: (tenant: String, landlord: String) -> Unit) {
    var tenant by rememberSaveable(phase) { mutableStateOf("") }
    var landlord by rememberSaveable(phase) { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf(false) }

    SignatureBlock(
        label = stringResource(R.string.report_tenant),
        text = tenant,
        onChange = { tenant = it },
        onClear = { tenant = "" }
    )
    SignatureBlock(
        label = stringResource(R.string.report_landlord),
        text = landlord,
        onChange = { landlord = it },
        onClear = { landlord = "" }
    )
    Button(
        onClick = { confirm = true },
        enabled = tenant.isNotBlank() && landlord.isNotBlank(),
        modifier = Modifier.fillMaxWidth()
    ) { Text(stringResource(R.string.report_sign)) }
    Text(
        stringResource(
            if (tenant.isBlank() || landlord.isBlank()) R.string.report_both_must_sign
            else R.string.report_ready_to_sign
        ),
        style = MaterialTheme.typography.bodySmall
    )

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(stringResource(R.string.report_confirm_title)) },
            text = { Text(stringResource(R.string.report_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    onSign(tenant, landlord)
                }) { Text(stringResource(R.string.report_sign)) }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text(stringResource(android.R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun SignatureBlock(label: String, text: String, onChange: (String) -> Unit, onClear: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onClear, enabled = text.isNotBlank()) {
                Text(stringResource(R.string.signature_clear))
            }
        }
        SignaturePad(
            signature = decodeSignature(text),
            onSignatureChange = { onChange(encodeSignature(it)) }
        )
    }
}

/** A signed report: read only, with when it was signed. */
@Composable
private fun SignedReport(summary: PhaseSummary) {
    val report = summary.report ?: return
    val signedAt = report.signedAt ?: return
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            stringResource(
                R.string.report_signed_on,
                Instant.ofEpochMilli(signedAt).atZone(ZoneId.systemDefault()).format(signedAtFormatter)
            ),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(16.dp)
        )
    }
    Text(stringResource(R.string.report_tenant), style = MaterialTheme.typography.titleSmall)
    SignaturePad(signature = decodeSignature(report.tenantSignature), onSignatureChange = {}, enabled = false)
    Text(stringResource(R.string.report_landlord), style = MaterialTheme.typography.titleSmall)
    SignaturePad(signature = decodeSignature(report.landlordSignature), onSignatureChange = {}, enabled = false)
    Text(
        stringResource(R.string.report_locked),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
