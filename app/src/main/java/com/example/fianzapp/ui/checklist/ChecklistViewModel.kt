package com.example.fianzapp.ui.checklist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fianzapp.data.ChecklistRepository
import com.example.fianzapp.data.RoomType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ChecklistUiState {
    data object Loading : ChecklistUiState
    data class Loaded(val roomTypes: List<RoomType>) : ChecklistUiState
    data class Error(val message: String) : ChecklistUiState
}

class ChecklistViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChecklistRepository(application)

    private val _uiState = MutableStateFlow<ChecklistUiState>(ChecklistUiState.Loading)
    val uiState: StateFlow<ChecklistUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = ChecklistUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                ChecklistUiState.Loaded(repository.loadChecklist().roomTypes)
            } catch (e: Exception) {
                ChecklistUiState.Error(e.message ?: e.javaClass.simpleName)
            }
        }
    }
}
