package dev.ridill.oar.arithmetic.domain

sealed interface NumpadAction {
    data class Number(val digit: Char) : NumpadAction
    data class OperatorInput(val operation: Operation) : NumpadAction
    data object Decimal : NumpadAction
    data object Parenthesis : NumpadAction
    data object MultiplyHundred : NumpadAction
    data object Clear : NumpadAction
    data object Backspace : NumpadAction
    data object Equals : NumpadAction
    data object Done : NumpadAction
}
