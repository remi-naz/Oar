package dev.ridill.oar.tags.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.ridill.oar.core.domain.util.One
import dev.ridill.oar.core.ui.components.ExcludedIconSmall
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.SelectableColorsList
import dev.ridill.oar.core.ui.theme.contentColor
import dev.ridill.oar.core.ui.util.exclusionGraphicsLayer

@Composable
fun TagChip(
    name: String,
    color: Color,
    excluded: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val colors = tagChipColors(color)
    val shape = FilterChipDefaults.shape
    val border = FilterChipDefaults.filterChipBorder(enabled, selected)
    val containerAlpha by animateFloatAsState(
        targetValue = if (excluded) ContentAlpha.PERCENT_32 else Float.One,
        label = "ContainerAlpha"
    )
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .drawWithCache {
                val outline = shape.createOutline(size, layoutDirection, Density(density))
                val containerColor = when {
                    !enabled -> if (selected) colors.disabledSelectedContainerColor else colors.disabledContainerColor
                    !selected -> colors.containerColor
                    else -> colors.selectedContainerColor
                }
                val exclusionPathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(
                        ExclusionDashLength.toPx(),
                        ExclusionDashGap.toPx()
                    )
                )
                onDrawBehind {
                    drawOutline(
                        outline = outline,
                        color = containerColor.copy(alpha = containerAlpha),
                        style = Fill
                    )

                    drawOutline(
                        outline = outline,
                        color = color,
                        style = Stroke(
                            width = border.width.toPx(),
                            pathEffect = if (excluded) exclusionPathEffect else null
                        )
                    )
                }
            }
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = FilterChipDefaults.Height)
                .padding(FilterChipDefaults.ContentPadding)
                .exclusionGraphicsLayer(excluded),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = FilterChipDefaults.horizontalArrangement(
                hasLeadingIcon = excluded || selected,
                hasTrailingIcon = trailingIcon != null
            ),
        ) {
            Box {
                this@Row.AnimatedVisibility(
                    visible = excluded,
                    enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut(),
                ) {
                    val leadingIconColor = when {
                        !enabled -> colors.disabledLeadingIconColor
                        !selected -> colors.leadingIconColor
                        else -> colors.selectedLeadingIconColor
                    }
                    val leadingContentRetainedState = rememberRetainedState(
                        targetValue = when {
                            excluded -> {
                                @Composable {
                                    CompositionLocalProvider(LocalContentColor provides leadingIconColor) {
                                        ExcludedIconSmall()
                                    }
                                }
                            }

                            else -> null
                        }
                    )
                    Box(contentAlignment = Alignment.Center) {
                        leadingContentRetainedState.value?.invoke()
                    }
                }
                if (!excluded) {
                    Spacer(modifier = Modifier.width(0.dp))
                }
            }

            val labelColor = when {
                !enabled -> colors.disabledLabelColor
                !selected -> colors.labelColor
                else -> color.contentColor()
            }
            CompositionLocalProvider(
                LocalContentColor provides labelColor,
                LocalTextStyle provides MaterialTheme.typography.labelLarge
            ) {
                Text(
                    text = name,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box {
                this@Row.AnimatedVisibility(
                    visible = trailingIcon != null,
                    enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut(),
                ) {
                    val trailingIconColor = when {
                        !enabled -> colors.disabledTrailingIconColor
                        !selected -> colors.trailingIconColor
                        else -> colors.selectedTrailingIconColor
                    }
                    val trailingContentRetainedState = rememberRetainedState(
                        targetValue = if (trailingIcon != null) {
                            @Composable {
                                CompositionLocalProvider(LocalContentColor provides trailingIconColor) {
                                    trailingIcon()
                                }
                            }
                        } else null
                    )
                    Box(contentAlignment = Alignment.Center) {
                        trailingContentRetainedState.value?.invoke()
                    }
                }
                if (trailingIcon == null) {
                    Spacer(modifier = Modifier.width(0.dp))
                }
            }
        }
    }
}

private val ExclusionDashLength = 2.dp
private val ExclusionDashGap = 4.dp

/**
 * Remembers the last non-null value emitted by the [targetValue]. When [targetValue] becomes null,
 * this function continues to return the last non-null value, allowing content to gracefully animate
 * out.
 */
@Composable
private fun <T> rememberRetainedState(targetValue: T?): State<T?> {
    val retainedState = remember { mutableStateOf(targetValue) }
    if (targetValue != null) {
        retainedState.value = targetValue
    }
    return retainedState
}

@PreviewLightDark
@Composable
private fun PreviewTagChip() {
    OarTheme {
        Surface {
            Column {
                TagChip(
                    name = LoremIpsum(1).values.joinToString(),
                    color = SelectableColorsList.first(),
                    excluded = true,
                    selected = true,
                    onClick = {}
                )
                TagChip(
                    name = LoremIpsum(1).values.joinToString(),
                    color = SelectableColorsList.first(),
                    excluded = true,
                    selected = false,
                    onClick = {}
                )
                TagChip(
                    name = LoremIpsum(1).values.joinToString(),
                    color = SelectableColorsList.first(),
                    excluded = false,
                    selected = true,
                    onClick = {}
                )
                TagChip(
                    name = LoremIpsum(1).values.joinToString(),
                    color = SelectableColorsList.first(),
                    excluded = false,
                    selected = false,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun tagChipColors(color: Color): SelectableChipColors = FilterChipDefaults.filterChipColors(
    selectedContainerColor = color,
    selectedLabelColor = color.contentColor(),
    selectedLeadingIconColor = color.contentColor(),
    iconColor = color
)