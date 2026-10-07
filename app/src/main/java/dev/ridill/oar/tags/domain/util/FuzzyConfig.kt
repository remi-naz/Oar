package dev.ridill.oar.tags.domain.util

/** Tunable thresholds for fuzzy tag matching. See [typoBudgetFor] and [FuzzyTagScorer]. */
data class FuzzyConfig(
    val requireFirstCharMatch: Boolean = true,
    val minScore: Double = 0.6,
    /** A token at or below this length may only use [shortWordRelaxationEdits] if another query token matches exactly. */
    val shortWordRelaxationLength: Int = 3,
    val shortWordRelaxationEdits: Int = 1,
    val candidateLimit: Int = 100,
    /** Below this normalized query length, search falls back to exact/prefix matching only. */
    val minQueryLengthForFuzzy: Int = 3
) {
    /** Typo budget (allowed edits) by token length: 1-3 -> 0, 4-7 -> 1, 8+ -> 2. */
    fun typoBudgetFor(tokenLength: Int): Int = when {
        tokenLength <= 3 -> 0
        tokenLength <= 7 -> 1
        else -> 2
    }

    companion object {
        val DEFAULT = FuzzyConfig()
    }
}
