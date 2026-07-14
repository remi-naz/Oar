package dev.ridill.oar.moneyPiles.domain.repository

import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.PileDepositDirection
import kotlinx.coroutines.flow.Flow

interface PileDetailsRepository {
    fun getPileDetails(id: Long): Flow<MoneyPileDetails?>
    suspend fun addToPile(id: Long, amount: Double, direction: PileDepositDirection)
}
