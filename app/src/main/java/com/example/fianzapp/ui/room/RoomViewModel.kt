package com.example.fianzapp.ui.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fianzapp.data.ChecklistItem
import com.example.fianzapp.data.ChecklistRepository
import com.example.fianzapp.data.db.FianzappDatabase
import com.example.fianzapp.data.db.Phase
import com.example.fianzapp.data.db.PhotoEntity
import com.example.fianzapp.data.db.RoomEntity
import com.example.fianzapp.ui.RoomRoute
import com.example.fianzapp.ui.checklist.ChecklistUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One checklist item with its latest move-in and move-out photos (null when not taken yet). */
data class ItemWithPhotos(
    val item: ChecklistItem,
    val moveIn: PhotoEntity?,
    val moveOut: PhotoEntity?
)

sealed interface RoomUiState {
    data object Loading : RoomUiState
    /** The id does not exist, or the room was deleted while its screen was open. */
    data object NotFound : RoomUiState
    data class ChecklistError(val roomName: String, val message: String) : RoomUiState
    data class Loaded(
        val room: RoomEntity,
        val typeName: String,
        val items: List<ItemWithPhotos>,
        val damages: List<PhotoEntity>
    ) : RoomUiState
}

class RoomViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val roomId: Long = savedStateHandle.toRoute<RoomRoute>().roomId
    private val db = FianzappDatabase.getInstance(application)
    private val checklistRepository = ChecklistRepository(application)

    private val checklist = MutableStateFlow<ChecklistUiState>(ChecklistUiState.Loading)

    val state: StateFlow<RoomUiState> = combine(
        db.roomDao().observeRoom(roomId),
        db.photoDao().observePhotosOfRoom(roomId),
        checklist
    ) { room, photos, checklistState ->
        when {
            room == null -> RoomUiState.NotFound
            checklistState is ChecklistUiState.Loading -> RoomUiState.Loading
            checklistState is ChecklistUiState.Error ->
                RoomUiState.ChecklistError(room.name, checklistState.message)
            checklistState is ChecklistUiState.Loaded -> {
                val type = checklistState.roomTypes.find { it.id == room.typeId }
                RoomUiState.Loaded(
                    room = room,
                    typeName = type?.name ?: room.typeId,
                    items = type?.items.orEmpty().map { item ->
                        ItemWithPhotos(
                            item = item,
                            moveIn = latestPhoto(photos, item.id, Phase.MOVE_IN),
                            moveOut = latestPhoto(photos, item.id, Phase.MOVE_OUT)
                        )
                    },
                    damages = photos.filter { it.itemId == null }
                )
            }
            else -> RoomUiState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoomUiState.Loading)

    init {
        loadChecklist()
    }

    fun loadChecklist() {
        checklist.value = ChecklistUiState.Loading
        viewModelScope.launch {
            checklist.value = try {
                ChecklistUiState.Loaded(checklistRepository.loadChecklist().roomTypes)
            } catch (e: Exception) {
                ChecklistUiState.Error(e.message ?: e.javaClass.simpleName)
            }
        }
    }

    private fun latestPhoto(photos: List<PhotoEntity>, itemId: Int, phase: Phase): PhotoEntity? =
        photos.filter { it.itemId == itemId && it.phase == phase }.maxByOrNull { it.takenAt }
}
