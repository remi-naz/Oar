package dev.ridill.oar.folders.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.ridill.oar.core.data.db.OarDatabase
import java.time.LocalDateTime

@Entity(tableName = "folder_table")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = OarDatabase.DEFAULT_ID_LONG,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "created_timestamp")
    val createdTimestamp: LocalDateTime,

    @ColumnInfo(name = "is_excluded")
    val isExcluded: Boolean,

    @ColumnInfo(name = "archived_timestamp")
    val archivedTimestamp: LocalDateTime? = null
)