package dev.ridill.oar.statistics.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.ridill.oar.core.ui.components.BodySmallText
import dev.ridill.oar.core.ui.components.TitleMediumText
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.CornerRadiusLarge
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.UiText

data class StatTile(
    val key: String,
    val label: UiText,
    val value: String,
    val sub: UiText
)

@Composable
internal fun StatTileGrid(
    tileRows: List<List<StatTile>>,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = modifier
    ) {
        tileRows.forEach { rowTiles ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowTiles.forEach { tile ->
                    StatTileCard(tile = tile, modifier = Modifier.weight(1f))
                }
                if (rowTiles.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatTileCard(
    tile: StatTile,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CornerRadiusLarge))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(MaterialTheme.spacing.medium)
    ) {
        BodySmallText(
            text = tile.label.asString(),
            color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
        )
        TitleMediumText(text = tile.value, maxLines = 1, overflow = TextOverflow.Ellipsis)
        BodySmallText(
            text = tile.sub.asString(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.PERCENT_50)
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewStatTileGrid() {
    OarTheme {
        Surface {
            StatTileGrid(
                tileRows = listOf(
                    listOf(
                        StatTile(
                            key = "tile1",
                            label = UiText.DynamicString("Average per day"),
                            value = "₹1,332",
                            sub = UiText.DynamicString("over 22 days")
                        ),
                        StatTile(
                            key = "tile2",
                            label = UiText.DynamicString("Transactions"),
                            value = "63",
                            sub = UiText.DynamicString("excluding excluded")
                        )
                    ),
                    listOf(
                        StatTile(
                            key = "tile3",
                            label = UiText.DynamicString("Largest spend"),
                            value = "₹9,450",
                            sub = UiText.DynamicString("Rent — August")
                        ),
                        StatTile(
                            key = "tile4",
                            label = UiText.DynamicString("Busiest tag"),
                            value = "Food & Drink",
                            sub = UiText.DynamicString("24 transactions")
                        )
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
        }
    }
}
