package dev.ridill.oar.moneyPiles.presentation.pileDetails

import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.moneyPiles.domain.model.PileDetail
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.approxCycleDays
import kotlin.math.ceil

data class PileDetailState(
    val pile: PileDetail? = null,
) {
    val isLoading: Boolean get() = pile == null

    /** Formatted target month/year (eg. "Mar '26"), or null when a projection can't be made. */
    val projectedCompletionDate: String?
        get() {
            val p = pile ?: return null
            val target = p.targetAmount ?: return null
            if (p.savedAmount >= target) return null
            if (p.reminderBehavior != PileReminderBehavior.AUTO_ADD) return null
            val cycleDays = p.reminderCadence.approxCycleDays
            if (cycleDays <= 0) return null
            val reminderAmount = p.reminderAmount
            if (reminderAmount == null || reminderAmount <= 0) return null

            val remaining = target - p.savedAmount
            val cycles = ceil(remaining / reminderAmount).toInt().coerceAtLeast(1)
            return DateUtil.dateNow()
                .plusDays(cycleDays.toLong() * cycles)
                .format(DateUtil.Formatters.MMM_yy_spaceSep)
        }
}
