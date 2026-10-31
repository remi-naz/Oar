package dev.ridill.oar.core.domain.model

/**
 * A single slice of a pie or donut chart.
 *
 * Holds no UI types so it can be built anywhere. Anything the theme or resources decide, such
 * as the color or the text for an unnamed slice, is left `null` here and resolved by the
 * composable that shows it.
 *
 * @property id Identifier of whatever the slice represents, or `null` when it has none.
 * @property label Name of the slice, or `null` when the UI should show its own fallback text.
 * @property amount Absolute value the slice represents.
 * @property fraction Share of the whole, from 0 to 1.
 * @property colorCode ARGB color of the slice, or `null` when the UI should pick a fallback.
 */
data class ChartSlice(
    val id: Long?,
    val label: String?,
    val amount: Double,
    val fraction: Float,
    val colorCode: Int?
)
