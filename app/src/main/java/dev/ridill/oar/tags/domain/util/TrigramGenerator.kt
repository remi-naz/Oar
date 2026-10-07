package dev.ridill.oar.tags.domain.util

class TrigramGenerator(
    private val textNormalizer: TextNormalizer
) {
    fun forToken(normalizedToken: String): List<String> =
        "__${normalizedToken}_"
            .windowed(size = 3, step = 1)
            .distinct()

    fun forTokens(normalizedTokens: List<String>): Set<String> =
        normalizedTokens
            .flatMap(::forToken)
            .toSet()

    fun forName(name: String): Set<String> = forTokens(textNormalizer.tokenize(name))
}
