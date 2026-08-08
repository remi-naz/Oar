package dev.ridill.oar.moneyPiles.data.repository

import androidx.room.withTransaction
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileTransactionDao
import dev.ridill.oar.moneyPiles.data.toMoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.ContributionSource
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.pileReminder.PileReminder
import dev.ridill.oar.moneyPiles.domain.repository.MoneyPileRepository
import dev.ridill.oar.moneyPiles.domain.repository.PileReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

internal class PileReminderRepositoryImpl(
    private val db: OarDatabase,
    private val pileDao: MoneyPileDao,
    private val pileTransactionDao: MoneyPileTransactionDao,
    private val pileRepo: MoneyPileRepository,
    private val reminder: PileReminder,
) : PileReminderRepository {
    override suspend fun getPileDetails(id: Long): MoneyPileDetails? =
        pileRepo.getPileDetails(id)

    override suspend fun recordReminderFired(
        pile: MoneyPileDetails,
        nextReminderTimestamp: LocalDateTime?
    ) = withContext(Dispatchers.IO) {
        db.withTransaction {
            if (pile.reminderBehavior == PileReminderBehavior.AUTO_ADD &&
                (pile.reminderAmount ?: Double.Zero) > Double.Zero
            ) {
                val transaction = MoneyPileTransactionsEntity(
                    pileId = pile.id,
                    amount = pile.reminderAmount ?: Double.Zero,
                    movement = FundMovement.IN,
                    contributionSource = ContributionSource.AUTO,
                    createdTimestamp = DateUtil.now(),
                    transactionId = null,
                )
                pileTransactionDao.upsert(transaction)
            }

            pileDao.setNextReminderTimestampForPile(pile.id, nextReminderTimestamp)
        }
    }

    override suspend fun setAllFuturePileReminders() = withContext(Dispatchers.IO) {
        val now = DateUtil.now()
        pileDao.getPilesWithActiveReminders()
            .map { it.toMoneyPileDetails() }
            .forEach { pile ->
                val nextReminder = pile.nextReminderTimestamp
                    ?.takeIf { it.isAfter(now) }
                    ?: pile.reminderCadence.nextReminderFrom(now)
                        .also { pileDao.setNextReminderTimestampForPile(pile.id, it) }
                reminder.setReminder(pile.copy(nextReminderTimestamp = nextReminder))
            }
    }
}
