package dev.ridill.oar.transactions.presentation.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.R
import dev.ridill.oar.budgetCycles.presentation.currencyUpdate.CurrencySelectionSheet
import dev.ridill.oar.budgetCycles.presentation.currencyUpdate.CurrencySelectionViewModel
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.ui.components.ComponentViewModelScope
import dev.ridill.oar.core.ui.components.OarModalBottomSheet
import dev.ridill.oar.core.ui.components.OarTextField
import dev.ridill.oar.core.ui.components.rememberAmountOutputTransformation
import dev.ridill.oar.core.ui.theme.IconSizeMedium
import dev.ridill.oar.transactions.presentation.amountTransformation.AmountTransformationSheet
import dev.ridill.oar.transactions.presentation.amountTransformation.AmountTransformationViewModel
import java.util.Currency

@Composable
fun AmountInput(
    inputState: TextFieldState,
    modifier: Modifier = Modifier,
    currency: Currency = LocaleUtil.defaultCurrency,
    onCurrencySelect: (Currency) -> Unit = {},
    isInputAnExpression: Boolean = false,
    onExpressionEvalClick: () -> Unit = {},
    focusManager: FocusManager = LocalFocusManager.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = { focusManager.moveFocus(FocusDirection.Next) },
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
) {
    val showTransformButton by remember {
        derivedStateOf {
            inputState.text.toString()
                .toDoubleOrNull().orZero() > Double.Zero
        }
    }
    var showCurrencySelectionSheet by rememberSaveable { mutableStateOf(false) }
    var showTransformationSheet by rememberSaveable { mutableStateOf(false) }

    ComponentViewModelScope("amount_input") {
        val currencySelectionViewModel: CurrencySelectionViewModel = hiltViewModel()
        val amountTransformationViewModel: AmountTransformationViewModel = hiltViewModel()

        OarTextField(
            state = inputState,
            modifier = modifier
                .defaultMinSize(minWidth = InputMinWidth),
            leadingIcon = {
                FilledTonalIconButton(
                    onClick = { showCurrencySelectionSheet = true },
                ) {
                    Text(currency.symbol)
                }
            },
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                textAlign = TextAlign.Center
            ),
            placeholder = {
                Text(
                    text = stringResource(R.string.amount_zero),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .defaultMinSize(minWidth = InputMinWidth),
                    textAlign = TextAlign.Center
                )
            },
            keyboardOptions = keyboardOptions.copy(
                keyboardType = KeyboardType.Phone,
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            onKeyboardAction = onKeyboardAction,
            lineLimits = lineLimits,
            outputTransformation = rememberAmountOutputTransformation(),
            trailingIcon = {
                when {
                    isInputAnExpression -> {
                        IconButton(onClick = onExpressionEvalClick) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_equals),
                                contentDescription = stringResource(R.string.cd_evaluate_expression),
                                modifier = Modifier
                                    .size(IconSizeMedium)
                            )
                        }
                    }

                    showTransformButton -> {
                        IconButton(onClick = { showTransformationSheet = true }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_gears),
                                contentDescription = stringResource(R.string.cd_transform_amount)
                            )
                        }
                    }
                }
            }
        )

        if (showCurrencySelectionSheet) {
            val currenciesLazyPagingItems = currencySelectionViewModel.currencyPagingData
                .collectAsLazyPagingItems()

            OarModalBottomSheet(
                onDismissRequest = { showCurrencySelectionSheet = false }
            ) {
                CurrencySelectionSheet(
                    searchQueryState = currencySelectionViewModel.searchQueryState,
                    selectedCode = currency.currencyCode,
                    currenciesPagingData = currenciesLazyPagingItems,
                    onConfirm = { selectedCurrency ->
                        onCurrencySelect(selectedCurrency)
                        showCurrencySelectionSheet = false
                    }
                )
            }
        }

        if (showTransformationSheet) {
            val selectedTransformation by amountTransformationViewModel.selectedTransformation.collectAsStateWithLifecycle()

            OarModalBottomSheet(
                onDismissRequest = { showTransformationSheet = false }
            ) {
                AmountTransformationSheet(
                    selectedTransformation = selectedTransformation,
                    onTransformationSelect = amountTransformationViewModel::onTransformationSelect,
                    factorInput = amountTransformationViewModel.factorInputState,
                    onTransformClick = {
                        inputState.setTextAndPlaceCursorAtEnd(
                            amountTransformationViewModel
                                .transformedAmount(inputState.text.toString())
                        )
                        showTransformationSheet = false
                    }
                )
            }
        }
    }
}

private val InputMinWidth = 160.dp
