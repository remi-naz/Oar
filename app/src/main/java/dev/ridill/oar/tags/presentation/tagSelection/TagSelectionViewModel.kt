package dev.ridill.oar.tags.presentation.tagSelection

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.paging.TerminalSeparatorType
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.domain.util.textAsFlow
import dev.ridill.oar.core.ui.theme.SelectableColorsList
import dev.ridill.oar.tags.domain.model.TagSelectionEntry
import dev.ridill.oar.tags.domain.repository.TagsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagSelectionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: TagsRepository,
    private val eventBus: EventBus<TagSelectionEvent>,
) : ViewModel() {

    val searchQueryState = savedStateHandle.saveable(
        key = "SEARCH_QUERY_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )

    private val selectedIds = savedStateHandle
        .getStateFlow<Set<Long>>(SELECTED_IDS, emptySet())

    val tagsPagingData = combineTuple(
        searchQueryState.textAsFlow(),
        selectedIds,
    ).debounce(UtilConstants.DebounceTimeoutDuration)
        .flatMapLatest { (query, ignoreSet) ->
            repo.searchTagsForSelection(
                searchQuery = query,
                ignoreIds = ignoreSet
            ).map { pagingData ->
                pagingData
                    .map { TagSelectionEntry.Tag(it) }
                    .insertSeparators<TagSelectionEntry.Tag, TagSelectionEntry>(
                        terminalSeparatorType = TerminalSeparatorType.FULLY_COMPLETE
                    ) { before, after ->
                        if (query.isNotBlank()
                            && before == null
                            && after == null
                        ) TagSelectionEntry.NewTagIndicator(query.trim())
                        else null
                    }
            }
        }.cachedIn(viewModelScope)

    private val selectedTags = selectedIds
        .flatMapLatest { repo.getTagsListFlowByIds(it) }

    val state = combineTuple(
        selectedIds,
        selectedTags
    ).mapLatest { (
                      selectedIds,
                      selectedTags
                  ) ->
        TagSelectionState(
            selectedIds = selectedIds,
            selectedTags = selectedTags
        )
    }.asStateFlow(viewModelScope, TagSelectionState())

    val events = eventBus.eventFlow

    fun updateSelectedIds(ids: Set<Long>) {
        savedStateHandle[SELECTED_IDS] = ids
    }

    private var selectionChangeJob: Job? = null
    fun onTagSelect(id: Long, singleSelection: Boolean) {
        val newSet = if (singleSelection) setOf(id)
        else selectedIds.value + id
        selectionChangeJob?.cancel()
        selectionChangeJob = viewModelScope.launch {
            eventBus.send(TagSelectionEvent.TagSelectionChange(newSet))
            searchQueryState.clearText()
        }
    }

    fun onTagRemove(id: Long, singleSelection: Boolean) {
        val newSet = if (singleSelection) emptySet()
        else selectedIds.value - id
        selectionChangeJob?.cancel()
        selectionChangeJob = viewModelScope.launch {
            eventBus.send(TagSelectionEvent.TagSelectionChange(newSet))
        }
    }

    fun onNewTagClick(label: String, singleSelection: Boolean) = viewModelScope.launch {
        val savedId = repo.saveTag(
            id = OarDatabase.DEFAULT_ID_LONG,
            name = label.trim(),
            colorCode = SelectableColorsList.random().toArgb(),
            excluded = false,
            timestamp = DateUtil.now(),
        )
        onTagSelect(savedId, singleSelection)
    }

    sealed interface TagSelectionEvent {
        data class TagSelectionChange(val ids: Set<Long>) : TagSelectionEvent
    }
}

private const val SELECTED_IDS = "SELECTED_IDS"