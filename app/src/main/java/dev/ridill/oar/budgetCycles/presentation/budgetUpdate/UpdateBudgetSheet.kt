package dev.ridill.oar.budgetCycles.presentation.budgetUpdate

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.OutlinedTextFieldSheetContent
import dev.ridill.oar.core.ui.components.rememberAmountOutputTransformation
import dev.ridill.oar.core.ui.util.LocalCurrencyPreference
import dev.ridill.oar.core.ui.util.UiText

@Composable
fun UpdateBudgetSheet(
    placeholder: String,
    inputState: TextFieldState,
    onConfirm: () -> Unit,
    errorMessage: UiText?,
    modifier: Modifier = Modifier
) {
    val localCurrency = LocalCurrencyPreference.current
    OutlinedTextFieldSheetContent(
        titleRes = R.string.destination_update_budget,
        inputState = inputState,
        onConfirm = onConfirm,
        placeholder = placeholder,
        modifier = modifier,
        text = stringResource(R.string.monthly_budget_input_text),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        errorMessage = errorMessage,
        outputTransformation = rememberAmountOutputTransformation(),
        prefix = { Text(localCurrency.symbol) }
    )
}