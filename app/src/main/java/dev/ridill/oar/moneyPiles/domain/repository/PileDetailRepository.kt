package dev.ridill.oar.moneyPiles.domain.repository

import androidx.paging.PagingData
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.PileTransactionEntry
import kotlinx.coroutines.flow.Flow

interface PileDetailRepository {
    fun getPileDetailById(id: Long): Flow<MoneyPileDetails?>
    fun getSavedAmount(id: Long): Flow<Double>
    fun getTransactionsInPilePaged(
        pileId: Long,
        includeLocked: Boolean = true,
    ): Flow<PagingData<PileTransactionEntry>>
    fun doLockedEntriesExist(pileId: Long): Flow<Boolean>
    suspend fun deleteTransaction(id: Long)
    suspend fun deletePile(id: Long)
}
