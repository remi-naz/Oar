package dev.ridill.oar.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.oar.core.domain.model.ChartSlice
import dev.ridill.oar.core.domain.util.Zero
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws [slices] as a pie with rounded outer corners.
 *
 * @param slices Slices in drawing order, clockwise from 12 o'clock.
 * @param modifier Sizing from the caller wins. Without any, the chart is [DefaultDonutSize]
 * square.
 * @param fallbackColor Used for slices without a color of their own.
 * @param backgroundColor Color of the disc drawn behind the slices, visible in the gaps between
 * them and wherever they don't cover the full circle.
 * @param cornerRadius Radius of each slice's rounded outer corners.
 * @param sliceGap Gap between neighboring slices, measured at the outer rim.
 */
@Composable
fun DonutChart(
    slices: List<ChartSlice>,
    modifier: Modifier = Modifier,
    fallbackColor: Color = MaterialTheme.colorScheme.outline,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    cornerRadius: Dp = DefaultSliceCornerRadius,
    sliceGap: Dp = DefaultSliceGap,
) {
    Box(
        modifier = modifier
            .defaultMinSize(
                minWidth = DefaultDonutSize,
                minHeight = DefaultDonutSize
            )
            .drawWithCache {
                val cornerRadiusPx = cornerRadius.toPx()
                val halfGap = sliceGap.toPx() / 2f
                val radius = minOf(size.width, size.height) / 2f
                val pieCenter = size.center

                // Only a single slice that fills the whole pie is a plain circle
                val loneFullSlice = slices.singleOrNull()
                    ?.takeIf { it.fraction >= 1f }

                var startAngle = PIE_START_ANGLE_DEGREES
                val paths = slices.mapNotNull { slice ->
                    // NaN would otherwise poison startAngle for every later slice
                    val fraction = slice.fraction
                        .takeIf { it.isFinite() }
                        ?.coerceIn(Float.Zero, 1f)
                        ?: return@mapNotNull null
                    // Fractions that add up past 100% must not wrap around and draw over the
                    // first slices
                    val remainingSweep = PIE_START_ANGLE_DEGREES +
                            FULL_CIRCLE_DEGREES - startAngle
                    val sweep = (FULL_CIRCLE_DEGREES * fraction)
                        .coerceAtMost(remainingSweep)
                    val path = pieSlicePath(
                        center = pieCenter,
                        radius = radius,
                        startAngle = startAngle,
                        sweep = sweep,
                        cornerRadius = cornerRadiusPx,
                        halfGap = halfGap,
                    )
                    startAngle += sweep
                    path?.let { it to slice.color(fallbackColor) }
                }

                onDrawBehind {
                    drawCircle(
                        color = backgroundColor,
                        radius = radius,
                        center = pieCenter,
                    )
                    // A lone full slice has no edges to separate, so pieSlicePath skips it, and it's
                    // drawn as a plain circle
                    loneFullSlice?.let { slice ->
                        drawCircle(
                            color = slice.color(fallbackColor),
                            radius = radius,
                            center = pieCenter,
                        )
                    }
                    paths.forEach { (path, color) ->
                        drawPath(path = path, color = color)
                    }
                }
            }
    )
}

/**
 * Resolves the color of this slice, falling back to [fallback] when it has none of its own.
 */
fun ChartSlice.color(fallback: Color): Color = colorCode?.let(::Color) ?: fallback

/**
 * Builds the outline of a pie slice with its two outer corners rounded.
 *
 * The path starts at [center], runs along the start edge, rounds the corner into the outer
 * arc, follows the arc, rounds the corner into the end edge, and closes back to [center]
 * along the end edge. Each rounded corner is a circular fillet of radius [cornerRadius]
 * that is tangent to both the edge and the outer arc, so the path has no kinks. The center
 * tip is left sharp.
 *
 * Both edges stay on true radial lines, pulled in by an angle equal to [halfGap] at the
 * outer rim. Neighboring slices therefore all meet at [center], and the gap between them
 * grows from zero at the center to `2 * halfGap` at the rim. A [halfGap] of zero makes
 * neighbors touch, and a negative one makes them overlap.
 *
 * @param center Center of the pie.
 * @param radius Radius of the outer arc.
 * @param startAngle Angle in degrees where the slice begins, measured clockwise from the
 * positive x axis.
 * @param sweep Angle in degrees the slice spans clockwise, before the gap is removed.
 * @param cornerRadius Radius of the rounded outer corners. Capped at a quarter of [radius].
 * @param halfGap Half the gap between neighboring slices, in pixels at the outer rim.
 * @return The slice path, or `null` when there is nothing sensible to draw: a non-finite or
 * non-positive input, a full circle (draw a ring instead, since its two edges would
 * coincide), or a slice that the gap leaves with no width. A slice too narrow for the full
 * [cornerRadius] gets a smaller corner radius so the outer arc never sweeps backwards.
 */
private fun pieSlicePath(
    center: Offset,
    radius: Float,
    startAngle: Float,
    sweep: Float,
    cornerRadius: Float,
    halfGap: Float,
): Path? {
    // NaN/Infinity would poison every arc below, and a zero radius divides by zero
    if (!radius.isFinite() || radius <= 0f) return null
    if (!startAngle.isFinite() || !sweep.isFinite() || !halfGap.isFinite()) return null
    // No width to draw, or a full circle whose start and end edges would coincide
    if (sweep <= 0f || sweep >= FULL_CIRCLE_DEGREES) return null

    // Angle that equals half the gap, measured at the outer rim.
    val halfGapAngle = Math.toDegrees((halfGap / radius).toDouble()).toFloat()

    val edgeStartAngle = startAngle + halfGapAngle
    val edgeEndAngle = startAngle + sweep - halfGapAngle
    val edgeSweep = edgeEndAngle - edgeStartAngle
    // The gap has eaten the whole slice
    if (edgeSweep <= 0f) return null

    // Each fillet uses up filletAngleOffset of the outer arc at both ends. Past 180 degrees
    // the slice is wide enough for any fillet, so the sine is capped at its peak.
    val halfEdgeSweepSin = sin(
        Math.toRadians((minOf(edgeSweep, 180f) / 2f).toDouble())
    ).toFloat()
    val maxFilletForSweep = radius * halfEdgeSweepSin / (1f + halfEdgeSweepSin)
    val fillet = cornerRadius.coerceIn(0f, minOf(radius / 4f, maxFilletForSweep))
    val filletAngleOffset = Math.toDegrees(
        asin((fillet / (radius - fillet)).toDouble().coerceIn(-1.0, 1.0))
    ).toFloat()
    val startFilletCenter = polar(center, edgeStartAngle + filletAngleOffset, radius - fillet)
    val endFilletCenter = polar(center, edgeEndAngle - filletAngleOffset, radius - fillet)

    return Path().apply {
        moveTo(center.x, center.y)

        arcTo(
            rect = Rect(startFilletCenter, fillet),
            startAngleDegrees = edgeStartAngle - 90f,
            sweepAngleDegrees = 90f + filletAngleOffset,
            forceMoveTo = false,
        )

        arcTo(
            rect = Rect(center, radius),
            startAngleDegrees = edgeStartAngle + filletAngleOffset,
            sweepAngleDegrees = edgeSweep - 2f * filletAngleOffset,
            forceMoveTo = false,
        )

        arcTo(
            rect = Rect(endFilletCenter, fillet),
            startAngleDegrees = edgeEndAngle - filletAngleOffset,
            sweepAngleDegrees = 90f + filletAngleOffset,
            forceMoveTo = false,
        )

        close()
    }
}

private fun polar(
    from: Offset,
    angleDegrees: Float,
    distance: Float
): Offset {
    val rad = Math.toRadians(angleDegrees.toDouble())
    return Offset(
        x = from.x + distance * cos(rad).toFloat(),
        y = from.y + distance * sin(rad).toFloat()
    )
}

private const val FULL_CIRCLE_DEGREES = 360f

// Angles run clockwise from the positive x-axis, so -90 is 12 o'clock
private const val PIE_START_ANGLE_DEGREES = -90f
private val DefaultDonutSize = 148.dp
private val DefaultSliceCornerRadius = 4.dp
private val DefaultSliceGap = 0.dp
