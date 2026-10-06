package dev.ridill.oar.tags.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class FuzzyTagScorerTest {

    private lateinit var textNormalizer: TextNormalizer
    private lateinit var fuzzyTagScorer: FuzzyTagScorer

    @Before
    fun setUp() {
        textNormalizer = TextNormalizer()
        fuzzyTagScorer = FuzzyTagScorer(EditDistance())
    }

    private fun score(query: String, tagName: String): Double? =
        fuzzyTagScorer.score(textNormalizer.tokenize(query), textNormalizer.tokenize(tagName))

    @Test
    fun foodPut_matchesFoodOut() {
        assertThat(score("Food Put", "Food Out")).isNotNull()
    }

    @Test
    fun outFood_matchesFoodOut_wordOrderIndependent() {
        assertThat(score("Out Food", "Food Out")).isNotNull()
    }

    @Test
    fun fodOut_matchesFoodOut() {
        assertThat(score("Fod Out", "Food Out")).isNotNull()
    }

    @Test
    fun teh_matchesThe() {
        assertThat(score("teh", "the")).isNotNull()
    }

    @Test
    fun exactMatch_scoresHigherThanFuzzyMatch() {
        val exact = score("Food Out", "Food Out")!!
        val fuzzy = score("Fod Out", "Food Out")!!
        assertThat(exact).isGreaterThan(fuzzy)
    }

    @Test
    fun unrelatedTag_isRejected() {
        assertThat(score("Food", "Transport")).isNull()
    }

    @Test
    fun loneShortTokenTypo_doesNotMatchArbitraryShortTag() {
        assertThat(score("gas", "gym")).isNull()
    }

    @Test
    fun loneShortTokenTypo_withExactSiblingMatch_isAllowed() {
        // "Food Put" -> "put" alone would fail, but "food" matches exactly, relaxing "put".
        assertThat(score("Food Put", "Food Out")).isNotNull()
    }

    @Test
    fun accentedQuery_matchesUnaccentedTag() {
        assertThat(score("cafe", "café")).isEqualTo(1.0)
    }

    @Test
    fun lastTokenIsTreatedAsPrefix() {
        assertThat(score("Foo", "Food")).isNotNull()
    }

    @Test
    fun minScoreThreshold_filtersWeakMatches() {
        val fuzzy = score("Fod Out", "Food Out")!!
        assertThat(fuzzy).isAtLeast(FuzzyConfig.DEFAULT.minScore)
    }
}
