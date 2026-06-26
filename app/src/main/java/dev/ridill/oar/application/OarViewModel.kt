package dev.ridill.oar.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.domain.remoteConfig.FirebaseRemoteConfigService
import dev.ridill.oar.core.domain.service.ReceiverService
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.settings.domain.appInit.AppInitWorkManager
import dev.ridill.oar.settings.domain.appLock.AppLockServiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OarViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val receiverService: ReceiverService,
    private val appLockServiceManager: AppLockServiceManager,
    private val appInitWorkManager: AppInitWorkManager,
    private val remoteConfigService: FirebaseRemoteConfigService,
    cycleRepo: BudgetCycleRepository,
    private val eventBus: EventBus<OarEvent>
) : ViewModel() {
    private val preferences = preferencesManager.preferences

    val showOnboarding = preferences
        .mapLatest { it.showOnboarding }
        .distinctUntilChanged()
        .asStateFlow(viewModelScope, false)

    val appTheme = preferences.mapLatest { it.appTheme }
        .distinctUntilChanged()

    val dynamicThemeEnabled = preferences.mapLatest { it.dynamicColorsEnabled }
        .distinctUntilChanged()

    val isAppLocked = preferences
        .mapLatest { it.isAppLocked }
        .distinctUntilChanged()
        .asStateFlow(viewModelScope, false)

    val appLockAuthErrorMessage = MutableStateFlow<UiText?>(null)

    val screenSecurityEnabled = preferences.mapLatest { it.screenSecurityEnabled }
        .distinctUntilChanged()

    private val activeCycle = cycleRepo.getActiveCycleFlow()
    val currencyPreference = activeCycle
        .mapLatest { it?.currency ?: LocaleUtil.defaultCurrency }
        .distinctUntilChanged()

    val events = eventBus.eventFlow

    init {
        collectTransactionAutoDetectEnabled()
        collectIsAppLocked()
        initRemoteConfig()
    }

    private fun collectTransactionAutoDetectEnabled() = viewModelScope.launch {
        preferences.mapLatest { it.transactionAutoDetectEnabled }
            .distinctUntilChanged()
            .collectLatest { enabled ->
                receiverService.toggleSmsReceiver(enabled)
            }
    }

    fun onSmsPermissionCheck(granted: Boolean) = viewModelScope.launch {
        if (!granted) {
            preferencesManager.updateTransactionAutoDetectEnabled(false)
        }
    }

    fun onNotificationPermissionCheck(granted: Boolean) {
        receiverService.toggleNotificationActionReceivers(granted)
    }

    private fun collectIsAppLocked() = viewModelScope.launch {
        preferences
            .mapLatest { Pair(it.appLockEnabled, it.isAppLocked) }
            .distinctUntilChanged()
            .collectLatest { (appLockedEnabled, isLocked) ->
                if (!appLockedEnabled || isLocked) {
                    appLockServiceManager.stopAppUnlockedIndicator()
                } else {
                    appLockServiceManager.startAppUnlockedIndicator()
                }
            }
    }

    fun startAppAutoLockTimerIfApplicable() = viewModelScope.launch {
        val preferences = preferences.first()
        if (!preferences.appLockEnabled) {
            // Stop the entire service because app lock not enabled.
            appLockServiceManager.stopAppUnlockedIndicator()
            return@launch
        }
        if (preferences.isAppLocked) {
            // Stop the timer because app is locked.
            appLockServiceManager.stopAppLockTimer()
            return@launch
        }

        appLockServiceManager.startAppAutoLockTimer()
    }

    fun startAppUnlockOrServiceStop() = viewModelScope.launch {
        val preferences = preferences.first()
        if (!preferences.appLockEnabled) {
            appLockServiceManager.stopAppUnlockedIndicator()
            return@launch
        }
        if (preferences.isAppLocked) {
            eventBus.send(OarEvent.LaunchBiometricAuthentication)
        } else {
            appLockServiceManager.stopAppLockTimer()
        }
    }

    fun onAppLockAuthSucceeded() = viewModelScope.launch {
        preferencesManager.updateAppLocked(false)
    }

    fun updateAppLockErrorMessage(message: UiText?) {
        appLockAuthErrorMessage.update { message }
    }

    fun startConfigRestore() = appInitWorkManager.startConfigRestoreWorkers()

    private fun initRemoteConfig() = viewModelScope.launch {
        remoteConfigService.activate()
        remoteConfigService.fetch()
    }

    fun refreshTransactionAutoDetectFeatureEnabledState() = viewModelScope.launch {
        val isTransactionAutoDetectEnabled = remoteConfigService.getConfig()
            .transactionAutoDetectFeatureEnabled
        if (isTransactionAutoDetectEnabled) {
            preferencesManager.updateTransactionAutoDetectEnabled(false)
        }
    }

    sealed class OarEvent {
        data object LaunchBiometricAuthentication : OarEvent()
    }
}