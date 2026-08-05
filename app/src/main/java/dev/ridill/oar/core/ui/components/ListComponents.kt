package dev.ridill.oar.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastFirstOrNull
import dev.ridill.oar.core.ui.theme.elevation
import dev.ridill.oar.core.ui.theme.spacing
import kotlin.math.roundToInt

@Composable
fun ListSeparator(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    shape: Shape = RectangleShape,
    tonalElevation: Dp = MaterialTheme.elevation.level1
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = shape,
        tonalElevation = tonalElevation
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    vertical = MaterialTheme.spacing.small,
                    horizontal = MaterialTheme.spacing.medium
                )
        ) {
            TitleMediumText(text = label)
        }
    }
}

@Composable
fun ListItemLeadingContentContainer(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = MaterialTheme.elevation.level1,
    contentPadding: PaddingValues = PaddingValues(MaterialTheme.spacing.small),
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation
    ) {
        Box(
            modifier = Modifier
                .requiredWidthIn(min = ContainerMinWidth)
                .wrapContentHeight()
                .padding(contentPadding)
                .then(modifier),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

private val ContainerMinWidth: Dp = 56.dp

@Composable
fun ListItemLeadingContentWithColorIndicator(
    color: Color?,
    modifier: Modifier = Modifier,
    dividerColor: Color = DividerDefaults.color,
    thickness: Dp = DividerDefaults.Thickness,
    content: @Composable BoxScope.() -> Unit,
) {
    val decoratedContent: @Composable () -> Unit = @Composable {
        Box(
            modifier = Modifier
                .defaultMinSize(
                    minWidth = ContainerMinWidth,
                    minHeight = ContainerMinWidth,
                )
                .layoutId(ContentId),
            contentAlignment = Alignment.Center,
            content = content
        )
    }

    val dividerContent: @Composable () -> Unit = @Composable {
        VerticalDivider(
            modifier = Modifier
                .layoutId(DividerId),
            color = dividerColor,
            thickness = thickness
        )
    }

    val colorIndicatorContent: @Composable (() -> Unit)? = color?.let {
        @Composable {
            Box(
                modifier = Modifier
                    .width(ColorIndicatorWidth)
                    .drawWithCache {
                        val startOffset = Offset(x = size.width / 2, y = 0f)
                        val endOffset = Offset(x = size.width / 2, y = size.height)
                        val strokeCap = StrokeCap.Round
                        val strokeWidth = size.width
                        onDrawBehind {
                            drawLine(
                                color = Color.Black,
                                start = startOffset,
                                end = endOffset,
                                strokeWidth = strokeWidth,
                                cap = strokeCap,
                                blendMode = BlendMode.Clear
                            )
                            val paddingPx = size.height * 0.16f
                            drawLine(
                                color = color,
                                start = startOffset.copy(y = paddingPx),
                                end = endOffset.minus(Offset.Zero.copy(y = paddingPx)),
                                strokeWidth = strokeWidth,
                                cap = strokeCap
                            )
                        }
                    }
                    .layoutId(ColorIndicatorId)
            )
        }
    }

    Layout(
        content = {
            decoratedContent()
            dividerContent()
            colorIndicatorContent?.invoke()
        },
        modifier = modifier
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    ) { measurables, constraints ->
        val contentPlaceable = measurables
            .fastFirst { it.layoutId == ContentId }
            .measure(constraints)

        val totalHeight = contentPlaceable.height
        val indicatorWidthPx = ColorIndicatorWidth.roundToPx()
        val widthRestrictedConstraints = constraints.copy(
            minHeight = 0,
            minWidth = 0,
            maxWidth = indicatorWidthPx,
        )
        val dividerHeightPx = (totalHeight * DIV_HEIGHT_FRACTION).roundToInt()
        val dividerPlaceable = measurables
            .fastFirst { it.layoutId == DividerId }
            .measure(
                widthRestrictedConstraints.copy(
                    maxHeight = dividerHeightPx,
                    minHeight = dividerHeightPx
                )
            )

        val colorIndicatorHeightPx =
            (dividerHeightPx * COLOR_INDICATOR_HEIGHT_FRACTION).roundToInt()
        val colorIndicatorPlaceable = measurables
            .fastFirstOrNull { it.layoutId == ColorIndicatorId }
            ?.measure(
                widthRestrictedConstraints.copy(
                    maxHeight = colorIndicatorHeightPx,
                    minHeight = colorIndicatorHeightPx
                )
            )

        val totalWidth = contentPlaceable.width + indicatorWidthPx
        layout(height = totalHeight, width = totalWidth) {
            contentPlaceable.place(0, 0)

            val centerY = totalHeight / 2
            val endSectionCenterX = contentPlaceable.width + (indicatorWidthPx / 2)
            dividerPlaceable.placeRelative(
                x = endSectionCenterX - (dividerPlaceable.width / 2),
                y = centerY - (dividerPlaceable.height / 2)
            )

            colorIndicatorPlaceable?.placeRelative(
                x = endSectionCenterX - (colorIndicatorPlaceable.width / 2),
                y = centerY - colorIndicatorPlaceable.height / 2
            )
        }
    }
}

private const val ContentId = "Content"
private const val DividerId = "Divider"
private const val ColorIndicatorId = "ColorIndicator"
private val ColorIndicatorWidth = 4.dp
private const val DIV_HEIGHT_FRACTION = 0.64f
private const val COLOR_INDICATOR_HEIGHT_FRACTION = 0.40f

@Composable
fun ListItemLeadingTwoLineTextWithColorIndicator(
    line1: String,
    line2: String,
    color: Color?,
    modifier: Modifier = Modifier,
) = ListItemLeadingContentWithColorIndicator(
    color = color,
    modifier = modifier,
) {
    TwoLineDateText(
        dateLine1 = line1,
        dateLine2 = line2,
        modifier = Modifier
            .defaultMinSize(
                minWidth = ContainerMinWidth,
                minHeight = ContainerMinWidth,
            )
    )
}
