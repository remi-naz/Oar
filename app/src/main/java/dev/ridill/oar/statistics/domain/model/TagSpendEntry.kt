package dev.ridill.oar.statistics.domain.model

data class TagSpendEntry(
    val tagId: Long?,
    val name: String?,
    val colorCode: Int?,
    val amount: Double,
    val transactionCount: Int,
    val fraction: Float
)
