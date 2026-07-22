package dev.ridill.oar.moneyPiles.presentation.addToPile

import androidx.annotation.StringRes
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.ui.components.ButtonWithLoadingIndicator
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.adjustedContentColor
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import dev.ridill.oar.transactions.presentation.components.AmountInput
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AddToPileSheetContent(
    movement: FundMovement,
    amountInputState: TextFieldState,
    state: AddToPileState,
    actions: AddToPileActions,
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
        if (state.pile != null) {
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
                        .background(state.pile.color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(state.pile.icon.iconRes),
                        contentDescription = stringResource(state.pile.icon.labelRes),
                        tint = state.pile.color.adjustedContentColor()
                    )
                }
                Column {
                    Text(
                        text = stringResource(movement.pileTransactionMovement),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = state.pile.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        AmountInput(
            inputState = amountInputState,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            currency = state.pile?.currency ?: LocaleUtil.defaultCurrency,
            modifier = Modifier
                .focusRequester(focusRequester)
                .padding(vertical = MaterialTheme.spacing.small)
        )

        if (state.pile != null && state.pile.reminderAmount.orZero() > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
            ) {
                SuggestionChip(
                    onClick = actions::onAddRecommendedAmountClick,
                    label = {
                        Text(
                            text = stringResource(
                                R.string.add_recommended_amount,
                                TextFormat.currencyAmount(state.pile.reminderAmount.orZero())
                            )
                        )
                    }
                )
            }
        }

        ButtonWithLoadingIndicator(
            textRes = movement.pileTransactionMovement,
            loading = state.loading,
            onClick = actions::onConfirm,
            enabled = state.addEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = MaterialTheme.spacing.medium)
        )
    }
}

@get:StringRes
private val FundMovement.pileTransactionMovement: Int
    get() = when (this) {
        FundMovement.IN -> R.string.pile_action_add
        FundMovement.OUT -> R.string.pile_action_withdraw
    }

@PreviewLightDark
@Composable
private fun PreviewAddToPileSheetContent() {
    OarTheme {
        Surface {
            AddToPileSheetContent(
                movement = FundMovement.IN,
                state = AddToPileState(
                    pile = MoneyPileDetails(
                        id = 1L,
                        name = "Japan trip",
                        icon = PileIcon.LandProperty,
                        colorCode = 0xFFFF4CA6.toInt(),
                        contributionMode = PileContributionMode.FROM_BALANCE,
                        targetAmount = 5000.0,
                        locked = false,
                        reminderCadence = PileReminderCadence.WEEKLY,
                        reminderBehavior = PileReminderBehavior.AUTO_ADD,
                        reminderAmount = 150.0,
                        createdTimestamp = DateUtil.now(),
                        targetDate = null,
                        currency = LocaleUtil.defaultCurrency,
                    )
                ),
                amountInputState = rememberTextFieldState(),
                actions = object : AddToPileActions {
                    override fun onAddRecommendedAmountClick() {}
                    override fun onConfirm() {}
                },
            )
        }
    }
}
