package dev.ridill.oar.moneyPiles.domain.model

import android.os.Parcelable
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.ui.theme.PrimaryBrandColor
import androidx.compose.ui.graphics.toArgb
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class MoneyPile(
    val id: Long,
    val name: String,
    val note: String,
    val icon: String,
    val color: Int,
    val contributionMode: PileContributionMode,
    val targetAmount: Double?,
    val currentAmount: Double,
    val locked: Boolean,
    val reminderCadence: PileReminderCadence,
    val reminderBehavior: PileReminderBehavior,
    val reminderAmount: Double,
    val createdTimestamp: LocalDateTime
) : Parcelable {
    companion object {
        val NEW
            get() = MoneyPile(
                id = OarDatabase.DEFAULT_ID_LONG,
                name = String.Empty,
                note = String.Empty,
                icon = DEFAULT_ICON,
                color = PrimaryBrandColor.toArgb(),
                contributionMode = PileContributionMode.TRACK_ONLY,
                targetAmount = null,
                currentAmount = Double.Zero,
                locked = false,
                reminderCadence = PileReminderCadence.MONTHLY,
                reminderBehavior = PileReminderBehavior.REMIND,
                reminderAmount = Double.Zero,
                createdTimestamp = DateUtil.now()
            )

        const val DEFAULT_ICON = "💰" // 💰
    }
}
