package dev.ridill.oar.tags.presentation.allTags

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.SearchBarValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.domain.util.textAsFlow
import dev.ridill.oar.tags.domain.model.Tag
import dev.ridill.oar.tags.domain.repository.TagsRepository
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllTagsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: TagsRepository
) : ViewModel(), AllTagsActions {

    val searchQueryState = savedStateHandle.saveable(
        key = "SEARCH_QUERY_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )
    val allTagsPagingData = repo.getAllTagsPagingData()
        .cachedIn(viewModelScope)

    val searchResults = searchQueryState.textAsFlow()
        .debounce(UtilConstants.DebounceTimeoutDuration)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repo.searchTags(query = query).map { it.all }
        }
        .asStateFlow(viewModelScope, emptyList<Tag>())

    private val selectedIds = savedStateHandle.getStateFlow<Set<Long>>(SELECTED_IDS, emptySet())
    private val multiSelectionModeActive = selectedIds
        .mapLatest { it.isNotEmpty() }
        .distinctUntilChanged()

    private val showDeleteConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_CONFIRMATION, false)

    val state = combineTuple(
        multiSelectionModeActive,
        selectedIds,
        showDeleteConfirmation
    ).mapLatest { (
                      multiSelectionModeActive,
                      selectedIds,
                      showDeleteConfirmation
                  ) ->
        AllTagsState(
            multiSelectionModeActive = multiSelectionModeActive,
            selectedIds = selectedIds,
            showDeleteConfirmation = showDeleteConfirmation
        )
    }.asStateFlow(viewModelScope, AllTagsState())

    override fun onTagLongPress(id: Long) {
        savedStateHandle[SELECTED_IDS] = selectedIds.value + id
    }

    override fun onTagSelectionChange(id: Long) {
        val selectedIds = selectedIds.value
        savedStateHandle[SELECTED_IDS] = if (id in selectedIds) selectedIds - id
        else selectedIds + id
    }

    override fun onMultiSelectionModeDismiss() {
        savedStateHandle[SELECTED_IDS] = emptySet<Long>()
    }

    override fun onDeleteTagsClick() {
        savedStateHandle[SHOW_DELETE_CONFIRMATION] = true
    }

    override fun onDeleteDismiss() {
        savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
    }

    override fun onDeleteConfirm() {
        viewModelScope.launch {
            savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
            val selectedIds = selectedIds.value
            repo.deleteMultipleTagsByIds(selectedIds)
            savedStateHandle[SELECTED_IDS] = emptySet<Long>()
        }
    }

    override fun onSearchBarValueChange(value: SearchBarValue) {
        searchQueryState.clearText()
    }

    override fun onClearSearchQuery() {
        searchQueryState.clearText()
    }
}

private const val SELECTED_IDS = "SELECTED_IDS"
private const val SHOW_DELETE_CONFIRMATION = "SHOW_DELETE_CONFIRMATION"