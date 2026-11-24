package com.example.fianzapp.ui.landlord

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fianzapp.R
import com.example.fianzapp.SignaturePad
import com.example.fianzapp.data.remote.FIRESTORE_LEASE_ID
import com.example.fianzapp.data.remote.LEASES
import com.example.fianzapp.data.remote.REPORTS
import com.example.fianzapp.data.remote.ReportDocument
import com.example.fianzapp.decodeSignature
import com.example.fianzapp.ui.account.AccountViewModel
import com.example.fianzapp.ui.account.FirebaseNotConfigured
import com.example.fianzapp.ui.account.SignInForm
import com.example.fianzapp.ui.account.SignedInBar
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val timeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

/** A6: the landlord signs in with their own account and reads the reports of the lease, live. */
@Composable
fun LandlordScreen(
    modifier: Modifier = Modifier,
    accountViewModel: AccountViewModel = viewModel()
) {
    val email by accountViewModel.userEmail.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .widthIn(max = 640.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.landlord_title), style = MaterialTheme.typography.headlineSmall)
        val signedIn = email
        when {
            !accountViewModel.configured -> FirebaseNotConfigured()
            // Awkward case: signed out → asks to sign in.
            signedIn == null -> {
                Text(stringResource(R.string.landlord_sign_in_first))
                SignInForm(accountViewModel)
            }
            else -> {
                SignedInBar(signedIn, onSignOut = accountViewModel::signOut)
                LiveReports(key = signedIn)
            }
        }
    }
}

/** Listens to the reports while the screen is open (DisposableEffect, S9 Guide, section 11.2). */
@Composable
private fun LiveReports(key: String) {
    var reports by remember(key) { mutableStateOf<List<ReportDocument>?>(null) }
    var error by remember(key) { mutableStateOf<String?>(null) }

    DisposableEffect(key) {
        val registration = FirebaseFirestore.getInstance()
            .collection(LEASES).document(FIRESTORE_LEASE_ID).collection(REPORTS)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // A read the rules refuse is an error, not an empty list (S9 Guide, section 11.4).
                    error = e.message ?: e.javaClass.simpleName
                    return@addSnapshotListener
                }
                error = null
                reports = snapshot?.documents.orEmpty().mapNotNull { it.toObject(ReportDocument::class.java) }
            }
        onDispose { registration.remove() }
    }

    val current = reports
    when {
        error != null -> Text(
            stringResource(R.string.landlord_error, error.orEmpty()),
            color = MaterialTheme.colorScheme.error
        )
        current == null -> Text(stringResource(R.string.landlord_loading))
        current.isEmpty() -> Text(stringResource(R.string.landlord_no_reports))
        else -> current.sortedBy { it.phase }.forEach { ReportCard(it) }
    }
}

@Composable
private fun ReportCard(report: ReportDocument) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                stringResource(
                    if (report.phase == "MOVE_OUT") R.string.report_tab_move_out else R.string.report_tab_move_in
                ),
                style = MaterialTheme.typography.titleMedium
            )
            Text(report.address, style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(
                    R.string.report_signed_on,
                    Instant.ofEpochMilli(report.signedAt).atZone(ZoneId.systemDefault()).format(timeFormatter)
                ),
                style = MaterialTheme.typography.bodySmall
            )
            val items = report.rooms.flatMap { it.items }
            Text(
                stringResource(
                    R.string.landlord_counts,
                    report.rooms.size,
                    items.count { it.itemId != null },
                    items.count { it.itemId == null }
                ),
                style = MaterialTheme.typography.bodySmall
            )
            report.rooms.forEach { room ->
                Text(room.name, style = MaterialTheme.typography.labelLarge)
                room.items.forEach { item ->
                    Text(
                        "• " + (item.note ?: item.name) + (item.comparison?.let { " — $it" } ?: ""),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Text(stringResource(R.string.report_tenant), style = MaterialTheme.typography.labelMedium)
            SignaturePad(decodeSignature(report.tenantSignature), onSignatureChange = {}, enabled = false)
            Text(stringResource(R.string.report_landlord), style = MaterialTheme.typography.labelMedium)
            SignaturePad(decodeSignature(report.landlordSignature), onSignatureChange = {}, enabled = false)
        }
    }
}
