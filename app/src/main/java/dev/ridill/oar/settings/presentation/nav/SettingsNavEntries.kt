package dev.ridill.oar.settings.presentation.nav

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.R
import dev.ridill.oar.account.presentation.util.rememberCredentialService
import dev.ridill.oar.budgetCycles.presentation.budgetUpdate.UpdateBudgetSheet
import dev.ridill.oar.budgetCycles.presentation.budgetUpdate.UpdateBudgetViewModel
import dev.ridill.oar.budgetCycles.presentation.currencyUpdate.CurrencySelectionSheet
import dev.ridill.oar.budgetCycles.presentation.currencyUpdate.CurrencySelectionViewModel
import dev.ridill.oar.budgetCycles.presentation.cycleHistory.BudgetCyclesScreenContent
import dev.ridill.oar.budgetCycles.presentation.cycleHistory.BudgetCyclesViewModel
import dev.ridill.oar.budgetCycles.presentation.cycleSelection.CycleSelectionSheet
import dev.ridill.oar.budgetCycles.presentation.cycleSelection.CycleSelectionViewModel
import dev.ridill.oar.core.domain.util.BiometricUtil
import dev.ridill.oar.core.domain.util.BuildUtil
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.OnLifecycleStartEffect
import dev.ridill.oar.core.ui.components.rememberPermissionState
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.navigation.BackupEncryptionRoute
import dev.ridill.oar.core.ui.navigation.BackupSettingsRoute
import dev.ridill.oar.core.ui.navigation.BottomSheetSceneStrategy
import dev.ridill.oar.core.ui.navigation.BudgetCyclesRoute
import dev.ridill.oar.core.ui.navigation.AllTagsRoute
import dev.ridill.oar.core.ui.navigation.BudgetUpdatedResult
import dev.ridill.oar.core.ui.navigation.CurrencySelectionSheetRoute
import dev.ridill.oar.core.ui.navigation.CycleSelectedResult
import dev.ridill.oar.core.ui.navigation.CycleSelectionSheetRoute
import dev.ridill.oar.core.ui.navigation.EncryptionPasswordUpdatedResult
import dev.ridill.oar.core.ui.navigation.LocalResultBus
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.core.ui.navigation.ResultEffect
import dev.ridill.oar.core.ui.navigation.SecuritySettingsRoute
import dev.ridill.oar.core.ui.navigation.SettingsRoute
import dev.ridill.oar.core.ui.navigation.UpdateBudgetSheetRoute
import dev.ridill.oar.core.ui.util.LocalCurrencyPreference
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.core.ui.util.launchAppNotificationSettings
import dev.ridill.oar.core.ui.util.launchAppSettings
import dev.ridill.oar.settings.presentation.backupEncryption.BackupEncryptionScreen
import dev.ridill.oar.settings.presentation.backupEncryption.BackupEncryptionViewModel
import dev.ridill.oar.settings.presentation.backupEncryption.ENCRYPTION_PASSWORD_UPDATED
import dev.ridill.oar.settings.presentation.backupSettings.BackupSettingsScreen
import dev.ridill.oar.settings.presentation.backupSettings.BackupSettingsViewModel
import dev.ridill.oar.settings.presentation.securitySettings.SecuritySettingsScreen
import dev.ridill.oar.settings.presentation.securitySettings.SecuritySettingsViewModel
import dev.ridill.oar.settings.presentation.settings.SettingsScreen
import dev.ridill.oar.settings.presentation.settings.SettingsViewModel
import java.util.Currency

// region Settings

@Suppress("LongMethod")
fun EntryProviderScope<NavKey>.settingsEntries(navigator: OarNavigator) {
    entry<SettingsRoute> {
        val viewModel: SettingsViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val smsPermissionState = rememberPermissionState(
            permission = Manifest.permission.RECEIVE_SMS,
            onPermissionResult = viewModel::onSmsPermissionResult
        )

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()
        val credentialService = rememberCredentialService(context = context)
        val activity = LocalActivity.current

        ResultEffect<BudgetUpdatedResult> { _ ->
            snackbarController.showSnackbar(
                UiText.StringResource(R.string.budget_updated)
                    .asString(context)
            )
        }

        CollectFlowEffect(
            viewModel.events,
            snackbarController,
            context,
            credentialService
        ) { event ->
            when (event) {
                is SettingsViewModel.SettingsEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                SettingsViewModel.SettingsEvent.RequestSMSPermission -> {
                    smsPermissionState.launchRequest()
                }

                SettingsViewModel.SettingsEvent.LaunchAppSettings -> {
                    context.launchAppSettings()
                }

                SettingsViewModel.SettingsEvent.StartManualSignInFlow -> {
                    activity?.let {
                        val result =
                            credentialService.startManualGetCredentialFlow(activityContext = it)
                        viewModel.onCredentialResult(result)
                    }
                }
            }
        }

        SettingsScreen(
            snackbarController = snackbarController,
            state = state,
            actions = viewModel,
            navigateUp = navigator::goBack,
            navigateToNotificationSettings = context::launchAppNotificationSettings,
            navigateToCycles = { navigator.navigate(BudgetCyclesRoute) },
            navigateToManageTags = { navigator.navigate(AllTagsRoute) },
            navigateToBackupSettings = { navigator.navigate(BackupSettingsRoute) },
            navigateToSecuritySettings = { navigator.navigate(SecuritySettingsRoute) },
            launchUriInBrowser = {
                val intent = Intent(Intent.ACTION_VIEW, it)
                context.startActivity(intent)
            }
        )
    }

    entry<UpdateBudgetSheetRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
        val viewModel: UpdateBudgetViewModel = hiltViewModel()
        val currentBudget by viewModel.currentBudget.collectAsStateWithLifecycle(0L)
        val inputState = viewModel.budgetInputState
        val inputError by viewModel.budgetInputError.collectAsStateWithLifecycle()

        val resultBus = LocalResultBus.current

        CollectFlowEffect(viewModel.events) { event ->
            when (event) {
                UpdateBudgetViewModel.UpdateBudgetEvent.BudgetUpdated -> {
                    resultBus.sendResult<BudgetUpdatedResult>(BudgetUpdatedResult)
                    navigator.goBack()
                }
            }
        }

        UpdateBudgetSheet(
            placeholder = TextFormat.number(currentBudget),
            inputState = inputState,
            onConfirm = viewModel::onConfirm,
            errorMessage = inputError
        )
    }

    entry<BackupSettingsRoute> {
        val viewModel: BackupSettingsViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        ResultEffect<EncryptionPasswordUpdatedResult> { _ ->
            viewModel.onDestinationResult(ENCRYPTION_PASSWORD_UPDATED)
        }

        val authorizationResultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
            onResult = { result ->
                if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
                result.data?.let(viewModel::onAuthorizationResult)
            }
        )

        CollectFlowEffect(viewModel.events, snackbarController, context) { event ->
            when (event) {
                is BackupSettingsViewModel.BackupSettingsEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                is BackupSettingsViewModel.BackupSettingsEvent.NavigateToBackupEncryptionScreen -> {
                    navigator.navigate(BackupEncryptionRoute)
                }

                is BackupSettingsViewModel.BackupSettingsEvent.StartAuthorizationFlow -> {
                    authorizationResultLauncher.launch(
                        IntentSenderRequest.Builder(event.pendingIntent).build()
                    )
                }
            }
        }

        BackupSettingsScreen(
            context = context,
            snackbarController = snackbarController,
            state = state,
            actions = viewModel,
            navigateUp = navigator::goBack
        )
    }

    entry<BackupEncryptionRoute> {
        val viewModel: BackupEncryptionViewModel = hiltViewModel()
        val currentPasswordState = viewModel.currentPasswordState
        val newPasswordState = viewModel.newPasswordState
        val confirmNewPasswordState = viewModel.confirmNewPasswordState
        val state by viewModel.state.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()
        val biometricManager = remember(context) { BiometricManager.from(context) }

        val resultBus = LocalResultBus.current

        LaunchedEffect(viewModel, context, snackbarController, biometricManager) {
            viewModel.events.collect { event ->
                when (event) {
                    is BackupEncryptionViewModel.BackupEncryptionEvent.ShowUiMessage -> {
                        snackbarController.showSnackbar(
                            event.message.asString(context),
                            event.message.isErrorText
                        )
                    }

                    BackupEncryptionViewModel.BackupEncryptionEvent.PasswordUpdated -> {
                        resultBus.sendResult<EncryptionPasswordUpdatedResult>(
                            EncryptionPasswordUpdatedResult
                        )
                        navigator.goBack()
                    }

                    BackupEncryptionViewModel.BackupEncryptionEvent.LaunchBiometricAuthentication -> {
                        BiometricUtil.startBiometricAuthentication(
                            context = context,
                            onAuthSuccess = viewModel::onBiometricAuthSucceeded
                        )
                    }
                }
            }
        }

        BackupEncryptionScreen(
            snackbarController = snackbarController,
            state = state,
            currentPasswordState = currentPasswordState,
            newPasswordState = newPasswordState,
            confirmNewPasswordState = confirmNewPasswordState,
            actions = viewModel,
            navigateUp = navigator::goBack
        )
    }

    entry<SecuritySettingsRoute> {
        val viewModel: SecuritySettingsViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        val biometricManager = remember(context) { BiometricManager.from(context) }
        val biometricEnrollLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    if (biometricManager.canAuthenticate(BiometricUtil.DefaultBiometricAuthenticators) == BiometricManager.BIOMETRIC_SUCCESS) {
                        BiometricUtil.startBiometricAuthentication(
                            context = context,
                            onAuthSuccess = viewModel::onAuthenticationSuccess
                        )
                    }
                }
            }
        )

        val notificationPermissionLauncher = if (BuildUtil.isNotificationRuntimePermissionNeeded())
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = viewModel::onNotificationPermissionResult
            ) else null

        LaunchedEffect(snackbarController, context) {
            viewModel.events.collect { event ->
                when (event) {
                    SecuritySettingsViewModel.SecuritySettingsEvent.LaunchBiometricAuthentication -> {
                        when (biometricManager.canAuthenticate(BiometricUtil.DefaultBiometricAuthenticators)) {
                            BiometricManager.BIOMETRIC_SUCCESS -> {
                                BiometricUtil.startBiometricAuthentication(
                                    context = context,
                                    onAuthSuccess = viewModel::onAuthenticationSuccess
                                )
                            }

                            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                                snackbarController.showSnackbar(context.getString(R.string.error_biometric_hw_unavailable))
                            }

                            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                                if (BuildUtil.isApiLevelAtLeast30) {
                                    val enrollIntent =
                                        Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                            putExtra(
                                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                                BiometricUtil.DefaultBiometricAuthenticators
                                            )
                                        }
                                    biometricEnrollLauncher.launch(enrollIntent)
                                }
                            }

                            else -> Unit
                        }
                    }

                    SecuritySettingsViewModel.SecuritySettingsEvent.CheckNotificationPermission -> {
                        if (BuildUtil.isNotificationRuntimePermissionNeeded()) {
                            notificationPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    SecuritySettingsViewModel.SecuritySettingsEvent.NavigateToNotificationSettings -> {
                        context.launchAppNotificationSettings()
                    }
                }
            }
        }

        SecuritySettingsScreen(
            snackbarController = snackbarController,
            state = state,
            actions = viewModel,
            navigateUp = navigator::goBack
        )
    }

    entry<BudgetCyclesRoute> {
        val viewModel: BudgetCyclesViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val cycleHistory = viewModel.history.collectAsLazyPagingItems()
        val currencyPreference = LocalCurrencyPreference.current

        OnLifecycleStartEffect { viewModel.refreshCurrentDate() }

        ResultEffect<Currency> { currency ->
            viewModel.onCurrencySelected(currency)
        }

        BudgetCyclesScreenContent(
            state = state,
            history = cycleHistory,
            actions = viewModel,
            navigateUp = navigator::goBack,
            navigateToUpdateBudget = { navigator.navigate(UpdateBudgetSheetRoute) },
            navigateToCurrencySelection = {
                navigator.navigate(CurrencySelectionSheetRoute(preSelectedCurrCode = currencyPreference.currencyCode))
            }
        )
    }

    entry<CurrencySelectionSheetRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
        val viewModel: CurrencySelectionViewModel = hiltViewModel()
        val searchQueryState = viewModel.searchQueryState
        val currenciesLazyPagingItems = viewModel.currencyPagingData.collectAsLazyPagingItems()

        val resultBus = LocalResultBus.current

        CurrencySelectionSheet(
            searchQueryState = searchQueryState,
            selectedCode = key.preSelectedCurrCode,
            currenciesPagingData = currenciesLazyPagingItems,
            onConfirm = { currency ->
                resultBus.sendResult<Currency>(currency)
                navigator.goBack()
            }
        )
    }

    entry<CycleSelectionSheetRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
        val viewModel = hiltViewModel<CycleSelectionViewModel, CycleSelectionViewModel.Factory>(
            creationCallback = { it.create(key) }
        )
        val queryState = viewModel.query
        val cyclesLazyPagingItems = viewModel.cyclesPagingData.collectAsLazyPagingItems()
        val selectedId by viewModel.selectedId.collectAsStateWithLifecycle()

        val resultBus = LocalResultBus.current

        CycleSelectionSheet(
            queryState = queryState,
            cyclesLazyPagingItems = cyclesLazyPagingItems,
            selectedId = selectedId,
            onCycleSelect = viewModel::onCycleSelect,
            onConfirm = {
                resultBus.sendResult<CycleSelectedResult>(CycleSelectedResult(selectedId))
                navigator.goBack()
            }
        )
    }
}

// endregion
