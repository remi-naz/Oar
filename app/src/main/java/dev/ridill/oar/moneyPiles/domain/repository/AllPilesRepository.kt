package dev.ridill.oar.moneyPiles.domain.repository

import androidx.paging.PagingData
import dev.ridill.oar.moneyPiles.domain.model.MoneyPile
import kotlinx.coroutines.flow.Flow

interface AllPilesRepository {
    fun getAllPiles(): Flow<PagingData<MoneyPile>>
    fun getTotalAmountSetAside(): Flow<Double>
}
