package dev.ridill.oar.moneyPiles.presentation.pileDetails

import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import java.time.LocalDate

data class PileDetailState(
    val loading: Boolean = false,
    val pile: MoneyPileDetails? = null,
    val savedAmount: Double = Double.Zero,
    val progressFraction: Float = Float.Zero,
    val progressState: PileProgressState = PileProgressState.SavingFreely,
    val projectedCompletion: LocalDate? = null,
    val canWithdraw: Boolean = false,
)

sealed interface PileProgressState {
    data object SavingFreely : PileProgressState
    data object GoalReached : PileProgressState
    data class AmountToGo(val amount: Double) : PileProgressState
}
