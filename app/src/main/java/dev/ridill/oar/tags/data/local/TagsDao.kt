package dev.ridill.oar.tags.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Transaction
import dev.ridill.oar.core.data.db.BaseDao
import dev.ridill.oar.tags.data.local.entity.TagEntity
import dev.ridill.oar.transactions.data.local.relation.TagAndAggregateRelation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TagsDao : BaseDao<TagEntity> {
    @RawQuery
    suspend fun getTagsPagedRaw(query: RoomRawQuery): List<TagEntity>

    @Transaction
    @Query(
        """
        SELECT tg.id as id, tg.name as name,
            tg.color_code as colorCode,
            tg.is_excluded as excluded,
            tg.created_timestamp as createdTimestamp,
            IFNULL(SUM(
                CASE
                    WHEN tx.type = 'OUT' THEN tx.amount
                    WHEN tx.type = 'IN' THEN -tx.amount
                END
                ), 0) as aggregate
        FROM tag_table tg
        JOIN transaction_table tx ON (tg.id = tx.tag_id
            AND tx.is_excluded = 0
            AND ((:startDate IS NULL OR :endDate IS NULL) OR DATE(tx.timestamp) BETWEEN DATE(:startDate) AND DATE(:endDate)))
        GROUP BY tg.id
        ORDER BY aggregate DESC, DATETIME(tg.created_timestamp) DESC, tg.name ASC
        LIMIT :limit
    """
    )
    fun getTagAndAggregatePaged(
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Int
    ): PagingSource<Int, TagAndAggregateRelation>

    @Query("SELECT * FROM tag_table WHERE id = :id")
    suspend fun getTagById(id: Long): TagEntity?

    @Query("SELECT * FROM tag_table WHERE id IN (:ids)")
    fun getTagsByIdFlow(ids: Set<Long>): Flow<List<TagEntity>>

    @Transaction
    suspend fun untagTransactionsAndDeleteTag(id: Long) {
        untagTransactionsByTag(id)
        deleteTagById(id)
    }

    @Transaction
    suspend fun untagTransactionsAndDeleteTags(ids: Set<Long>) {
        untagTransactionsByTags(ids)
        deleteTagsByIds(ids)
    }

    @Transaction
    suspend fun deleteTagWithTransactions(id: Long) {
        deleteTransactionsByTag(id)
        deleteTagById(id)
    }

    @Query("UPDATE transaction_table SET tag_id = NULL WHERE tag_id = :id")
    suspend fun untagTransactionsByTag(id: Long)

    @Query("UPDATE transaction_table SET tag_id = NULL WHERE tag_id IN (:ids)")
    suspend fun untagTransactionsByTags(ids: Set<Long>)

    @Query("DELETE FROM tag_table WHERE id = :id")
    suspend fun deleteTagById(id: Long)

    @Query("DELETE FROM tag_table WHERE id IN (:ids)")
    suspend fun deleteTagsByIds(ids: Set<Long>)

    @Query("DELETE FROM transaction_table WHERE tag_id = :id")
    suspend fun deleteTransactionsByTag(id: Long)
}