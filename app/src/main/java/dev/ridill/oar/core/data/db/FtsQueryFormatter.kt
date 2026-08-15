package dev.ridill.oar.core.data.db

import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.core.domain.util.WhiteSpace

/** Converts free-form user input into a safe FTS4 prefix MATCH expression. */
class FtsQueryFormatter {
    fun prefixMatchOrNull(query: String?): String? = query
        ?.split(WHITESPACE)
        ?.mapNotNull { token ->
            token.replace(NON_ALPHANUMERIC, String.Empty)
                .takeIf { it.isNotBlank() }
                ?.let { "$it*" }
        }
        ?.filter { it.isNotEmpty() }
        ?.takeIf { it.isNotEmpty() }
        ?.joinToString(String.WhiteSpace)

    companion object {
        private val WHITESPACE = Regex("\\s+")
        private val NON_ALPHANUMERIC = Regex("[^\\p{L}\\p{N}]")
    }
}
