// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent

import android.content.Context
import com.lifeeasy.agent.core.security.AndroidKeystoreCipher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.net.URI

/** Provider settings. API keys are stored only as Android-Keystore AES-GCM ciphertext. */
data class AiProviderConfig(
    val baseUrl: String,
    val model: String,
    val apiKey: String,
    val providerName: String = "OpenAI-compatible"
) {
    val isConfigured: Boolean get() = baseUrl.isNotBlank() && model.isNotBlank()
}

class AiProviderStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("lifeeasy_ai_provider_v1", Context.MODE_PRIVATE)
    private val cipher = AndroidKeystoreCipher("lifeeasy.ai.provider.key.v1")

    fun load(): AiProviderConfig = AiProviderConfig(
        baseUrl = prefs.getString("base_url", "https://api.openai.com/v1") ?: "https://api.openai.com/v1",
        model = prefs.getString("model", "") ?: "",
        apiKey = prefs.getString("api_key_enc", null)?.let { encoded ->
            runCatching { cipher.decrypt(encoded).toString(Charsets.UTF_8) }.getOrDefault("")
        } ?: "",
        providerName = prefs.getString("provider_name", "OpenAI-compatible") ?: "OpenAI-compatible"
    )

    fun save(config: AiProviderConfig) {
        require(isAllowedEndpoint(config.baseUrl)) {
            "Use HTTPS for remote providers. HTTP is allowed only for a private/local Ollama address."
        }
        require(config.model.isNotBlank()) { "Model name is required" }
        val encryptedKey = if (config.apiKey.isBlank()) "" else cipher.encrypt(config.apiKey.toByteArray(Charsets.UTF_8))
        check(prefs.edit().putString("base_url", config.baseUrl.trim().trimEnd('/'))
            .putString("model", config.model.trim())
            .putString("api_key_enc", encryptedKey)
            .putString("provider_name", config.providerName).commit()) { "Could not save provider settings" }
    }

    fun clear() { prefs.edit().clear().apply() }

    companion object {
        fun isAllowedEndpoint(value: String): Boolean = runCatching {
            val uri = URI(value.trim())
            val scheme = uri.scheme?.lowercase()
            val host = uri.host?.lowercase() ?: return@runCatching false
            scheme == "https" || (scheme == "http" && isPrivateOrLocalHost(host))
        }.getOrDefault(false)

        private fun isPrivateOrLocalHost(host: String): Boolean {
            if (host == "localhost" || host == "127.0.0.1" || host == "10.0.2.2") return true
            if (host.startsWith("10.") || host.startsWith("192.168.")) return true
            val parts = host.split('.')
            return parts.size == 4 && parts[0] == "172" && (parts[1].toIntOrNull()?.let { it in 16..31 } == true)
        }
    }
}

class OpenAiCompatibleClient(
    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(100, TimeUnit.SECONDS)
        .build()
) {
    suspend fun reply(config: AiProviderConfig, conversation: List<Pair<String, String>>): String = withContext(Dispatchers.IO) {
        require(config.isConfigured) { "AI provider is not configured. Open Settings and add an endpoint and model." }
        require(AiProviderStore.isAllowedEndpoint(config.baseUrl)) { "Endpoint rejected. Use HTTPS or a private/local HTTP address." }
        require(conversation.isNotEmpty()) { "Message is empty" }
        val messages = JSONArray()
        conversation.takeLast(20).forEach { (role, content) ->
            messages.put(JSONObject().put("role", role).put("content", content))
        }
        val body = JSONObject()
            .put("model", config.model)
            .put("messages", messages)
            .put("temperature", 0.7)
            .put("stream", false)
            .toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val endpoint = config.baseUrl.trimEnd('/') + "/chat/completions"
        val requestBuilder = Request.Builder().url(endpoint).post(body).header("Accept", "application/json")
        if (config.apiKey.isNotBlank()) requestBuilder.header("Authorization", "Bearer ${config.apiKey}")
        http.newCall(requestBuilder.build()).execute().use { response ->
            val responseText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val detail = runCatching { JSONObject(responseText).optJSONObject("error")?.optString("message") }.getOrNull()
                throw IllegalStateException("Provider HTTP ${response.code}: ${detail?.takeIf { it.isNotBlank() } ?: response.message}")
            }
            val root = JSONObject(responseText)
            val content = root.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")?.optString("content")
            content?.takeIf { it.isNotBlank() } ?: throw IllegalStateException("Provider returned no message content")
        }
    }
}
