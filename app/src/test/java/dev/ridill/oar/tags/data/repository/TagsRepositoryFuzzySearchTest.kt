package dev.ridill.oar.tags.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.ridill.oar.core.data.db.FtsQueryFormatter
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.tags.data.local.TagPagedQueryBuilder
import dev.ridill.oar.tags.domain.model.Tag
import dev.ridill.oar.tags.domain.model.TagSearchResult
import dev.ridill.oar.tags.domain.util.EditDistance
import dev.ridill.oar.tags.domain.util.FuzzyTagScorer
import dev.ridill.oar.tags.domain.util.TextNormalizer
import dev.ridill.oar.tags.domain.util.TrigramGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TagsRepositoryFuzzySearchTest {

    private lateinit var db: OarDatabase
    private lateinit var repository: TagsRepositoryImpl

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            OarDatabase::class.java
        ).allowMainThreadQueries().build()

        val textNormalizer = TextNormalizer()
        repository = TagsRepositoryImpl(
            dao = db.tagsDao(),
            trigramDao = db.tagTrigramDao(),
            queryBuilder = TagPagedQueryBuilder(FtsQueryFormatter()),
            textNormalizer = textNormalizer,
            trigramGenerator = TrigramGenerator(textNormalizer),
            fuzzyTagScorer = FuzzyTagScorer(EditDistance()),
            db = db,
            applicationScope = CoroutineScope(Dispatchers.Unconfined)
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun saveTag(name: String): Long = runBlocking {
        repository.saveTag(
            id = OarDatabase.DEFAULT_ID_LONG,
            name = name,
            colorCode = 0,
            excluded = false,
            timestamp = LocalDateTime.now()
        )
    }

    private fun search(
        query: String,
        ignoreIds: Set<Long> = emptySet(),
        maxCandidates: Int = 100
    ): TagSearchResult = runBlocking {
        repository.searchTags(query, ignoreIds, maxCandidates).first()
    }

    private fun trigramCountForTag(tagId: Long): Int {
        db.query("SELECT COUNT(*) FROM tag_trigram WHERE tagId = ?", arrayOf(tagId)).use { cursor ->
            cursor.moveToFirst()
            return cursor.getInt(0)
        }
    }

    @Test
    fun searchTags_withTypo_findsTag() {
        saveTag("Food Out")
        saveTag("Transport")

        val result = search("Food Put")

        assertThat(result.all.map(Tag::name)).contains("Food Out")
    }

    @Test
    fun searchTags_wordOrderIndependent() {
        saveTag("Food Out")

        val result = search("Out Food")

        assertThat(result.all.map(Tag::name)).contains("Food Out")
    }

    @Test
    fun searchTags_exactMatchRanksAboveFuzzyMatch() {
        saveTag("Food Out")
        saveTag("Fod Out")

        val result = search("Food Out")

        assertThat(result.all.map(Tag::name).first()).isEqualTo("Food Out")
    }

    @Test
    fun searchTags_unrelatedTagNotReturned() {
        saveTag("Food Out")
        saveTag("Transport")

        val result = search("Food Out")

        assertThat(result.all.map(Tag::name)).doesNotContain("Transport")
    }

    @Test
    fun searchTags_emptyQuery_returnsEmpty() {
        saveTag("Food Out")

        val result = search("")

        assertThat(result.all).isEmpty()
    }

    @Test
    fun searchTags_shortQuery_prefixOnlyNoFuzzy() {
        saveTag("Out")
        saveTag("Transport")

        // 2-char query: prefix match only, must not fuzzy-match "Transport" via loose scoring.
        val result = search("ou")

        assertThat(result.all.map(Tag::name)).containsExactly("Out")
    }

    @Test
    fun searchTags_accentedTagMatchesUnaccentedQuery() {
        saveTag("Café")

        val result = search("cafe")

        assertThat(result.all.map(Tag::name)).contains("Café")
    }

    @Test
    fun searchTags_exactMatch_hasExactMatchIsTrue() {
        saveTag("Food Out")

        val result = search("Food Out")

        assertThat(result.hasExactMatch).isTrue()
        assertThat(result.exactMatches.map(Tag::name)).contains("Food Out")
    }

    @Test
    fun searchTags_onlyFuzzyMatch_hasExactMatchIsFalse() {
        saveTag("Food Out")

        val result = search("Food Put")

        assertThat(result.hasExactMatch).isFalse()
        assertThat(result.exactMatches).isEmpty()
        assertThat(result.fuzzyMatches.map(Tag::name)).contains("Food Out")
    }

    @Test
    fun searchTags_noMatch_hasExactMatchIsFalseAndAllIsEmpty() {
        saveTag("Transport")

        val result = search("Food Out")

        assertThat(result.hasExactMatch).isFalse()
        assertThat(result.all).isEmpty()
    }

    @Test
    fun searchTags_ignoreIds_excludesSelectedTagFromBothBuckets() {
        val exactId = saveTag("Food Out")
        val fuzzyId = saveTag("Fod Out")

        val result = search("Food Out", ignoreIds = setOf(exactId, fuzzyId))

        assertThat(result.all.map(Tag::id)).doesNotContain(exactId)
        assertThat(result.all.map(Tag::id)).doesNotContain(fuzzyId)
    }

    @Test
    fun searchTags_maxCandidates_capsFuzzyMatchCount() {
        // All typo'd variants of "Food Out" so every one is a fuzzy candidate, none exact.
        val names = listOf("Fod Out", "Food Ouf", "Fod Ouf", "Food Oot", "Foodd Out")
        names.forEach { saveTag(it) }

        val result = search("Food Out", maxCandidates = 2)

        assertThat(result.fuzzyMatches.size).isAtMost(2)
    }

    @Test
    fun saveTag_insert_createsTrigramRows() {
        val id = saveTag("Food Out")

        assertThat(trigramCountForTag(id)).isGreaterThan(0)
    }

    @Test
    fun saveTag_rename_updatesTrigramRows() {
        val id = saveTag("Food Out")
        val before = trigramCountForTag(id)

        runBlocking { repository.saveTag(id, "Groceries", 0, false, LocalDateTime.now()) }

        assertThat(search("Groceries").all.map(Tag::name)).contains("Groceries")
        assertThat(search("Food Out").all.map(Tag::id)).doesNotContain(id)
        assertThat(before).isGreaterThan(0)
    }

    @Test
    fun deleteTag_removesTrigramRowsViaCascade() {
        val id = saveTag("Food Out")

        runBlocking { repository.deleteTagById(id) }

        val remaining = db.query("SELECT COUNT(*) FROM tag_trigram WHERE tagId = ?", arrayOf(id))
        remaining.use {
            it.moveToFirst()
            assertThat(it.getInt(0)).isEqualTo(0)
        }
    }

    @Test
    fun rebuildTrigramIndex_reproducesSameRows() {
        saveTag("Food Out")
        saveTag("Transport")

        fun snapshot(): Set<Pair<String, Long>> {
            val rows = mutableSetOf<Pair<String, Long>>()
            db.query("SELECT trigram, tagId FROM tag_trigram", emptyArray()).use { cursor ->
                while (cursor.moveToNext()) rows.add(cursor.getString(0) to cursor.getLong(1))
            }
            return rows
        }

        val before = snapshot()
        runBlocking { repository.rebuildTrigramIndex() }
        val after = snapshot()

        assertThat(after).isEqualTo(before)
    }
}
