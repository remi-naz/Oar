package dev.ridill.oar.moneyPiles.domain.model

import androidx.annotation.StringRes
import dev.ridill.oar.R

enum class PileReminderBehavior(
    @StringRes val labelRes: Int
) {
    AUTO_ADD(R.string.pile_reminder_behavior_auto_add),
    REMIND(R.string.pile_reminder_behavior_remind)
}
