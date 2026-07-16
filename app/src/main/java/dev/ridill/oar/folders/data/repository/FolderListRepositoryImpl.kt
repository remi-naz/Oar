package dev.ridill.oar.folders.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.di.ApplicationScope
import dev.ridill.oar.folders.data.local.FolderDao
import dev.ridill.oar.folders.data.local.FolderPagingSource
import dev.ridill.oar.folders.data.local.entity.FolderEntity
import dev.ridill.oar.folders.data.toFolder
import dev.ridill.oar.folders.domain.model.Folder
import dev.ridill.oar.folders.domain.repository.FolderListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

class FolderListRepositoryImpl(
    private val folderDao: FolderDao,
    private val db: OarDatabase,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : FolderListRepository {
    override fun getFoldersListPaged(searchQuery: String): Flow<PagingData<Folder>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = {
            FolderPagingSource(
                dao = folderDao,
                db = db,
                applicationScope = applicationScope,
                query = searchQuery
            )
        }
    ).flow
        .mapLatest { it.map(FolderEntity::toFolder) }
}