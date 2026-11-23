package com.example.fianzapp.ui.lease

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fianzapp.data.db.FianzappDatabase
import com.example.fianzapp.data.db.LeaseEntity
import com.example.fianzapp.domain.LeaseCheck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** What the Lease screen knows about the saved lease. */
sealed interface SavedLease {
    data object Loading : SavedLease
    data object None : SavedLease
    data class Saved(val lease: LeaseEntity) : SavedLease
}

class LeaseViewModel(application: Application) : AndroidViewModel(application) {

    private val leaseDao = FianzappDatabase.getInstance(application).leaseDao()

    val savedLease: StateFlow<SavedLease> = leaseDao.observeLease()
        .map { if (it == null) SavedLease.None else SavedLease.Saved(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedLease.Loading)

    private val _justSaved = MutableStateFlow(false)
    val justSaved: StateFlow<Boolean> = _justSaved.asStateFlow()

    fun onEdited() {
        _justSaved.value = false
    }

    /** Saves the one lease of the app: inserts it the first time, updates it afterwards. */
    fun save(check: LeaseCheck, address: String, landlordName: String) {
        val moveIn = check.moveIn ?: return
        val cents = check.depositCents ?: return
        if (!check.isValid) return
        viewModelScope.launch {
            val current = leaseDao.observeLease().first()
            val lease = LeaseEntity(
                id = current?.id ?: 0,
                address = address.trim(),
                landlordName = landlordName.trim(),
                moveInDate = moveIn.toString(),
                depositCents = cents,
                keysReturnedDate = check.keysReturned?.toString()
            )
            if (current == null) leaseDao.insert(lease) else leaseDao.update(lease)
            _justSaved.value = true
        }
    }
}
