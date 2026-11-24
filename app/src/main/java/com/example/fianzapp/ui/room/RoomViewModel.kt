package com.example.fianzapp.ui.room

import android.app.Application
import android.location.Location
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
import com.example.fianzapp.domain.Comparison
import com.example.fianzapp.domain.comparePhotos
import com.example.fianzapp.domain.parseLabels
import com.example.fianzapp.ui.RoomRoute
import com.example.fianzapp.ui.checklist.ChecklistUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * One checklist item with its latest move-in and move-out photos (null when not taken yet),
 * and A10's answer when both exist.
 */
data class ItemWithPhotos(
    val item: ChecklistItem,
    val moveIn: PhotoEntity?,
    val moveOut: PhotoEntity?,
    val comparison: Comparison?
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
        val damages: List<PhotoEntity>,
        /** A9: metres from the flat for each photo id that has a position, when the flat has one. */
        val distances: Map<Long, Float>
    ) : RoomUiState
}

/** The room, its photos and all the photos of its lease (to find the flat's position). */
private data class RoomData(
    val room: RoomEntity?,
    val photos: List<PhotoEntity>,
    val leasePhotos: List<PhotoEntity>
)

class RoomViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val roomId: Long = savedStateHandle.toRoute<RoomRoute>().roomId
    private val db = FianzappDatabase.getInstance(application)
    private val checklistRepository = ChecklistRepository(application)

    private val checklist = MutableStateFlow<ChecklistUiState>(ChecklistUiState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val roomData = db.roomDao().observeRoom(roomId).flatMapLatest { room ->
        if (room == null) {
            flowOf(RoomData(null, emptyList(), emptyList()))
        } else {
            combine(
                db.photoDao().observePhotosOfRoom(roomId),
                db.photoDao().observePhotosOfLease(room.leaseId)
            ) { photos, leasePhotos -> RoomData(room, photos, leasePhotos) }
        }
    }

    val state: StateFlow<RoomUiState> = combine(roomData, checklist) { data, checklistState ->
        val room = data.room
        when {
            room == null -> RoomUiState.NotFound
            checklistState is ChecklistUiState.Error ->
                RoomUiState.ChecklistError(room.name, checklistState.message)
            checklistState is ChecklistUiState.Loaded -> {
                val type = checklistState.roomTypes.find { it.id == room.typeId }
                RoomUiState.Loaded(
                    room = room,
                    typeName = type?.name ?: room.typeId,
                    items = type?.items.orEmpty().map { item ->
                        val moveIn = latestPhoto(data.photos, item.id, Phase.MOVE_IN)
                        val moveOut = latestPhoto(data.photos, item.id, Phase.MOVE_OUT)
                        ItemWithPhotos(
                            item = item,
                            moveIn = moveIn,
                            moveOut = moveOut,
                            comparison = if (moveIn != null && moveOut != null) {
                                comparePhotos(parseLabels(moveIn.labels), parseLabels(moveOut.labels))
                            } else {
                                null
                            }
                        )
                    },
                    damages = data.photos.filter { it.itemId == null },
                    distances = distancesFromFlat(data.photos, data.leasePhotos)
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

    /**
     * A9: the flat's position is the place of the first move-in photo of the lease that has one.
     * Each photo with a position gets its distance to it, with Android's own formula.
     */
    private fun distancesFromFlat(photos: List<PhotoEntity>, leasePhotos: List<PhotoEntity>): Map<Long, Float> {
        val flat = leasePhotos
            .filter { it.phase == Phase.MOVE_IN && it.latitude != null && it.longitude != null }
            .minByOrNull { it.takenAt } ?: return emptyMap()
        val flatLat = flat.latitude ?: return emptyMap()
        val flatLng = flat.longitude ?: return emptyMap()
        return photos.mapNotNull { photo ->
            val lat = photo.latitude ?: return@mapNotNull null
            val lng = photo.longitude ?: return@mapNotNull null
            val results = FloatArray(1)
            Location.distanceBetween(flatLat, flatLng, lat, lng, results)
            photo.id to results[0]
        }.toMap()
    }
}
