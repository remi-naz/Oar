package dev.ridill.oar.moneyPiles.presentation.sweepout

import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import java.time.LocalDateTime

data class PileSweepOutState(
    val timestampNow: LocalDateTime = DateUtil.now(),
    val loading: Boolean = false,
    val pile: MoneyPileDetails? = null,
    val maxLimit: Double = Double.Zero,
    val amountInputError: UiText? = null,
    val createLinkedTransaction: Boolean = true,
    val previewAmount: Double = Double.Zero,
    val confirmEnabled: Boolean = false,
    val showCompletionWarning: Boolean = false,
)
