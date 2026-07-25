package dev.ridill.oar.moneyPiles.domain.model

import dev.ridill.oar.core.domain.model.FundMovement
import java.time.LocalDateTime

data class PileTransactionEntry(
    val id: Long,
    val amount: Double,
    val movement: FundMovement,
    val contributionSource: ContributionSource,
    val timestamp: LocalDateTime,
)
