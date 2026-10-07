package dev.ridill.oar.tags.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Candidate index for fuzzy tag search - trigrams of every tag's normalized name, scored against
 * the query in Kotlin ([dev.ridill.oar.tags.domain.util.FuzzyTagScorer]). Never joined against
 * transaction_table; this only narrows candidates among tags.
 */
@Entity(
    tableName = "tag_trigram",
    primaryKeys = ["trigram", "tagId"],
    indices = [Index("trigram"), Index("tagId")],
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TagTrigramEntity(
    val trigram: String,
    val tagId: Long
)
