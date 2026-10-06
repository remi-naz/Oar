package dev.ridill.oar.tags.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.ridill.oar.tags.data.local.entity.TagEntity
import dev.ridill.oar.tags.data.local.entity.TagTrigramEntity

data class TagTrigramCandidate(
    @Embedded val tag: TagEntity,
    val hits: Int
)

@Dao
interface TagTrigramDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(rows: List<TagTrigramEntity>)

    @Query("DELETE FROM tag_trigram WHERE tagId = :tagId")
    suspend fun deleteForTag(tagId: Long)

    @Query("DELETE FROM tag_trigram")
    suspend fun clearAll()

    @Query(
        """
        SELECT t.*, c.hits as hits FROM tag_table t
        JOIN (
            SELECT tagId, COUNT(*) AS hits
            FROM tag_trigram
            WHERE trigram IN (:trigrams)
            GROUP BY tagId
            ORDER BY hits DESC
            LIMIT :limit
        ) c ON t.id = c.tagId
        ORDER BY c.hits DESC
        """
    )
    suspend fun findCandidatesByTrigrams(trigrams: List<String>, limit: Int): List<TagTrigramCandidate>
}
