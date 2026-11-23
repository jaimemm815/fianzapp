package com.example.fianzapp.ui.rooms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fianzapp.data.ChecklistRepository
import com.example.fianzapp.data.db.FianzappDatabase
import com.example.fianzapp.data.db.RoomEntity
import com.example.fianzapp.ui.checklist.ChecklistUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed interface RoomsUiState {
    data object Loading : RoomsUiState
    /** No lease saved yet: rooms cannot be added. */
    data object NoLease : RoomsUiState
    data class Rooms(val leaseId: Long, val rooms: List<RoomEntity>) : RoomsUiState
}

class RoomsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FianzappDatabase.getInstance(application)
    private val checklistRepository = ChecklistRepository(application)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<RoomsUiState> = db.leaseDao().observeLease()
        .flatMapLatest { lease ->
            if (lease == null) {
                flowOf(RoomsUiState.NoLease)
            } else {
                db.roomDao().observeRoomsOfLease(lease.id).map { RoomsUiState.Rooms(lease.id, it) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoomsUiState.Loading)

    /** The room types of the checklist file (A5), to choose one when adding a room. */
    private val _roomTypes = MutableStateFlow<ChecklistUiState>(ChecklistUiState.Loading)
    val roomTypes: StateFlow<ChecklistUiState> = _roomTypes.asStateFlow()

    init {
        loadRoomTypes()
    }

    fun loadRoomTypes() {
        _roomTypes.value = ChecklistUiState.Loading
        viewModelScope.launch {
            _roomTypes.value = try {
                ChecklistUiState.Loaded(checklistRepository.loadChecklist().roomTypes)
            } catch (e: Exception) {
                ChecklistUiState.Error(e.message ?: e.javaClass.simpleName)
            }
        }
    }

    fun addRoom(leaseId: Long, name: String, typeId: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            db.roomDao().insert(RoomEntity(leaseId = leaseId, name = name.trim(), typeId = typeId))
        }
    }

    /** Deletes the room, its photo rows (CASCADE) and its photo files in filesDir/photos/<roomId>/. */
    fun deleteRoom(room: RoomEntity) {
        viewModelScope.launch {
            db.roomDao().delete(room)
            withContext(Dispatchers.IO) {
                File(getApplication<Application>().filesDir, "photos/${room.id}").deleteRecursively()
            }
        }
    }
}
