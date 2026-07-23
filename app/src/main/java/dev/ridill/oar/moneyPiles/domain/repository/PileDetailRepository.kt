package dev.ridill.oar.moneyPiles.domain.repository

import androidx.paging.PagingData
import dev.ridill.oar.moneyPiles.domain.model.PileDetail
import dev.ridill.oar.moneyPiles.domain.model.PileTransactionEntry
import kotlinx.coroutines.flow.Flow

interface PileDetailRepository {
    fun getPileDetailById(id: Long): Flow<PileDetail?>
    fun getTransactionsInPilePaged(pileId: Long): Flow<PagingData<PileTransactionEntry>>
}
