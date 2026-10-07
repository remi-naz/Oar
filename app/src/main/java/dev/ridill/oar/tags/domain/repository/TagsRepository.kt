package dev.ridill.oar.tags.domain.repository

import androidx.paging.PagingData
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.tags.domain.model.Tag
import dev.ridill.oar.tags.domain.model.TagInfo
import dev.ridill.oar.tags.domain.model.TagSearchResult
import dev.ridill.oar.tags.domain.util.FuzzyConfig
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface TagsRepository {
    fun getAllTagsPagingData(
        searchQuery: String = String.Empty,
        limit: Int = OarDatabase.INVALID_LIMIT,
        requireNonBlankQuery: Boolean = false,
    ): Flow<PagingData<Tag>>

    fun getTagInfoPagingData(
        dateRange: Pair<LocalDate, LocalDate>?,
        limit: Int = UtilConstants.DEFAULT_TAG_LIST_LIMIT
    ): Flow<PagingData<TagInfo>>

    fun searchTagsForSelection(
        searchQuery: String = String.Empty,
        ignoreIds: Set<Long> = emptySet(),
        limit: Int = OarDatabase.INVALID_LIMIT
    ): Flow<PagingData<Tag>>

    suspend fun saveTag(
        id: Long,
        name: String,
        colorCode: Int,
        excluded: Boolean,
        timestamp: LocalDateTime
    ): Long

    suspend fun deleteTagById(id: Long)
    suspend fun deleteMultipleTagsByIds(ids: Set<Long>)
    suspend fun deleteTagWithTransactions(tagId: Long)
    suspend fun getTagById(id: Long): Tag?
    fun getTagsListFlowByIds(ids: Set<Long>): Flow<List<Tag>>

    /**
     * Typo-tolerant tag search: exact/prefix matches first, then fuzzy matches scored via
     * trigram candidates ranked in Kotlin (never scored against transaction_table). Reactive -
     * re-emits whenever a tag is added/renamed/deleted. Bounded, not paginated: [maxCandidates]
     * caps how many trigram candidates are scored, not how many results the UI can page through.
     */
    fun searchTags(
        query: String,
        ignoreIds: Set<Long> = emptySet(),
        maxCandidates: Int = FuzzyConfig.DEFAULT.candidateLimit,
        requireFirstCharMatch: Boolean = FuzzyConfig.DEFAULT.requireFirstCharMatch
    ): Flow<TagSearchResult>

    /** Clears and regenerates the entire trigram index from current tag names. */
    suspend fun rebuildTrigramIndex()
}