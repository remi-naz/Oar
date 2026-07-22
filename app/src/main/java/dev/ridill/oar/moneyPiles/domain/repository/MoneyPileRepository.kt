package dev.ridill.oar.moneyPiles.domain.repository

import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.moneyPiles.domain.model.ContributionSource
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface MoneyPileRepository {
    suspend fun getPileDetails(id: Long): MoneyPileDetails?
    fun getPileDetailsFlow(id: Long): Flow<MoneyPileDetails?>
    suspend fun addMoneyToPile(
        pileId: Long,
        amount: Double,
        movement: FundMovement,
        source: ContributionSource,
        timestamp: LocalDateTime = DateUtil.now()
    ): Long
}
