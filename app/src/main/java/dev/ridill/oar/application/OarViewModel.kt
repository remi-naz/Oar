package dev.ridill.oar.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.remoteConfig.FirebaseRemoteConfigService
import dev.ridill.oar.core.domain.service.ReceiverService
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.settings.domain.appInit.AppInitWorkManager
import dev.ridill.oar.di.ApplicationScope
import dev.ridill.oar.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.oar.settings.domain.appLock.AppLockScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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
    private val appLockScheduler: AppLockScheduler,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val appInitWorkManager: AppInitWorkManager,
    private val remoteConfigService: FirebaseRemoteConfigService,
    cycleRepo: BudgetCycleRepository,
    private val eventBus: EventBus<OarEvent>
) : ViewModel() {
    private val preferences = preferencesManager.preferences

    // null until the preference is loaded, so the splash can be held and the start destination resolved
    val showOnboarding = preferences
        .mapLatest { it.showOnboarding }
        .distinctUntilChanged()
        .asStateFlow(viewModelScope, null)

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
        initRemoteConfig()
        startCycleCatchUp()
    }

    // Safety net for a lost cycle alarm; no-op while the active cycle is still running
    private fun startCycleCatchUp() {
        appInitWorkManager.startCycleCatchUpWorker()
        appInitWorkManager.scheduleDailyCycleCatchUp()
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

    /**
     * App moved to the background: lock right away or arm the auto lock alarm.
     * Uses the application scope so the lock still lands if the activity is being torn down.
     */
    fun onAppStop() {
        applicationScope.launch {
            val preferences = preferences.first()
            if (!preferences.appLockEnabled || preferences.isAppLocked) {
                appLockScheduler.cancel()
                return@launch
            }

            if (preferences.appAutoLockInterval == AppAutoLockInterval.IMMEDIATELY) {
                appLockScheduler.cancel()
                preferencesManager.updateAppLocked(true)
            } else {
                appLockScheduler.schedule(preferences.appAutoLockInterval)
            }
        }
    }

    /**
     * App is visible again: the pending auto lock alarm no longer applies.
     * The alarm may have fired late or been lost, so lock here if the interval already ran
     * out, then ask for authentication if the app is locked.
     */
    fun onAppStart() = viewModelScope.launch {
        refreshRemoteConfigIfRequested()
        val deadlinePassed = appLockScheduler.hasDeadlinePassed()
        appLockScheduler.cancel()
        val preferences = preferences.first()
        if (!preferences.appLockEnabled) return@launch

        if (deadlinePassed && !preferences.isAppLocked) {
            preferencesManager.updateAppLocked(true)
        }
        if (deadlinePassed || preferences.isAppLocked) {
            eventBus.send(OarEvent.LaunchBiometricAuthentication)
        }
    }

    fun onAppLockAuthSucceeded() = viewModelScope.launch {
        preferencesManager.updateAppLocked(false)
    }

    fun updateAppLockErrorMessage(message: UiText?) {
        appLockAuthErrorMessage.update { message }
    }

    fun startConfigRestore() = appInitWorkManager.startConfigRestoreWorkers()

    private var remoteConfigJob: Job? = null

    private fun initRemoteConfig() {
        remoteConfigJob = viewModelScope.launch {
            if (preferences.first().remoteConfigForceRefreshRequired) {
                forceRefreshRemoteConfig()
                return@launch
            }
            remoteConfigService.fetch()
            remoteConfigService.activate()
        }
    }

    /**
     * Picks up a refresh requested by a push while the app was in the background.
     * Skipped while another remote config run is in progress, e.g. on launch or after the
     * activity is recreated, so runs never overlap.
     */
    private fun refreshRemoteConfigIfRequested() {
        if (remoteConfigJob?.isActive == true) return
        remoteConfigJob = viewModelScope.launch {
            if (!preferences.first().remoteConfigForceRefreshRequired) return@launch
            forceRefreshRemoteConfig()
        }
    }

    // The flag is cleared only after a successful fetch, so a failed attempt retries next launch
    private suspend fun forceRefreshRemoteConfig() {
        // Interval 0 bypasses the fetch throttle; activate so the new values apply right away
        val result = remoteConfigService.fetch(interval = 0)
        if (result !is Result.Success) return
        remoteConfigService.activate()
        preferencesManager.updateRemoteConfigForceRefreshRequired(false)
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