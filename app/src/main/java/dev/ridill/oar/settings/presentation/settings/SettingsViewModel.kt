package dev.ridill.oar.settings.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.account.domain.repository.AuthRepository
import dev.ridill.oar.account.presentation.util.CredentialService
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.domain.util.logE
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.settings.domain.modal.AppTheme
import dev.ridill.oar.settings.domain.repositoty.BackupSettingsRepository
import dev.ridill.oar.settings.domain.repositoty.DataResetRepository
import dev.ridill.oar.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val settingsRepo: SettingsRepository,
    private val authRepo: AuthRepository,
    private val backupSettingsRepo: BackupSettingsRepository,
    private val dataResetRepo: DataResetRepository,
    private val eventBus: EventBus<SettingsEvent>
) : ViewModel(), SettingsActions {

    private val authState = authRepo.getAuthState()

    private val appTheme = settingsRepo.getCurrentAppTheme()
    private val dynamicColorsEnabled = settingsRepo.getDynamicColorsEnabled()

    private val showAppThemeSelection = savedStateHandle
        .getStateFlow(SHOW_APP_THEME_SELECTION, false)

    private val logoutDialog = savedStateHandle
        .getStateFlow<String?>(LOGOUT_DIALOG, null)
        .mapLatest { name -> LogoutDialog.entries.firstOrNull { it.name == name } }
        .distinctUntilChanged()

    private val lastBackupDateTime = backupSettingsRepo.getLastBackupTime()

    private val isClearingLocalData = MutableStateFlow(false)

    val state = combineTuple(
        authState,
        appTheme,
        dynamicColorsEnabled,
        showAppThemeSelection,
        logoutDialog,
        lastBackupDateTime,
        isClearingLocalData
    ).mapLatest { (
                      authState,
                      appTheme,
                      dynamicColorsEnabled,
                      showAppThemeSelection,
                      logoutDialog,
                      lastBackupDateTime,
                      isClearingLocalData
                  ) ->
        SettingsState(
            authState = authState,
            appTheme = appTheme,
            dynamicColorsEnabled = dynamicColorsEnabled,
            showAppThemeSelection = showAppThemeSelection,
            logoutDialog = logoutDialog,
            lastBackupDateTime = lastBackupDateTime,
            isClearingLocalData = isClearingLocalData
        )
    }
        .onStart { settingsRepo.refreshCurrentDate() }
        .asStateFlow(viewModelScope, SettingsState())

    val events = eventBus.eventFlow

    override fun onAppThemePreferenceClick() {
        savedStateHandle[SHOW_APP_THEME_SELECTION] = true
    }

    override fun onAppThemeSelectionDismiss() {
        savedStateHandle[SHOW_APP_THEME_SELECTION] = false
    }

    override fun onAppThemeSelectionConfirm(appTheme: AppTheme) {
        viewModelScope.launch {
            savedStateHandle[SHOW_APP_THEME_SELECTION] = false
            settingsRepo.updateAppTheme(appTheme)
        }
    }

    override fun onDynamicThemeEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.toggleDynamicColors(enabled)
        }
    }

    private var loginJob: Job? = null
    override fun onLoginClick() {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            eventBus.send(SettingsEvent.StartManualSignInFlow)
        }
    }

    fun onCredentialResult(
        result: Result<String, CredentialService.CredentialError>
    ) = viewModelScope.launch {
        when (result) {
            is Result.Error -> {
                when (result.error) {
                    CredentialService.CredentialError.NO_AUTHORIZED_CREDENTIAL -> {
                        eventBus.send(SettingsEvent.StartManualSignInFlow)
                    }

                    CredentialService.CredentialError.CREDENTIAL_PROCESS_FAILED -> eventBus.send(
                        SettingsEvent.ShowUiMessage(result.message)
                    )
                }
            }

            is Result.Success -> {
                signInUserWithIdToken(result.data)
            }
        }
    }

    private suspend fun signInUserWithIdToken(idToken: String) {
        when (val result = authRepo.signUserInWithToken(idToken)) {
            is Result.Error -> {
                eventBus.send(SettingsEvent.ShowUiMessage(result.message))
            }

            is Result.Success -> {
                eventBus.send(SettingsEvent.ShowUiMessage(UiText.StringResource(R.string.sign_in_success)))
            }
        }
    }

    override fun onLogoutClick() {
        savedStateHandle[LOGOUT_DIALOG] = LogoutDialog.CHOICE.name
    }

    override fun onLogoutDialogDismiss() {
        savedStateHandle[LOGOUT_DIALOG] = null
    }

    override fun onLogoutKeepDataClick() {
        savedStateHandle[LOGOUT_DIALOG] = null
        startLogout()
    }

    override fun onLogoutClearDataClick() {
        savedStateHandle[LOGOUT_DIALOG] = LogoutDialog.CONFIRM_CLEAR_DATA.name
    }

    override fun onLogoutClearDataConfirm() {
        savedStateHandle[LOGOUT_DIALOG] = null
        startLogout(clearLocalData = true)
    }

    private var logoutJob: Job? = null
    private fun startLogout(clearLocalData: Boolean = false) {
        logoutJob?.cancel()
        logoutJob = viewModelScope.launch {
            // Sign-out and clear must not be interrupted halfway, e.g. by leaving the screen
            withContext(NonCancellable) {
                try {
                    if (clearLocalData) isClearingLocalData.update { true }
                    if (authRepo.signUserOut() is Result.Error) {
                        eventBus.send(
                            SettingsEvent.ShowUiMessage(
                                UiText.StringResource(R.string.error_sign_out_failed, true)
                            )
                        )
                        // Keep the data if the account is still signed in
                        return@withContext
                    }
                    if (clearLocalData) clearLocalData()
                } finally {
                    isClearingLocalData.update { false }
                }
            }
        }
    }

    private suspend fun clearLocalData() {
        return try {
            dataResetRepo.clearAllLocalData()
        } catch (t: Throwable) {
            t.rethrowIfCoroutineCancellation()
            logE(t) { "Failed to clear local data" }
            eventBus.send(
                SettingsEvent.ShowUiMessage(
                    UiText.StringResource(R.string.error_clear_local_data_failed, true)
                )
            )
        }
    }

    sealed interface SettingsEvent {
        data class ShowUiMessage(val uiText: UiText) : SettingsEvent
        data object StartManualSignInFlow : SettingsEvent
    }
}

private const val SHOW_APP_THEME_SELECTION = "SHOW_APP_THEME_SELECTION"
private const val LOGOUT_DIALOG = "LOGOUT_DIALOG"
