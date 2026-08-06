package dev.ridill.oar.moneyPiles.domain.repository

import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import kotlinx.coroutines.flow.Flow

interface PileSweepOutRepository {
    fun getPileDetails(pileId: Long): Flow<MoneyPileDetails?>
    fun getSweepableAmount(pileId: Long): Flow<Double>
    suspend fun sweepOutPile(
        pileId: Long,
        sweepAmount: Double,
        createLinkedTransaction: Boolean = true,
    ): Result<Unit, MoneyPileError>
}
