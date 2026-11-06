package com.example.fianzapp.ui.checklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fianzapp.R
import com.example.fianzapp.data.RoomType

@Composable
fun ChecklistScreen(
    modifier: Modifier = Modifier,
    viewModel: ChecklistViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = state) {
        ChecklistUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is ChecklistUiState.Error -> Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.checklist_error, s.message))
            Button(onClick = viewModel::load) { Text(stringResource(R.string.retry)) }
        }

        is ChecklistUiState.Loaded -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(s.roomTypes, key = { it.id }) { room -> RoomCard(room) }
        }
    }
}

@Composable
private fun RoomCard(room: RoomType) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(room.name, style = MaterialTheme.typography.titleMedium)
            if (room.items.isEmpty()) {
                // storage has "items": []
                Text(
                    stringResource(R.string.checklist_no_items),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            room.items.forEach { item ->
                Column {
                    Text(item.name, style = MaterialTheme.typography.bodyLarge)
                    // item 30 has "hint": null
                    item.hint?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
