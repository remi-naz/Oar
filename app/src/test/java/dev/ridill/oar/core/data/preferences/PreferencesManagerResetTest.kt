package dev.ridill.oar.core.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.common.truth.Truth.assertThat
import dev.ridill.oar.settings.domain.modal.AppTheme
import dev.ridill.oar.settings.domain.repositoty.FatalBackupError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.LocalDateTime

class PreferencesManagerResetTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun manager() = PreferencesManagerImpl(
        PreferenceDataStoreFactory.create(produceFile = { tmp.newFile("prefs.preferences_pb") })
    )

    @Test
    fun resetDataPreferences_clearsDataFieldsAndReenablesOnboarding_keepsAppLevelChoices() = runTest {
        val manager = manager()
        manager.updateAppThem(AppTheme.DARK)
        manager.updateAppLockEnabled(true)
        manager.updateScreenSecurityEnabled(true)
        manager.updateTransactionAutoDetectEnabled(true)
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

        assertThat(prefs.appTheme).isEqualTo(AppTheme.DARK)
        assertThat(prefs.appLockEnabled).isTrue()
        assertThat(prefs.screenSecurityEnabled).isTrue()
        assertThat(prefs.transactionAutoDetectEnabled).isTrue()
    }
}
