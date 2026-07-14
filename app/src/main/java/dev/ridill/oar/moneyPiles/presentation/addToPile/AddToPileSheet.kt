package dev.ridill.oar.moneyPiles.presentation.addToPile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.ui.components.ButtonWithLoadingIndicator
import dev.ridill.oar.core.ui.components.OarTextField
import dev.ridill.oar.core.ui.components.rememberAmountOutputTransformation
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.LocalCurrencyPreference
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.moneyPiles.domain.model.MoneyPile
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileDepositDirection
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AddToPileSheet(
    pile: MoneyPile?,
    direction: PileDepositDirection,
    amountInputState: TextFieldState,
    isLoading: Boolean,
    actions: AddToPileActions,
    onQuickAmountClick: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(300.milliseconds)
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (pile != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.spacing.small)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color(pile.color).copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = pile.icon)
                }
                Column {
                    Text(
                        text = stringResource(
                            if (direction == PileDepositDirection.OUT) {
                                R.string.pile_action_withdraw
                            } else {
                                R.string.pile_action_add
                            }
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = pile.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        OarTextField(
            state = amountInputState,
            textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
            placeholder = {
                Text(
                    text = stringResource(R.string.amount_zero),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            },
            leadingIcon = { Text(LocalCurrencyPreference.current.symbol) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            outputTransformation = rememberAmountOutputTransformation(),
            modifier = Modifier
                .focusRequester(focusRequester)
                .padding(vertical = MaterialTheme.spacing.small)
        )

        if (pile != null && pile.reminderAmount > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
            ) {
                SuggestionChip(
                    onClick = { onQuickAmountClick(pile.reminderAmount) },
                    label = { Text("+" + TextFormat.currencyAmount(pile.reminderAmount)) }
                )
                SuggestionChip(
                    onClick = { onQuickAmountClick(QuickAmountExtra) },
                    label = { Text("+" + TextFormat.currencyAmount(QuickAmountExtra)) }
                )
            }
        }

        ButtonWithLoadingIndicator(
            textRes = if (direction == PileDepositDirection.OUT) {
                R.string.pile_action_withdraw
            } else {
                R.string.pile_action_add
            },
            loading = isLoading,
            onClick = actions::onConfirm,
            enabled = amountInputState.text.toString().toDoubleOrNull()?.let { it > 0 } ?: false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = MaterialTheme.spacing.medium)
        )
    }
}

private const val QuickAmountExtra = 100.0

@PreviewLightDark
@Composable
private fun PreviewAddToPileSheet() {
    OarTheme {
        AddToPileSheet(
            pile = MoneyPile(
                id = 1L,
                name = "Japan trip",
                note = "",
                icon = "🌸",
                color = 0xFFFF4CA6.toInt(),
                contributionMode = PileContributionMode.FROM_BALANCE,
                targetAmount = 5000.0,
                currentAmount = 3200.0,
                locked = false,
                reminderCadence = PileReminderCadence.WEEKLY,
                reminderBehavior = PileReminderBehavior.AUTO_ADD,
                reminderAmount = 150.0,
                createdTimestamp = DateUtil.now()
            ),
            direction = PileDepositDirection.IN,
            amountInputState = rememberTextFieldState(),
            isLoading = false,
            actions = object : AddToPileActions {
                override fun onConfirm() {}
            },
            onQuickAmountClick = {}
        )
    }
}
