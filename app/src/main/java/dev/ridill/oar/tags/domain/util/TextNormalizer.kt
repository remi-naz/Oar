package dev.ridill.oar.tags.domain.util

import java.text.Normalizer

class TextNormalizer {
    private val COMBINING_MARKS = Regex("\\p{Mn}+")
    private val TOKEN_SPLIT = Regex("\\W+")

    fun normalize(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(COMBINING_MARKS, "")
        .lowercase()
        .trim()

    fun tokenize(text: String): List<String> = normalize(text)
        .split(TOKEN_SPLIT)
        .filter { it.isNotBlank() }
}
