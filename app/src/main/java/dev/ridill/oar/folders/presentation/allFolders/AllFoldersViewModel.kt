package dev.ridill.oar.folders.presentation.allFolders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.folders.domain.model.FolderListItemUiModel
import dev.ridill.oar.folders.domain.repository.FolderListRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
internal class AllFoldersViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    repo: FolderListRepository,
) : ViewModel() {
    val showArchived = savedStateHandle
        .getStateFlow(SHOW_ARCHIVED, false)

    val folderListPagingData = showArchived
        .flatMapLatest { includeArchived ->
            repo.getFoldersListPaged(includeArchived = includeArchived)
        }
        .map { pagingData ->
            pagingData
                .map<_, FolderListItemUiModel> { FolderListItemUiModel.FolderItem(it) }
                .insertSeparators { before, after ->
                    val beforeArchived =
                        (before as? FolderListItemUiModel.FolderItem)?.folder?.isArchived
                    val afterArchived =
                        (after as? FolderListItemUiModel.FolderItem)?.folder?.isArchived
                    if (beforeArchived != true && afterArchived == true) FolderListItemUiModel.ArchivedSeparator
                    else null
                }
        }.cachedIn(viewModelScope)

    val archivedFoldersExist = repo.archivedFoldersExist()
        .asStateFlow(viewModelScope, false)

    fun onShowArchivedToggle(show: Boolean) {
        savedStateHandle[SHOW_ARCHIVED] = show
    }
}

private const val SHOW_ARCHIVED = "SHOW_ARCHIVED"
