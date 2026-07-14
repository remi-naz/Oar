package dev.ridill.oar.moneyPiles.data.repository

import androidx.paging.PagingData
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.domain.model.MoneyPile
import dev.ridill.oar.moneyPiles.domain.repository.AllPilesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class AllPilesRepositoryImpl(
    private val dao: MoneyPileDao,
) : AllPilesRepository {

    override fun getAllPiles(): Flow<PagingData<MoneyPile>> = flowOf()

    override fun getTotalAmountSetAside(): Flow<Double> = flowOf(Double.Zero)
}