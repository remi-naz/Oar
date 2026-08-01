package dev.ridill.oar.statistics.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.ui.components.BodySmallText
import dev.ridill.oar.core.ui.components.DisplaySmallText
import dev.ridill.oar.core.ui.components.SpacerMedium
import dev.ridill.oar.core.ui.components.SpacerSmall
import dev.ridill.oar.core.ui.components.TitleMediumText
import dev.ridill.oar.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.NegativeRed
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PositiveGreen
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.folders.domain.model.AggregateType
import dev.ridill.oar.statistics.domain.model.StatisticsCycleSummary
import kotlin.math.absoluteValue

@Composable
internal fun CycleSummaryCard(
    summary: StatisticsCycleSummary,
    modifier: Modifier = Modifier
) {
    val netColor = AggregateType.fromAmount(summary.net).color
    EntryCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    BodySmallText(
                        text = stringResource(R.string.statistics_net_this_cycle),
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                    )
                    VerticalNumberSpinnerContent(summary.net.absoluteValue) {
                        DisplaySmallText(text = summary.netFormatted, color = netColor)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    BodySmallText(
                        text = stringResource(
                            R.string.statistics_of_budget,
                            summary.budgetFormatted
                        ),
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                    )
                    TitleMediumText(
                        text = stringResource(
                            if (summary.isOnPace) R.string.statistics_on_track
                            else R.string.statistics_over_pace
                        ),
                        color = if (summary.isOnPace) PositiveGreen else NegativeRed
                    )
                }
            }

            SpacerMedium()

            UsageProgress(
                usageFraction = summary.usageFraction,
                isOnPace = summary.isOnPace
            )

            SpacerSmall()

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                BodySmallText(
                    text = stringResource(
                        R.string.statistics_budget_used,
                        "${summary.usagePercent}%"
                    ),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                )
                BodySmallText(
                    text = stringResource(
                        R.string.statistics_day_x_of_y,
                        summary.daysElapsed,
                        summary.daysTotal
                    ),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                )
            }

            SpacerMedium()

            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                SummaryTile(
                    label = stringResource(R.string.statistics_spent),
                    value = summary.spentFormatted,
                    color = NegativeRed,
                    modifier = Modifier.weight(1f)
                )
                SummaryTile(
                    label = stringResource(R.string.statistics_received),
                    value = summary.receivedFormatted,
                    color = PositiveGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

internal val CardShape: CornerBasedShape
    @Composable get() = MaterialTheme.shapes.large

@Composable
private fun UsageProgress(
    usageFraction: Float,
    isOnPace: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedFraction = animateFloatAsState(
        usageFraction.coerceIn(0f, 1f),
        label = "UsageFraction"
    )
    LinearWavyProgressIndicator(
        progress = { animatedFraction.value },
        color = if (isOnPace) MaterialTheme.colorScheme.primary else NegativeRed,
        trackColor = MaterialTheme.colorScheme.surfaceContainerLow,
        stopSize = Dp.Zero,
        modifier = modifier
            .fillMaxWidth(),
    )
}

@Composable
private fun SummaryTile(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    EntryCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.small
                )
        ) {
            BodySmallText(
                text = label,
                color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
            )
            TitleMediumText(text = value, color = color)
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewCycleSummaryCard() {
    OarTheme {
        Surface {
            CycleSummaryCard(
                summary = StatisticsCycleSummary(
                    cycleId = 1L,
                    startDate = DateUtil.dateNow().withDayOfMonth(1),
                    endDate = DateUtil.dateNow().withDayOfMonth(1).plusMonths(1).minusDays(1),
                    spent = 41_280.0,
                    received = 8_400.0,
                    budget = 60_000L,
                    currency = LocaleUtil.currencyForCode("INR"),
                    transactionCount = 63,
                    daysElapsed = 22,
                    daysTotal = 31
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewCycleSummaryCardOverBudget() {
    OarTheme {
        Surface {
            CycleSummaryCard(
                summary = StatisticsCycleSummary(
                    cycleId = 1L,
                    startDate = DateUtil.dateNow().withDayOfMonth(1),
                    endDate = DateUtil.dateNow().withDayOfMonth(1).plusMonths(1).minusDays(1),
                    spent = 61_900.0,
                    received = 4_800.0,
                    budget = 60_000L,
                    currency = LocaleUtil.currencyForCode("INR"),
                    transactionCount = 48,
                    daysElapsed = 12,
                    daysTotal = 31
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
        }
    }
}
