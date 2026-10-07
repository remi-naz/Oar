package dev.ridill.oar.tags.domain.util

import java.text.Normalizer

class TextNormalizer {
    private val combiningMarksRegex = Regex("\\p{Mn}+")
    private val tokenSplitRegex = Regex("\\W+")

    fun normalize(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(combiningMarksRegex, "")
        .lowercase()
        .trim()

    fun tokenize(text: String): List<String> = tokenizeNormalized(normalize(text))

    /** Splits text that's already been through [normalize] - skips re-normalizing it. */
    fun tokenizeNormalized(normalizedText: String): List<String> = normalizedText
        .split(tokenSplitRegex)
        .filter { it.isNotBlank() }
}
