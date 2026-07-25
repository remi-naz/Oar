package dev.ridill.oar.moneyPiles.domain.repository

import androidx.paging.PagingData
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.PileTransactionEntry
import kotlinx.coroutines.flow.Flow

interface PileDetailRepository {
    fun getPileDetailById(id: Long): Flow<MoneyPileDetails?>
    fun getSavedAmount(id: Long): Flow<Double>
    fun getTransactionsInPilePaged(pileId: Long): Flow<PagingData<PileTransactionEntry>>
    suspend fun deleteTransaction(id: Long)
}
