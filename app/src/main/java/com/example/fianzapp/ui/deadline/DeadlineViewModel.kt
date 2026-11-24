package com.example.fianzapp.ui.deadline

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fianzapp.data.db.FianzappDatabase
import com.example.fianzapp.data.db.LeaseEntity
import com.example.fianzapp.domain.parseDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

sealed interface DeadlineUiState {
    data object Loading : DeadlineUiState
    data object NoLease : DeadlineUiState
    /** The lease is saved but the keys have not been handed back yet: not an error. */
    data class KeysNotReturned(val lease: LeaseEntity) : DeadlineUiState
    data class Ready(val lease: LeaseEntity, val moveIn: LocalDate?, val keysReturned: LocalDate) : DeadlineUiState
}

class DeadlineViewModel(application: Application) : AndroidViewModel(application) {

    val state: StateFlow<DeadlineUiState> = FianzappDatabase.getInstance(application).leaseDao()
        .observeLease()
        .map { lease ->
            val keys = lease?.keysReturnedDate?.let(::parseDate)
            when {
                lease == null -> DeadlineUiState.NoLease
                keys == null -> DeadlineUiState.KeysNotReturned(lease)
                else -> DeadlineUiState.Ready(lease, parseDate(lease.moveInDate), keys)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DeadlineUiState.Loading)
}
