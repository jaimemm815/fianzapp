package com.example.fianzapp.ui.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fianzapp.R
import com.example.fianzapp.data.db.Phase
import com.example.fianzapp.data.db.PhotoEntity
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** A2: one room of the flat, its checklist and the photos before and after. */
@Composable
fun RoomScreen(
    modifier: Modifier = Modifier,
    viewModel: RoomViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        RoomUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        // Awkward case: unknown id or a room deleted while this screen is open.
        RoomUiState.NotFound -> CenteredMessage(stringResource(R.string.room_not_found), modifier)

        is RoomUiState.ChecklistError -> Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(s.roomName, style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.checklist_error, s.message), textAlign = TextAlign.Center)
            Button(onClick = viewModel::loadChecklist) { Text(stringResource(R.string.retry)) }
        }

        is RoomUiState.Loaded -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(s.room.name, style = MaterialTheme.typography.headlineSmall)
                    Text(s.typeName, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (s.items.isEmpty()) {
                // storage has "items": []
                item { Text(stringResource(R.string.checklist_no_items)) }
            }
            items(s.items, key = { it.item.id }) { ItemCard(it) }

            item {
                Text(
                    stringResource(R.string.room_damage_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            if (s.damages.isEmpty()) {
                item { Text(stringResource(R.string.room_no_damage)) }
            }
            items(s.damages, key = { it.id }) { DamageCard(it) }
        }
    }
}

@Composable
private fun ItemCard(entry: ItemWithPhotos) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(entry.item.name, style = MaterialTheme.typography.titleMedium)
            // item 30 has "hint": null: show nothing, never "null".
            entry.item.hint?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PhotoSlot(stringResource(R.string.room_move_in), entry.moveIn, Modifier.weight(1f))
                PhotoSlot(stringResource(R.string.room_move_out), entry.moveOut, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DamageCard(photo: PhotoEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                photo.note ?: stringResource(R.string.room_damage_no_note),
                style = MaterialTheme.typography.titleMedium
            )
            PhotoSlot(
                label = stringResource(
                    if (photo.phase == Phase.MOVE_IN) R.string.room_move_in else R.string.room_move_out
                ),
                photo = photo,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** One photo with its label and when it was taken, or an empty box when there is none yet. */
@Composable
private fun PhotoSlot(label: String, photo: PhotoEntity?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (photo == null) {
                Text(
                    stringResource(R.string.room_no_photo),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                AsyncImage(
                    model = File(photo.path),
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        photo?.let {
            Text(formatTakenAt(it.takenAt), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CenteredMessage(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}

private val takenAtFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)

private fun formatTakenAt(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(takenAtFormatter)
