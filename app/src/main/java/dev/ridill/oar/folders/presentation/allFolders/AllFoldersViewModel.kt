package dev.ridill.oar.folders.presentation.allFolders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.folders.domain.repository.FolderListRepository
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class AllFoldersViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    repo: FolderListRepository,
) : ViewModel() {
    val showArchived = savedStateHandle
        .getStateFlow(SHOW_ARCHIVED, false)

    val folderListPagingData = showArchived
        .flatMapLatest { includeArchived ->
            repo.getFoldersListPaged(includeArchived = includeArchived)
        }
        .cachedIn(viewModelScope)

    fun onShowArchivedToggle(show: Boolean) {
        savedStateHandle[SHOW_ARCHIVED] = show
    }
}

private const val SHOW_ARCHIVED = "SHOW_ARCHIVED"
