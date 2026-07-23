package dev.ridill.oar.moneyPiles.presentation.pileDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.ui.navigation.PileDetailRoute
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.moneyPiles.domain.repository.PileDetailRepository
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = PileDetailViewModel.Factory::class)
class PileDetailViewModel @AssistedInject constructor(
    @Assisted val route: PileDetailRoute,
    private val repo: PileDetailRepository,
    private val eventBus: EventBus<PileDetailEvent>,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(route: PileDetailRoute): PileDetailViewModel
    }

    val transactionPagingData = repo.getTransactionsInPilePaged(route.pileId)
        .cachedIn(viewModelScope)

    val state = repo.getPileDetailById(route.pileId)
        .mapLatest { PileDetailState(pile = it) }
        .asStateFlow(viewModelScope, PileDetailState())

    val events = eventBus.eventFlow

    fun onFundAddedToPile() {
        viewModelScope.launch {
            eventBus.send(
                PileDetailEvent.ShowUiMessage(UiText.StringResource(R.string.fund_added_to_pile))
            )
        }
    }

    fun onPileSaved() {
        viewModelScope.launch {
            eventBus.send(
                PileDetailEvent.ShowUiMessage(UiText.StringResource(R.string.pile_saved))
            )
        }
    }

    sealed interface PileDetailEvent {
        data class ShowUiMessage(val text: UiText) : PileDetailEvent
    }
}
