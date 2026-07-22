package dev.ridill.oar.moneyPiles.presentation.allPiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.ui.navigation.AddEditPileResult
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.moneyPiles.domain.repository.AllPilesRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllPilesViewModel @Inject constructor(
    repo: AllPilesRepository,
    private val eventBus: EventBus<AllPilesEvent>,
) : ViewModel() {

    val pilesPagingData = repo.getAllPiles()
        .cachedIn(viewModelScope)

    val events = eventBus.eventFlow

    fun onAddEditPileResult(result: AddEditPileResult) = viewModelScope.launch {
        val message = when (result) {
            AddEditPileResult.PILE_DELETED -> UiText.StringResource(R.string.pile_deleted)
            AddEditPileResult.PILE_SAVED -> UiText.StringResource(R.string.pile_saved)
        }

        eventBus.send(AllPilesEvent.ShowUiMessage(message))
    }

    fun onFundAddedToPile() = viewModelScope.launch {
        eventBus.send(AllPilesEvent.ShowUiMessage(UiText.StringResource(R.string.fund_added_to_pile)))
    }

    sealed interface AllPilesEvent {
        data class ShowUiMessage(val text: UiText) : AllPilesEvent
    }
}
