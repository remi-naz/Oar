package dev.ridill.oar.statistics.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.model.ChartSlice
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.ui.components.BodySmallText
import dev.ridill.oar.core.ui.components.DonutChart
import dev.ridill.oar.core.ui.components.TitleMediumText
import dev.ridill.oar.core.ui.components.color
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.SelectableColorsList
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat
import java.util.Currency

@Composable
internal fun TagDonutChart(
    entries: List<ChartSlice>,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    EntryCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                val untaggedColor = UntaggedColor
                DonutChart(
                    slices = entries,
                    fallbackColor = untaggedColor,
                    // Card uses surfaceContainer, so go a step higher to stand out against it
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                    modifier = Modifier.weight(1f)
                ) {
                    entries.forEach { entry ->
                        TagLegendRow(
                            entry = entry,
                            currency = currency,
                            fallbackColor = untaggedColor,
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT),
                    modifier = Modifier.size(16.dp)
                )
                BodySmallText(
                    text = stringResource(R.string.statistics_tag_percentage_note),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                )
            }
        }
    }
}

@Composable
private fun TagLegendRow(
    entry: ChartSlice,
    currency: Currency,
    fallbackColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(entry.color(fallbackColor))
        )
        BodySmallText(
            text = entry.label ?: stringResource(R.string.statistics_untagged),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        BodySmallText(
            text = TextFormat.currency(entry.amount, currency),
        )
    }
}

private val UntaggedColor: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant

private fun previewTagEntries(): List<ChartSlice> {
    val entries = listOf(
        "Rent & Bills" to 11_000.0,
        "Food & Drink" to 9_860.0,
        "Transport" to 6_420.0,
        "Shopping" to 5_980.0,
        "Health" to 3_540.0
    )
    val total = entries.sumOf { it.second }
    return entries.mapIndexed { index, (name, amount) ->
        ChartSlice(
            id = index.toLong(),
            label = name,
            amount = amount,
            fraction = (amount / total).toFloat(),
            colorCode = SelectableColorsList[index].toArgb()
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
                currency = LocaleUtil.currencyForCode("INR"),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
        }
    }
}
