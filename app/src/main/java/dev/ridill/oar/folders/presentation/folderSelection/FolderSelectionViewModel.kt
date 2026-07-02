package dev.ridill.oar.folders.presentation.folderSelection

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.paging.cachedIn
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.core.domain.util.textAsFlow
import dev.ridill.oar.core.ui.navigation.FolderSelectionSheetRoute
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.folders.domain.repository.FolderListRepository
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest

@HiltViewModel(assistedFactory = FolderSelectionViewModel.Factory::class)
class FolderSelectionViewModel @AssistedInject constructor(
    @Assisted val route: FolderSelectionSheetRoute,
    private val savedStateHandle: SavedStateHandle,
    private val repo: FolderListRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(route: FolderSelectionSheetRoute): FolderSelectionViewModel
    }

    val searchQueryState = savedStateHandle.saveable(
        key = "SEARCH_QUERY_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )

    val selectedFolderId = savedStateHandle
        .getStateFlow<Long?>(SELECTED_FOLDER_ID, null)

    val folderListPaged = searchQueryState.textAsFlow()
        .debounce(UtilConstants.DEBOUNCE_TIMEOUT)
        .flatMapLatest {
            repo.getFoldersListPaged(it)
        }.cachedIn(viewModelScope)

    init {
        savedStateHandle[SELECTED_FOLDER_ID] = route.preselectedId.takeIf { it != INVALID_ID_LONG }
    }

    fun onFolderSelect(folderId: Long) {
        savedStateHandle[SELECTED_FOLDER_ID] = folderId
            .takeIf { it != selectedFolderId.value }
    }
}

private const val SELECTED_FOLDER_ID = "SELECTED_FOLDER_ID"
