package dev.ridill.oar.moneyPiles.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.di.ApplicationScope
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.data.local.MoneyPilesPagingSource
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileAggregateView
import dev.ridill.oar.moneyPiles.data.toMoneyPile
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileEntryUiModel
import dev.ridill.oar.moneyPiles.domain.repository.AllPilesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

internal class AllPilesRepositoryImpl(
    private val db: OarDatabase,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val dao: MoneyPileDao,
) : AllPilesRepository {

    override fun getAllPilesPagedGroupedByCompleted(
        query: String,
        includeCompleted: Boolean,
    ): Flow<PagingData<MoneyPileEntryUiModel>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = {
            MoneyPilesPagingSource(
                db = db,
                applicationScope = applicationScope,
                query = query,
                dao = dao,
                includeCompleted = includeCompleted,
            )
        }
    ).flow
        .mapLatest { pagingData ->
            pagingData.map(MoneyPileAggregateView::toMoneyPile)
        }
        .mapLatest { pagingData ->
            pagingData.insertSeparators<MoneyPileEntryUiModel.MoneyPileWithSavedAmount, MoneyPileEntryUiModel> { before, after ->
                if (before?.completionTimestamp == null && after?.completionTimestamp != null) MoneyPileEntryUiModel.CompletedSeparator
                else null
            }
        }
}
