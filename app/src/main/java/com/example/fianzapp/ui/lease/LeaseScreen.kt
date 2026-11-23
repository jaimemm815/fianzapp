package com.example.fianzapp.ui.lease

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fianzapp.R
import com.example.fianzapp.domain.LeaseCheck
import com.example.fianzapp.domain.LeaseFieldError
import com.example.fianzapp.domain.checkLease
import com.example.fianzapp.domain.formatCents

/** The text of the five fields. Kept as plain text so that it survives a rotation. */
data class LeaseFormValues(
    val address: String = "",
    val landlordName: String = "",
    val moveInDate: String = "",
    val deposit: String = "",
    val keysReturnedDate: String = ""
)

/** A1: owns the state of the form and talks to the ViewModel. */
@Composable
fun LeaseScreen(
    modifier: Modifier = Modifier,
    viewModel: LeaseViewModel = viewModel()
) {
    val saved by viewModel.savedLease.collectAsStateWithLifecycle()
    val justSaved by viewModel.justSaved.collectAsStateWithLifecycle()

    var address by rememberSaveable { mutableStateOf("") }
    var landlordName by rememberSaveable { mutableStateOf("") }
    var moveInDate by rememberSaveable { mutableStateOf("") }
    var deposit by rememberSaveable { mutableStateOf("") }
    var keysReturnedDate by rememberSaveable { mutableStateOf("") }
    var prefilled by rememberSaveable { mutableStateOf(false) }

    // Fill the form once with the saved lease, so that Save edits it instead of starting again.
    LaunchedEffect(saved) {
        val current = saved
        if (!prefilled && current is SavedLease.Saved) {
            address = current.lease.address
            landlordName = current.lease.landlordName
            moveInDate = current.lease.moveInDate
            deposit = formatCents(current.lease.depositCents)
            keysReturnedDate = current.lease.keysReturnedDate.orEmpty()
            prefilled = true
        }
    }

    val values = LeaseFormValues(address, landlordName, moveInDate, deposit, keysReturnedDate)
    val check = checkLease(address, landlordName, moveInDate, deposit, keysReturnedDate)

    LeaseForm(
        values = values,
        check = check,
        hasSavedLease = saved is SavedLease.Saved,
        justSaved = justSaved,
        onChange = { new ->
            address = new.address
            landlordName = new.landlordName
            moveInDate = new.moveInDate
            deposit = new.deposit
            keysReturnedDate = new.keysReturnedDate
            viewModel.onEdited()
        },
        onSave = { viewModel.save(check, address, landlordName) },
        modifier = modifier
    )
}

/** Only draws the form: it receives the values and reports every change. */
@Composable
fun LeaseForm(
    values: LeaseFormValues,
    check: LeaseCheck,
    hasSavedLease: Boolean,
    justSaved: Boolean,
    onChange: (LeaseFormValues) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .widthIn(max = 640.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.lease_title), style = MaterialTheme.typography.headlineSmall)
        if (!hasSavedLease) {
            // A4 awkward case: no lease yet.
            Text(stringResource(R.string.lease_empty), style = MaterialTheme.typography.bodyMedium)
        }

        LeaseField(
            value = values.address,
            onValueChange = { onChange(values.copy(address = it)) },
            label = R.string.lease_address,
            error = check.address
        )
        LeaseField(
            value = values.landlordName,
            onValueChange = { onChange(values.copy(landlordName = it)) },
            label = R.string.lease_landlord,
            error = check.landlordName
        )
        LeaseField(
            value = values.moveInDate,
            onValueChange = { onChange(values.copy(moveInDate = it)) },
            label = R.string.lease_move_in,
            error = check.moveInDate,
            hint = R.string.lease_date_hint
        )
        LeaseField(
            value = values.deposit,
            onValueChange = { onChange(values.copy(deposit = it)) },
            label = R.string.lease_deposit,
            error = check.deposit,
            hint = R.string.lease_deposit_hint,
            keyboardType = KeyboardType.Decimal
        )
        LeaseField(
            value = values.keysReturnedDate,
            onValueChange = { onChange(values.copy(keysReturnedDate = it)) },
            label = R.string.lease_keys_returned,
            error = check.keysReturnedDate,
            hint = R.string.lease_keys_hint
        )

        Button(
            onClick = onSave,
            enabled = check.isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.lease_save))
        }
        Text(
            text = stringResource(
                when {
                    !check.isValid -> R.string.lease_fill_required
                    justSaved -> R.string.lease_saved
                    else -> R.string.lease_ready
                }
            ),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun LeaseField(
    value: String,
    onValueChange: (String) -> Unit,
    label: Int,
    error: LeaseFieldError?,
    hint: Int? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    // An empty required field is not shown in red: Save stays disabled and the text below says why.
    val shownError = if (error == LeaseFieldError.REQUIRED) null else error
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        isError = shownError != null,
        supportingText = when {
            shownError != null -> { { Text(stringResource(errorText(shownError))) } }
            hint != null -> { { Text(stringResource(hint)) } }
            else -> null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun errorText(error: LeaseFieldError): Int = when (error) {
    LeaseFieldError.REQUIRED -> R.string.lease_error_required
    LeaseFieldError.NOT_A_DATE -> R.string.lease_error_date
    LeaseFieldError.NOT_AN_AMOUNT -> R.string.lease_error_amount
    LeaseFieldError.NOT_POSITIVE -> R.string.lease_error_positive
    LeaseFieldError.KEYS_BEFORE_MOVE_IN -> R.string.lease_error_keys_before
}
