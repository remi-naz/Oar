package dev.ridill.oar.core.ui.util

import android.icu.text.CompactDecimalFormat
import androidx.compose.runtime.Composable
import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.tryOrNull
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object TextFormat {
    fun currency(
        amount: Number,
        currency: Currency = LocaleUtil.defaultCurrency,
        locale: Locale = LocaleUtil.defaultLocale,
        maxFractionDigits: Int = currency.defaultFractionDigits,
        minFractionDigits: Int = DEFAULT_MIN_FRACTION_DIGITS
    ): String = NumberFormat.getCurrencyInstance(locale)
        .apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = minFractionDigits
            setCurrency(currency)
        }
        .format(amount)

    @Composable
    fun currencyAmount(
        amount: Number,
        currency: Currency = LocalCurrencyPreference.current,
        locale: Locale = LocaleUtil.defaultLocale,
        maxFractionDigits: Int = currency.defaultFractionDigits,
        minFractionDigits: Int = DEFAULT_MIN_FRACTION_DIGITS
    ): String = NumberFormat.getCurrencyInstance(locale)
        .apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = minFractionDigits
            setCurrency(currency)
        }
        .format(amount)

    fun number(
        value: Number,
        locale: Locale = LocaleUtil.defaultLocale,
        maxFractionDigits: Int = DEFAULT_MAX_FRACTION_DIGITS,
        minFractionDigits: Int = DEFAULT_MIN_FRACTION_DIGITS,
        isGroupingUsed: Boolean = true
    ): String = NumberFormat.getNumberInstance(locale)
        .apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = minFractionDigits
            this.isGroupingUsed = isGroupingUsed
        }
        .format(value)

    fun percent(
        value: Number,
        locale: Locale = LocaleUtil.defaultLocale,
        maxFractionDigits: Int = DEFAULT_MAX_FRACTION_DIGITS,
        minFractionDigits: Int = DEFAULT_MIN_FRACTION_DIGITS,
        isGroupingUsed: Boolean = true
    ): String = NumberFormat.getPercentInstance(locale)
        .apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = minFractionDigits
            this.isGroupingUsed = isGroupingUsed
        }
        .format(value)

    fun compactNumber(
        value: Number,
        locale: Locale = LocaleUtil.defaultLocale,
        compactStyle: CompactDecimalFormat.CompactStyle = CompactDecimalFormat.CompactStyle.SHORT,
        maxFractionDigits: Int = DEFAULT_MAX_FRACTION_DIGITS,
        minFractionDigits: Int = DEFAULT_MIN_FRACTION_DIGITS,
        isGroupingUsed: Boolean = true
    ): String = CompactDecimalFormat.getInstance(locale, compactStyle)
        .apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = minFractionDigits
            this.isGroupingUsed = isGroupingUsed
        }
        .format(value)

    fun parseNumber(
        value: String,
        locale: Locale = LocaleUtil.defaultLocale
    ): Double? = tryOrNull {
        val regex = NUMBER_PARSE_CLEANER_PATTERN.toRegex()
        NumberFormat.getNumberInstance(locale)
            .parse(value.replace(regex, String.Empty))
            ?.toDouble()
    }
}

private const val DEFAULT_MAX_FRACTION_DIGITS = 2
private const val DEFAULT_MIN_FRACTION_DIGITS = 0

private const val NUMBER_PARSE_CLEANER_PATTERN = "[^\\d.,]"