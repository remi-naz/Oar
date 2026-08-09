package dev.ridill.oar.moneyPiles.domain.model

import androidx.compose.ui.graphics.Color
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.domain.util.ifNaN
import java.time.LocalDateTime
import java.util.Currency

sealed class MoneyPileEntryUiModel {
    data class MoneyPileWithSavedAmount(
        val id: Long,
        val name: String,
        val icon: PileIcon,
        val color: Color,
        val currency: Currency,
        val targetAmount: Double?,
        val savedAmount: Double,
        val locked: Boolean,
        val createdTimestamp: LocalDateTime,
        val completionTimestamp: LocalDateTime?,
    ) : MoneyPileEntryUiModel() {
        val progressFraction: Float
            get() = if (targetAmount != null) (savedAmount / targetAmount).toFloat()
                .ifNaN { Float.Zero }
            else Float.Zero
    }

    data object CompletedSeparator : MoneyPileEntryUiModel()
}
