package dev.ridill.oar.core.ui.components

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.ui.util.TextFormat
import java.util.Locale

class AmountOutputTransformation(
    private val locale: Locale = LocaleUtil.defaultLocale
) : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        val text = originalText
        val containsNonDigitChars = text.any { !it.isDigit() }
        if (containsNonDigitChars) return

        val formatted = text.toString().toDoubleOrNull()
            ?.let { TextFormat.number(value = it, locale = locale) }
            ?: return

        // Insert only the grouping separators rather than replacing the whole
        // buffer, so each digit keeps its original offset mapping. Replacing
        // the entire range collapses that mapping into a single segment,
        // which makes the cursor position (and backspace) resolve incorrectly
        // and can end up deleting the whole string instead of one digit.
        val insertions = mutableListOf<Pair<Int, Char>>()
        var digitIndex = 0
        formatted.forEach { ch ->
            if (digitIndex < text.length && ch == text[digitIndex]) {
                digitIndex++
            } else {
                insertions += digitIndex to ch
            }
        }
        if (digitIndex != text.length) return
        insertions.asReversed().forEach { (index, separator) ->
            replace(index, index, separator.toString())
        }
    }
}

@Composable
fun rememberAmountOutputTransformation(
    locale: Locale = LocaleUtil.defaultLocale
): AmountOutputTransformation = remember(locale) {
    AmountOutputTransformation(locale = locale)
}