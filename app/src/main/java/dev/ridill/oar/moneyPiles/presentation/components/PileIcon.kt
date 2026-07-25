package dev.ridill.oar.moneyPiles.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import dev.ridill.oar.core.ui.theme.BorderWidthStandard
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.adjustedContentColor
import dev.ridill.oar.moneyPiles.domain.model.PileIcon

@Composable
internal fun PileIconIndicator(
    icon: PileIcon,
    color: () -> Color,
    modifier: Modifier = Modifier,
    containerColor: () -> Color = { color().adjustedContentColor() },
    borderStroke: BorderStroke? = null,
    shape: CornerBasedShape = IndicatorContainerShape,
) {
    val container = @Composable {
        Box(
            modifier = Modifier
                .defaultMinSize(
                    minWidth = SmallIndicatorMinSize,
                    minHeight = SmallIndicatorMinSize,
                )
                .clip(shape)
                .drawWithCache {
                    val outline = shape.createOutline(size, layoutDirection, this)
                    onDrawBehind {
                        drawOutline(
                            outline = outline,
                            color = containerColor(),
                            style = Fill
                        )

                        if (borderStroke != null) {
                            drawOutline(
                                outline = outline,
                                brush = borderStroke.brush,
                                style = Stroke(borderStroke.width.toPx())
                            )
                        }
                    }
                }
                .then(modifier)
                .layoutId(ContainerId),
        )
    }

    val pileIcon = @Composable {
        Icon(
            imageVector = ImageVector.vectorResource(icon.iconRes),
            contentDescription = stringResource(icon.labelRes),
            tint = color(),
            modifier = Modifier
                .layoutId(IconId)
        )
    }

    Layout(
        content = {
            container()
            pileIcon()
        },
    ) { measureables, constraints ->
        val containerPleaceable = measureables
            .fastFirst { it.layoutId == ContainerId }
            .measure(constraints)

        val iconWidth = containerPleaceable.width / 2
        val iconHeight = containerPleaceable.height / 2
        val iconPlaceable = measureables
            .fastFirst { it.layoutId == IconId }
            .measure(
                constraints.copy(
                    maxWidth = iconWidth,
                    minWidth = iconWidth,
                    maxHeight = iconHeight,
                    minHeight = iconHeight,
                )
            )
        layout(width = containerPleaceable.width, height = containerPleaceable.height) {
            containerPleaceable.place(0, 0)
            iconPlaceable.place(
                containerPleaceable.width / 2 - iconPlaceable.width / 2,
                containerPleaceable.height / 2 - iconPlaceable.height / 2
            )
        }
    }
}

@Composable
internal fun PileIconIndicator(
    icon: PileIcon,
    color: Color,
    modifier: Modifier = Modifier,
    containerColor: Color = color.adjustedContentColor(),
    borderStroke: BorderStroke? = null,
    shape: CornerBasedShape = IndicatorContainerShape,
) = PileIconIndicator(
    icon = icon,
    color = { color },
    modifier = modifier,
    borderStroke = borderStroke,
    containerColor = { containerColor },
    shape = shape,
)

private const val ContainerId = "Container"
private const val IconId = "Icon"
private val SmallIndicatorMinSize = 40.dp
private val IndicatorContainerShape: CornerBasedShape
    @Composable get() = MaterialTheme.shapes.medium

object PileIconDefaults {
    fun accentedBorder(color: Color): BorderStroke = BorderStroke(
        width = BorderWidthStandard,
        color = color
    )
}

@PreviewLightDark
@Composable
private fun PreviewPileIconIndicator() {
    OarTheme {
        PileIconIndicator(
            icon = PileIcon.General,
            color = Color.Green,
            borderStroke = PileIconDefaults.accentedBorder(Color.Green),
            modifier = Modifier
        )
    }
}