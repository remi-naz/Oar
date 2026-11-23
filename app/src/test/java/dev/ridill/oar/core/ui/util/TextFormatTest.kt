package dev.ridill.oar.core.ui.util

import com.google.common.truth.Truth.assertThat
import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.core.domain.util.LocaleUtil

import org.junit.After
import org.junit.Before
import org.junit.Test

class TextFormatTest {

    private lateinit var amountString: String

    @Before
    fun setUp() {
        amountString = TextFormat.currency(2000.0, LocaleUtil.currencyForCode("INR"))
    }

    @After
    fun tearDown() {
        amountString = String.Empty
    }

    @Test
    fun parseAmountStringWithCurrencySymbol_isNotNull() {
        println("Amount string - $amountString")
        val parsedNumber = TextFormat.parseNumber(amountString)
        assertThat(parsedNumber).isNotNull()
    }
}