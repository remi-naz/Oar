package dev.ridill.oar.tags.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.ridill.oar.core.data.db.FtsQueryFormatter
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.tags.data.local.TagPagedQueryBuilder
import dev.ridill.oar.tags.domain.model.Tag
import dev.ridill.oar.tags.domain.util.EditDistance
import dev.ridill.oar.tags.domain.util.FuzzyTagScorer
import dev.ridill.oar.tags.domain.util.TextNormalizer
import dev.ridill.oar.tags.domain.util.TrigramGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private fun trigramCountForTag(tagId: Long): Int {
        db.query("SELECT COUNT(*) FROM tag_trigram WHERE tagId = ?", arrayOf(tagId)).use { cursor ->
            cursor.moveToFirst()
            return cursor.getInt(0)
        }
    }

    @Test
    fun searchTags_withTypo_findsTag() = runBlocking {
        saveTag("Food Out")
        saveTag("Transport")

        val results = repository.searchTags("Food Put")

        assertThat(results.map(Tag::name)).contains("Food Out")
    }

    @Test
    fun searchTags_wordOrderIndependent() = runBlocking {
        saveTag("Food Out")

        val results = repository.searchTags("Out Food")

        assertThat(results.map(Tag::name)).contains("Food Out")
    }

    @Test
    fun searchTags_exactMatchRanksAboveFuzzyMatch() = runBlocking {
        saveTag("Food Out")
        saveTag("Fod Out")

        val results = repository.searchTags("Food Out")

        assertThat(results.map(Tag::name).first()).isEqualTo("Food Out")
    }

    @Test
    fun searchTags_unrelatedTagNotReturned() = runBlocking {
        saveTag("Food Out")
        saveTag("Transport")

        val results = repository.searchTags("Food Out")

        assertThat(results.map(Tag::name)).doesNotContain("Transport")
    }

    @Test
    fun searchTags_emptyQuery_returnsEmpty() = runBlocking {
        saveTag("Food Out")

        assertThat(repository.searchTags("")).isEmpty()
    }

    @Test
    fun searchTags_shortQuery_prefixOnlyNoFuzzy() = runBlocking {
        saveTag("Out")
        saveTag("Transport")

        // 2-char query: prefix match only, must not fuzzy-match "Transport" via loose scoring.
        val results = repository.searchTags("ou")

        assertThat(results.map(Tag::name)).containsExactly("Out")
        Unit
    }

    @Test
    fun searchTags_accentedTagMatchesUnaccentedQuery() = runBlocking {
        saveTag("Café")

        val results = repository.searchTags("cafe")

        assertThat(results.map(Tag::name)).contains("Café")
    }

    @Test
    fun saveTag_insert_createsTrigramRows() = runBlocking {
        val id = saveTag("Food Out")

        assertThat(trigramCountForTag(id)).isGreaterThan(0)
    }

    @Test
    fun saveTag_rename_updatesTrigramRows() = runBlocking {
        val id = saveTag("Food Out")
        val before = trigramCountForTag(id)

        repository.saveTag(id, "Groceries", 0, false, LocalDateTime.now())
        val results = repository.searchTags("Groceries")

        assertThat(results.map(Tag::name)).contains("Groceries")
        assertThat(repository.searchTags("Food Out").map(Tag::id)).doesNotContain(id)
        assertThat(before).isGreaterThan(0)
    }

    @Test
    fun deleteTag_removesTrigramRowsViaCascade() = runBlocking {
        val id = saveTag("Food Out")

        repository.deleteTagById(id)

        val remaining = db.query("SELECT COUNT(*) FROM tag_trigram WHERE tagId = ?", arrayOf(id))
        remaining.use {
            it.moveToFirst()
            assertThat(it.getInt(0)).isEqualTo(0)
        }
    }

    @Test
    fun rebuildTrigramIndex_reproducesSameRows() = runBlocking {
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
        repository.rebuildTrigramIndex()
        val after = snapshot()

        assertThat(after).isEqualTo(before)
    }
}
