package dev.ridill.oar.moneyPiles.domain.repository

import dev.ridill.oar.moneyPiles.domain.model.MoneyPile

interface AddEditPileRepository {
    suspend fun getPileById(id: Long): MoneyPile?
    suspend fun savePile(pile: MoneyPile): Long
}
