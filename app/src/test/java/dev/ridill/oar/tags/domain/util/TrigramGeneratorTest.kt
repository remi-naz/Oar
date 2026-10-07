package dev.ridill.oar.tags.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class TrigramGeneratorTest {

    private lateinit var trigramGenerator: TrigramGenerator

    @Before
    fun setUp() {
        trigramGenerator = TrigramGenerator(TextNormalizer())
    }

    @Test
    fun forToken_padsAndWindows() {
        // "cat" -> "__cat_" -> __c, _ca, cat, at_
        assertThat(trigramGenerator.forToken("cat")).containsExactly("__c", "_ca", "cat", "at_").inOrder()
    }

    @Test
    fun forName_unionsTrigramsOfAllTokens() {
        val trigrams = trigramGenerator.forName("Food Out")
        assertThat(trigrams).containsAtLeastElementsIn(trigramGenerator.forToken("food"))
        assertThat(trigrams).containsAtLeastElementsIn(trigramGenerator.forToken("out"))
    }

    @Test
    fun forName_isAccentInsensitive() {
        assertThat(trigramGenerator.forName("café")).isEqualTo(trigramGenerator.forName("cafe"))
    }
}
