package dev.ridill.oar.moneyPiles.domain.model

import androidx.annotation.StringRes
import dev.ridill.oar.R

enum class PileReminderBehavior(
    @StringRes val labelRes: Int
) {
    REMIND(R.string.pile_reminder_behavior_remind),
    SUGGEST(R.string.pile_reminder_behavior_suggest),
    AUTO_ADD(R.string.pile_reminder_behavior_auto_add)
}
