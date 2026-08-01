package dev.ridill.oar.statistics.data.local.relation

import java.time.LocalDate

data class CycleAggregateRelation(
    val id: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val budget: Long,
    val spent: Double,
    val received: Double,
    val currencyCode: String,
)
