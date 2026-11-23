// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.core.security

/** Redacts common secrets before diagnostic text is written to logs or support reports. */
object SensitiveDataRedactor {
    private val patterns = listOf(
        Regex("(?i)(authorization\\s*:\\s*bearer\\s+)[A-Za-z0-9._~+/-]+=*") to "$1[REDACTED]",
        Regex("(?i)(api[_-]?key|access[_-]?token|refresh[_-]?token|password|passwd|secret)(\\s*[=:]\\s*)[^\\s,;]+") to "$1$2[REDACTED]",
        Regex("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", RegexOption.IGNORE_CASE) to "[EMAIL]",
        Regex("(?<!\\d)(?:\\+?\\d[\\d .()-]{7,}\\d)(?!\\d)") to "[PHONE]"
    )

    fun redact(input: String): String = patterns.fold(input) { value, (pattern, replacement) ->
        pattern.replace(value, replacement)
    }
}
