package dev.ridill.oar.arithmetic.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.ridill.oar.R

enum class Operation(
    val expressionSymbol: Char,
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val contentDescriptionRes: Int,
) {
    Add(
        expressionSymbol = '+',
        iconRes = R.drawable.ic_rounded_plus,
        contentDescriptionRes = R.string.cd_operation_add
    ),
    Subtract(
        expressionSymbol = '-',
        iconRes = R.drawable.ic_rounded_minus,
        contentDescriptionRes = R.string.cd_operation_subtract
    ),
    Multiply(
        expressionSymbol = '*',
        iconRes = R.drawable.ic_rounded_multiply,
        contentDescriptionRes = R.string.cd_operation_multiply
    ),
    Divide(
        expressionSymbol = '/',
        iconRes = R.drawable.ic_rounded_divide,
        contentDescriptionRes = R.string.cd_operation_divide
    )
}
