package dev.ridill.oar.moneyPiles.domain.repository

import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import java.time.LocalDateTime

interface PileReminderRepository {
    suspend fun getPileDetails(id: Long): MoneyPileDetails?

    /**
     * Atomically records that [pile]'s reminder fired: inserts the AUTO contribution
     * (when the pile is set to auto-add) and advances its next-reminder timestamp in the
     * same DB transaction, so a process death between the two can't duplicate or drop
     * the contribution on the next reminder/restore pass.
     */
    suspend fun recordReminderFired(pile: MoneyPileDetails, nextReminderTimestamp: LocalDateTime?)

    suspend fun setAllFuturePileReminders()
}
