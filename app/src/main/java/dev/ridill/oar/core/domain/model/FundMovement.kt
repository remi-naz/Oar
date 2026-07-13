package dev.ridill.oar.core.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.theme.NegativeRed
import dev.ridill.oar.core.ui.theme.PositiveGreen

enum class FundMovement(
    @DrawableRes val iconRes: Int,
    val color: Color
) {
    IN(
        iconRes = R.drawable.ic_arrow_bottom_right,
        color = PositiveGreen
    ),
    OUT(
        iconRes = R.drawable.ic_arrow_top_right,
        color = NegativeRed
    ),
}

// Moved to extension val so every feature can have its own label
@get:StringRes
val FundMovement.creditOrDebitLabel: Int
    get() = when (this) {
        FundMovement.IN -> R.string.transaction_type_label_credit
        FundMovement.OUT -> R.string.transaction_type_label_debit
    }