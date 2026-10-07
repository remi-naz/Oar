package dev.ridill.oar.core.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dev.ridill.oar.tags.domain.util.TextNormalizer
import dev.ridill.oar.tags.domain.util.TrigramGenerator
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TagTrigramMigrationTest {

    private val trigramGenerator = TrigramGenerator(TextNormalizer())

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        OarDatabase::class.java,
        listOf(TagTrigramBackfillSpec())
    )

    @Test
    fun migrate11To12_backfillsTrigramsForExistingTags() {
        val dbName = "tag-trigram-migration-test"
        val tags = listOf(
            1L to "Food Out",
            2L to "Transport",
            3L to "Café"
        )

        helper.createDatabase(dbName, 11).apply {
            tags.forEach { (id, name) ->
                execSQL(
                    "INSERT INTO tag_table (id, name, color_code, created_timestamp, is_excluded) " +
                        "VALUES ($id, '$name', 0, '2024-01-01T00:00:00Z', 0)"
                )
            }
            close()
        }

        val migrated = helper.runMigrationsAndValidate(dbName, 12, true)

        tags.forEach { (id, name) ->
            val expected = trigramGenerator.forName(name)
            val actual = mutableSetOf<String>()
            migrated.query("SELECT trigram FROM tag_trigram WHERE tagId = $id").use { cursor ->
                while (cursor.moveToNext()) actual.add(cursor.getString(0))
            }
            assertThat(actual).isEqualTo(expected)
        }
    }
}
