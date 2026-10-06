package dev.ridill.oar.tags.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class TextNormalizerTest {

    private lateinit var textNormalizer: TextNormalizer

    @Before
    fun setUp() {
        textNormalizer = TextNormalizer()
    }

    @Test
    fun normalize_lowercasesAndTrims() {
        assertThat(textNormalizer.normalize("  Food Out  ")).isEqualTo("food out")
    }

    @Test
    fun normalize_stripsDiacritics() {
        assertThat(textNormalizer.normalize("café")).isEqualTo("cafe")
    }

    @Test
    fun tokenize_splitsOnNonWordChars() {
        assertThat(textNormalizer.tokenize("Food Put!")).containsExactly("food", "put").inOrder()
    }

    @Test
    fun tokenize_ofBlank_returnsEmptyList() {
        assertThat(textNormalizer.tokenize("   ")).isEmpty()
    }
}
