package dev.ridill.oar.core.domain.logging

/**
 * Last line of defense that masks sensitive values before a log line is persisted.
 * Log statements should avoid including such values in the first place.
 */
class LogRedactor {

    // Insertion order matters: tokens and emails must be masked before the broader VPA and number patterns
    private val rules: Map<Regex, String> = mapOf(
        "(?i)bearer\\s+[A-Za-z0-9._~+/=-]+".toRegex() to "Bearer [REDACTED]",
        "eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+".toRegex() to "[TOKEN]",
        "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}".toRegex() to "[EMAIL]",
        "[A-Za-z0-9._-]{2,}@[A-Za-z]{2,}".toRegex() to "[VPA]",
        "(?i)(rs\\.?|inr|usd|eur|gbp|[₹$€£¥])\\s?\\d[\\d,]*(\\.\\d+)?".toRegex() to "[AMOUNT]",
        "\\b\\d[\\d,]*\\.\\d+\\b".toRegex() to "[NUMBER]",
        "\\d{6,}".toRegex() to "[NUMBER]",
    )

    fun redact(message: String): String = rules.entries.fold(message) { acc, (regex, replacement) ->
        regex.replace(acc, replacement)
    }
}
