package dev.ridill.oar.statistics.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.ui.components.BodySmallText
import dev.ridill.oar.core.ui.components.SpacerMedium
import dev.ridill.oar.core.ui.components.SpacerSmall
import dev.ridill.oar.core.ui.components.TitleMediumText
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.NegativeRed
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PositiveGreen
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.statistics.domain.model.CycleBarEntry
import dev.ridill.oar.statistics.domain.model.StatisticsChartMode

@Composable
internal fun CycleBarChart(
    chartMode: StatisticsChartMode,
    bars: List<CycleBarEntry>,
    selectedBar: CycleBarEntry?,
    cycleBarChartSummaryText: UiText,
    onModeChange: (StatisticsChartMode) -> Unit,
    onBarSelect: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    EntryCard(
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
                    text = bar.description,
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                )
                Crossfade(
                    targetState = cycleBarChartSummaryText.asString()
                ) { text ->
                    BodySmallText(text)
                }
            }

            SpacerMedium()

            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    space = MaterialTheme.spacing.small,
                    alignment = Alignment.End
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BarChartHeight)
                    .horizontalScroll(rememberScrollState())
            ) {
                bars.forEach { bar ->
                    CycleBarColumn(
                        selected = bar.cycleId == selectedBar?.cycleId,
                        onClick = { onBarSelect(bar.cycleId) },
                        incoming = bar.received,
                        outgoing = bar.spent,
                        label = bar.label,
                        maxValue = bar.budget.toDouble()
                    )
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
    label: String,
    incoming: Double,
    outgoing: Double,
    maxValue: Double,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedOutgoingFraction = animateFloatAsState(
        targetValue = (outgoing / maxValue).toFloat()
    )
    val containerColorAlpha = animateFloatAsState(
        if (selected) 1f else ContentAlpha.PERCENT_50
    )
    val barShape = MaterialTheme.shapes.extraSmall.copy(
        bottomEnd = CornerSize(Dp.Zero),
        bottomStart = CornerSize(Dp.Zero),
    )
    Column(
        modifier = modifier
            .width(BarWidth)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(barShape)
                .clickable(onClick = onClick)
                .drawWithCache {
                    val outgoingHeightPx = size.height * animatedOutgoingFraction.value
                    val incomingHeightPx = size.height - outgoingHeightPx
                    onDrawBehind {
                        translate(
                            top = size.height - outgoingHeightPx
                        ) {
                            drawOutline(
                                outline = barShape.createOutline(
                                    size = size.copy(height = outgoingHeightPx),
                                    layoutDirection = layoutDirection,
                                    density = this
                                ),
                                color = NegativeRed,
                                alpha = containerColorAlpha.value,
                            )
                        }

                        drawOutline(
                            outline = barShape.createOutline(
                                size = size.copy(height = incomingHeightPx),
                                layoutDirection = layoutDirection,
                                density = this
                            ),
                            color = PositiveGreen,
                            alpha = containerColorAlpha.value,
                        )
                    }
                }
        )

        BodySmallText(
            text = label,
            overflow = TextOverflow.MiddleEllipsis,
            maxLines = 1,
            color = if (selected) LocalContentColor.current
            else LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
        )
    }
}

private val BarWidth = 48.dp
private val BarChartHeight = 158.dp

@Composable
private fun ChartModeSegmentedControl(
    chartMode: StatisticsChartMode,
    onModeChange: (StatisticsChartMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            ButtonGroupDefaults.ConnectedSpaceBetween,
            Alignment.CenterHorizontally
        )
    ) {
        StatisticsChartMode.entries.forEachIndexed { index, mode ->
            val selected = mode == chartMode
            OutlinedToggleButton(
                checked = selected,
                onCheckedChange = { onModeChange(mode) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    else -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                },
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(stringResource(mode.labelRes))
            }
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
            budget = 60_000L,
            currency = LocaleUtil.currencyForCode("INR"),
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
                selectedBar = bars.find { it.cycleId == selectedCycleId },
                onModeChange = { chartMode = it },
                onBarSelect = { selectedCycleId = it },
                cycleBarChartSummaryText = UiText.DynamicString(LoremIpsum(3).values.joinToString()),
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
                cycleBarChartSummaryText = UiText.DynamicString(LoremIpsum(3).values.joinToString()),
                onModeChange = {},
                onBarSelect = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
        }
    }
}
