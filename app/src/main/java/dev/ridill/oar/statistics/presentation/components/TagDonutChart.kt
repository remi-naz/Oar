package dev.ridill.oar.statistics.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.ui.components.BodySmallText
import dev.ridill.oar.core.ui.components.SpacerMedium
import dev.ridill.oar.core.ui.components.TitleMediumText
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.CornerRadiusSmall
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.SelectableColorsList
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.statistics.domain.model.TagSpendEntry
import java.util.Currency
import kotlin.math.atan2
import kotlin.math.sqrt

@Composable
fun TagDonutChart(
    entries: List<TagSpendEntry>,
    selectedEntry: TagSpendEntry?,
    onSelect: (TagSpendEntry) -> Unit,
    totalSpend: Double,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    val untaggedLabel = stringResource(R.string.statistics_untagged)
    EntryCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                TitleMediumText(text = stringResource(R.string.statistics_by_tag))
                BodySmallText(
                    text = stringResource(R.string.statistics_tag_count, entries.size),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                )
            }

            SpacerMedium()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                Box(
                    modifier = Modifier.size(DonutSize),
                    contentAlignment = Alignment.Center
                ) {
                    val outlineColor = MaterialTheme.colorScheme.outline
                    Canvas(
                        modifier = Modifier
                            .size(DonutSize)
                            .pointerInput(entries) {
                                detectTapGestures { offset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = offset.x - center.x
                                    val dy = offset.y - center.y
                                    val distance = sqrt(dx * dx + dy * dy)
                                    val radius = size.width / 2f
                                    if (distance < radius * MinTapRadiusFraction ||
                                        distance > radius * MaxTapRadiusFraction
                                    ) return@detectTapGestures

                                    val angleDeg = (Math.toDegrees(
                                        atan2(dy.toDouble(), dx.toDouble())
                                    ).toFloat() + 360f) % 360f
                                    val fromStart = (angleDeg + 90f + 360f) % 360f

                                    var accumulated = 0f
                                    entries.forEach { entry ->
                                        val sweep = if (totalSpend > 0) {
                                            (entry.amount / totalSpend).toFloat() * 360f
                                        } else 0f
                                        if (fromStart in accumulated..(accumulated + sweep)) {
                                            onSelect(entry)
                                            return@detectTapGestures
                                        }
                                        accumulated += sweep
                                    }
                                }
                            }
                    ) {
                        val strokeWidthSelected = 22.dp.toPx()
                        val strokeWidthNormal = 16.dp.toPx()
                        val gapDegrees = if (entries.size > 1) 3f else 0f
                        var startAngle = -90f

                        entries.forEach { entry ->
                            val fullSweep = if (totalSpend > 0) {
                                (entry.amount / totalSpend).toFloat() * 360f
                            } else 0f
                            val sweep = (fullSweep - gapDegrees).coerceAtLeast(0f)
                            val isSelected = entry.tagId == selectedEntry?.tagId
                            val color = entry.colorCode?.let { Color(it) } ?: outlineColor
                            val alpha = if (selectedEntry == null || isSelected) 1f
                            else ContentAlpha.PERCENT_32
                            val strokeWidth =
                                if (isSelected) strokeWidthSelected else strokeWidthNormal

                            drawArc(
                                color = color.copy(alpha = alpha),
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            )
                            startAngle += fullSweep
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large)
                    ) {
                        BodySmallText(
                            text = selectedEntry?.name ?: selectedEntry?.let { untaggedLabel }
                            ?: stringResource(R.string.statistics_total_spent),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                        )
                        TitleMediumText(
                            text = TextFormat.currency(
                                selectedEntry?.amount ?: totalSpend,
                                currency
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        BodySmallText(
                            text = if (selectedEntry != null && totalSpend > 0) {
                                stringResource(
                                    R.string.statistics_percent_of_spend,
                                    ((selectedEntry.amount / totalSpend) * 100).toInt()
                                )
                            } else {
                                stringResource(R.string.statistics_tap_a_slice)
                            },
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                    modifier = Modifier.weight(1f)
                ) {
                    entries.forEach { entry ->
                        TagLegendRow(
                            entry = entry,
                            currency = currency,
                            dimmed = selectedEntry != null && selectedEntry.tagId != entry.tagId,
                            onClick = { onSelect(entry) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagLegendRow(
    entry: TagSpendEntry,
    currency: Currency,
    dimmed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerRadiusSmall))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(entry.colorCode?.let { Color(it) } ?: MaterialTheme.colorScheme.outline)
        )
        BodySmallText(
            text = entry.name ?: stringResource(R.string.statistics_untagged),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (dimmed) LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
            else LocalContentColor.current,
            modifier = Modifier.weight(1f)
        )
        BodySmallText(
            text = TextFormat.currency(entry.amount, currency),
            color = if (dimmed) LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
            else LocalContentColor.current
        )
    }
}

private val DonutSize = 148.dp
private const val MinTapRadiusFraction = 0.55f
private const val MaxTapRadiusFraction = 1f

private fun previewTagEntries(): List<TagSpendEntry> {
    val entries = listOf(
        Triple("Rent & Bills", 11_000.0, 24),
        Triple("Food & Drink", 9_860.0, 18),
        Triple("Transport", 6_420.0, 12),
        Triple("Shopping", 5_980.0, 7),
        Triple("Health", 3_540.0, 4)
    )
    val total = entries.sumOf { it.second }
    return entries.mapIndexed { index, (name, amount, count) ->
        TagSpendEntry(
            tagId = index.toLong(),
            name = name,
            colorCode = SelectableColorsList[index].toArgb(),
            amount = amount,
            transactionCount = count,
            fraction = (amount / total).toFloat()
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewTagDonutChart() {
    val entries = remember { previewTagEntries() }
    OarTheme {
        Surface {
            TagDonutChart(
                entries = entries,
                selectedEntry = null,
                totalSpend = 1000.0,
                currency = LocaleUtil.currencyForCode("INR"),
                onSelect = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewTagDonutChartWithSelection() {
    val entries = remember { previewTagEntries() }

    OarTheme {
        Surface {
            TagDonutChart(
                entries = entries,
                currency = LocaleUtil.currencyForCode("INR"),
                selectedEntry = null,
                totalSpend = 1000.0,
                onSelect = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
        }
    }
}
