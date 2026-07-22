package dev.ridill.oar.moneyPiles.domain.repository

import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import kotlinx.coroutines.flow.Flow

interface AddToPileRepository {
    fun getPileById(id: Long): Flow<MoneyPileDetails?>
    suspend fun addToPile(
        pileId: Long,
        amount: Double,
        movement: FundMovement,
    )
}
