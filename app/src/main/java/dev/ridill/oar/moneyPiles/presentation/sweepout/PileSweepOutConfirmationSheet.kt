package dev.ridill.oar.moneyPiles.presentation.sweepout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.ui.components.ButtonWithLoadingIndicator
import dev.ridill.oar.core.ui.components.LabelledSwitch
import dev.ridill.oar.core.ui.theme.BorderWidthStandard
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.IconSizeMedium
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import dev.ridill.oar.moneyPiles.presentation.components.ContributionTransactionItem
import dev.ridill.oar.moneyPiles.presentation.components.PileIconIndicator
import dev.ridill.oar.transactions.presentation.components.AmountInput
import java.time.LocalDateTime

@Composable
internal fun PileSweepOutConfirmationSheet(
    sweepAmountState: TextFieldState,
    state: PileSweepOutState,
    actions: PileSweepOutActions,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = state.pile?.currency ?: LocaleUtil.defaultCurrency
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        modifier = modifier
            .padding(horizontal = MaterialTheme.spacing.medium)
            .padding(bottom = MaterialTheme.spacing.medium)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            state.pile?.let { pile ->
                PileIconIndicator(
                    icon = pile.icon,
                    color = pile.color,
                    modifier = Modifier
                        .size(SweepIconContainerSize)
                )
            }
            Text(
                text = stringResource(R.string.pile_sweep_out_confirmation_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = stringResource(R.string.pile_sweep_out_confirmation_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AnimatedVisibility(visible = state.showCompletionWarning) {
            PileCompletionWarningBanner(
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        val isAmountError by remember(state.amountInputError) {
            derivedStateOf { state.amountInputError != null }
        }
        AmountInput(
            inputState = sweepAmountState,
            currency = currency,
            colors = TextFieldDefaults.tonalColors(),
            isError = isAmountError,
            supportingText = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.small)
                ) {
                    Box {
                        this@Row.AnimatedVisibility(isAmountError) {
                            Text(state.amountInputError?.asString().orEmpty())
                        }
                    }
                    Text(
                        text = stringResource(
                            R.string.pile_sweep_out_max_amount_hint,
                            TextFormat.currency(state.maxLimit, currency)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            LabelledSwitch(
                labelRes = R.string.pile_sweep_out_create_linked_transaction_label,
                checked = state.createLinkedTransaction,
                onCheckedChange = actions::onCreateLinkedTransactionToggle,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
            )
            AnimatedVisibility(visible = state.createLinkedTransaction) {
                state.pile?.let { pile ->
                    ExpensePreviewCard(
                        pile = pile,
                        formattedAmount = TextFormat.currency(
                            state.previewAmount,
                            currency
                        ),
                        timestamp = state.timestampNow,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.action_cancel))
            }
            ButtonWithLoadingIndicator(
                text = stringResource(R.string.pile_sweep_out_confirm_button),
                loading = state.loading,
                onClick = actions::onConfirm,
                enabled = state.confirmEnabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ExpensePreviewCard(
    pile: MoneyPileDetails,
    formattedAmount: String,
    timestamp: LocalDateTime,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        ContributionTransactionItem(
            pileName = pile.name,
            pileColor = pile.color,
            amount = formattedAmount,
            excluded = pile.contributionMode == PileContributionMode.FROM_BALANCE,
            timestamp = timestamp,
            movement = FundMovement.OUT,
        )
    }
}

@Composable
private fun PileCompletionWarningBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = WarningColor.copy(alpha = ContentAlpha.PERCENT_16),
        border = BorderStroke(
            width = BorderWidthStandard,
            color = WarningColor.copy(alpha = ContentAlpha.PERCENT_50),
        ),
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = WarningColor,
                modifier = Modifier.size(IconSizeMedium)
            )
            Text(
                text = stringResource(R.string.pile_sweep_out_will_complete_pile_warning),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private val WarningColor = Color(0xFFF9A825)

private val SweepIconContainerSize = 48.dp

@PreviewLightDark
@Composable
private fun PreviewPileSweepOutConfirmationSheet() {
    OarTheme {
        Surface {
            PileSweepOutConfirmationSheet(
                sweepAmountState = rememberTextFieldState("1000"),
                state = PileSweepOutState(
                    pile = MoneyPileDetails(
                        id = 1L,
                        name = "Japan trip",
                        icon = PileIcon.Travel,
                        colorCode = 0,
                        currency = LocaleUtil.defaultCurrency,
                        contributionMode = PileContributionMode.FROM_BALANCE,
                        targetAmount = 5000.0,
                        locked = false,
                        reminderCadence = PileReminderCadence.WEEKLY,
                        reminderBehavior = PileReminderBehavior.REMIND,
                        reminderAmount = 150.0,
                        createdTimestamp = DateUtil.now(),
                        targetDate = null,
                        completionTimestamp = null,
                    ),
                    maxLimit = 5000.0,
                    previewAmount = 5000.0,
                    createLinkedTransaction = true,
                ),
                actions = object : PileSweepOutActions {
                    override fun refreshTimestampNow() {}
                    override fun onCreateLinkedTransactionToggle(checked: Boolean) {}
                    override fun onConfirm() {}
                },
                onCancel = {}
            )
        }
    }
}
