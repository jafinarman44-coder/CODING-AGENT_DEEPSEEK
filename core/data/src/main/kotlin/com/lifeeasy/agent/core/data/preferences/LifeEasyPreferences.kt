// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.core.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.lifeEasyDataStore by preferencesDataStore(name = "lifeeasy_preferences")

/** Non-secret user preferences only. Never store passwords, API keys, or tokens here. */
class LifeEasyPreferences(private val context: Context) {
    val darkThemeEnabled: Flow<Boolean> = context.lifeEasyDataStore.data.map { prefs ->
        prefs[DARK_THEME] ?: false
    }

    val languageTag: Flow<String> = context.lifeEasyDataStore.data.map { prefs ->
        prefs[LANGUAGE_TAG] ?: "en"
    }

    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        context.lifeEasyDataStore.edit { it[DARK_THEME] = enabled }
    }

    suspend fun setLanguageTag(tag: String) {
        require(tag.matches(Regex("^[a-z]{2}(-[A-Z]{2})?$"))) { "Invalid language tag" }
        context.lifeEasyDataStore.edit { it[LANGUAGE_TAG] = tag }
    }

    private companion object {
        val DARK_THEME = booleanPreferencesKey("dark_theme_enabled")
        val LANGUAGE_TAG = stringPreferencesKey("language_tag")
    }
}
