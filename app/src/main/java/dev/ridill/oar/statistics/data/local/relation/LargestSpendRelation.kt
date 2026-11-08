package dev.ridill.oar.statistics.data.local.relation

import java.time.LocalDateTime

data class LargestSpendRelation(
    val amount: Double,
    val note: String,
    val tagName: String?,
    val folderName: String?,
    val timestamp: LocalDateTime
)
