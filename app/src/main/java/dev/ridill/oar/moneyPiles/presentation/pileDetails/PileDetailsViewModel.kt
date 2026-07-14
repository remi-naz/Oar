package dev.ridill.oar.moneyPiles.presentation.pileDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.ui.navigation.PileDetailsRoute
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.moneyPiles.domain.model.MoneyPile
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import dev.ridill.oar.moneyPiles.domain.repository.PileDetailsRepository
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import kotlin.math.ceil

//@HiltViewModel(assistedFactory = PileDetailsViewModel.Factory::class)
class PileDetailsViewModel @Inject constructor(
//    @Assisted val route: PileDetailsRoute,
//    repo: PileDetailsRepository
) : ViewModel() {

//    @AssistedFactory
//    interface Factory {
//        fun create(route: PileDetailsRoute): PileDetailsViewModel
//    }

//    val state = repo.getPileDetails(route.pileId)
//        .mapLatest { details -> details.toState() }
//        .asStateFlow(viewModelScope, PileDetailsState())
//
//    private fun MoneyPileDetails?.toState(): PileDetailsState {
//        if (this == null) return PileDetailsState()
//        return PileDetailsState(
//            pile = pile,
//            progressPercent = progressPercent,
//            isGoalReached = isGoalReached,
//            projectedCompletionLabel = projectedCompletionLabel(pile),
//            history = history.sortedByDescending { it.timestamp },
//            canWithdraw = canWithdraw
//        )
//    }
//
//    private fun projectedCompletionLabel(pile: MoneyPile): UiText {
//        val target = pile.targetAmount
//            ?: return UiText.StringResource(R.string.pile_projection_no_goal)
//
//        if (pile.currentAmount >= target) {
//            return UiText.StringResource(R.string.pile_projection_reached)
//        }
//        if (pile.reminderBehavior == PileReminderBehavior.REMIND ||
//            pile.reminderAmount <= 0 ||
//            pile.reminderCadence == PileReminderCadence.NO_REPEAT
//        ) {
//            return UiText.StringResource(R.string.pile_projection_set_pace)
//        }
//
//        val remaining = target - pile.currentAmount
//        val cycles = ceil(remaining / pile.reminderAmount).toInt()
//        val cadenceDays = when (pile.reminderCadence) {
//            PileReminderCadence.WEEKLY -> 7
//            PileReminderCadence.MONTHLY -> 30
//            PileReminderCadence.BI_MONTHLY -> 60
//            PileReminderCadence.YEARLY -> 365
//            PileReminderCadence.NO_REPEAT -> 30
//        }
//        val projectedDate = DateUtil.now().plusDays(cycles.toLong() * cadenceDays)
//        return UiText.DynamicString(projectedDate.format(DateUtil.Formatters.MMM_yy_spaceSep))
//    }
}
