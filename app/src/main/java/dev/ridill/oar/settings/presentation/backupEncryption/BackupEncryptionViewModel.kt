package dev.ridill.oar.settings.presentation.backupEncryption

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.core.data.preferences.security.SecurityPreferencesManager
import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.settings.domain.repositoty.BackupSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupEncryptionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val securityPreferencesManager: SecurityPreferencesManager,
    private val repo: BackupSettingsRepository,
    private val eventBus: EventBus<BackupEncryptionEvent>
) : ViewModel(), BackupEncryptionActions {

    val currentPasswordState = TextFieldState()
    val newPasswordState = TextFieldState()
    val confirmNewPasswordState = TextFieldState()

    private val showPasswordInput = savedStateHandle.getStateFlow(SHOW_PASSWORD_INPUT, false)

    private val hasExistingPassword = securityPreferencesManager.preferences
        .mapLatest { it.hasValidBackupEncryptionPassword }
        .distinctUntilChanged()

    private val isLoading = MutableStateFlow(false)
    private val isPasswordUpdateButtonLoading = MutableStateFlow(false)

    val state = combineTuple(
        showPasswordInput,
        hasExistingPassword,
        isLoading,
        isPasswordUpdateButtonLoading
    ).mapLatest { (
                      showPasswordInput,
                      hasExistingPassword,
                      isLoading,
                      isPasswordUpdateButtonLoading,
                  ) ->
        BackupEncryptionState(
            hasExistingPassword = hasExistingPassword,
            showPasswordInput = showPasswordInput,
            isLoading = isLoading,
            isPasswordUpdateButtonLoading = isPasswordUpdateButtonLoading
        )
    }.asStateFlow(viewModelScope, BackupEncryptionState())

    val events = eventBus.eventFlow

    override fun onUpdatePasswordClick() {
        savedStateHandle[SHOW_PASSWORD_INPUT] = true
    }

    override fun onForgotCurrentPasswordClick() {
        viewModelScope.launch {
            eventBus.send(BackupEncryptionEvent.LaunchBiometricAuthentication)
        }
    }

    fun onBiometricAuthSucceeded() = viewModelScope.launch {
        securityPreferencesManager.updateBackupEncryptionHash(
            hash = null,
            salt = null,
            scheme = EncryptionScheme.ARGON2_GCM
        )
    }

    override fun onPasswordInputDismiss() {
        savedStateHandle[SHOW_PASSWORD_INPUT] = false
        clearPasswordInputs()
    }

    override fun onPasswordUpdateConfirm() {
        viewModelScope.launch {
            isPasswordUpdateButtonLoading.update { true }
            val currentPassword = currentPasswordState.text.toString()
            val newPassword = newPasswordState.text.toString()
            val confirmNewPassword = confirmNewPasswordState.text.toString()

            if (hasExistingPassword.first()) {
                if (!repo.isCurrentPasswordMatch(currentPassword)) {
                    savedStateHandle[SHOW_PASSWORD_INPUT] = false
                    clearPasswordInputs()
                    eventBus.send(
                        BackupEncryptionEvent.ShowUiMessage(
                            UiText.StringResource(
                                R.string.error_incorrect_existing_encryption_password,
                                true
                            )
                        )
                    )
                    isPasswordUpdateButtonLoading.update { false }
                    return@launch
                }
            }

            if (newPassword != confirmNewPassword) {
                isPasswordUpdateButtonLoading.update { false }
                savedStateHandle[SHOW_PASSWORD_INPUT] = false
                clearPasswordInputs()
                eventBus.send(
                    BackupEncryptionEvent.ShowUiMessage(
                        UiText.StringResource(
                            R.string.error_passwords_do_not_match,
                            true
                        )
                    )
                )
                return@launch
            }

            repo.updateEncryptionPassword(newPassword)
            isPasswordUpdateButtonLoading.update { false }
            savedStateHandle[SHOW_PASSWORD_INPUT] = false
            eventBus.send(BackupEncryptionEvent.PasswordUpdated)
            clearPasswordInputs()
        }
    }

    private fun clearPasswordInputs() {
        currentPasswordState.clearText()
        newPasswordState.clearText()
        confirmNewPasswordState.clearText()
    }

    sealed interface BackupEncryptionEvent {
        data class ShowUiMessage(val message: UiText) : BackupEncryptionEvent
        data object PasswordUpdated : BackupEncryptionEvent
        data object LaunchBiometricAuthentication : BackupEncryptionEvent
    }
}

const val ENCRYPTION_PASSWORD_UPDATED = "ENCRYPTION_PASSWORD_UPDATED"

private const val SHOW_PASSWORD_INPUT = "SHOW_PASSWORD_INPUT"