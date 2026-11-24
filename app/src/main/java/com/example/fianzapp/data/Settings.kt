package com.example.fianzapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** The one DataStore of the app, declared outside any class (S6 Guide, sections 5.1 and 5.4). */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** A3: what the days of warning are on a fresh install, before anything is saved. */
const val DEFAULT_WARNING_DAYS = 7

private object Keys {
    /** A3: days before the deposit deadline when the reminder arrives. */
    val WARNING_DAYS = intPreferencesKey("warning_days")
    /** A7: the switch of the reminder. */
    val REMINDER_ON = booleanPreferencesKey("reminder_on")
    /** A7: the deadline (as 2027-04-15) whose warning was already sent, so it is never sent twice. */
    val WARNING_SENT_FOR = stringPreferencesKey("warning_sent_for")
    /** A7: the deadline whose "deadline reached" notification was already sent. */
    val DEADLINE_SENT_FOR = stringPreferencesKey("deadline_sent_for")
}

/** Reads and writes the settings. Screens and the A7 worker all use this same DataStore. */
class SettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.settingsDataStore

    val warningDays: Flow<Int> = dataStore.data.map { it[Keys.WARNING_DAYS] ?: DEFAULT_WARNING_DAYS }
    val reminderOn: Flow<Boolean> = dataStore.data.map { it[Keys.REMINDER_ON] ?: false }

    suspend fun setWarningDays(days: Int) {
        dataStore.edit { it[Keys.WARNING_DAYS] = days }
    }

    suspend fun setReminderOn(on: Boolean) {
        dataStore.edit { it[Keys.REMINDER_ON] = on }
    }

    suspend fun warningSentFor(): String? = dataStore.data.first()[Keys.WARNING_SENT_FOR]
    suspend fun deadlineSentFor(): String? = dataStore.data.first()[Keys.DEADLINE_SENT_FOR]

    suspend fun markWarningSent(deadline: String) {
        dataStore.edit { it[Keys.WARNING_SENT_FOR] = deadline }
    }

    suspend fun markDeadlineSent(deadline: String) {
        dataStore.edit { it[Keys.DEADLINE_SENT_FOR] = deadline }
    }
}
