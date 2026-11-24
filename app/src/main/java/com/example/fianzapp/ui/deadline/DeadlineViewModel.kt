package com.example.fianzapp.ui.deadline

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fianzapp.data.SettingsRepository
import com.example.fianzapp.data.db.FianzappDatabase
import com.example.fianzapp.data.db.LeaseEntity
import com.example.fianzapp.domain.parseDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

sealed interface DeadlineUiState {
    data object Loading : DeadlineUiState
    data object NoLease : DeadlineUiState
    /** The lease is saved but the keys have not been handed back yet: not an error. */
    data class KeysNotReturned(val lease: LeaseEntity) : DeadlineUiState
    data class Ready(
        val lease: LeaseEntity,
        val moveIn: LocalDate?,
        val keysReturned: LocalDate,
        /** A3: days of warning, from the same DataStore as the worker. */
        val warningDays: Int,
        val reminderOn: Boolean
    ) : DeadlineUiState
}

class DeadlineViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = SettingsRepository(application)

    val state: StateFlow<DeadlineUiState> = combine(
        FianzappDatabase.getInstance(application).leaseDao().observeLease(),
        settings.warningDays,
        settings.reminderOn
    ) { lease, warningDays, reminderOn ->
        val keys = lease?.keysReturnedDate?.let(::parseDate)
        when {
            lease == null -> DeadlineUiState.NoLease
            keys == null -> DeadlineUiState.KeysNotReturned(lease)
            else -> DeadlineUiState.Ready(lease, parseDate(lease.moveInDate), keys, warningDays, reminderOn)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DeadlineUiState.Loading)
}
