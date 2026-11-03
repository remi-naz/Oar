package dev.ridill.oar.folders.data.local

import dev.ridill.oar.core.data.db.KeysetPageKey
import dev.ridill.oar.core.data.db.toSqliteUtcDateTimeString
import dev.ridill.oar.folders.data.local.entity.FolderEntity

/**
 * Keyset cursor mirroring FolderPagedQueryBuilder's ORDER BY (archived ASC, name ASC, created_timestamp DESC,
 * id ASC).
 */
data class FolderPageKey(
    val archived: Long,
    val name: String,
    val createdTimestamp: String,
    val id: Long
) : KeysetPageKey {
    override fun toValues(): List<Any> = listOf(archived, name, createdTimestamp, id)
}

fun FolderEntity.toPageKey(): FolderPageKey = FolderPageKey(
    archived = if (archivedTimestamp != null) 1L else 0L,
    name = name,
    createdTimestamp = createdTimestamp.toSqliteUtcDateTimeString(),
    id = id
)
