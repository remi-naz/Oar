package dev.ridill.oar.folders.data.repository

import dev.ridill.oar.folders.data.local.FolderDao
import dev.ridill.oar.folders.data.local.entity.FolderEntity
import dev.ridill.oar.folders.data.toFolder
import dev.ridill.oar.folders.domain.model.Folder
import dev.ridill.oar.folders.domain.repository.AddEditFolderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class AddEditFolderRepositoryImpl(
    private val dao: FolderDao
) : AddEditFolderRepository {
    override suspend fun getFolderDetails(id: Long): Folder? = withContext(Dispatchers.IO) {
        dao.getFolderById(id)?.toFolder()
    }

    override suspend fun saveFolder(
        id: Long,
        name: String,
        timestamp: LocalDateTime,
        excluded: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val entity = FolderEntity(
            id = id,
            name = name,
            createdTimestamp = timestamp,
            isExcluded = excluded,
            archivedTimestamp = dao.getFolderById(id)?.archivedTimestamp
        )
        dao.upsert(entity).first()
    }
}