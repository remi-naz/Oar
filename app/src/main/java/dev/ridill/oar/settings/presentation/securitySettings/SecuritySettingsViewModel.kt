package dev.ridill.oar.settings.presentation.securitySettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.oar.settings.domain.appLock.AppLockScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val appLockScheduler: AppLockScheduler,
    private val eventBus: EventBus<SecuritySettingsEvent>
) : ViewModel(), SecuritySettingsActions {

    private val preferences = preferencesManager.preferences
    private val appLockEnabled = preferences
        .mapLatest { it.appLockEnabled }
        .distinctUntilChanged()

    private val autoLockInterval = preferences
        .mapLatest { it.appAutoLockInterval }
        .distinctUntilChanged()

    private val screenSecurityEnabled = preferences
        .mapLatest { it.screenSecurityEnabled }
        .distinctUntilChanged()

    // The user grants this in system settings, so it is re-checked whenever the screen resumes
    private val canScheduleExactAlarms = MutableStateFlow(appLockScheduler.canScheduleExactAlarms())

    val state = combineTuple(
        appLockEnabled,
        autoLockInterval,
        screenSecurityEnabled,
        canScheduleExactAlarms
    ).mapLatest { (
                      appLockEnabled,
                      autoLockInterval,
                      screenSecurityEnabled,
                      canScheduleExactAlarms
                  ) ->
        SecuritySettingsState(
            appLockEnabled = appLockEnabled,
            autoLockInterval = autoLockInterval,
            screenSecurityEnabled = screenSecurityEnabled,
            exactAlarmPermissionRequired = appLockEnabled && !canScheduleExactAlarms
        )
    }.asStateFlow(viewModelScope, SecuritySettingsState())

    val events = eventBus.eventFlow

    fun refreshExactAlarmPermission() {
        canScheduleExactAlarms.value = appLockScheduler.canScheduleExactAlarms()
    }

    override fun onAppLockToggle(enabled: Boolean) {
        viewModelScope.launch {
            if (!enabled) {
                preferencesManager.updateAppLockEnabled(false)
                return@launch
            }
            eventBus.send(SecuritySettingsEvent.LaunchBiometricAuthentication)
        }
    }

    fun onAuthenticationSuccess() {
        viewModelScope.launch {
            preferencesManager.updateAppLockEnabled(true)
        }
    }

    override fun onAutoLockIntervalSelect(interval: AppAutoLockInterval) {
        viewModelScope.launch {
            preferencesManager.updateAppAutoLockInterval(interval)
        }
    }

    override fun onScreenSecurityToggle(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateScreenSecurityEnabled(enabled)
        }
    }

    override fun onExactAlarmPermissionClick() {
        viewModelScope.launch {
            eventBus.send(SecuritySettingsEvent.LaunchExactAlarmPermissionSettings)
        }
    }

    sealed interface SecuritySettingsEvent {
        data object LaunchBiometricAuthentication : SecuritySettingsEvent
        data object LaunchExactAlarmPermissionSettings : SecuritySettingsEvent
    }
}
