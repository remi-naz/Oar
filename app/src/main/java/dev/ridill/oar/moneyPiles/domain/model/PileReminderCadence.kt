package dev.ridill.oar.moneyPiles.domain.model

import androidx.annotation.StringRes
import dev.ridill.oar.R

enum class PileReminderCadence(
    @StringRes val labelRes: Int
) {
    NO_REMIND(R.string.pile_reminder_cadence_no_remind),
    WEEKLY(R.string.pile_reminder_cadence_weekly),
    MONTHLY(R.string.pile_reminder_cadence_monthly),
    BI_MONTHLY(R.string.pile_reminder_cadence_bi_monthly),
    YEARLY(R.string.pile_reminder_cadence_yearly)
}
