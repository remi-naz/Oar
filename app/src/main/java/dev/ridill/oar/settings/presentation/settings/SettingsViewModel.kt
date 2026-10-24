package dev.ridill.oar.settings.presentation.settings

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.account.domain.repository.AuthRepository
import dev.ridill.oar.account.presentation.util.CredentialService
import dev.ridill.oar.core.data.logging.LogFileManager
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.remoteConfig.FirebaseRemoteConfigService
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
    private val remoteConfigService: FirebaseRemoteConfigService,
    private val logFileManager: LogFileManager,
    private val eventBus: EventBus<SettingsEvent>
) : ViewModel(), SettingsActions {

    private val authState = authRepo.getAuthState()

    private val appTheme = settingsRepo.getCurrentAppTheme()
    private val dynamicColorsEnabled = settingsRepo.getDynamicColorsEnabled()

    private val _transactionAutoDetectFeatureEnabled = MutableStateFlow(false)
    private val _isValidTxAutoDetectPatternsAvailable = MutableStateFlow(false)
    private val showAutoDetectTxOption = combineTuple(
        _transactionAutoDetectFeatureEnabled,
        _isValidTxAutoDetectPatternsAvailable
    ).mapLatest { it.first && it.second }
        .distinctUntilChanged()
    private val transactionAutoDetectEnabled = settingsRepo.getTransactionAutoDetectEnabled()

    private val showAppThemeSelection = savedStateHandle
        .getStateFlow(SHOW_APP_THEME_SELECTION, false)

    private val showSmsPermissionRationale = savedStateHandle
        .getStateFlow(SHOW_SMS_PERMISSION_RATIONALE, false)

    private val showTransactionAutoDetectInfo = savedStateHandle
        .getStateFlow(SHOW_AUTO_DETECT_TX_INFO, false)

    private val _appSourceCodeUrl = MutableStateFlow<String?>(null)

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
        showAutoDetectTxOption,
        transactionAutoDetectEnabled,
        showSmsPermissionRationale,
        showTransactionAutoDetectInfo,
        _appSourceCodeUrl,
        logoutDialog,
        lastBackupDateTime,
        isClearingLocalData
    ).mapLatest { (
                      authState,
                      appTheme,
                      dynamicColorsEnabled,
                      showAppThemeSelection,
                      showAutoDetectTxOption,
                      autoAddTransactionEnabled,
                      showSmsPermissionRationale,
                      showTransactionAutoDetectInfo,
                      appSourceCodeUrl,
                      logoutDialog,
                      lastBackupDateTime,
                      isClearingLocalData
                  ) ->
        SettingsState(
            authState = authState,
            appTheme = appTheme,
            dynamicColorsEnabled = dynamicColorsEnabled,
            showAppThemeSelection = showAppThemeSelection,
            showAutoDetectTxOption = showAutoDetectTxOption,
            autoDetectTransactionEnabled = autoAddTransactionEnabled,
            showSmsPermissionRationale = showSmsPermissionRationale,
            showAutoDetectTransactionFeatureInfo = showTransactionAutoDetectInfo,
            sourceCodeUrl = appSourceCodeUrl,
            logoutDialog = logoutDialog,
            lastBackupDateTime = lastBackupDateTime,
            isClearingLocalData = isClearingLocalData
        )
    }
        .onStart { settingsRepo.refreshCurrentDate() }
        .onStart { refreshConfigs() }
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

    override fun onToggleAutoAddTransactions(enabled: Boolean) {
        viewModelScope.launch {
            savedStateHandle[TEMP_AUTO_ADD_TRANSACTION_STATE] = enabled
            if (settingsRepo.getShowTransactionAutoDetectInfoValue() && enabled) {
                savedStateHandle[SHOW_AUTO_DETECT_TX_INFO] = true
            } else {
                eventBus.send(SettingsEvent.RequestSMSPermission)
            }
        }
    }

    override fun onAutoDetectTxFeatureInfoDismiss() {
        savedStateHandle[SHOW_AUTO_DETECT_TX_INFO] = false
    }

    override fun onAutoDetectTxFeatureInfoAcknowledge() {
        viewModelScope.launch {
            settingsRepo.toggleShowAutoDetectTxInfoFalse()
            savedStateHandle[SHOW_AUTO_DETECT_TX_INFO] = false
            eventBus.send(SettingsEvent.RequestSMSPermission)
        }
    }

    fun onSmsPermissionResult(granted: Boolean) = viewModelScope.launch {
        if (granted) {
            val enabled = savedStateHandle.get<Boolean?>(TEMP_AUTO_ADD_TRANSACTION_STATE) == true
            settingsRepo.toggleAutoDetectTransactions(enabled)
            savedStateHandle[TEMP_AUTO_ADD_TRANSACTION_STATE] = null
        }
        savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = !granted
    }

    override fun onSmsPermissionRationaleDismiss() {
        savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = false
    }

    override fun onSmsPermissionRationaleSettingsClick() {
        viewModelScope.launch {
            savedStateHandle[SHOW_SMS_PERMISSION_RATIONALE] = false
            eventBus.send(SettingsEvent.LaunchAppSettings)
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

    private var exportLogsJob: Job? = null
    override fun onExportLogsClick() {
        exportLogsJob?.cancel()
        exportLogsJob = viewModelScope.launch {
            try {
                val logsUri = logFileManager.exportLogs()
                eventBus.send(SettingsEvent.ShareLogs(logsUri))
            } catch (t: Throwable) {
                t.rethrowIfCoroutineCancellation()
                logE(t) { "Failed to export logs" }
                eventBus.send(
                    SettingsEvent.ShowUiMessage(
                        UiText.StringResource(R.string.error_export_logs_failed, true)
                    )
                )
            }
        }
    }

    private fun refreshConfigs() = viewModelScope.launch {
        val config = remoteConfigService.getConfig()
        _appSourceCodeUrl.update { config.sourceCodeUrl }
        _transactionAutoDetectFeatureEnabled.update { config.transactionAutoDetectFeatureEnabled }
        _isValidTxAutoDetectPatternsAvailable.update { config.autoDetectTransactionRegexPatterns != null }
    }

    sealed interface SettingsEvent {
        data class ShowUiMessage(val uiText: UiText) : SettingsEvent
        data object RequestSMSPermission : SettingsEvent
        data object LaunchAppSettings : SettingsEvent
        data object StartManualSignInFlow : SettingsEvent
        data class ShareLogs(val uri: Uri) : SettingsEvent
    }
}

private const val SHOW_APP_THEME_SELECTION = "SHOW_APP_THEME_SELECTION"
private const val SHOW_SMS_PERMISSION_RATIONALE = "SHOW_SMS_PERMISSION_RATIONALE"
private const val TEMP_AUTO_ADD_TRANSACTION_STATE = "TEMP_AUTO_ADD_TRANSACTION_STATE"
private const val SHOW_AUTO_DETECT_TX_INFO = "SHOW_AUTO_DETECT_TX_INFO"
private const val LOGOUT_DIALOG = "LOGOUT_DIALOG"
