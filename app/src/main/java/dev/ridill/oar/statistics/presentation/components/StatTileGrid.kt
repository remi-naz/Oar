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

data class StatTile(
    val label: String,
    val value: String,
    val sub: String
)

@Composable
fun StatTileGrid(
    tiles: List<StatTile>,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = modifier
    ) {
        tiles.chunked(2).forEach { rowTiles ->
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
            text = tile.label,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
        )
        TitleMediumText(text = tile.value, maxLines = 1, overflow = TextOverflow.Ellipsis)
        BodySmallText(
            text = tile.sub,
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
                tiles = listOf(
                    StatTile(
                        label = "Average per day",
                        value = "₹1,332",
                        sub = "over 22 days"
                    ),
                    StatTile(
                        label = "Transactions",
                        value = "63",
                        sub = "excluding excluded"
                    ),
                    StatTile(
                        label = "Largest spend",
                        value = "₹9,450",
                        sub = "Rent — August"
                    ),
                    StatTile(
                        label = "Busiest tag",
                        value = "Food & Drink",
                        sub = "24 transactions"
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
        }
    }
}
