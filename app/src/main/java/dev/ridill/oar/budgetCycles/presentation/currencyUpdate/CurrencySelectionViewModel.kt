package dev.ridill.oar.budgetCycles.presentation.currencyUpdate

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.core.domain.util.textAsFlow
import dev.ridill.oar.settings.domain.repositoty.CurrencyRepository
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class CurrencySelectionViewModel @Inject constructor(
    private val repo: CurrencyRepository
) : ViewModel() {

    val searchQueryState = TextFieldState()

    val currencyPagingData = searchQueryState.textAsFlow()
        .debounce(UtilConstants.DebounceTimeoutDuration)
        .flatMapLatest { query ->
            repo.getCurrencyListPaged(query)
        }.cachedIn(viewModelScope)
}