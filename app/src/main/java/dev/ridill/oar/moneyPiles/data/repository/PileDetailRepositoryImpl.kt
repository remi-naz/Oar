package dev.ridill.oar.moneyPiles.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.di.ApplicationScope
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.data.local.PileTransactionPagingSource
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity
import dev.ridill.oar.moneyPiles.data.toPileDetail
import dev.ridill.oar.moneyPiles.data.toPileTransactionEntry
import dev.ridill.oar.moneyPiles.domain.model.PileDetail
import dev.ridill.oar.moneyPiles.domain.model.PileTransactionEntry
import dev.ridill.oar.moneyPiles.domain.repository.MoneyPileRepository
import dev.ridill.oar.moneyPiles.domain.repository.PileDetailRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest

internal class PileDetailRepositoryImpl(
    private val db: OarDatabase,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val pileDao: MoneyPileDao,
    private val pileRepo: MoneyPileRepository,
) : PileDetailRepository {

    override fun getPileDetailById(id: Long): Flow<PileDetail?> = combine(
        pileRepo.getPileDetailsFlow(id),
        pileDao.getPileSavedAmountFlow(id),
    ) { details, savedAmount ->
        details?.toPileDetail(savedAmount.orZero())
    }

    override fun getTransactionsInPilePaged(
        pileId: Long
    ): Flow<PagingData<PileTransactionEntry>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = {
            PileTransactionPagingSource(
                db = db,
                applicationScope = applicationScope,
                pileId = pileId,
                dao = pileDao,
            )
        }
    ).flow
        .mapLatest { pagingData ->
            pagingData.map(MoneyPileTransactionsEntity::toPileTransactionEntry)
        }
}
