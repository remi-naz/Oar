package dev.ridill.oar.moneyPiles.domain.model

import androidx.annotation.StringRes
import dev.ridill.oar.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

enum class PileReminderCadence(
    @StringRes val labelRes: Int,
    val duration: Duration
) {
    NO_REMIND(labelRes = R.string.pile_reminder_cadence_no_remind, duration = Duration.ZERO),
    WEEKLY(labelRes = R.string.pile_reminder_cadence_weekly, duration = 7.days),
    MONTHLY(labelRes = R.string.pile_reminder_cadence_monthly, duration = 30.days),
    BI_MONTHLY(labelRes = R.string.pile_reminder_cadence_bi_monthly, duration = 60.days),
    YEARLY(labelRes = R.string.pile_reminder_cadence_yearly, duration = 365.days)
}
