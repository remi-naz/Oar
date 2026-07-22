package dev.ridill.oar.moneyPiles.data.repository

import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.moneyPiles.domain.model.ContributionSource
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.repository.AddToPileRepository
import dev.ridill.oar.moneyPiles.domain.repository.MoneyPileRepository
import kotlinx.coroutines.flow.Flow

internal class AddToPileRepositoryImpl(
    private val pileRepo: MoneyPileRepository,
) : AddToPileRepository {
    override fun getPileById(id: Long): Flow<MoneyPileDetails?> = pileRepo
        .getPileDetailsFlow(id)

    override suspend fun addToPile(
        pileId: Long,
        amount: Double,
        movement: FundMovement
    ) {
        pileRepo.addMoneyToPile(
            pileId = pileId,
            amount = amount,
            movement = movement,
            source = ContributionSource.MANUAL
        )
    }
}
