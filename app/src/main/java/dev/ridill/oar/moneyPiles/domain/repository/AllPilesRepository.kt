package dev.ridill.oar.moneyPiles.domain.repository

import androidx.paging.PagingData
import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileEntryUiModel
import kotlinx.coroutines.flow.Flow

interface AllPilesRepository {
    fun getAllPilesPagedGroupedByCompleted(
        query: String = String.Empty,
        includeCompleted: Boolean = true,
    ): Flow<PagingData<MoneyPileEntryUiModel>>
}
