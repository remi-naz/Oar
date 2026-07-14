package dev.ridill.oar.moneyPiles.domain.model

import java.time.LocalDateTime

data class PileHistoryEntry(
    val id: Long,
    val type: PileHistoryEntryType,
    val amount: Double,
    val timestamp: LocalDateTime
)
