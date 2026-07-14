package dev.ridill.oar.moneyPiles.domain.repository

import dev.ridill.oar.moneyPiles.domain.model.MoneyPile
import dev.ridill.oar.moneyPiles.domain.model.PileDepositDirection
import kotlinx.coroutines.flow.Flow

interface AddToPileRepository {
    fun getPileById(id: Long): Flow<MoneyPile?>
    suspend fun addToPile(id: Long, amount: Double, direction: PileDepositDirection)
}
