package dev.ridill.oar.moneyPiles.domain.model

import android.os.Parcelable
import androidx.compose.ui.graphics.toArgb
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.ui.theme.SelectableColorsList
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Currency

@Parcelize
data class MoneyPileDetails(
    val id: Long,
    val name: String,
    val icon: PileIcon,
    val colorCode: Int,
    val contributionMode: PileContributionMode,
    val targetAmount: Double?,
    val targetDate: LocalDate?,
    val locked: Boolean,
    val currency: Currency,
    val reminderCadence: PileReminderCadence,
    val reminderBehavior: PileReminderBehavior,
    val reminderAmount: Double?,
    val createdTimestamp: LocalDateTime,
) : Parcelable {
    companion object {
        val NEW
            get() = MoneyPileDetails(
                id = OarDatabase.DEFAULT_ID_LONG,
                name = String.Empty,
                icon = PileIcon.Savings,
                colorCode = SelectableColorsList.random().toArgb(),
                contributionMode = PileContributionMode.FROM_BALANCE,
                targetAmount = null,
                targetDate = null,
                locked = false,
                reminderCadence = PileReminderCadence.NO_REMIND,
                reminderBehavior = PileReminderBehavior.REMIND,
                reminderAmount = 0.0,
                createdTimestamp = LocalDateTime.now(),
                currency = LocaleUtil.defaultCurrency,
            )
    }
}
