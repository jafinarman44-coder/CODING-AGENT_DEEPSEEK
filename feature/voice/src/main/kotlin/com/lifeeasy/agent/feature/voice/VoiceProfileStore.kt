// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.voice

import android.content.Context

/** Stores only already-encrypted voiceprint payloads. Plain voice samples never enter preferences. */
interface VoiceProfileStore {
    fun saveEncryptedProfile(payload: String)
    fun readEncryptedProfile(): String?
    fun clearProfile()
}

class AndroidVoiceProfileStore(context: Context) : VoiceProfileStore {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    override fun saveEncryptedProfile(payload: String) {
        require(payload.isNotBlank()) { "Encrypted voice profile cannot be blank" }
        check(preferences.edit().putString(PROFILE_KEY, payload).commit()) {
            "Could not persist encrypted voice profile"
        }
    }

    override fun readEncryptedProfile(): String? = preferences.getString(PROFILE_KEY, null)

    override fun clearProfile() {
        check(preferences.edit().remove(PROFILE_KEY).commit()) { "Could not remove voice profile" }
    }

    private companion object {
        const val FILE_NAME = "lifeeasy_voice_profile_secure"
        const val PROFILE_KEY = "encrypted_voice_profile_v1"
    }
}
