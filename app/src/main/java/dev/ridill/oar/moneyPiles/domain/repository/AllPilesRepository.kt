package dev.ridill.oar.moneyPiles.domain.repository

import androidx.paging.PagingData
import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileWithSavedAmount
import kotlinx.coroutines.flow.Flow

interface AllPilesRepository {
    fun getAllPiles(query: String = String.Empty): Flow<PagingData<MoneyPileWithSavedAmount>>
}
