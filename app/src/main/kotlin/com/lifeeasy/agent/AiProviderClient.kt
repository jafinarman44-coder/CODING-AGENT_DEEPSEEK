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
    val isConfigured: Boolean get() = baseUrl.isNotBlank() && model.isNotBlank() && (apiKey.isNotBlank() || isLocalEndpoint(baseUrl))
}

private fun isLocalEndpoint(value: String): Boolean = runCatching {
    val host = URI(value).host?.lowercase().orEmpty()
    host == "localhost" || host == "127.0.0.1" || host == "10.0.2.2" || host.startsWith("192.168.") || host.startsWith("10.") || (host.startsWith("172.") && host.split('.').getOrNull(1)?.toIntOrNull()?.let { it in 16..31 } == true)
}.getOrDefault(false)

class AiProviderStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("lifeeasy_ai_provider_v1", Context.MODE_PRIVATE)
    private val cipher = AndroidKeystoreCipher("lifeeasy.ai.provider.key.v1")

    fun load(): AiProviderConfig = AiProviderConfig(
        baseUrl = prefs.getString("base_url", "https://generativelanguage.googleapis.com/v1beta/openai") ?: "https://generativelanguage.googleapis.com/v1beta/openai",
        model = prefs.getString("model", "gemini-3.8-flash") ?: "gemini-3.8-flash",
        apiKey = prefs.getString("api_key_enc", null)?.let { encoded ->
            runCatching { cipher.decrypt(encoded).toString(Charsets.UTF_8) }.getOrDefault("")
        } ?: "",
        providerName = prefs.getString("provider_name", "Google Gemini") ?: "Google Gemini"
    )

    fun save(config: AiProviderConfig) {
        require(isAllowedEndpoint(config.baseUrl)) {
            "Use HTTPS for remote providers. HTTP is allowed only for a private/local Ollama address."
        }
        require(config.model.isNotBlank()) { "Model name is required" }
        val normalizedKey = config.apiKey.trim().removePrefix("Bearer ").trim()
        val encryptedKey = if (normalizedKey.isBlank()) "" else cipher.encrypt(normalizedKey.toByteArray(Charsets.UTF_8))
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
    suspend fun listModels(config: AiProviderConfig): List<String> = withContext(Dispatchers.IO) {
        require(config.baseUrl.isNotBlank()) { "Provider endpoint is empty" }
        require(AiProviderStore.isAllowedEndpoint(config.baseUrl)) { "Endpoint rejected" }
        val endpoint = config.baseUrl.trimEnd('/') + "/models"
        val builder = Request.Builder().url(endpoint).get().header("Accept", "application/json")
        if (config.apiKey.isNotBlank()) builder.header("Authorization", "Bearer ${config.apiKey.trim().removePrefix("Bearer ").trim()}")
        http.newCall(builder.build()).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IllegalStateException("Model list HTTP ${response.code} at $endpoint: ${body.take(350)}")
            val data = JSONObject(body).optJSONArray("data") ?: JSONArray()
            (0 until data.length()).mapNotNull { index ->
                data.optJSONObject(index)?.optString("id")?.takeIf(String::isNotBlank)?.removePrefix("models/")
            }.distinct().sorted()
        }
    }

    suspend fun reply(config: AiProviderConfig, conversation: List<Pair<String, String>>): String = withContext(Dispatchers.IO) {
        require(config.isConfigured) {
            if (config.apiKey.isBlank() && !isLocalEndpoint(config.baseUrl)) "Gemini API key is missing. Open Settings → Quick setup and paste your Google AI Studio key."
            else "AI provider is not configured. Open Settings and choose a model."
        }
        require(AiProviderStore.isAllowedEndpoint(config.baseUrl)) { "Endpoint rejected. Use HTTPS or a private/local HTTP address." }
        require(conversation.isNotEmpty()) { "Message is empty" }
        val messages = JSONArray()
        conversation.takeLast(20).forEach { (role, content) ->
            messages.put(JSONObject().put("role", role).put("content", content))
        }
        val body = JSONObject()
            .put("model", if (config.providerName.contains("Gemini", ignoreCase = true) && config.model == "gemini-2.0-flash") "gemini-3.8-flash" else config.model)
            .put("messages", messages)
            .put("temperature", 0.7)
            .put("stream", false)
            .toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val endpoint = config.baseUrl.trimEnd('/') + "/chat/completions"
        val requestBuilder = Request.Builder().url(endpoint).post(body).header("Accept", "application/json")
        if (config.apiKey.isNotBlank()) requestBuilder.header("Authorization", "Bearer ${config.apiKey.trim().removePrefix("Bearer ").trim()}")
        var lastFailure: IllegalStateException? = null
        for (attempt in 0..2) {
            http.newCall(requestBuilder.build()).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val root = JSONObject(responseText)
                    val content = root.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")?.optString("content")
                    return@withContext content?.takeIf { it.isNotBlank() } ?: throw IllegalStateException("Provider returned no message content")
                }
                val detail = runCatching { JSONObject(responseText).optJSONObject("error")?.optString("message") }.getOrNull()
                val message = detail?.takeIf { it.isNotBlank() } ?: responseText.take(500).ifBlank { response.message }
                if (response.code == 503 || response.code == 429 || response.code in 500..599) {
                    lastFailure = IllegalStateException("Provider temporarily unavailable (HTTP ${response.code}). Retried ${attempt + 1}/3. $message")
                } else if (response.code == 400 && config.apiKey.isBlank() && !isLocalEndpoint(config.baseUrl)) {
                    throw IllegalStateException("API key is missing. Open Settings → Quick setup and paste your Google AI Studio key.")
                } else if (response.code == 401 || response.code == 403) {
                    throw IllegalStateException("API key rejected or permission missing (HTTP ${response.code}). Check the key and provider project. $message")
                } else {
                    throw IllegalStateException("Provider HTTP ${response.code} at $endpoint: $message")
                }
            }
            if (attempt < 2) kotlinx.coroutines.delay(900L * (attempt + 1))
        }
        throw lastFailure ?: IllegalStateException("Provider temporarily unavailable. Please try again later.")
    }
}
