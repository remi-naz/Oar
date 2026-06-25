package dev.ridill.oar.folders.presentation.allFolders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.folders.domain.repository.FolderListRepository
import javax.inject.Inject

@HiltViewModel
class AllFoldersViewModel @Inject constructor(
    repo: FolderListRepository,
) : ViewModel() {
    val folderListPagingData = repo.getFoldersListPaged()
        .cachedIn(viewModelScope)
}