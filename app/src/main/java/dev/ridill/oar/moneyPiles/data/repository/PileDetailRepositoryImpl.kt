package dev.ridill.oar.moneyPiles.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.ridill.oar.aggregations.domain.repository.AggregationsRepository
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.di.ApplicationScope
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.data.local.PileTransactionPagingSource
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileTransactionDao
import dev.ridill.oar.moneyPiles.data.toPileTransactionEntry
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.PileTransactionEntry
import dev.ridill.oar.moneyPiles.domain.pileReminder.PileReminder
import dev.ridill.oar.moneyPiles.domain.repository.MoneyPileRepository
import dev.ridill.oar.moneyPiles.domain.repository.PileDetailRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

internal class PileDetailRepositoryImpl(
    private val db: OarDatabase,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val dao: MoneyPileTransactionDao,
    private val pileDao: MoneyPileDao,
    private val pileRepo: MoneyPileRepository,
    private val aggregationsRepo: AggregationsRepository,
    private val pileReminder: PileReminder,
) : PileDetailRepository {

    override fun getPileDetailById(id: Long): Flow<MoneyPileDetails?> = pileRepo
        .getPileDetailsFlow(id)

    override fun getSavedAmount(id: Long): Flow<Double> = aggregationsRepo
        .getAggregateForMoneyPile(id)

    override fun getTransactionsInPilePaged(
        pileId: Long,
        includeLocked: Boolean
    ): Flow<PagingData<PileTransactionEntry>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = {
            PileTransactionPagingSource(
                db = db,
                applicationScope = applicationScope,
                pileId = pileId,
                dao = dao,
                includeLocked = includeLocked,
            )
        }
    ).flow
        .mapLatest { pagingData ->
            pagingData.map(MoneyPileTransactionsEntity::toPileTransactionEntry)
        }

    override fun doLockedEntriesExist(pileId: Long): Flow<Boolean> = dao
        .doLockedEntriesExistForPile(pileId)
        .distinctUntilChanged()

    override suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    override suspend fun deletePile(id: Long) = withContext(Dispatchers.IO) {
        pileReminder.cancel(id)
        pileDao.deletePileById(id)
    }
}
