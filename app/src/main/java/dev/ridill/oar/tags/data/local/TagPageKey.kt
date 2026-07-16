package dev.ridill.oar.tags.data.local

import dev.ridill.oar.core.data.db.KeysetPageKey
import dev.ridill.oar.core.data.db.toSqliteUtcDateTimeString
import dev.ridill.oar.tags.data.local.entity.TagEntity

/**
 * Keyset cursor mirroring TagPagedQueryBuilder's ORDER BY (created_timestamp DESC, name ASC,
 * id ASC).
 */
data class TagPageKey(
    val createdTimestamp: String,
    val name: String,
    val id: Long
) : KeysetPageKey {
    override fun toValues(): List<Any> = listOf(createdTimestamp, name, id)
}

fun TagEntity.toPageKey(): TagPageKey = TagPageKey(
    createdTimestamp = createdTimestamp.toSqliteUtcDateTimeString(),
    name = name,
    id = id
)
