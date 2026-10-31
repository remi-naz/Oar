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
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.ui.components.BodySmallText
import dev.ridill.oar.core.ui.components.DisplaySmallText
import dev.ridill.oar.core.ui.components.SpacerExtraSmall
import dev.ridill.oar.core.ui.components.SpacerSmall
import dev.ridill.oar.core.ui.components.TitleMediumText
import dev.ridill.oar.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.NegativeRed
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PositiveGreen
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.folders.domain.model.AggregateType
import java.util.Currency

@Composable
internal fun CycleSummaryCard(
    net: Double,
    budget: Long,
    usageFraction: Float,
    elapsedDays: Int,
    totalDays: Int,
    spent: Double,
    received: Double,
    baseCurrency: Currency,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    val netColor = AggregateType.fromAmount(net).color
    EntryCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            Column {
                BodySmallText(
                    text = stringResource(R.string.statistics_net_this_cycle),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    VerticalNumberSpinnerContent(
                        number = net,
                        modifier = Modifier
                            .alignBy(LastBaseline)
                    ) {
                        DisplaySmallText(
                            text = TextFormat.currencyAmount(amount = it, currency = currency),
                            color = netColor,
                        )
                    }

                    SpacerExtraSmall()

                    VerticalNumberSpinnerContent(
                        number = budget,
                        modifier = Modifier
                            .alignBy(LastBaseline)
                    ) {
                        TitleMediumText(
                            text = stringResource(
                                R.string.of_value,
                                TextFormat.currencyAmount(amount = it, currency = baseCurrency),
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = LocalContentColor.current.copy(
                                alpha = ContentAlpha.SUB_CONTENT
                            ),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            Column {
                val animatedFraction = animateFloatAsState(
                    targetValue = usageFraction,
                    label = "UsageFraction"
                )
                LinearWavyProgressIndicator(
                    progress = { animatedFraction.value },
                    stopSize = Dp.Zero,
                    modifier = modifier
                        .fillMaxWidth(),
                )

                SpacerSmall()

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BodySmallText(
                        text = stringResource(
                            R.string.statistics_budget_used,
                            "${TextFormat.number(animatedFraction.value)}%"
                        ),
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                    )
                    BodySmallText(
                        text = stringResource(
                            R.string.statistics_day_x_of_y,
                            elapsedDays,
                            totalDays
                        ),
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                    )
                }
            }

            SpacerSmall()

            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                SummaryTile(
                    label = stringResource(R.string.statistics_spent),
                    value = TextFormat.currencyAmount(amount = spent, currency = currency),
                    color = NegativeRed,
                    modifier = Modifier.weight(1f)
                )
                SummaryTile(
                    label = stringResource(R.string.statistics_received),
                    value = TextFormat.currencyAmount(amount = received, currency = currency),
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
                net = 10_000.0,
                spent = 41_280.0,
                received = 8_400.0,
                budget = 60_000L,
                baseCurrency = LocaleUtil.currencyForCode("USD"),
                currency = LocaleUtil.currencyForCode("INR"),
                usageFraction = 0.50f,
                totalDays = 30,
                elapsedDays = 10,
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
                net = 10_000.0,
                spent = 41_280.0,
                received = 8_400.0,
                budget = 60_000L,
                baseCurrency = LocaleUtil.currencyForCode("USD"),
                currency = LocaleUtil.currencyForCode("INR"),
                usageFraction = 0.50f,
                totalDays = 30,
                elapsedDays = 10,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
        }
    }
}
