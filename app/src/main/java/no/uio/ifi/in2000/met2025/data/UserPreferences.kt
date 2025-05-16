package no.uio.ifi.in2000.met2025.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(
    private val dataStore: DataStore<Preferences>
) {
    private val FIRST_RUN_KEY    = booleanPreferencesKey("first_run")
    private val ROCKET_CONFIG_KEY    = booleanPreferencesKey("rocket_config_seen")
    private val FIRST_LAUNCH_KEY = booleanPreferencesKey("first_launch")

    /** emits true until you call markFirstRunComplete() */
    val isFirstRunFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[FIRST_RUN_KEY] ?: true }

    suspend fun markFirstRunComplete() {
        dataStore.edit { it[FIRST_RUN_KEY] = false }
    }

    /** emits true until you call markRocketConfigSeen() */
    val isRocketConfigFirstRunFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[ROCKET_CONFIG_KEY] ?: true }

    suspend fun markRocketConfigSeen() {
        dataStore.edit { it[ROCKET_CONFIG_KEY] = false }
    }

    /** emits true until you call markFirstLaunchTutorialSeen() */
    val isFirstLaunchFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[FIRST_LAUNCH_KEY] ?: true }

    suspend fun markFirstLaunchTutorialSeen() {
        dataStore.edit { it[FIRST_LAUNCH_KEY] = false }
    }
}