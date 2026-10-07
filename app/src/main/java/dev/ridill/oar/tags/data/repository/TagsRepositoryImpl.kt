package dev.ridill.oar.tags.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.di.ApplicationScope
import dev.ridill.oar.tags.data.local.TagPagedQueryBuilder
import dev.ridill.oar.tags.data.local.TagPagingSource
import dev.ridill.oar.tags.data.local.TagTrigramDao
import dev.ridill.oar.tags.data.local.TagsDao
import dev.ridill.oar.tags.data.local.entity.TagEntity
import dev.ridill.oar.tags.data.local.entity.TagTrigramEntity
import dev.ridill.oar.tags.data.toTag
import dev.ridill.oar.tags.data.toTagInfo
import dev.ridill.oar.tags.domain.model.Tag
import dev.ridill.oar.tags.domain.model.TagInfo
import dev.ridill.oar.tags.domain.model.TagSearchResult
import dev.ridill.oar.tags.domain.repository.TagsRepository
import dev.ridill.oar.tags.domain.util.FuzzyConfig
import dev.ridill.oar.tags.domain.util.FuzzyTagScorer
import dev.ridill.oar.tags.domain.util.TextNormalizer
import dev.ridill.oar.tags.domain.util.TrigramGenerator
import dev.ridill.oar.transactions.data.local.relation.TagAndAggregateRelation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

internal class TagsRepositoryImpl(
    private val dao: TagsDao,
    private val trigramDao: TagTrigramDao,
    private val queryBuilder: TagPagedQueryBuilder,
    private val textNormalizer: TextNormalizer,
    private val trigramGenerator: TrigramGenerator,
    private val fuzzyTagScorer: FuzzyTagScorer,
    private val db: OarDatabase,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : TagsRepository {
    override fun getAllTagsPagingData(
        searchQuery: String,
        limit: Int,
        requireNonBlankQuery: Boolean
    ): Flow<PagingData<Tag>> = Pager(
        config = PagingConfig(UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = {
            TagPagingSource(
                dao = dao,
                queryBuilder = queryBuilder,
                db = db,
                applicationScope = applicationScope,
                query = searchQuery,
                requireNonBlankQuery = requireNonBlankQuery,
                idIgnoreSet = null,
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
            TagPagingSource(
                dao = dao,
                queryBuilder = queryBuilder,
                db = db,
                applicationScope = applicationScope,
                query = searchQuery,
                requireNonBlankQuery = true,
                idIgnoreSet = ignoreIds.takeIf { it.isNotEmpty() },
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

        db.withTransaction {
            val insertedId = dao.upsert(entity).first()
            // Upsert returns -1 for the update branch (no row was inserted) - the entity's own
            // id is already the real one in that case, so fall back to it.
            val tagId = if (id != OarDatabase.DEFAULT_ID_LONG) id else insertedId
            syncTrigramsForTag(tagId, name)
            tagId
        }
    }

    private suspend fun syncTrigramsForTag(tagId: Long, name: String) {
        trigramDao.deleteForTag(tagId)
        val trigrams = trigramGenerator.forName(name)
        if (trigrams.isNotEmpty()) {
            trigramDao.insertAll(trigrams.map { TagTrigramEntity(trigram = it, tagId = tagId) })
        }
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

    override fun searchTags(
        query: String,
        ignoreIds: Set<Long>,
        maxCandidates: Int,
        requireFirstCharMatch: Boolean
    ): Flow<TagSearchResult> = db.invalidationTracker
        .createFlow(TAG_TABLE, TAG_TRIGRAM_TABLE, emitInitialState = true)
        .mapLatest { fetchSearchResult(query, ignoreIds, maxCandidates, requireFirstCharMatch) }
        .flowOn(Dispatchers.IO)

    private suspend fun fetchSearchResult(
        query: String,
        ignoreIds: Set<Long>,
        maxCandidates: Int,
        requireFirstCharMatch: Boolean
    ): TagSearchResult {
        val normalized = textNormalizer.normalize(query)
        if (normalized.isBlank()) return TagSearchResult.EMPTY

        val exactMatches = fetchExactOrPrefixMatches(query, ignoreIds).map(TagEntity::toTag)
        if (normalized.length < FuzzyConfig.DEFAULT.minQueryLengthForFuzzy) {
            return TagSearchResult(exactMatches = exactMatches, fuzzyMatches = emptyList())
        }

        val exactIds = exactMatches.mapTo(mutableSetOf(), Tag::id)
        val queryTokens = textNormalizer.tokenizeNormalized(normalized)
        val fuzzyMatches = withContext(Dispatchers.Default) {
            val trigrams = trigramGenerator.forTokens(queryTokens)
            fetchCandidates(trigrams, maxCandidates)
                .filter { it.id !in exactIds && it.id !in ignoreIds }
                .mapNotNull { entity ->
                    fuzzyTagScorer.score(
                        queryTokens = queryTokens,
                        tagTokens = textNormalizer.tokenize(entity.name),
                        requireFirstCharMatch = requireFirstCharMatch
                    )
                        ?.takeIf { it >= FuzzyConfig.DEFAULT.minScore }
                        ?.let { score -> entity to score }
                }
                .sortedByDescending { it.second }
                .map { it.first.toTag() }
        }

        return TagSearchResult(exactMatches = exactMatches, fuzzyMatches = fuzzyMatches)
    }

    private suspend fun fetchExactOrPrefixMatches(
        query: String,
        ignoreIds: Set<Long>
    ): List<TagEntity> {
        val rawQuery = queryBuilder.build(
            query = query,
            requireNonBlankQuery = true,
            idIgnoreSet = ignoreIds.takeIf { it.isNotEmpty() },
            cursor = null,
            direction = PageLoadDirection.FORWARD,
            limit = EXACT_MATCH_LIMIT
        )
        return dao.getTagsPagedRaw(rawQuery)
    }

    private suspend fun fetchCandidates(
        trigrams: Set<String>,
        maxCandidates: Int
    ): List<TagEntity> {
        if (trigrams.isEmpty()) return emptyList()
        val chunks = trigrams.toList().chunked(SQLITE_MAX_VARIABLES_PER_QUERY)
        if (chunks.size == 1) {
            return trigramDao.findCandidatesByTrigrams(chunks.first(), maxCandidates)
                .map { it.tag }
        }

        // Each chunk's hit count is partial (only trigrams in that chunk), so it can't be capped
        // to maxCandidates per chunk - a tag split across chunks could rank outside every chunk's
        // local top-N yet still belong in the true top-N once hits are summed below. Only the
        // final merged ranking may be capped.
        val hitsByTagId = mutableMapOf<Long, Int>()
        val tagsById = mutableMapOf<Long, TagEntity>()
        chunks.forEach { chunk ->
            trigramDao.findCandidatesByTrigrams(chunk, Int.MAX_VALUE).forEach { candidate ->
                hitsByTagId[candidate.tag.id] =
                    (hitsByTagId[candidate.tag.id] ?: 0) + candidate.hits
                tagsById[candidate.tag.id] = candidate.tag
            }
        }
        return hitsByTagId.entries
            .sortedByDescending { it.value }
            .take(maxCandidates)
            .mapNotNull { tagsById[it.key] }
    }

    override suspend fun rebuildTrigramIndex() = withContext(Dispatchers.IO) {
        db.withTransaction {
            trigramDao.clearAll()
            dao.getAllTagsSync().forEach { tag ->
                val trigrams = trigramGenerator.forName(tag.name)
                if (trigrams.isNotEmpty()) {
                    trigramDao.insertAll(
                        trigrams.map {
                            TagTrigramEntity(
                                trigram = it,
                                tagId = tag.id
                            )
                        }
                    )
                }
            }
        }
    }

    private companion object {
        const val EXACT_MATCH_LIMIT = 50
        const val SQLITE_MAX_VARIABLES_PER_QUERY = 900
        const val TAG_TABLE = "tag_table"
        const val TAG_TRIGRAM_TABLE = "tag_trigram"
    }
}