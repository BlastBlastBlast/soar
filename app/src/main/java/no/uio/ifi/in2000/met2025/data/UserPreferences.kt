package no.uio.ifi.in2000.met2025.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(
    private val dataStore: DataStore<Preferences>
) {
    private val FIRST_RUN_KEY    = booleanPreferencesKey("first_run")
    private val MAP_TUTOR_KEY    = booleanPreferencesKey("map_tutorial_seen")

    /** emits true until you call markFirstRunComplete() */
    val isFirstRunFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[FIRST_RUN_KEY] ?: true }

    suspend fun markFirstRunComplete() {
        dataStore.edit { it[FIRST_RUN_KEY] = false }
    }

    /** emits true until you call markMapTutorialSeen() */
    val isMapTutorialFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[MAP_TUTOR_KEY] ?: true }

    suspend fun markMapTutorialSeen() {
        dataStore.edit { it[MAP_TUTOR_KEY] = false }
    }
}