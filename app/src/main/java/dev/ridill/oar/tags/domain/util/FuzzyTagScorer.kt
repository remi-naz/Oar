package dev.ridill.oar.tags.domain.util

/**
 * Scores a tag's tokens against a query's tokens. Word order doesn't matter - each query token
 * is matched against its best candidate among the tag's tokens. The last query token is treated
 * as a prefix, since the user may still be typing it.
 */
class FuzzyTagScorer(
    private val editDistance: EditDistance
) {

    /** Null means the tag is rejected - at least one query token had no acceptable match. */
    fun score(
        queryTokens: List<String>,
        tagTokens: List<String>,
        config: FuzzyConfig = FuzzyConfig.DEFAULT
    ): Double? {
        if (queryTokens.isEmpty() || tagTokens.isEmpty()) return null

        val matches = arrayOfNulls<Double>(queryTokens.size)
        val isExact = BooleanArray(queryTokens.size)
        for (i in queryTokens.indices) {
            val isLast = i == queryTokens.lastIndex
            val match = bestMatch(queryTokens[i], tagTokens, isLast, config, allowRelaxation = false)
            matches[i] = match
            isExact[i] = match == EXACT_SCORE
        }

        for (i in queryTokens.indices) {
            if (matches[i] != null) continue
            val token = queryTokens[i]
            if (token.length > config.shortWordRelaxationLength) continue
            val anotherTokenMatchedExactly = isExact.indices.any { it != i && isExact[it] }
            if (!anotherTokenMatchedExactly) continue
            val isLast = i == queryTokens.lastIndex
            matches[i] = bestMatch(token, tagTokens, isLast, config, allowRelaxation = true)
        }

        if (matches.any { it == null }) return null
        return matches.filterNotNull().average()
    }

    private fun bestMatch(
        queryToken: String,
        tagTokens: List<String>,
        isLast: Boolean,
        config: FuzzyConfig,
        allowRelaxation: Boolean
    ): Double? = tagTokens
        .mapNotNull { tagToken -> matchScore(queryToken, tagToken, isLast, config, allowRelaxation) }
        .maxOrNull()

    private fun matchScore(
        queryToken: String,
        tagToken: String,
        isLast: Boolean,
        config: FuzzyConfig,
        allowRelaxation: Boolean
    ): Double? {
        if (queryToken == tagToken) return EXACT_SCORE
        if (isLast && tagToken.length >= queryToken.length && tagToken.startsWith(queryToken)) {
            return PREFIX_SCORE
        }
        if (isAdjacentTransposition(queryToken, tagToken)) return TRANSPOSITION_SCORE

        val budget = if (allowRelaxation) config.shortWordRelaxationEdits else config.typoBudgetFor(queryToken.length)
        if (budget <= 0) return null
        if (kotlin.math.abs(queryToken.length - tagToken.length) > budget) return null
        if (config.requireFirstCharMatch && !allowRelaxation && queryToken.first() != tagToken.first()) return null

        val distance = editDistance.withinDistance(queryToken, tagToken, budget) ?: return null
        return if (distance == 0) EXACT_SCORE else (1.0 - distance * 0.2).coerceAtLeast(0.5)
    }

    /** True if [a] and [b] are identical except for one adjacent pair of swapped characters. */
    private fun isAdjacentTransposition(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        val diffs = a.indices.filter { a[it] != b[it] }
        if (diffs.size != 2) return false
        val (i, j) = diffs
        return j == i + 1 && a[i] == b[j] && a[j] == b[i]
    }

    private companion object {
        const val EXACT_SCORE = 1.0
        const val PREFIX_SCORE = 0.9
        const val TRANSPOSITION_SCORE = 0.85
    }
}
