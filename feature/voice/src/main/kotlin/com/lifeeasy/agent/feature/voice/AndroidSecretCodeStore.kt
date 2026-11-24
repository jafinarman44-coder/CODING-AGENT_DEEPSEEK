// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.voice

import android.content.Context

/** Preferences contain ciphertext only; code and derived hash are never stored as plaintext. */
class AndroidSecretCodeStore(context: Context) : SecretCodeStore {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    override fun writeEncryptedCode(payload: String) {
        check(preferences.edit().putString(CODE_KEY, payload).commit()) {
            "Could not persist encrypted security code"
        }
    }

    override fun readEncryptedCode(): String? = preferences.getString(CODE_KEY, null)

    private companion object {
        const val FILE_NAME = "lifeeasy_secret_code_secure"
        const val CODE_KEY = "encrypted_code_hash_v1"
    }
}
