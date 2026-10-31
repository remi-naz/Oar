package dev.ridill.oar.core.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FolderArchiveMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        OarDatabase::class.java
    )

    @Test
    fun migrate12To13_existingFoldersAreNotArchived() {
        val dbName = "folder-archive-migration-test"

        helper.createDatabase(dbName, 12).apply {
            execSQL(
                "INSERT INTO folder_table (id, name, created_timestamp, is_excluded) " +
                    "VALUES (1, 'Trip', '2024-01-01T00:00:00Z', 0)"
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(dbName, 13, true)

        migrated.query("SELECT name, archived_timestamp FROM folder_table WHERE id = 1").use {
            assertThat(it.moveToFirst()).isTrue()
            assertThat(it.getString(0)).isEqualTo("Trip")
            assertThat(it.isNull(1)).isTrue()
        }
    }
}
