package dev.ridill.oar.transactions.presentation.amountTransformation

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.domain.util.ifInfinite
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.transactions.domain.model.AmountTransformation
import javax.inject.Inject

@HiltViewModel
class AmountTransformationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val selectedTransformation = savedStateHandle
        .getStateFlow(SELECTED_TRANSFORMATION, AmountTransformation.DIVIDE_BY)

    val factorInputState = TextFieldState()

    fun onTransformationSelect(transformation: AmountTransformation) {
        savedStateHandle[SELECTED_TRANSFORMATION] = transformation
    }

    fun transformedAmount(amountString: String): String {
        val amount = amountString.toDoubleOrNull().orZero()
        val factor = factorInputState.text.toString()
        val result = when (selectedTransformation.value) {
            AmountTransformation.DIVIDE_BY ->
                amount / factor.toDoubleOrNull().orZero()

            AmountTransformation.MULTIPLIER ->
                amount * factor.toDoubleOrNull().orZero()

            AmountTransformation.PERCENT ->
                amount * (factor.toFloatOrNull().orZero() / 100f)
        }.ifInfinite { Double.Zero }

        return TextFormat.number(result)
    }
}

private const val SELECTED_TRANSFORMATION = "SELECTED_TRANSFORMATION"