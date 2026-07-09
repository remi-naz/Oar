package dev.ridill.oar.arithmetic.domain

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import com.notkamui.keval.Keval
import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.core.domain.util.tryOrNull
import dev.ridill.oar.core.ui.util.TextFormat

class ArithmeticInputProcessor {

    fun isExpression(value: String): Boolean = value.any { char ->
        char == '+'
                || char == '-'
                || char == '*'
                || char == '/'
                || char == '%'
                || char == '('
                || char == ')'
    }

    fun onAction(action: NumpadAction, input: TextFieldState) {
        when (action) {
            is NumpadAction.Number -> {
                insertText(input, action.digit.toString())
            }

            is NumpadAction.OperatorInput -> {
                insertOperator(input, action.operation.expressionSymbol.toString())
            }

            NumpadAction.Decimal -> {
                insertDecimalPoint(input)
            }

            NumpadAction.Parenthesis -> {
                insertParenthesis(input)
            }

            NumpadAction.MultiplyHundred -> {
                multiplyHundred(input)
            }

            NumpadAction.Clear -> {
                clear(input)
            }

            NumpadAction.Backspace -> {
                backspace(input)
            }

            NumpadAction.Equals -> {
                evaluateExpression(input)
            }

            NumpadAction.Done -> Unit
        }
    }

    private fun backspace(input: TextFieldState) {
        val text = input.text
        if (text.isEmpty()) return
        input.setTextAndPlaceCursorAtEnd(text.dropLast(1).toString())
    }

    private fun clear(input: TextFieldState) {
        input.setTextAndPlaceCursorAtEnd(String.Empty)
    }

    private fun evaluateExpression(input: TextFieldState) {
        val expression = input.text.toString()
        if (expression.isBlank()) return
        val result = if (isExpression(expression)) {
            tryOrNull { Keval.eval(sanitizeExpression(expression)) }
        } else expression.toDoubleOrNull()

        result?.let { input.setTextAndPlaceCursorAtEnd(formatResult(it)) }
    }

    private fun insertText(input: TextFieldState, text: String) {
        val currentText = input.text
        input.setTextAndPlaceCursorAtEnd(currentText.toString() + text)
    }

    private fun insertOperator(input: TextFieldState, operator: String) {
        val currentText = input.text.toString()
        if (currentText.isEmpty()) {
            if (operator == MINUS_SYMBOL) input.setTextAndPlaceCursorAtEnd(operator)
            return
        }

        val lastChar = currentText.last()
        val newText = when (lastChar) {
            in OPERATOR_CHARS -> currentText.dropLast(1) + operator
            OPEN_PARENTHESIS_CHAR ->
                if (operator == MINUS_SYMBOL) currentText + operator else return

            else -> currentText + operator
        }

        input.setTextAndPlaceCursorAtEnd(newText)
    }

    private fun insertDecimalPoint(input: TextFieldState) {
        val currentText = input.text.toString()
        val segment = currentNumberSegment(currentText)
        if (segment.contains(DECIMAL_SYMBOL)) return

        val newText = if (segment.isEmpty()) "${currentText}0$DECIMAL_SYMBOL"
        else "$currentText$DECIMAL_SYMBOL"
        input.setTextAndPlaceCursorAtEnd(newText)
    }

    private fun insertParenthesis(input: TextFieldState) {
        val currentText = input.text.toString()
        val openCount = currentText.count { it == OPEN_PARENTHESIS_CHAR }
        val closeCount = currentText.count { it == CLOSE_PARENTHESIS_CHAR }
        val lastChar = currentText.lastOrNull()
        val canClose = openCount > closeCount
                && lastChar != null
                && (lastChar.isDigit() || lastChar == CLOSE_PARENTHESIS_CHAR)

        val symbol = if (canClose) CLOSE_PARENTHESIS_CHAR else OPEN_PARENTHESIS_CHAR
        input.setTextAndPlaceCursorAtEnd(currentText + symbol)
    }

    private fun multiplyHundred(input: TextFieldState) {
        val currentText = input.text.toString()
        val segment = currentNumberSegment(currentText)
        if (segment.isEmpty()) return
        val prefix = currentText.substring(0, currentText.length - segment.length)
        val newSegment = "${segment}00"
        input.setTextAndPlaceCursorAtEnd(prefix + newSegment)
    }

    // Trailing digits/decimal of the number being typed; a trailing '-' counts only as its sign, not a binary minus.
    private fun currentNumberSegment(text: String): String {
        var index = text.length
        while (index > 0) {
            val char = text[index - 1]
            when {
                char.isDigit() || char == '.' -> index--
                char == '-' && isSignAt(text, index - 1) -> index--
                else -> return text.substring(index)
            }
        }
        return text.substring(index)
    }

    private fun isSignAt(text: String, minusIndex: Int): Boolean {
        val precedingChar = text.getOrNull(minusIndex - 1)
        return precedingChar == null || precedingChar in OPERATOR_CHARS || precedingChar == OPEN_PARENTHESIS_CHAR
    }

    private fun formatResult(result: Double): String = TextFormat.number(result)

    // Strips a trailing operator/decimal-point/dangling '(' run left by an unfinished
    // expression, then auto-closes any remaining unmatched '(' so Keval can parse it.
    private fun sanitizeExpression(expression: String): String {
        var sanitized = expression.trim()
        while (sanitized.isNotEmpty() && sanitized.last().let {
                it in OPERATOR_CHARS || it == DECIMAL_SYMBOL.first() || it == OPEN_PARENTHESIS_CHAR
            }
        ) {
            sanitized = sanitized.dropLast(1)
        }

        val openCount = sanitized.count { it == OPEN_PARENTHESIS_CHAR }
        val closeCount = sanitized.count { it == CLOSE_PARENTHESIS_CHAR }
        if (openCount > closeCount) {
            sanitized += CLOSE_PARENTHESIS_CHAR.toString().repeat(openCount - closeCount)
        }

        return sanitized
    }
}

private const val DECIMAL_SYMBOL = "."
private const val MINUS_SYMBOL = "-"
private const val OPEN_PARENTHESIS_CHAR = '('
private const val CLOSE_PARENTHESIS_CHAR = ')'
private val OPERATOR_CHARS = Operation.entries.map { it.expressionSymbol }.toSet()
