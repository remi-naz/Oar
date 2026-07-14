package dev.ridill.oar.moneyPiles.presentation.allPiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat

@Composable
internal fun PileGridItem(
    icon: String,
    name: String,
    accent: Color,
    accumulatedAmountLabel: String,
    targetLabel: String,
    progressFraction: Float?,
    locked: Boolean,
    complete: Boolean,
    onClick: () -> Unit,
    onQuickAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.large
            )
            .clickable(onClick = onClick)
            .height(PileCardHeight)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .fillMaxHeight(progressFraction?.coerceIn(0f, 1f) ?: MinFillFraction)
                .background(accent.copy(alpha = if (complete) 0.30f else 0.20f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, style = MaterialTheme.typography.titleMedium)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)) {
                    if (locked) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = stringResource(R.string.pile_locked),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (complete) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = stringResource(R.string.pile_goal_reached),
                            tint = accent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = MaterialTheme.spacing.small)
            )

            Box(modifier = Modifier.weight(1f))

            Text(
                text = accumulatedAmountLabel,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = targetLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(MaterialTheme.spacing.small)
                .size(32.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.22f))
                .border(width = 1.dp, color = accent.copy(alpha = 0.5f), shape = CircleShape)
                .clickable(onClick = onQuickAddClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleMedium,
                color = accent
            )
        }
    }
}

private val PileCardHeight = 158.dp
private const val MinFillFraction = 0.12f

@PreviewLightDark
@Composable
private fun PreviewPileGridItem() {
    OarTheme {
        PileGridItem(
            icon = "🌸",
            name = "Japan trip",
            accent = Color(0xFFFF4CA6),
            accumulatedAmountLabel = TextFormat.currency(3200),
            targetLabel = "of " + TextFormat.currency(5000),
            progressFraction = 0.64f,
            locked = true,
            complete = false,
            onClick = {},
            onQuickAddClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
