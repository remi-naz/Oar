package dev.ridill.oar.moneyPiles.domain.repository

import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.model.RootError
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface PileFundMovementRepository {
    fun getPileById(id: Long): Flow<MoneyPileDetails?>
    suspend fun movePileFund(
        pileId: Long,
        amount: Double,
        movement: FundMovement,
        timestamp: LocalDateTime = DateUtil.now(),
        cycleId: Long,
    ): Result<Unit, RootError>
}

enum class PileFundMovementError : RootError {
    PileNotFound
}

class PileFundMovementThrowable(val error: PileFundMovementError) : Throwable()
