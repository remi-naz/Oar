package dev.ridill.oar.onboarding.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.DisplayMediumText
import dev.ridill.oar.core.ui.components.OarTextField
import dev.ridill.oar.core.ui.components.SpacerSmall
import dev.ridill.oar.core.ui.components.rememberAmountOutputTransformation
import dev.ridill.oar.core.ui.theme.BorderWidthStandard
import dev.ridill.oar.core.ui.theme.spacing
import java.util.Currency

@Composable
fun SetupBudgetCyclesPage(
    budgetInput: TextFieldState,
    selectedCurrency: Currency,
    onCurrencyClick: () -> Unit,
    onStartBudgetingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInputNotEmpty by remember {
        derivedStateOf { budgetInput.text.isNotEmpty() }
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large)
            .verticalScroll(rememberScrollState())
    ) {
        DisplayMediumText(
            text = stringResource(R.string.onboarding_page_setup_budget_cycle_title),
            modifier = Modifier
                .padding(vertical = MaterialTheme.spacing.medium)
        )

        BudgetInput(
            state = budgetInput,
            selectedCurrency = selectedCurrency,
            onCurrencyClick = onCurrencyClick
        )
        SpacerSmall()
        AnimatedVisibility(
            visible = isInputNotEmpty,
            modifier = Modifier
                .align(Alignment.End)
        ) {
            OnboardingButton(
                onClick = {
                    keyboardController?.hide()
                    onStartBudgetingClick()
                }
            ) {
                Text(stringResource(R.string.start_budgeting))
            }
        }
    }
}

@Composable
private fun BudgetInput(
    state: TextFieldState,
    selectedCurrency: Currency,
    onCurrencyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = LocalContentColor.current
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.onboarding_page_set_budget_message),
            style = MaterialTheme.typography.labelMedium
        )
        OarTextField(
            state = state,
            modifier = Modifier
                .fillMaxWidth(),
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = contentColor,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = contentColor,
                unfocusedIndicatorColor = contentColor,
                unfocusedPlaceholderColor = contentColor,
                focusedPlaceholderColor = contentColor,
                unfocusedSupportingTextColor = contentColor,
                focusedSupportingTextColor = contentColor,
                cursorColor = contentColor,
            ),
            shape = MaterialTheme.shapes.medium,
            placeholder = { Text(stringResource(R.string.enter_budget)) },
            supportingText = { Text(stringResource(R.string.you_can_change_budget_later_in_settings)) },
            outputTransformation = rememberAmountOutputTransformation(),
            prefix = {
                OutlinedIconButton(
                    onClick = onCurrencyClick,
                    border = BorderStroke(BorderWidthStandard, contentColor)
                ) {
                    Text(
                        text = selectedCurrency.symbol,
                        color = contentColor
                    )
                }
            }
        )
    }
}