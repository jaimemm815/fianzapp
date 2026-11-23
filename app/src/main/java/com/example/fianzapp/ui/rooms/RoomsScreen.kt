package com.example.fianzapp.ui.rooms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fianzapp.R
import com.example.fianzapp.data.RoomType
import com.example.fianzapp.data.db.RoomEntity
import com.example.fianzapp.ui.checklist.ChecklistUiState

/** A2/A4: the rooms of the flat, read from Room. */
@Composable
fun RoomsScreen(
    onOpenLease: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoomsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val roomTypes by viewModel.roomTypes.collectAsStateWithLifecycle()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var roomToDelete by remember { mutableStateOf<RoomEntity?>(null) }

    val typeNames = (roomTypes as? ChecklistUiState.Loaded)?.roomTypes?.associate { it.id to it.name }.orEmpty()

    when (val s = state) {
        RoomsUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        // Awkward case: rooms need a lease first.
        RoomsUiState.NoLease -> Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.rooms_no_lease), style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onOpenLease) { Text(stringResource(R.string.rooms_open_lease)) }
        }

        is RoomsUiState.Rooms -> Column(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.rooms_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(stringResource(R.string.rooms_add), modifier = Modifier.padding(start = 6.dp))
                }
            }

            if (s.rooms.isEmpty()) {
                // Awkward case: an empty table says what to do.
                Text(
                    stringResource(R.string.rooms_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // The key is the row's id, never its position.
                    items(s.rooms, key = { it.id }) { room ->
                        RoomRow(
                            room = room,
                            typeName = typeNames[room.typeId] ?: room.typeId,
                            onDelete = { roomToDelete = room }
                        )
                    }
                }
            }

            if (showAddDialog) {
                AddRoomDialog(
                    roomTypes = roomTypes,
                    onRetry = viewModel::loadRoomTypes,
                    onDismiss = { showAddDialog = false },
                    onAdd = { name, typeId ->
                        viewModel.addRoom(s.leaseId, name, typeId)
                        showAddDialog = false
                    }
                )
            }
        }
    }

    roomToDelete?.let { room ->
        AlertDialog(
            onDismissRequest = { roomToDelete = null },
            title = { Text(stringResource(R.string.rooms_delete_title, room.name)) },
            text = { Text(stringResource(R.string.rooms_delete_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRoom(room)
                    roomToDelete = null
                }) { Text(stringResource(R.string.rooms_delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { roomToDelete = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun RoomRow(room: RoomEntity, typeName: String, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(room.name, style = MaterialTheme.typography.titleMedium)
                Text(typeName, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.rooms_delete_cd, room.name))
            }
        }
    }
}

/** Name and type of a new room. The types come from the checklist file (A5). */
@Composable
private fun AddRoomDialog(
    roomTypes: ChecklistUiState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onAdd: (name: String, typeId: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var typeId by rememberSaveable { mutableStateOf<String?>(null) }
    val canAdd = name.isNotBlank() && typeId != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rooms_add_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.rooms_name)) },
                    supportingText = { Text(stringResource(R.string.rooms_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.rooms_type), style = MaterialTheme.typography.titleSmall)
                when (roomTypes) {
                    ChecklistUiState.Loading -> CircularProgressIndicator()
                    is ChecklistUiState.Error -> {
                        Text(stringResource(R.string.checklist_error, roomTypes.message))
                        TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                    }
                    is ChecklistUiState.Loaded -> roomTypes.roomTypes.forEach { type ->
                        RoomTypeOption(
                            type = type,
                            selected = type.id == typeId,
                            onSelect = {
                                // Suggest the type's name when the person has not typed one.
                                if (name.isBlank()) name = type.name
                                typeId = type.id
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { typeId?.let { onAdd(name, it) } },
                enabled = canAdd
            ) { Text(stringResource(R.string.rooms_add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        }
    )
}

@Composable
private fun RoomTypeOption(type: RoomType, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
            Text(type.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                pluralItems(type.items.size),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun pluralItems(count: Int): String =
    if (count == 0) stringResource(R.string.checklist_no_items)
    else stringResource(R.string.rooms_type_items, count)
