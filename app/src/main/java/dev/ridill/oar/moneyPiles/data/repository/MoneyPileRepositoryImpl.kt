package dev.ridill.oar.moneyPiles.data.repository

import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileTransactionDao
import dev.ridill.oar.moneyPiles.data.toMoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.ContributionSource
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.repository.MoneyPileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

internal class MoneyPileRepositoryImpl(
    private val pileDao: MoneyPileDao,
    private val transactionDao: MoneyPileTransactionDao,
) : MoneyPileRepository {
    override suspend fun getPileDetails(id: Long): MoneyPileDetails? =
        withContext(Dispatchers.IO) {
            pileDao.getPileById(id)?.toMoneyPileDetails()
        }

    override fun getPileDetailsFlow(id: Long): Flow<MoneyPileDetails?> = pileDao
        .getPileByIdFlow(id)
        .mapLatest { it?.toMoneyPileDetails() }

    override suspend fun addMoneyToPile(
        pileId: Long,
        amount: Double,
        movement: FundMovement,
        source: ContributionSource,
        timestamp: LocalDateTime
    ): Long = withContext(Dispatchers.IO) {
        val entity = MoneyPileTransactionsEntity(
            id = OarDatabase.DEFAULT_ID_LONG,
            pileId = pileId,
            amount = amount,
            movement = movement,
            contributionSource = source,
            createdTimestamp = timestamp
        )

        transactionDao.upsert(entity).first()
    }
}
