package dev.ridill.oar.tags.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class EditDistanceTest {

    private lateinit var editDistance: EditDistance

    @Before
    fun setUp() {
        editDistance = EditDistance()
    }

    @Test
    fun identicalStrings_haveZeroDistance() {
        assertThat(editDistance.withinDistance("food", "food", maxDistance = 2)).isEqualTo(0)
    }

    @Test
    fun transposition_costsOneEdit() {
        assertThat(editDistance.withinDistance("teh", "the", maxDistance = 1)).isEqualTo(1)
    }

    @Test
    fun singleInsertion_costsOneEdit() {
        assertThat(editDistance.withinDistance("fod", "food", maxDistance = 1)).isEqualTo(1)
    }

    @Test
    fun distanceBeyondMax_returnsNull() {
        assertThat(editDistance.withinDistance("gym", "gas", maxDistance = 1)).isNull()
    }

    @Test
    fun lengthGapBeyondMax_returnsNullWithoutComputing() {
        assertThat(editDistance.withinDistance("a", "abcdef", maxDistance = 2)).isNull()
    }
}
