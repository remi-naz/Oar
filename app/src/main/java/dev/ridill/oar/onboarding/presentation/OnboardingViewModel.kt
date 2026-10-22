package dev.ridill.oar.onboarding.presentation

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.work.WorkInfo
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.account.domain.model.AuthState
import dev.ridill.oar.account.domain.repository.AuthRepository
import dev.ridill.oar.account.presentation.util.AuthorizationService
import dev.ridill.oar.account.presentation.util.CredentialService
import dev.ridill.oar.budgetCycles.domain.model.CycleDurationUnit
import dev.ridill.oar.budgetCycles.domain.model.CycleStartDay
import dev.ridill.oar.budgetCycles.domain.model.CycleStartDayType
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.domain.util.logD
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.onboarding.domain.model.DataRestoreState
import dev.ridill.oar.onboarding.domain.model.OnboardingPage
import dev.ridill.oar.onboarding.domain.model.SignInAndDataRestoreState
import dev.ridill.oar.settings.domain.appInit.AppInitWorkManager
import dev.ridill.oar.settings.domain.backup.BackupWorkManager
import dev.ridill.oar.settings.domain.modal.BackupDetails
import dev.ridill.oar.settings.domain.repositoty.BackupRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.Currency
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val eventBus: EventBus<OnboardingEvent>,
    private val backupWorkManager: BackupWorkManager,
    private val preferencesManager: PreferencesManager,
    private val backupRepo: BackupRepository,
    private val authRepo: AuthRepository,
    private val cycleRepo: BudgetCycleRepository,
    appInitWorkManager: AppInitWorkManager,
) : ViewModel(), OnboardingActions {

    val signInAndDataRestoreState = savedStateHandle
        .getStateFlow(SIGN_IN_AND_DATA_RESTORE_STATE, SignInAndDataRestoreState.SIGN_IN)
    private val authState = authRepo.getAuthState()
    private val _dataRestoreState = MutableStateFlow(DataRestoreState.IDLE)
    private val dataRestoreState get() = _dataRestoreState.asStateFlow()
    private val showEncryptionPasswordInput = savedStateHandle
        .getStateFlow(SHOW_ENCRYPTION_PASSWORD_INPUT, false)
    private val _appRestartTimer = MutableStateFlow(Duration.ZERO)

    private val cycleStartDayType = savedStateHandle
        .getStateFlow(CYCLE_START_DAY_TYPE, CycleStartDayType.FIRST_DAY_OF_MONTH)
    private val specificStartDate = savedStateHandle.getStateFlow(
        SPECIFIC_CYCLE_START_DATE,
        DateUtil.dateNow().dayOfMonth
    )
    private val selectedStartOfDay = combineTuple(
        cycleStartDayType,
        specificStartDate
    ).mapLatest { (type, date) ->
        when (type) {
            CycleStartDayType.FIRST_DAY_OF_MONTH -> CycleStartDay.FirstDayOfMonth
            CycleStartDayType.LAST_DAY_OF_MONTH -> CycleStartDay.LastDayOfMonth
            CycleStartDayType.SPECIFIC_DAY_OF_MONTH -> CycleStartDay.SpecificDayOfMonth(date)
        }
    }
    val budgetInputState = savedStateHandle.saveable(
        key = "BUDGET_INPUT_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )

    private val currency = savedStateHandle
        .getStateFlow(SELECTED_CURRENCY, LocaleUtil.defaultCurrency)

    private val _isLoading = MutableStateFlow(false)

    val state = combineTuple(
        signInAndDataRestoreState,
        authState,
        dataRestoreState,
        showEncryptionPasswordInput,
        _appRestartTimer.asStateFlow(),
        currency,
        _isLoading
    ).mapLatest { (
                      signInAndDataRestoreState,
                      authState,
                      dataRestoreState,
                      showEncryptionPasswordInput,
                      appRestartTimer,
                      appCurrency,
                      isLoading
                  ) ->
        OnboardingState(
            signInAndDataRestoreState = signInAndDataRestoreState,
            authState = authState,
            dataRestoreState = dataRestoreState,
            showEncryptionPasswordInput = showEncryptionPasswordInput,
            appRestartTimer = appRestartTimer,
            appCurrency = appCurrency,
            isLoading = isLoading
        )
    }.onStart { collectRestoreWorkState() }
        .asStateFlow(viewModelScope, OnboardingState())

    init {
        appInitWorkManager.startAppInitWorker()
    }

    val events = eventBus.eventFlow

    private var hasRestoreJobRunThisSession: Boolean = false
    private var restoreWorkStateCollectionJob: Job? = null
    private fun collectRestoreWorkState() {
        restoreWorkStateCollectionJob?.cancel()
        restoreWorkStateCollectionJob = viewModelScope.launch {
            combineTuple(
                backupWorkManager.getRestoreDataDownloadWorkInfoFlow(),
                backupWorkManager.getImmediateDataRestoreWorkInfoFlow()
            ).collectLatest { (downloadInfo, restoreInfo) ->
                val isDownloadRunning = downloadInfo?.state == WorkInfo.State.RUNNING
                val isRestoreRunning = restoreInfo?.state == WorkInfo.State.RUNNING
                _dataRestoreState.update {
                    when {
                        isDownloadRunning -> DataRestoreState.DOWNLOADING_DATA
                        isRestoreRunning -> DataRestoreState.RESTORE_IN_PROGRESS
                        else -> it
                    }
                }

                if (isDownloadRunning || isRestoreRunning) {
                    hasRestoreJobRunThisSession = true
                }

                when {
                    restoreInfo?.state == WorkInfo.State.SUCCEEDED -> {
                        _dataRestoreState.update { DataRestoreState.COMPLETED }
                        savedStateHandle[AVAILABLE_BACKUP] = null
                        preferencesManager.concludeOnboarding()
                        startAppRestartProcedure()
                    }

                    downloadInfo?.state == WorkInfo.State.FAILED -> {
                        _dataRestoreState.update { DataRestoreState.FAILED }
                        if (hasRestoreJobRunThisSession) eventBus.send(
                            OnboardingEvent.ShowUiMessage(
                                downloadInfo.outputData.getString(BackupWorkManager.KEY_MESSAGE)
                                    ?.let { UiText.DynamicString(it) }
                                    ?: UiText.StringResource(
                                        R.string.error_app_data_restore_failed,
                                        true
                                    )
                            )
                        )
                    }

                    restoreInfo?.state == WorkInfo.State.FAILED -> {
                        _dataRestoreState.update { DataRestoreState.FAILED }
                        if (hasRestoreJobRunThisSession) eventBus.send(
                            OnboardingEvent.ShowUiMessage(
                                restoreInfo.outputData.getString(BackupWorkManager.KEY_MESSAGE)
                                    ?.let { UiText.DynamicString(it) }
                                    ?: UiText.StringResource(
                                        R.string.error_app_data_restore_failed,
                                        true
                                    )
                            )
                        )
                    }

                    else -> Unit
                }
            }
        }
    }

    private var appRestartJob: Job? = null
    private fun startAppRestartProcedure() {
        appRestartJob?.cancel()
        appRestartJob = viewModelScope.launch {
            _appRestartTimer.update { 5.seconds }
            while (_appRestartTimer.value > 0.seconds) {
                delay(1.seconds)
                _appRestartTimer.update { it - 1.seconds }
            }
        }
        appRestartJob?.invokeOnCompletion { cause ->
            if (cause == null) viewModelScope.launch {
                eventBus.send(OnboardingEvent.RestartApplication)
            }
        }
    }

    override fun onGivePermissionsClick() {
        viewModelScope.launch {
            eventBus.send(OnboardingEvent.LaunchPermissionsRequest)
        }
    }

    override fun onSkipPermissionsClick() {
        viewModelScope.launch {
            eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.ACCOUNT_SIGN_IN_AND_DATA_RESTORE))
        }
    }

    fun onPermissionsRequestResult(result: Map<String, Boolean>) {
        logD { "onPermissionsRequestResult() called with: result = $result" }
        viewModelScope.launch {
            val isSMSPermissionGranted = result[Manifest.permission.RECEIVE_SMS] == true
            preferencesManager.updateTransactionAutoDetectEnabled(isSMSPermissionGranted)

            val areAllGranted = result.all { it.value }
            if (areAllGranted) {
                eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.ACCOUNT_SIGN_IN_AND_DATA_RESTORE))
                return@launch
            }
        }
    }

    private var pageChangeJob: Job? = null
    fun onPageChange(page: Int) {
        pageChangeJob?.cancel()
        pageChangeJob = viewModelScope.launch {
            when (page) {
                OnboardingPage.ACCOUNT_SIGN_IN_AND_DATA_RESTORE.ordinal -> {
                    onAccountPageReached()
                }
            }
        }
    }

    private suspend fun onAccountPageReached() {
        when (authRepo.getAuthState().first()) {
            is AuthState.Authenticated -> {
                savedStateHandle[SIGN_IN_AND_DATA_RESTORE_STATE] =
                    SignInAndDataRestoreState.DATA_RESTORE
            }

            AuthState.UnAuthenticated -> {
                delay(400.milliseconds)
                eventBus.send(OnboardingEvent.StartAutoSignInFlow(true))
            }
        }
    }

    fun onCredentialResult(
        result: Result<String, CredentialService.CredentialError>
    ) = viewModelScope.launch {
        when (result) {
            is Result.Error -> {
                when (result.error) {
                    CredentialService.CredentialError.NO_AUTHORIZED_CREDENTIAL -> {
                        eventBus.send(OnboardingEvent.StartAutoSignInFlow(false))
                    }

                    CredentialService.CredentialError.CREDENTIAL_PROCESS_FAILED -> eventBus.send(
                        OnboardingEvent.ShowUiMessage(result.message)
                    )
                }
            }

            is Result.Success -> {
                signInUser(result.data)
            }
        }
    }

    private suspend fun signInUser(idToken: String) {
        when (val result = authRepo.signUserInWithToken(idToken)) {
            is Result.Error -> {
                eventBus.send(OnboardingEvent.ShowUiMessage(result.message))
            }

            is Result.Success -> {
                savedStateHandle[SIGN_IN_AND_DATA_RESTORE_STATE] =
                    SignInAndDataRestoreState.DATA_RESTORE
            }
        }
    }

    private var signInJob: Job? = null
    override fun onSignInClick() {
        signInJob?.cancel()
        signInJob = viewModelScope.launch {
            eventBus.send(OnboardingEvent.StartManualSignInFlow)
        }
    }

    override fun onSkipSignInClick() {
        viewModelScope.launch {
            eventBus.send(
                OnboardingEvent.NavigateToPage(OnboardingPage.SETUP_BUDGET_CYCLES)
            )
        }
    }

    private var backupJob: Job? = null
    override fun onCheckOrRestoreClick() {
        backupJob?.cancel()
        backupJob = viewModelScope.launch {
            when (val result = authRepo.authorizeUserAccount()) {
                is Result.Error -> {
                    when (result.error) {
                        AuthorizationService.AuthorizationError.NEEDS_RESOLUTION -> {
                            result.data?.let {
                                eventBus.send(OnboardingEvent.StartAuthorizationFlow(it))
                            }
                        }

                        AuthorizationService.AuthorizationError.AUTHORIZATION_FAILED -> {
                            eventBus.send(OnboardingEvent.ShowUiMessage(result.message))
                        }
                    }
                }

                is Result.Success -> {
                    checkIfBackupExists()
                }
            }
        }
    }

    fun onAuthorizationResult(intent: Intent) = viewModelScope.launch {
        when (val result = authRepo.decodeAuthorizationResult(intent)) {
            is Result.Error -> {
                eventBus.send(OnboardingEvent.ShowUiMessage(result.message))
            }

            is Result.Success -> {
                checkIfBackupExists()
            }
        }
    }

    private suspend fun checkIfBackupExists() {
        if (savedStateHandle.get<BackupDetails?>(AVAILABLE_BACKUP) != null) {
            _dataRestoreState.update { DataRestoreState.PASSWORD_VERIFICATION }
            savedStateHandle[SHOW_ENCRYPTION_PASSWORD_INPUT] = true
            return
        }
        _dataRestoreState.update { DataRestoreState.CHECKING_FOR_BACKUP }
        logI { "Running Backup Check" }
        when (val result = backupRepo.checkForBackup()) {
            is Result.Error -> {
                _dataRestoreState.update { DataRestoreState.IDLE }
                eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.SETUP_BUDGET_CYCLES))
            }

            is Result.Success -> {
                _dataRestoreState.update { DataRestoreState.PASSWORD_VERIFICATION }
                savedStateHandle[SHOW_ENCRYPTION_PASSWORD_INPUT] = true
                savedStateHandle[AVAILABLE_BACKUP] = result.data
            }
        }
    }

    override fun onEncryptionPasswordInputDismiss() {
        savedStateHandle[SHOW_ENCRYPTION_PASSWORD_INPUT] = false
    }

    override fun onEncryptionPasswordSubmit(password: String) {
        val backupDetails = savedStateHandle.get<BackupDetails?>(AVAILABLE_BACKUP)
            ?: return
        savedStateHandle[SHOW_ENCRYPTION_PASSWORD_INPUT] = false
        backupWorkManager.runImmediateRestoreWork(
            backupDetails = backupDetails,
            password = password,
        )
    }

    override fun onDataRestoreSkip() {
        viewModelScope.launch {
            eventBus.send(OnboardingEvent.NavigateToPage(OnboardingPage.SETUP_BUDGET_CYCLES))
            savedStateHandle[AVAILABLE_BACKUP] = null
        }
    }

    fun onCurrencySelected(currency: Currency) = viewModelScope.launch {
        savedStateHandle[SELECTED_CURRENCY] = currency
    }

    var startBudgetingJob: Job? = null
    override fun onStartBudgetingClick() {
        startBudgetingJob?.cancel()
        startBudgetingJob = viewModelScope.launch {
            _isLoading.update { true }
            val budgetValue = budgetInputState.text.toString().toLongOrNull() ?: -1L
            if (budgetValue <= Long.Zero) {
                eventBus.send(
                    OnboardingEvent.ShowUiMessage(
                        UiText.StringResource(R.string.error_invalid_amount, true)
                    )
                )
                return@launch
            }

            val startDayType = selectedStartOfDay.first()
            val result = cycleRepo.updateConfigAndCreateNewCycle(
                budget = budgetValue,
                currency = currency.value,
                startDay = startDayType,
                month = YearMonth.now(),
                duration = 1L,
                durationUnit = CycleDurationUnit.MONTH // TODO: Change fixed values
            )

            when (result) {
                is Result.Error -> {
                    eventBus.send(OnboardingEvent.ShowUiMessage(result.message))
                }

                is Result.Success -> {
                    preferencesManager.concludeOnboarding()
                    eventBus.send(OnboardingEvent.OnboardingConcluded)
                }
            }
            _isLoading.update { false }
        }
    }

    sealed interface OnboardingEvent {
        data class NavigateToPage(val page: OnboardingPage) : OnboardingEvent
        data object OnboardingConcluded : OnboardingEvent
        data class ShowUiMessage(val uiText: UiText) : OnboardingEvent
        data object LaunchPermissionsRequest : OnboardingEvent
        data class StartAutoSignInFlow(val filterByAuthorizedAccounts: Boolean) :
            OnboardingEvent

        data object StartManualSignInFlow : OnboardingEvent
        data class StartAuthorizationFlow(val pendingIntent: PendingIntent) : OnboardingEvent
        data object RestartApplication : OnboardingEvent
    }
}

private const val SIGN_IN_AND_DATA_RESTORE_STATE = "SIGN_IN_AND_DATA_RESTORE_STATE"
private const val AVAILABLE_BACKUP = "AVAILABLE_BACKUP"
private const val SHOW_ENCRYPTION_PASSWORD_INPUT = "SHOW_ENCRYPTION_PASSWORD_INPUT"
private const val CYCLE_START_DAY_TYPE = "SHOW_CYCLE_START_DAY_TYPE"
private const val SPECIFIC_CYCLE_START_DATE = "SPECIFIC_CYCLE_START_DATE"
private const val SELECTED_CURRENCY = "SELECTED_CURRENCY"