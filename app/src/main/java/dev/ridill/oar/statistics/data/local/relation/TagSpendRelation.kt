package dev.ridill.oar.statistics.data.local.relation

data class TagSpendRelation(
    val tagId: Long?,
    val tagName: String?,
    val tagColorCode: Int?,
    val amount: Double,
    val transactionCount: Int
)
