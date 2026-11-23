package dev.ridill.oar.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.core.FileStorage
import androidx.datastore.preferences.core.PreferencesFileSerializer
import com.google.common.truth.Truth.assertThat
import dev.ridill.oar.settings.domain.modal.AppTheme
import dev.ridill.oar.settings.domain.repositoty.FatalBackupError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.LocalDateTime

class PreferencesManagerResetTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    private fun manager() = PreferencesManagerImpl(
        DataStore.Builder(
            storage = FileStorage(
                serializer = PreferencesFileSerializer,
                produceFile = { File(tmp.root, "prefs.preferences_pb") }
            ),
            context = dataStoreScope.coroutineContext
        ).build()
    )

    @Test
    fun resetDataPreferences_clearsDataAndSecurityFieldsAndReenablesOnboarding_keepsAppearance() = runTest {
        val manager = manager()
        manager.updateAppThem(AppTheme.DARK)
        manager.updateAppLockEnabled(true)
        manager.updateScreenSecurityEnabled(true)
        manager.concludeOnboarding()
        manager.updateLastBackupTimestamp(LocalDateTime.of(2026, 1, 1, 10, 0))
        manager.updateFatalBackupError(FatalBackupError.entries.first())
        manager.updateAllTransactionsShowExcludedOption(false)

        manager.resetDataPreferences()

        val prefs = manager.preferences.first()
        assertThat(prefs.lastBackupDateTime).isNull()
        assertThat(prefs.fatalBackupError).isNull()
        assertThat(prefs.allTransactionsShowExcludedOption).isTrue()
        assertThat(prefs.showOnboarding).isTrue()
        // Security toggles are reset along with the data they protected
        assertThat(prefs.appLockEnabled).isFalse()
        assertThat(prefs.screenSecurityEnabled).isFalse()

        assertThat(prefs.appTheme).isEqualTo(AppTheme.DARK)
    }
}
