package dev.ridill.oar.statistics.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.ui.components.BodySmallText
import dev.ridill.oar.core.ui.components.SpacerMedium
import dev.ridill.oar.core.ui.components.SpacerSmall
import dev.ridill.oar.core.ui.components.TitleMediumText
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.CornerRadiusLarge
import dev.ridill.oar.core.ui.theme.CornerRadiusMedium
import dev.ridill.oar.core.ui.theme.CornerRadiusSmall
import dev.ridill.oar.core.ui.theme.NegativeRed
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PositiveGreen
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.statistics.domain.model.CycleBarEntry
import dev.ridill.oar.statistics.domain.model.StatisticsChartMode
import java.util.Currency
import kotlin.math.max

@Composable
internal fun CycleBarChart(
    bars: List<CycleBarEntry>,
    selectedBar: CycleBarEntry?,
    cycleNet: Double?,
    inVsOutDiff: Double?,
    chartMode: StatisticsChartMode,
    onModeChange: (StatisticsChartMode) -> Unit,
    currency: Currency,
    onBarSelect: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val referenceBudget = bars.lastOrNull()?.budget ?: 0L
    val maxValue = (bars.maxOfOrNull { max(it.spent, it.received) } ?: 0.0)
        .let { if (it <= 0.0) 1.0 else it * 1.1 }

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TitleMediumText(text = stringResource(R.string.statistics_across_cycles))
                ChartModeSegmentedControl(
                    chartMode = chartMode,
                    onModeChange = onModeChange
                )
            }

            SpacerSmall()

            selectedBar?.let { bar ->
                BodySmallText(
                    text = "${bar.description} · " +
                            stringResource(
                                R.string.statistics_x_spent,
                                TextFormat.currency(bar.spent, currency)
                            ),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                )
                Crossfade(
                    targetState = chartMode
                ) { mode ->
                    when (mode) {
                        StatisticsChartMode.SPEND -> {
                            BodySmallText(
                                text = if (inVsOutDiff.orZero() >= 0) {
                                    stringResource(
                                        R.string.statistics_under_budget_of,
                                        TextFormat.currency(inVsOutDiff.orZero(), currency),
                                        TextFormat.currency(bar.budget)
                                    )
                                } else {
                                    stringResource(
                                        R.string.statistics_over_budget_of,
                                        TextFormat.currency(-inVsOutDiff.orZero(), currency),
                                        TextFormat.currency(bar.budget)
                                    )
                                }
                            )
                        }

                        StatisticsChartMode.IN_VS_OUT -> {
                            BodySmallText(
                                text = stringResource(
                                    R.string.statistics_received_and_net,
                                    TextFormat.currency(bar.received, currency),
                                    TextFormat.currency(cycleNet.orZero(), currency)
                                )
                            )
                        }
                    }
                }
            }

            SpacerMedium()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ChartHeight)
            ) {
                if (chartMode == StatisticsChartMode.SPEND && referenceBudget > 0) {
                    val dashColor = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val fraction = (referenceBudget / maxValue).toFloat().coerceIn(0f, 1f)
                        val y = size.height * (1f - fraction)
                        drawLine(
                            color = dashColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    modifier = Modifier.fillMaxSize()
                ) {
                    bars.forEach { bar ->
                        CycleBarColumn(
                            bar = bar,
                            maxValue = maxValue,
                            chartMode = chartMode,
                            selected = bar.cycleId == selectedBar?.cycleId,
                            onClick = { onBarSelect(bar.cycleId) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }

            SpacerSmall()

            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                if (chartMode == StatisticsChartMode.IN_VS_OUT) {
                    LegendItem(
                        color = NegativeRed,
                        label = stringResource(R.string.statistics_spent)
                    )
                    LegendItem(
                        color = PositiveGreen,
                        label = stringResource(R.string.statistics_received)
                    )
                } else {
                    LegendItem(
                        color = NegativeRed,
                        label = stringResource(R.string.statistics_spent)
                    )
                    LegendItem(
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT),
                        label = stringResource(R.string.statistics_budget_line)
                    )
                }
            }
        }
    }
}

@Composable
private fun CycleBarColumn(
    bar: CycleBarEntry,
    maxValue: Double,
    chartMode: StatisticsChartMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha = if (selected) 1f else ContentAlpha.PERCENT_50
    val spentFraction = (bar.spent / maxValue).toFloat().coerceIn(0f, 1f)
    val receivedFraction = (bar.received / maxValue).toFloat().coerceIn(0f, 1f)
    val barShape = RoundedCornerShape(
        topStart = CornerRadiusSmall,
        topEnd = CornerRadiusSmall
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(spentFraction.coerceAtLeast(MinBarFraction))
                    .clip(barShape)
                    .background(NegativeRed.copy(alpha = alpha))
            )
            if (chartMode == StatisticsChartMode.IN_VS_OUT) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(receivedFraction.coerceAtLeast(MinBarFraction))
                        .clip(barShape)
                        .background(PositiveGreen.copy(alpha = alpha))
                )
            }
        }

        SpacerSmall()

        BodySmallText(
            text = bar.label,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = if (selected) LocalContentColor.current
            else LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
        )
    }
}

@Composable
private fun ChartModeSegmentedControl(
    chartMode: StatisticsChartMode,
    onModeChange: (StatisticsChartMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .clip(RoundedCornerShape(CornerRadiusLarge))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(2.dp)
    ) {
        StatisticsChartMode.entries.forEach { mode ->
            val selected = mode == chartMode
            BodySmallText(
                text = stringResource(mode.labelRes),
                color = if (selected) LocalContentColor.current
                else LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT),
                modifier = Modifier
                    .clip(RoundedCornerShape(CornerRadiusMedium))
                    .background(
                        if (selected) MaterialTheme.colorScheme.surfaceContainerHighest
                        else Color.Transparent
                    )
                    .clickable { onModeChange(mode) }
                    .padding(horizontal = MaterialTheme.spacing.small, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(9.dp)
                .height(9.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        BodySmallText(
            text = label,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
        )
    }
}

private val ChartHeight = 158.dp
private const val MinBarFraction = 0.02f

private fun previewBars(): List<CycleBarEntry> {
    val amounts = listOf(
        52_100.0 to 5_200.0,
        47_300.0 to 12_000.0,
        61_900.0 to 4_800.0,
        44_800.0 to 9_600.0,
        55_200.0 to 3_400.0,
        41_280.0 to 8_400.0
    )
    return amounts.mapIndexed { index, (spent, received) ->
        val start =
            DateUtil.dateNow().withDayOfMonth(1).minusMonths((amounts.size - 1 - index).toLong())
        CycleBarEntry(
            cycleId = index.toLong(),
            startDate = start,
            endDate = start.plusMonths(1).minusDays(1),
            spent = spent,
            received = received,
            budget = 60_000L
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewCycleBarChart() {
    val bars = remember { previewBars() }
    var chartMode by remember { mutableStateOf(StatisticsChartMode.SPEND) }
    var selectedCycleId by remember { mutableStateOf(bars.last().cycleId) }

    OarTheme {
        Surface {
            CycleBarChart(
                bars = bars,
                chartMode = chartMode,
                currency = LocaleUtil.currencyForCode("INR"),
                selectedBar = bars.find { it.cycleId == selectedCycleId },
                cycleNet = null,
                inVsOutDiff = null,
                onModeChange = { chartMode = it },
                onBarSelect = { selectedCycleId = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewCycleBarChartInVsOut() {
    val bars = remember { previewBars() }
    OarTheme {
        Surface {
            CycleBarChart(
                bars = bars,
                chartMode = StatisticsChartMode.IN_VS_OUT,
                selectedBar = bars.last(),
                currency = LocaleUtil.currencyForCode("INR"),
                cycleNet = null,
                inVsOutDiff = null,
                onModeChange = {},
                onBarSelect = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
        }
    }
}
