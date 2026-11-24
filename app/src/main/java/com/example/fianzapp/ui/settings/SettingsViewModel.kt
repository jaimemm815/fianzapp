package com.example.fianzapp.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fianzapp.data.DEFAULT_WARNING_DAYS
import com.example.fianzapp.data.SettingsRepository
import com.example.fianzapp.domain.parseWarningDays
import com.example.fianzapp.reminder.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = SettingsRepository(application)

    val warningDays: StateFlow<Int> =
        settings.warningDays.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DEFAULT_WARNING_DAYS)
    val reminderOn: StateFlow<Boolean> =
        settings.reminderOn.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Saves the days only when valid; returns false otherwise, and the last valid value stays. */
    fun saveWarningDays(text: String): Boolean {
        val days = parseWarningDays(text) ?: return false
        viewModelScope.launch { settings.setWarningDays(days) }
        return true
    }

    fun setReminder(on: Boolean) {
        viewModelScope.launch { settings.setReminderOn(on) }
        val context = getApplication<Application>()
        if (on) ReminderScheduler.enable(context) else ReminderScheduler.disable(context)
    }

    fun checkNow() = ReminderScheduler.runNow(getApplication())
}
