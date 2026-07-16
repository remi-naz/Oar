package dev.ridill.oar.tags.data.local

import dev.ridill.oar.core.data.db.KeysetPagingSource
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.tags.data.local.entity.TagEntity
import kotlinx.coroutines.CoroutineScope

/**
 * Keyset-paginated alternative to Room's generated OFFSET-based PagingSource, shared by the
 * general tag list and the tag-selection search. [limit], when not [OarDatabase.INVALID_LIMIT],
 * caps the total rows ever returned across pages - mirroring the bounded-list behavior the
 * previous LIMIT-clause-based PagingSource supported.
 */
class TagPagingSource(
    private val dao: TagsDao,
    db: OarDatabase,
    applicationScope: CoroutineScope,
    private val query: String,
    private val requireNonBlankQuery: Boolean,
    private val idIgnoreSet: Set<Long>?,
    private val limit: Int
) : KeysetPagingSource<TagPageKey, TagEntity>(
    db = db,
    applicationScope = applicationScope,
    invalidationTables = setOf("tag_table")
) {

    private var totalLoaded = 0

    override fun resolveLoadSize(requested: Int): Int =
        if (limit == OarDatabase.INVALID_LIMIT) requested
        else (limit - totalLoaded).coerceAtMost(requested)

    override fun onLoaded(rowCount: Int) {
        totalLoaded += rowCount
    }

    override suspend fun fetch(
        cursor: TagPageKey?,
        direction: PageLoadDirection,
        loadSize: Int
    ): List<TagEntity> {
        val rawQuery = TagPagedQueryBuilder.build(
            query = query,
            requireNonBlankQuery = requireNonBlankQuery,
            idIgnoreSet = idIgnoreSet,
            cursor = cursor,
            direction = direction,
            limit = loadSize
        )
        return dao.getTagsPagedRaw(rawQuery)
    }

    override fun keyOf(value: TagEntity): TagPageKey = value.toPageKey()
}
