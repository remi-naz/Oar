package dev.ridill.oar.settings.presentation.settings

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import dev.ridill.oar.account.domain.model.AuthState
import dev.ridill.oar.account.domain.repository.AuthRepository
import dev.ridill.oar.core.data.logging.LogFileManager
import dev.ridill.oar.core.domain.model.DataError
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.remoteConfig.FirebaseRemoteConfigService
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.settings.domain.modal.AppTheme
import dev.ridill.oar.settings.domain.repositoty.BackupSettingsRepository
import dev.ridill.oar.settings.domain.repositoty.DataResetRepository
import dev.ridill.oar.settings.domain.repositoty.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelLogoutTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val authRepo = mockk<AuthRepository>()
    private val dataResetRepo = mockk<DataResetRepository>()
    private val backupSettingsRepo = mockk<BackupSettingsRepository>()
    private val settingsRepo = mockk<SettingsRepository>(relaxed = true)
    private val remoteConfig = mockk<FirebaseRemoteConfigService>(relaxed = true)
    private val logFileManager = mockk<LogFileManager>(relaxed = true)

    private val lastBackup = MutableStateFlow<LocalDateTime?>(null)
    private val eventBus = EventBus<SettingsViewModel.SettingsEvent>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { authRepo.getAuthState() } returns flowOf(AuthState.UnAuthenticated)
        every { settingsRepo.getCurrentAppTheme() } returns flowOf(AppTheme.SYSTEM_DEFAULT)
        every { settingsRepo.getDynamicColorsEnabled() } returns flowOf(false)
        every { settingsRepo.getTransactionAutoDetectEnabled() } returns flowOf(false)
        every { backupSettingsRepo.getLastBackupTime() } returns lastBackup
        coEvery { authRepo.signUserOut() } returns Result.Success(Unit)
        coEvery { dataResetRepo.clearAllLocalData() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SettingsViewModel(
        savedStateHandle = SavedStateHandle(),
        settingsRepo = settingsRepo,
        authRepo = authRepo,
        backupSettingsRepo = backupSettingsRepo,
        dataResetRepo = dataResetRepo,
        remoteConfigService = remoteConfig,
        logFileManager = logFileManager,
        eventBus = eventBus
    )

    private fun signOutError() = Result.Error<Unit, DataError>(
        error = DataError.Network.entries.first(),
        message = UiText.DynamicString("error")
    )

    @Test
    fun logoutClick_opensChoiceDialog_withoutSigningOut() = runTest(testDispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        vm.onLogoutClick()

        assertThat(vm.state.value.logoutDialog).isEqualTo(LogoutDialog.CHOICE)
        coVerify(exactly = 0) { authRepo.signUserOut() }
    }

    @Test
    fun dismiss_closesDialog() = runTest(testDispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        vm.onLogoutClick()
        vm.onLogoutDialogDismiss()

        assertThat(vm.state.value.logoutDialog).isNull()
        coVerify(exactly = 0) { authRepo.signUserOut() }
    }

    @Test
    fun keepData_signsOutAndDoesNotClear() = runTest(testDispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        vm.onLogoutClick()
        vm.onLogoutKeepDataClick()

        coVerify(exactly = 1) { authRepo.signUserOut() }
        coVerify(exactly = 0) { dataResetRepo.clearAllLocalData() }
        assertThat(vm.state.value.logoutDialog).isNull()
    }

    @Test
    fun clearDataClick_opensConfirmation_withoutClearing() = runTest(testDispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        vm.onLogoutClick()
        vm.onLogoutClearDataClick()

        assertThat(vm.state.value.logoutDialog).isEqualTo(LogoutDialog.CONFIRM_CLEAR_DATA)
        coVerify(exactly = 0) { dataResetRepo.clearAllLocalData() }
    }

    @Test
    fun confirmClear_signsOutThenClears_andStartsOnboarding() = runTest(testDispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }
        val events = mutableListOf<SettingsViewModel.SettingsEvent>()
        backgroundScope.launch { vm.events.collect { events += it } }

        vm.onLogoutClearDataConfirm()

        coVerifyOrder {
            authRepo.signUserOut()
            dataResetRepo.clearAllLocalData()
        }
        assertThat(vm.state.value.isClearingLocalData).isFalse()
        assertThat(events).containsExactly(SettingsViewModel.SettingsEvent.StartOnboarding)
    }

    @Test
    fun confirmClear_signOutFails_keepsData() = runTest(testDispatcher) {
        coEvery { authRepo.signUserOut() } returns signOutError()
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }
        val events = mutableListOf<SettingsViewModel.SettingsEvent>()
        backgroundScope.launch { vm.events.collect { events += it } }

        vm.onLogoutClearDataConfirm()

        coVerify(exactly = 0) { dataResetRepo.clearAllLocalData() }
        assertThat(events).hasSize(1)
        assertThat(events.single()).isInstanceOf(SettingsViewModel.SettingsEvent.ShowUiMessage::class.java)
        assertThat(events).doesNotContain(SettingsViewModel.SettingsEvent.StartOnboarding)
        assertThat(vm.state.value.isClearingLocalData).isFalse()
    }

    @Test
    fun confirmClear_clearThrows_reportsErrorAndResetsProgress() = runTest(testDispatcher) {
        coEvery { dataResetRepo.clearAllLocalData() } throws IllegalStateException("boom")
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }
        val events = mutableListOf<SettingsViewModel.SettingsEvent>()
        backgroundScope.launch { vm.events.collect { events += it } }

        vm.onLogoutClearDataConfirm()

        assertThat(events).hasSize(1)
        assertThat(events.single()).isInstanceOf(SettingsViewModel.SettingsEvent.ShowUiMessage::class.java)
        assertThat(events).doesNotContain(SettingsViewModel.SettingsEvent.StartOnboarding)
        assertThat(vm.state.value.isClearingLocalData).isFalse()
    }

    @Test
    fun keepData_doesNotStartOnboarding() = runTest(testDispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }
        val events = mutableListOf<SettingsViewModel.SettingsEvent>()
        backgroundScope.launch { vm.events.collect { events += it } }

        vm.onLogoutKeepDataClick()

        assertThat(events).isEmpty()
    }
}
