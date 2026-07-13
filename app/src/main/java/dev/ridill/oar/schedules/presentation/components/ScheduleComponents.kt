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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.model.creditOrDebitLabel
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.NewLine
import dev.ridill.oar.core.domain.util.One
import dev.ridill.oar.core.domain.util.WhiteSpace
import dev.ridill.oar.core.ui.components.AmountWithTypeIndicator
import dev.ridill.oar.core.ui.components.BodyMediumText
import dev.ridill.oar.core.ui.components.BodySmallText
import dev.ridill.oar.core.ui.components.ListItemLeadingContentContainer
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.mergedContentDescription
import dev.ridill.oar.transactions.presentation.components.TypeIndicatorIcon
import java.time.LocalDateTime

@Composable
fun ScheduleListItem(
    note: String?,
    amount: String,
    type: FundMovement,
    nextPaymentTimestamp: LocalDateTime?,
    lastPaymentTimestamp: LocalDateTime?,
    modifier: Modifier = Modifier,
    colors: ListItemColors = ListItemDefaults.colors(),
    elevation: ListItemElevation = ListItemDefaults.elevation(),
) {
    val nextPaymentDateFormatted = remember(nextPaymentTimestamp) {
        nextPaymentTimestamp?.format(DateUtil.Formatters.MMM_ddth_spaceSep)
            ?.replace(String.WhiteSpace, String.NewLine)
    }
    val scheduleItemContentDescription = stringResource(
        R.string.cd_schedule_of_amount_for_date,
        amount,
        nextPaymentDateFormatted.orEmpty()
    )

    ListItem(
        modifier = modifier
            .mergedContentDescription(scheduleItemContentDescription),
        leadingContent = {
            ListItemLeadingContentContainer {
                if (!nextPaymentDateFormatted.isNullOrEmpty()) {
                    BodyMediumText(
                        text = nextPaymentDateFormatted,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_wallet_done),
                        contentDescription = null
                    )
                }
            }
        },
        trailingContent = {
            AmountWithTypeIndicator(
                value = amount,
                type = type
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
                )
        )
    }
}

@Composable
fun ScheduleListItem(
    onClick: () -> Unit,
    note: String?,
    amount: String,
    type: FundMovement,
    nextPaymentTimestamp: LocalDateTime?,
    lastPaymentTimestamp: LocalDateTime?,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    colors: ListItemColors = ListItemDefaults.colors(),
    elevation: ListItemElevation = ListItemDefaults.elevation(),
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
) {
    val nextPaymentDateFormatted = remember(nextPaymentTimestamp) {
        nextPaymentTimestamp?.format(DateUtil.Formatters.MMM_ddth_spaceSep)
            ?.replace(String.WhiteSpace, String.NewLine)
    }
    val scheduleItemContentDescription = stringResource(
        R.string.cd_schedule_of_amount_for_date,
        amount,
        nextPaymentDateFormatted.orEmpty()
    )

    ListItem(
        onClick = onClick,
        modifier = modifier
            .mergedContentDescription(scheduleItemContentDescription),
        leadingContent = {
            ListItemLeadingContentContainer {
                if (!nextPaymentDateFormatted.isNullOrEmpty()) {
                    BodyMediumText(
                        text = nextPaymentDateFormatted,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_wallet_done),
                        contentDescription = null
                    )
                }
            }
        },
        trailingContent = {
            AmountWithTypeIndicator(
                value = amount,
                type = type
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
                )
        )
    }
}

@Composable
fun ActiveScheduleItem(
    note: String?,
    amount: String,
    type: FundMovement,
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
                TypeIndicatorIcon(type)
            }

            Column(
                modifier = Modifier
                    .weight(Float.One)
            ) {
                BodyMediumText(
                    text = note.orEmpty()
                        .ifEmpty { stringResource(type.creditOrDebitLabel) },
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

            AmountWithTypeIndicator(
                value = amount,
                type = type,
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
            type = FundMovement.OUT,
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
            type = FundMovement.OUT,
            paymentDay = "10th Wed",
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}