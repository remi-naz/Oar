package dev.ridill.oar.moneyPiles.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.WhiteSpace
import dev.ridill.oar.core.ui.components.AmountWithMovementIndicator
import dev.ridill.oar.core.ui.components.ExcludedIconSmall
import dev.ridill.oar.core.ui.components.ListItemLeadingTwoLineTextWithColorIndicator
import dev.ridill.oar.core.ui.theme.IconSizeMedium
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.exclusionGraphicsLayer
import dev.ridill.oar.core.ui.util.mergedContentDescription
import java.time.LocalDateTime

@Composable
private fun Content(
    pileName: String,
    excluded: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = modifier,
    ) {
        if (excluded) {
            ExcludedIconSmall()
        }
        Text(
            text = pileName,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        )
    }
}

@Composable
private fun SupportingContent(
    color: Color,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(
        LocalContentColor provides color,
        LocalTextStyle provides MaterialTheme.typography.bodySmall
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_money_pile),
                contentDescription = null,
                modifier = Modifier
                    .size(IconSizeMedium)
            )
            Text(
                text = stringResource(R.string.pile_contribution_transaction_supporting_text),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun buildContributionContentDesc(
    pileName: String,
    amount: String,
    timestamp: LocalDateTime,
    excluded: Boolean = false,
): String = buildString {
    append(
        stringResource(
            R.string.cd_pile_contribution_of_amount_towards_pile,
            amount,
            pileName
        )
    )
    append(String.WhiteSpace)
    append(
        stringResource(
            R.string.cd_content_desc_on_date_append,
            timestamp.format(DateUtil.Formatters.localizedDateLong)
        )
    )

    if (excluded) {
        append(",")
        append(String.WhiteSpace)
        append(stringResource(R.string.cd_excluded_append))
    }
}

@Composable
internal fun ContributionTransactionItem(
    pileName: String,
    pileColor: Color,
    amount: String,
    timestamp: LocalDateTime,
    movement: FundMovement,
    modifier: Modifier = Modifier,
    excluded: Boolean = false,
    enabled: Boolean = true,
    leadingContentLine1: String = timestamp.format(DateUtil.Formatters.EEE),
    leadingContentLine2: String = timestamp.format(DateUtil.Formatters.dayOfMonthOrdinal),
    colors: ListItemColors = ListItemDefaults.colors(),
    elevation: ListItemElevation = ListItemDefaults.elevation(),
) {
    val contributionContentDescription = buildContributionContentDesc(
        pileName = pileName,
        amount = amount,
        timestamp = timestamp,
        excluded = excluded,
    )
    ListItem(
        modifier = Modifier
            .mergedContentDescription(contributionContentDescription)
            .then(modifier),
        leadingContent = {
            ListItemLeadingTwoLineTextWithColorIndicator(
                line1 = leadingContentLine1,
                line2 = leadingContentLine2,
                color = pileColor,
            )
        },
        trailingContent = {
            AmountWithMovementIndicator(
                value = amount,
                movement = movement
            )
        },
        enabled = enabled,
        supportingContent = { SupportingContent(pileColor) },
        colors = colors,
        elevation = elevation,
    ) {
        Content(
            pileName = pileName,
            excluded = excluded
        )
    }
}

@Composable
internal fun ContributionTransactionItem(
    onClick: () -> Unit,
    pileName: String,
    pileColor: Color,
    amount: String,
    timestamp: LocalDateTime,
    movement: FundMovement,
    modifier: Modifier = Modifier,
    excluded: Boolean = false,
    enabled: Boolean = true,
    leadingContentLine1: String = timestamp.format(DateUtil.Formatters.EEE),
    leadingContentLine2: String = timestamp.format(DateUtil.Formatters.dayOfMonthOrdinal),
    colors: ListItemColors = ListItemDefaults.colors(),
    elevation: ListItemElevation = ListItemDefaults.elevation(),
) {
    val contributionContentDescription = buildContributionContentDesc(
        pileName = pileName,
        amount = amount,
        timestamp = timestamp,
        excluded = excluded,
    )
    ListItem(
        onClick = onClick,
        modifier = Modifier
            .mergedContentDescription(contributionContentDescription)
            .then(modifier)
            .exclusionGraphicsLayer(excluded),
        leadingContent = {
            ListItemLeadingTwoLineTextWithColorIndicator(
                line1 = leadingContentLine1,
                line2 = leadingContentLine2,
                color = pileColor,
            )
        },
        trailingContent = {
            AmountWithMovementIndicator(
                value = amount,
                movement = movement
            )
        },
        enabled = enabled,
        supportingContent = { SupportingContent(pileColor) },
        colors = colors,
        elevation = elevation,
    ) {
        Content(pileName = pileName, excluded = excluded)
    }
}

@PreviewLightDark
@Composable
private fun PreviewContributionTransactionItem() {
    OarTheme {
        ContributionTransactionItem(
            pileName = "Emergency Fund 2026",
            pileColor = Color(0xFF457BE6),
            amount = "$200",
            timestamp = LocalDateTime.now(),
            movement = FundMovement.OUT,
            modifier = Modifier
        )
    }
}
