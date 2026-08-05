package dev.ridill.oar.transactions.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.model.creditOrDebitLabel
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.WhiteSpace
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.ui.components.AmountWithMovementIndicator
import dev.ridill.oar.core.ui.components.ExcludedIconSmall
import dev.ridill.oar.core.ui.components.ListItemLeadingTwoLineTextWithColorIndicator
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.exclusionGraphicsLayer
import dev.ridill.oar.transactions.domain.model.FolderIndicator
import dev.ridill.oar.transactions.domain.model.TagIndicator
import java.time.LocalDateTime

@Composable
private fun LeadingContent(
    leadingContentLine1: String,
    leadingContentLine2: String,
    color: Color?,
    modifier: Modifier = Modifier
) {
    ListItemLeadingTwoLineTextWithColorIndicator(
        line1 = leadingContentLine1,
        line2 = leadingContentLine2,
        color = color,
        modifier = modifier
    )
}

@Composable
private fun Content(
    note: String,
    tag: TagIndicator?,
    folder: FolderIndicator?,
    excluded: Boolean,
    movement: FundMovement,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = modifier,
    ) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
            if (excluded) {
                ExcludedIconSmall()
            }
            when {
                note.isNotEmpty() -> {
                    Text(
                        text = note,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = LocalContentColor.current,
                        style = LocalTextStyle.current.copy(fontWeight = FontWeight.Medium)
                    )
                }

                folder != null -> {
                    FolderLabel(folder.name)
                }

                tag != null -> {
                    TagLabel(tag.name)
                }


                else -> {
                    Text(
                        text = stringResource(movement.creditOrDebitLabel),
                        overflow = TextOverflow.Ellipsis,
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT),
                        style = LocalTextStyle.current.copy(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SupportingContent(
    tag: TagIndicator?,
    folder: FolderIndicator?,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = modifier,
        ) {
            tag?.let { TagLabel(it.name) }
            folder?.let { FolderLabel(it.name) }
        }
    }
}

@Composable
private fun buildContentDesc(
    note: String,
    amount: String,
    timestamp: LocalDateTime,
    movement: FundMovement,
    tag: TagIndicator? = null,
    folder: FolderIndicator? = null,
    excluded: Boolean = false,
): String = buildString {
    val timestampFormatted = timestamp.format(DateUtil.Formatters.localizedDateLong)
    if (note.isNotEmpty()) {
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
    append(String.WhiteSpace)
    append(stringResource(R.string.cd_content_desc_on_date_append, timestampFormatted))

    if (excluded) {
        append(",")
        append(String.WhiteSpace)
        append(stringResource(R.string.cd_excluded_append))
    }

    tag?.let {
        append(",")
        append(String.WhiteSpace)
        append(stringResource(R.string.cd_transaction_list_item_tag_append, it.name))
    }

    folder?.let {
        append(",")
        append(String.WhiteSpace)
        append(stringResource(R.string.cd_transaction_list_item_folder_append, it.name))
    }
}

@Composable
internal fun TransactionListItem(
    note: String,
    amount: String,
    timestamp: LocalDateTime,
    movement: FundMovement,
    modifier: Modifier = Modifier,
    leadingContentLine1: String = timestamp.format(DateUtil.Formatters.EEE),
    leadingContentLine2: String = timestamp.format(DateUtil.Formatters.dayOfMonthOrdinal),
    tag: TagIndicator? = null,
    folder: FolderIndicator? = null,
    excluded: Boolean = false,
    overlineContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    elevation: ListItemElevation = ListItemDefaults.elevation(),
) {
    val transactionListItemContentDescription = buildContentDesc(
        note = note,
        amount = amount,
        timestamp = timestamp,
        movement = movement,
        tag = tag,
        folder = folder,
        excluded = excluded,
    )
    ListItem(
        modifier = Modifier
            .semantics(mergeDescendants = true) {}
            .clearAndSetSemantics {
                contentDescription = transactionListItemContentDescription
            }
            .then(modifier)
            .exclusionGraphicsLayer(excluded),
        leadingContent = {
            LeadingContent(
                leadingContentLine1 = leadingContentLine1,
                leadingContentLine2 = leadingContentLine2,
                color = tag?.color,
            )
        },
        trailingContent = {
            AmountWithMovementIndicator(
                value = amount,
                movement = movement
            )
        },
        overlineContent = overlineContent,
        supportingContent = {
            if (note.isNotEmpty()) {
                SupportingContent(
                    tag = tag,
                    folder = folder,
                )
            }
        },
        colors = colors,
        elevation = elevation,
    ) {
        Content(
            note = note,
            tag = tag,
            folder = folder,
            excluded = excluded,
            movement = movement
        )
    }
}

@Composable
internal fun TransactionListItem(
    onClick: () -> Unit,
    note: String,
    amount: String,
    timestamp: LocalDateTime,
    movement: FundMovement,
    modifier: Modifier = Modifier,
    leadingContentLine1: String = timestamp.format(DateUtil.Formatters.EEE),
    leadingContentLine2: String = timestamp.format(DateUtil.Formatters.dayOfMonthOrdinal),
    selected: Boolean = false,
    tag: TagIndicator? = null,
    folder: FolderIndicator? = null,
    excluded: Boolean = false,
    overlineContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    elevation: ListItemElevation = ListItemDefaults.elevation(),
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
) {
    val transactionListItemContentDescription = buildContentDesc(
        note = note,
        amount = amount,
        timestamp = timestamp,
        movement = movement,
        tag = tag,
        folder = folder,
        excluded = excluded,
    )
    ListItem(
        onClick = onClick,
        modifier = Modifier
            .semantics(mergeDescendants = true) {}
            .clearAndSetSemantics {
                contentDescription = transactionListItemContentDescription
            }
            .then(modifier)
            .exclusionGraphicsLayer(excluded),
        leadingContent = {
            LeadingContent(
                leadingContentLine1 = leadingContentLine1,
                leadingContentLine2 = leadingContentLine2,
                color = tag?.color,
            )
        },
        trailingContent = {
            AmountWithMovementIndicator(
                value = amount,
                movement = movement
            )
        },
        overlineContent = overlineContent,
        supportingContent = {
            if (note.isNotEmpty()) {
                SupportingContent(
                    tag = tag,
                    folder = folder,
                )
            }
        },
        selected = selected,
        colors = colors,
        elevation = elevation,
        onLongClick = onLongClick,
        onLongClickLabel = onLongClickLabel,
    ) {
        Content(
            note = note,
            tag = tag,
            folder = folder,
            excluded = excluded,
            movement = movement
        )
    }
}

@Composable
private fun TagLabel(
    name: String,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(
        LocalContentColor provides LocalContentColor.current
            .copy(alpha = ContentAlpha.SUB_CONTENT)
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_tag),
                contentDescription = null,
                modifier = Modifier
                    .size(SmallIndicatorSize)
            )
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FolderLabel(
    name: String,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(
        LocalContentColor provides LocalContentColor.current
            .copy(alpha = ContentAlpha.SUB_CONTENT)
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_folder),
                contentDescription = null,
                modifier = Modifier
                    .size(SmallIndicatorSize)
            )
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val SmallIndicatorSize = 12.dp

@Composable
fun NewTransactionFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation()
) {
    FloatingActionButton(
        onClick = onClick,
        elevation = elevation,
        modifier = modifier
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_money_add),
            contentDescription = stringResource(R.string.cd_new_transaction_fab)
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewTransactionListItem() {
    OarTheme {
        TransactionListItem(
            note = "Note",
            amount = "Rs.1000",
            timestamp = LocalDateTime.now(),
            movement = FundMovement.IN,
            modifier = Modifier,
            tag = TagIndicator(id = Long.Zero, name = "Test", color = Color.Yellow),
            folder = FolderIndicator(id = Long.Zero, name = "Test"),
            excluded = false
        )
    }
}