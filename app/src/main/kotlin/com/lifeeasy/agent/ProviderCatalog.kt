// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent

/** Curated OpenAI Chat Completions-compatible endpoints. Model IDs are editable because providers change availability. */
data class ProviderPreset(
    val name: String,
    val baseUrl: String,
    val keyUrl: String,
    val models: List<ModelPreset>,
    val note: String = ""
)
data class ModelPreset(val id: String, val tier: String)

object ProviderCatalog {
    val providers = listOf(
        ProviderPreset("OpenAI", "https://api.openai.com/v1", "https://platform.openai.com/api-keys", listOf(
            ModelPreset("gpt-4o-mini", "Paid"), ModelPreset("gpt-4o", "Paid"), ModelPreset("gpt-4.1-mini", "Paid"), ModelPreset("gpt-4.1", "Paid")
        )),
        ProviderPreset("Google Gemini", "https://generativelanguage.googleapis.com/v1beta/openai", "https://aistudio.google.com/app/apikey", listOf(
            ModelPreset("gemini-3.8-flash", "Current Flash · check project quota"), ModelPreset("gemini-3.1-flash-lite", "Lower-cost option · availability varies"), ModelPreset("gemini-2.5-flash", "Legacy fallback · availability varies"), ModelPreset("gemini-2.5-pro", "Higher capability · quota/billing applies")
        )),
        ProviderPreset("DeepSeek", "https://api.deepseek.com", "https://platform.deepseek.com/api_keys", listOf(
            ModelPreset("deepseek-chat", "Provider pricing applies"), ModelPreset("deepseek-reasoner", "Provider pricing applies")
        ), "Use the model IDs currently shown in your DeepSeek console; retired aliases may return 404."),
        ProviderPreset("xAI Grok", "https://api.x.ai/v1", "https://console.x.ai/", listOf(
            ModelPreset("grok-3-mini", "Paid"), ModelPreset("grok-3", "Paid"), ModelPreset("grok-4", "Paid; availability varies")
        )),
        ProviderPreset("Groq", "https://api.groq.com/openai/v1", "https://console.groq.com/keys", listOf(
            ModelPreset("openai/gpt-oss-20b", "Free tier may be available"), ModelPreset("openai/gpt-oss-120b", "Free tier / rate limits"), ModelPreset("groq/compound", "Availability varies")
        )),
        ProviderPreset("Mistral", "https://api.mistral.ai/v1", "https://console.mistral.ai/api-keys/", listOf(
            ModelPreset("mistral-small-latest", "Paid / free trial may vary"), ModelPreset("mistral-large-latest", "Paid")
        )),
        ProviderPreset("OpenRouter", "https://openrouter.ai/api/v1", "https://openrouter.ai/settings/keys", listOf(
            ModelPreset("openrouter/auto", "Paid routing"), ModelPreset("meta-llama/llama-3.3-70b-instruct:free", "Free route; availability varies"), ModelPreset("deepseek/deepseek-chat-v3-0324:free", "Free route; availability varies")
        ), "OpenRouter model IDs and free routes change frequently; verify availability in its model directory."),
        ProviderPreset("Ollama (local)", "http://10.0.2.2:11434/v1", "https://ollama.com/download", listOf(
            ModelPreset("llama3.1", "Local / no per-token API fee"), ModelPreset("qwen2.5", "Local / no per-token API fee"), ModelPreset("deepseek-r1", "Local / no per-token API fee")
        ), "On a physical phone, replace 10.0.2.2 with your PC's LAN IP; keep Ollama bound to the LAN securely."),
        ProviderPreset("GitHub Copilot", "", "https://github.com/features/copilot", emptyList(),
            "Copilot subscription is not a general-purpose public API key for this app. It cannot be connected through this Chat Completions adapter.")
    )
}
