package dev.ridill.oar.folders.domain.repository

import androidx.paging.PagingData
import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.folders.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderListRepository {
    fun getFoldersListPaged(searchQuery: String = String.Empty): Flow<PagingData<Folder>>
}