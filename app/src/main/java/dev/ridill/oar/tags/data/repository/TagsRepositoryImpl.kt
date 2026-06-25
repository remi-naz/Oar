package dev.ridill.oar.tags.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.tags.data.local.TagsDao
import dev.ridill.oar.tags.data.local.entity.TagEntity
import dev.ridill.oar.tags.data.toTag
import dev.ridill.oar.tags.data.toTagInfo
import dev.ridill.oar.tags.domain.model.Tag
import dev.ridill.oar.tags.domain.model.TagInfo
import dev.ridill.oar.tags.domain.repository.TagsRepository
import dev.ridill.oar.transactions.data.local.relation.TagAndAggregateRelation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class TagsRepositoryImpl(
    private val dao: TagsDao
) : TagsRepository {
    override fun getAllTagsPagingData(
        searchQuery: String,
        limit: Int
    ): Flow<PagingData<Tag>> = Pager(
        config = PagingConfig(UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = {
            dao.getAllTagsPaged(
                query = searchQuery,
                limit = limit
            )
        }
    ).flow
        .mapLatest { pagingData -> pagingData.map(TagEntity::toTag) }

    override fun getTagInfoPagingData(
        dateRange: Pair<LocalDate, LocalDate>?,
        limit: Int
    ): Flow<PagingData<TagInfo>> = Pager(
        config = PagingConfig(UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = {
            dao.getTagAndAggregatePaged(
                startDate = dateRange?.first,
                endDate = dateRange?.second,
                limit = limit
            )
        }
    ).flow
        .mapLatest { pagingData -> pagingData.map(TagAndAggregateRelation::toTagInfo) }

    override fun searchTagsForSelection(
        searchQuery: String,
        ignoreIds: Set<Long>,
        limit: Int
    ): Flow<PagingData<Tag>> = Pager(
        config = PagingConfig(UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = {
            dao.searchTagsForSelection(
                query = searchQuery,
                idIgnoreSet = ignoreIds,
                limit = limit
            )
        }
    ).flow
        .mapLatest { pagingData -> pagingData.map(TagEntity::toTag) }

    override suspend fun getTagById(id: Long): Tag? = withContext(Dispatchers.IO) {
        dao.getTagById(id)?.toTag()
    }

    override fun getTagsListFlowByIds(ids: Set<Long>): Flow<List<Tag>> =
        dao.getTagsByIdFlow(ids).mapLatest { entities -> entities.map(TagEntity::toTag) }

    override suspend fun saveTag(
        id: Long,
        name: String,
        colorCode: Int,
        excluded: Boolean,
        timestamp: LocalDateTime
    ): Long = withContext(Dispatchers.IO) {
        val entity = TagEntity(
            id = id,
            name = name,
            colorCode = colorCode,
            createdTimestamp = timestamp,
            isExcluded = excluded
        )

        dao.upsert(entity).first()
    }

    override suspend fun deleteTagById(id: Long) = withContext(Dispatchers.IO) {
        dao.untagTransactionsAndDeleteTag(id)
    }

    override suspend fun deleteMultipleTagsByIds(ids: Set<Long>) = withContext(Dispatchers.IO) {
        dao.untagTransactionsAndDeleteTags(ids)
    }

    override suspend fun deleteTagWithTransactions(tagId: Long) = withContext(Dispatchers.IO) {
        dao.deleteTagWithTransactions(tagId)
    }
}