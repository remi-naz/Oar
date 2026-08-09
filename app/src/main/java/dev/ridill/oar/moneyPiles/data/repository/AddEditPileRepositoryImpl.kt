package dev.ridill.oar.moneyPiles.data.repository

import androidx.room.withTransaction
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileTransactionDao
import dev.ridill.oar.moneyPiles.data.toEntity
import dev.ridill.oar.moneyPiles.domain.model.ContributionSource
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import dev.ridill.oar.moneyPiles.domain.pileReminder.PileReminder
import dev.ridill.oar.moneyPiles.domain.repository.AddEditPileRepository
import dev.ridill.oar.moneyPiles.domain.repository.MoneyPileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AddEditPileRepositoryImpl(
    private val db: OarDatabase,
    private val pileDao: MoneyPileDao,
    private val pileTransactionDao: MoneyPileTransactionDao,
    private val pileRepo: MoneyPileRepository,
    private val pileReminder: PileReminder,
) : AddEditPileRepository {
    override suspend fun getPileById(id: Long): MoneyPileDetails? =
        pileRepo.getPileDetails(id)

    override suspend fun savePile(
        pile: MoneyPileDetails,
        starterAmount: Double?
    ): Long = withContext(Dispatchers.IO) {
        val nextReminderTimestamp = pile.reminderCadence
            .takeIf { it != PileReminderCadence.NO_REMIND }
            ?.let {
                pile.nextReminderTimestamp
                    ?.takeIf { existing -> existing.isAfter(DateUtil.now()) }
                    ?: it.nextReminderFrom(DateUtil.now())
            }
        val pileToSave = pile.copy(nextReminderTimestamp = nextReminderTimestamp)

        val insertedPileId = db.withTransaction {
            val insertedPileId = pileDao.upsert(
                pileToSave.toEntity(remainingAmount = pileToSave.targetAmount)
            ).first().takeIf { it > OarDatabase.DEFAULT_ID_LONG } ?: pileToSave.id

            if (insertedPileId > OarDatabase.DEFAULT_ID_LONG && starterAmount != null) {
                val transaction = MoneyPileTransactionsEntity(
                    pileId = insertedPileId,
                    amount = starterAmount,
                    movement = FundMovement.IN,
                    contributionSource = ContributionSource.STARTER,
                    createdTimestamp = DateUtil.now(),
                    transactionId = null,
                )
                pileTransactionDao.upsert(transaction)
            }

            insertedPileId
        }

        pileReminder.cancel(insertedPileId)
        if (pileToSave.reminderCadence != PileReminderCadence.NO_REMIND) {
            pileReminder.setReminder(pileToSave.copy(id = insertedPileId))
        }

        insertedPileId
    }

    override suspend fun deletePile(id: Long) = withContext(Dispatchers.IO) {
        pileReminder.cancel(id)
        pileDao.deletePileById(id)
    }
}
