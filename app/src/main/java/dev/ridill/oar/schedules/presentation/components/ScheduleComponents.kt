package dev.ridill.oar.schedules.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.model.creditOrDebitLabel
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.One
import dev.ridill.oar.core.domain.util.WhiteSpace
import dev.ridill.oar.core.ui.components.AmountWithMovementIndicator
import dev.ridill.oar.core.ui.components.BodyMediumText
import dev.ridill.oar.core.ui.components.BodySmallText
import dev.ridill.oar.core.ui.components.ListItemLeadingContentWithColorIndicator
import dev.ridill.oar.core.ui.components.MovementIndicatorIcon
import dev.ridill.oar.core.ui.components.TwoLineDateText
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.mergedContentDescription
import java.time.LocalDateTime

@Composable
private fun LeadingContent(
    nextPaymentTimestamp: LocalDateTime?,
    modifier: Modifier = Modifier
) {
    ListItemLeadingContentWithColorIndicator(color = null, modifier = modifier) {
        if (nextPaymentTimestamp != null) {
            TwoLineDateText(
                dateLine1 = nextPaymentTimestamp.format(DateUtil.Formatters.MMM),
                dateLine2 = nextPaymentTimestamp.format(DateUtil.Formatters.dayOfMonthOrdinal)
            )
        } else {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_wallet_done),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun ListItemContent(
    note: String?,
    modifier: Modifier = Modifier
) {
    val isNoteNullOrEmpty = remember(note) { note.isNullOrEmpty() }
    Text(
        text = note.orEmpty()
            .ifEmpty { stringResource(R.string.generic_schedule_title) },
        fontStyle = if (isNoteNullOrEmpty) FontStyle.Italic
        else null,
        color = LocalContentColor.current
            .copy(
                alpha = if (isNoteNullOrEmpty) ContentAlpha.SUB_CONTENT
                else Float.One
            ),
        modifier = modifier
    )
}

@Composable
private fun buildScheduleContentDesc(
    note: String?,
    amount: String,
    movement: FundMovement,
    nextPaymentTimestamp: LocalDateTime?,
    lastPaymentTimestamp: LocalDateTime?,
): String = buildString {
    if (!note.isNullOrEmpty()) {
        append(
            stringResource(
                when (movement) {
                    FundMovement.IN -> R.string.cd_credit_of_amount_for_note
                    FundMovement.OUT -> R.string.cd_debit_of_amount_for_note
                },
                amount,
                note
            )
        )
    } else {
        append(
            stringResource(
                when (movement) {
                    FundMovement.IN -> R.string.cd_credit_of_amount
                    FundMovement.OUT -> R.string.cd_debit_of_amount
                },
                amount
            )
        )
    }

    nextPaymentTimestamp?.let {
        append(",")
        append(String.WhiteSpace)
        append(
            stringResource(
                R.string.cd_schedule_list_item_due_append,
                it.format(DateUtil.Formatters.localizedDateLong)
            )
        )
    }

    lastPaymentTimestamp?.let {
        append(",")
        append(String.WhiteSpace)
        append(
            stringResource(
                R.string.cd_schedule_list_item_last_payment_append,
                it.format(DateUtil.Formatters.localizedDateLong)
            )
        )
    }
}

@Composable
internal fun ScheduleListItem(
    note: String?,
    amount: String,
    movement: FundMovement,
    nextPaymentTimestamp: LocalDateTime?,
    lastPaymentTimestamp: LocalDateTime?,
    modifier: Modifier = Modifier,
    colors: ListItemColors = ListItemDefaults.colors(),
    elevation: ListItemElevation = ListItemDefaults.elevation(),
) {
    val scheduleItemContentDescription = buildScheduleContentDesc(
        note = note,
        amount = amount,
        movement = movement,
        nextPaymentTimestamp = nextPaymentTimestamp,
        lastPaymentTimestamp = lastPaymentTimestamp,
    )

    ListItem(
        modifier = modifier
            .mergedContentDescription(scheduleItemContentDescription),
        leadingContent = { LeadingContent(nextPaymentTimestamp) },
        trailingContent = {
            AmountWithMovementIndicator(
                value = amount,
                movement = movement
            )
        },
        supportingContent = lastPaymentTimestamp?.let { timestamp ->
            {
                Text(
                    text = stringResource(
                        R.string.last_payment_colon_value,
                        timestamp.format(DateUtil.Formatters.localizedDateMedium)
                    )
                )
            }
        },
        colors = colors,
        elevation = elevation,
    ) {
        ListItemContent(note)
    }
}

@Composable
internal fun ScheduleListItem(
    onClick: () -> Unit,
    note: String?,
    amount: String,
    movement: FundMovement,
    nextPaymentTimestamp: LocalDateTime?,
    lastPaymentTimestamp: LocalDateTime?,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    colors: ListItemColors = ListItemDefaults.colors(),
    elevation: ListItemElevation = ListItemDefaults.elevation(),
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
) {
    val scheduleItemContentDescription = buildScheduleContentDesc(
        note = note,
        amount = amount,
        movement = movement,
        nextPaymentTimestamp = nextPaymentTimestamp,
        lastPaymentTimestamp = lastPaymentTimestamp,
    )

    ListItem(
        onClick = onClick,
        modifier = modifier
            .mergedContentDescription(scheduleItemContentDescription),
        leadingContent = { LeadingContent(nextPaymentTimestamp) },
        trailingContent = {
            AmountWithMovementIndicator(
                value = amount,
                movement = movement
            )
        },
        supportingContent = lastPaymentTimestamp?.let { timestamp ->
            {
                Text(
                    text = stringResource(
                        R.string.last_payment_colon_value,
                        timestamp.format(DateUtil.Formatters.localizedDateMedium)
                    )
                )
            }
        },
        selected = selected,
        colors = colors,
        elevation = elevation,
        onLongClick = onLongClick,
        onLongClickLabel = onLongClickLabel,
    ) {
        ListItemContent(note)
    }
}

@Composable
fun ActiveScheduleItem(
    note: String?,
    amount: String,
    movement: FundMovement,
    paymentDay: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    elevation: CardElevation = CardDefaults.elevatedCardElevation()
) {
    ElevatedCard(
        modifier = Modifier
            .widthIn(max = ActiveScheduleMaxWidth)
            .then(modifier),
        onClick = onClick,
        elevation = elevation
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            Box(
                modifier = Modifier
                    .size(
                        width = ActiveScheduleTypeIndicatorSize,
                        height = ActiveScheduleTypeIndicatorSize
                    )
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                MovementIndicatorIcon(movement)
            }

            Column(
                modifier = Modifier
                    .weight(Float.One)
            ) {
                BodyMediumText(
                    text = note.orEmpty()
                        .ifEmpty { stringResource(movement.creditOrDebitLabel) },
                    color = LocalContentColor.current.copy(
                        alpha = if (note.isNullOrEmpty()) ContentAlpha.SUB_CONTENT
                        else Float.One
                    ),
                    fontStyle = if (note.isNullOrEmpty()) FontStyle.Italic
                    else null,
                    maxLines = 2
                )
                BodySmallText(
                    text = stringResource(R.string.due_on_date, paymentDay),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                )
            }

            AmountWithMovementIndicator(
                value = amount,
                movement = movement,
                showTypeIndicator = false,
                textStyle = MaterialTheme.typography.titleLarge
            )
        }
    }
}

private val ActiveScheduleTypeIndicatorSize = 30.dp
private val ActiveScheduleMaxWidth = 300.dp

@PreviewLightDark
@Composable
private fun PreviewScheduleListItemCard() {
    OarTheme {
        ScheduleListItem(
            amount = "100",
            note = "Test",
            movement = FundMovement.OUT,
            nextPaymentTimestamp = DateUtil.now(),
            lastPaymentTimestamp = DateUtil.now(),
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewActiveScheduleCard() {
    OarTheme {
        ActiveScheduleItem(
            note = LoremIpsum().values.joinToString(),
            amount = "Rs.100",
            movement = FundMovement.OUT,
            paymentDay = "10th Wed",
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}
