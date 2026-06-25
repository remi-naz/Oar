package dev.ridill.oar.folders.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.folders.data.local.FolderDao
import dev.ridill.oar.folders.data.local.entity.FolderEntity
import dev.ridill.oar.folders.data.toFolder
import dev.ridill.oar.folders.domain.model.Folder
import dev.ridill.oar.folders.domain.repository.FolderListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

class FolderListRepositoryImpl(
    private val folderDao: FolderDao
) : FolderListRepository {
    override fun getFoldersListPaged(searchQuery: String): Flow<PagingData<Folder>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = { folderDao.getFoldersPaged(searchQuery) }
    ).flow
        .mapLatest { it.map(FolderEntity::toFolder) }
}