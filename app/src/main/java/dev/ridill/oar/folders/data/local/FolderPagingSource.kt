package dev.ridill.oar.folders.data.local

import dev.ridill.oar.core.data.db.KeysetPagingSource
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.folders.data.local.entity.FolderEntity
import kotlinx.coroutines.CoroutineScope

/**
 * Keyset-paginated alternative to Room's generated OFFSET-based PagingSource, mirroring
 * TransactionPagingSource so cost shrinks with the remaining unseen rows instead of staying
 * constant on every page load.
 */
class FolderPagingSource(
    private val dao: FolderDao,
    db: OarDatabase,
    applicationScope: CoroutineScope,
    private val query: String
) : KeysetPagingSource<FolderPageKey, FolderEntity>(
    db = db,
    applicationScope = applicationScope,
    invalidationTables = setOf("folder_table")
) {

    override suspend fun fetch(
        cursor: FolderPageKey?,
        direction: PageLoadDirection,
        loadSize: Int
    ): List<FolderEntity> {
        val rawQuery = FolderPagedQueryBuilder.build(
            query = query,
            cursor = cursor,
            direction = direction,
            limit = loadSize
        )
        return dao.getFoldersPagedRaw(rawQuery)
    }

    override fun keyOf(value: FolderEntity): FolderPageKey = value.toPageKey()
}
