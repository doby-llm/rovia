package com.gusanitolabs.robia.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gusanitolabs.robia.core.model.LanguagePreference
import com.gusanitolabs.robia.core.model.RobiaSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    val settings: Flow<RobiaSettings>
    suspend fun setLanguagePreference(languagePreference: LanguagePreference)
}

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    override val settings: Flow<RobiaSettings> = dataStore.data.map { preferences ->
        RobiaSettings(
            languagePreference = preferences[languageKey].toLanguagePreference(),
        )
    }

    override suspend fun setLanguagePreference(languagePreference: LanguagePreference) {
        dataStore.edit { preferences ->
            val storageValue = languagePreference.storageValue
            if (storageValue == null) {
                preferences.remove(languageKey)
            } else {
                preferences[languageKey] = storageValue
            }
        }
    }

    private fun String?.toLanguagePreference(): LanguagePreference =
        LanguagePreference.entries.firstOrNull { it.storageValue == this } ?: LanguagePreference.System

    private companion object {
        val languageKey = stringPreferencesKey("language")
    }
}
