package dev.ridill.oar.moneyPiles.presentation.pileDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.ui.navigation.AddEditMoneyPileResult
import dev.ridill.oar.core.ui.navigation.MoneyPileDetailsRoute
import dev.ridill.oar.core.ui.navigation.PileFundMovementResult
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import dev.ridill.oar.moneyPiles.domain.repository.PileDetailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.roundToLong

@HiltViewModel(assistedFactory = PileDetailViewModel.Factory::class)
class PileDetailViewModel @AssistedInject constructor(
    @Assisted val route: MoneyPileDetailsRoute,
    private val savedStateHandle: SavedStateHandle,
    private val repo: PileDetailRepository,
    private val eventBus: EventBus<PileDetailEvent>,
) : ViewModel(), PileDetailActions {

    @AssistedFactory
    interface Factory {
        fun create(route: MoneyPileDetailsRoute): PileDetailViewModel
    }

    private val includeLockedTransactions = savedStateHandle
        .getStateFlow(SHOW_LOCKED, false)

    val transactionPagingData = includeLockedTransactions
        .flatMapLatest { includeLocked ->
            repo.getTransactionsInPilePaged(
                pileId = route.pileId,
                includeLocked = includeLocked
            )
        }
        .cachedIn(viewModelScope)

    private val dateNow = MutableStateFlow(DateUtil.dateNow())
    private val details = repo.getPileDetailById(route.pileId)
    private val targetAmount = details
        .mapLatest { it?.targetAmount }
        .distinctUntilChanged()
    private val savedAmount = repo.getSavedAmount(route.pileId)
        .distinctUntilChanged()
    private val progressFraction = combineTuple(
        savedAmount,
        targetAmount,
    ).mapLatest { (saved, target) ->
        if (target != null) (saved / target).toFloat()
        else Float.Zero
    }.distinctUntilChanged()
    private val progressState = combineTuple(
        savedAmount,
        targetAmount,
    ).mapLatest { (saved, target) ->
        return@mapLatest when {
            target == null -> PileProgressState.SavingFreely
            saved >= target -> PileProgressState.GoalReached
            else -> PileProgressState.AmountToGo(target - saved)
        }
    }.distinctUntilChanged()

    private val projectedCompletion = combineTuple(
        dateNow,
        details,
        savedAmount,
    ).mapLatest { (
                      dateNow,
                      pile,
                      savedAmount,
                  ) ->
        val p = pile ?: return@mapLatest null
        if (p.reminderCadence == PileReminderCadence.NO_REMIND) return@mapLatest null
        val reminderAmount = p.reminderAmount ?: return@mapLatest null
        val target = p.targetAmount ?: return@mapLatest null
        if (reminderAmount <= Double.Zero || target <= Double.Zero) return@mapLatest null

        val remaining = target - savedAmount
        val cycleDuration = p.reminderCadence.duration
        val cyclesUntilCompletion = ceil(remaining / reminderAmount).roundToLong().coerceAtLeast(1)

        return@mapLatest dateNow
            .plusDays(cyclesUntilCompletion * cycleDuration.inWholeDays)
    }
    private val canWithdraw = combineTuple(
        details.mapLatest { it?.locked == false }.distinctUntilChanged(),
        savedAmount
    )
        .mapLatest { (unlocked, savedAmount) ->
            unlocked && savedAmount > Double.Zero
        }
        .distinctUntilChanged()

    val state = combineTuple(
        details,
        savedAmount,
        progressFraction,
        progressState,
        projectedCompletion,
        canWithdraw,
        includeLockedTransactions,
    ).mapLatest { (
                      details,
                      savedAmount,
                      progressFraction,
                      progressState,
                      projectedCompletion,
                      canWithdraw,
                      includeLockedTransactions,
                  ) ->
        PileDetailState(
            pile = details,
            savedAmount = savedAmount,
            progressFraction = progressFraction,
            progressState = progressState,
            projectedCompletion = projectedCompletion,
            canWithdraw = canWithdraw,
            includeLockedTransactions = includeLockedTransactions,
        )
    }.asStateFlow(viewModelScope, PileDetailState())

    val events = eventBus.eventFlow

    fun onAddEditPileResult(result: AddEditMoneyPileResult) = viewModelScope.launch {
        val message = when (result) {
            AddEditMoneyPileResult.PILE_DELETED -> UiText.StringResource(R.string.pile_deleted)
            AddEditMoneyPileResult.PILE_SAVED -> UiText.StringResource(R.string.pile_saved)
        }

        eventBus.send(PileDetailEvent.ShowUiMessage(message))
    }

    fun onPileFundMovementResult(result: PileFundMovementResult) = viewModelScope.launch {
        eventBus.send(
            PileDetailEvent.ShowUiMessage(
                UiText.StringResource(
                    when (result) {
                        PileFundMovementResult.FUND_ADDED -> R.string.fund_added_to_pile
                        PileFundMovementResult.FUND_WITHDRAWN -> R.string.fund_withdrawn_from_pile
                    }
                )
            )
        )
    }

    fun onPileSweptOut() = viewModelScope.launch {
        eventBus.send(PileDetailEvent.ShowUiMessage(UiText.StringResource(R.string.pile_swept_out)))
    }

    fun refreshDateNow() {
        dateNow.update { DateUtil.dateNow() }
    }

    override fun onTransactionActionRevealed() {

    }

    override fun onTransactionDelete(id: Long) {
        viewModelScope.launch {
            repo.deleteTransaction(id)
        }
    }

    override fun onIncludeLockedTransactionsToggle(includeLocked: Boolean) {
        savedStateHandle[SHOW_LOCKED] = includeLocked
    }

    sealed interface PileDetailEvent {
        data class ShowUiMessage(val text: UiText) : PileDetailEvent
    }
}

private const val SHOW_LOCKED = "SHOW_LOCKED"
