package dev.ridill.oar.statistics.domain.model

/**
 * Spend attributed to a single tag within a cycle.
 *
 * Spends that have no usable tag are grouped together with [tagId], [name] and [colorCode]
 * all `null`.
 */
data class TagSpend(
    val tagId: Long?,
    val name: String?,
    val colorCode: Int?,
    val amount: Double,
    val fraction: Float
)
