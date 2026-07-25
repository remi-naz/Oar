package dev.ridill.oar.moneyPiles.domain.repository

import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails

interface AddEditPileRepository {
    suspend fun getPileById(id: Long): MoneyPileDetails?
    suspend fun savePile(
        pile: MoneyPileDetails,
        starterAmount: Double? = null
    ): Long
    suspend fun deletePile(id: Long)
}
