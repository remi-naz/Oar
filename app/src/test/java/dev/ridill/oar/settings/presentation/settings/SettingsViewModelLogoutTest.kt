package dev.ridill.oar.settings.presentation.settings

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import dev.ridill.oar.core.domain.model.DataError
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.fakes.FakeAuthRepository
import dev.ridill.oar.fakes.FakeBackupSettingsRepository
import dev.ridill.oar.fakes.FakeDataResetRepository
import dev.ridill.oar.fakes.FakeSettingsRepository
import dev.ridill.oar.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelLogoutTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepo = FakeAuthRepository()
    private var signOutsBeforeClear: Int? = null
    private val dataResetRepo = FakeDataResetRepository(
        onClear = { signOutsBeforeClear = authRepo.signOutCount }
    )

    private val eventBus = EventBus<SettingsViewModel.SettingsEvent>()

    private fun viewModel() = SettingsViewModel(
        savedStateHandle = SavedStateHandle(),
        settingsRepo = FakeSettingsRepository(),
        authRepo = authRepo,
        backupSettingsRepo = FakeBackupSettingsRepository(),
        dataResetRepo = dataResetRepo,
        eventBus = eventBus
    )

    private fun signOutError() = Result.Error<Unit, DataError>(
        error = DataError.Network.entries.first(),
        message = UiText.DynamicString("error")
    )

    @Test
    fun logoutClick_opensChoiceDialog_withoutSigningOut() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        vm.onLogoutClick()

        assertThat(vm.state.value.logoutDialog).isEqualTo(LogoutDialog.CHOICE)
        assertThat(authRepo.signOutCount).isEqualTo(0)
    }

    @Test
    fun dismiss_closesDialog() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        vm.onLogoutClick()
        vm.onLogoutDialogDismiss()

        assertThat(vm.state.value.logoutDialog).isNull()
        assertThat(authRepo.signOutCount).isEqualTo(0)
    }

    @Test
    fun keepData_signsOutAndDoesNotClear() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }
        val events = mutableListOf<SettingsViewModel.SettingsEvent>()
        backgroundScope.launch { vm.events.collect { events += it } }

        vm.onLogoutClick()
        vm.onLogoutKeepDataClick()

        assertThat(authRepo.signOutCount).isEqualTo(1)
        assertThat(dataResetRepo.clearCount).isEqualTo(0)
        assertThat(vm.state.value.logoutDialog).isNull()
        assertThat(events).isEmpty()
    }

    @Test
    fun clearDataClick_opensConfirmation_withoutClearing() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        vm.onLogoutClick()
        vm.onLogoutClearDataClick()

        assertThat(vm.state.value.logoutDialog).isEqualTo(LogoutDialog.CONFIRM_CLEAR_DATA)
        assertThat(dataResetRepo.clearCount).isEqualTo(0)
    }

    @Test
    fun confirmClear_signsOutThenClears() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }
        val events = mutableListOf<SettingsViewModel.SettingsEvent>()
        backgroundScope.launch { vm.events.collect { events += it } }

        vm.onLogoutClearDataConfirm()

        assertThat(authRepo.signOutCount).isEqualTo(1)
        assertThat(dataResetRepo.clearCount).isEqualTo(1)
        assertThat(signOutsBeforeClear).isEqualTo(1)
        assertThat(vm.state.value.isClearingLocalData).isFalse()
        assertThat(events).isEmpty()
    }

    @Test
    fun confirmClear_signOutFails_keepsData() = runTest(mainDispatcherRule.testDispatcher) {
        authRepo.signOutResult = signOutError()
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }
        val events = mutableListOf<SettingsViewModel.SettingsEvent>()
        backgroundScope.launch { vm.events.collect { events += it } }

        vm.onLogoutClearDataConfirm()

        assertThat(dataResetRepo.clearCount).isEqualTo(0)
        assertThat(events).hasSize(1)
        assertThat(events.single()).isInstanceOf(SettingsViewModel.SettingsEvent.ShowUiMessage::class.java)
        assertThat(vm.state.value.isClearingLocalData).isFalse()
    }

    @Test
    fun confirmClear_clearThrows_reportsErrorAndResetsProgress() = runTest(mainDispatcherRule.testDispatcher) {
        dataResetRepo.failure = IllegalStateException("boom")
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }
        val events = mutableListOf<SettingsViewModel.SettingsEvent>()
        backgroundScope.launch { vm.events.collect { events += it } }

        vm.onLogoutClearDataConfirm()

        assertThat(events).hasSize(1)
        assertThat(events.single()).isInstanceOf(SettingsViewModel.SettingsEvent.ShowUiMessage::class.java)
        assertThat(vm.state.value.isClearingLocalData).isFalse()
    }
}
