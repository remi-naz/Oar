package dev.ridill.oar.moneyPiles.data.repository

import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.moneyPiles.domain.model.ContributionSource
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.repository.MoneyPileRepository
import dev.ridill.oar.moneyPiles.domain.repository.PileFundMovementRepository
import kotlinx.coroutines.flow.Flow

internal class PileFundMovementRepositoryImpl(
    private val pileRepo: MoneyPileRepository,
) : PileFundMovementRepository {
    override fun getPileById(id: Long): Flow<MoneyPileDetails?> = pileRepo
        .getPileDetailsFlow(id)

    override suspend fun movePileFund(
        pileId: Long,
        amount: Double,
        movement: FundMovement
    ) {
        pileRepo.addEntryToPile(
            pileId = pileId,
            amount = amount,
            movement = movement,
            source = ContributionSource.MANUAL
        )
    }
}
