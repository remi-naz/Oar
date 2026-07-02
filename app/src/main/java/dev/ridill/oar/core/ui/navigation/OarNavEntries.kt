package dev.ridill.oar.core.ui.navigation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.ui.NavDisplay
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.R
import dev.ridill.oar.account.presentation.util.rememberCredentialService
import dev.ridill.oar.application.NAV_ANIM_SCALE
import dev.ridill.oar.application.RUN_CONFIG_RESTORE_EXTRA
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
import dev.ridill.oar.core.ui.components.rememberMultiplePermissionsState
import dev.ridill.oar.core.ui.components.rememberPermissionState
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.components.slideInVerticallyWithFadeIn
import dev.ridill.oar.core.ui.components.slideOutVerticallyWithFadeOut
import dev.ridill.oar.core.ui.util.LocalCurrencyPreference
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.ui.util.launchAppNotificationSettings
import dev.ridill.oar.core.ui.util.launchAppSettings
import dev.ridill.oar.core.ui.util.restartApplication
import dev.ridill.oar.dashboard.presentation.DashboardScreen
import dev.ridill.oar.dashboard.presentation.DashboardViewModel
import dev.ridill.oar.folders.presentation.addEditFolder.AddEditFolderSheet
import dev.ridill.oar.folders.presentation.addEditFolder.AddEditFolderViewModel
import dev.ridill.oar.folders.presentation.allFolders.AllFoldersScreen
import dev.ridill.oar.folders.presentation.allFolders.AllFoldersViewModel
import dev.ridill.oar.folders.presentation.folderDetails.FolderDetailsScreen
import dev.ridill.oar.folders.presentation.folderDetails.FolderDetailsViewModel
import dev.ridill.oar.folders.presentation.folderSelection.FolderSelectionSheet
import dev.ridill.oar.folders.presentation.folderSelection.FolderSelectionViewModel
import dev.ridill.oar.onboarding.domain.model.OnboardingPage
import dev.ridill.oar.onboarding.presentation.OnboardingScreen
import dev.ridill.oar.onboarding.presentation.OnboardingViewModel
import dev.ridill.oar.schedules.presentation.allSchedules.AllSchedulesScreen
import dev.ridill.oar.schedules.presentation.allSchedules.AllSchedulesViewModel
import dev.ridill.oar.settings.presentation.backupEncryption.BackupEncryptionScreen
import dev.ridill.oar.settings.presentation.backupEncryption.BackupEncryptionViewModel
import dev.ridill.oar.settings.presentation.backupEncryption.ENCRYPTION_PASSWORD_UPDATED
import dev.ridill.oar.settings.presentation.backupSettings.BackupSettingsScreen
import dev.ridill.oar.settings.presentation.backupSettings.BackupSettingsViewModel
import dev.ridill.oar.settings.presentation.securitySettings.SecuritySettingsScreen
import dev.ridill.oar.settings.presentation.securitySettings.SecuritySettingsViewModel
import dev.ridill.oar.settings.presentation.settings.SettingsScreen
import dev.ridill.oar.settings.presentation.settings.SettingsViewModel
import dev.ridill.oar.tags.presentation.addEditTag.AddEditTagSheet
import dev.ridill.oar.tags.presentation.addEditTag.AddEditTagViewModel
import dev.ridill.oar.tags.presentation.allTags.AllTagsScreen
import dev.ridill.oar.tags.presentation.allTags.AllTagsViewModel
import dev.ridill.oar.tags.presentation.tagSelection.SingleTagSelectionSheet
import dev.ridill.oar.transactions.presentation.addEditTransaction.AddEditTransactionScreen
import dev.ridill.oar.transactions.presentation.addEditTransaction.AddEditTransactionViewModel
import dev.ridill.oar.transactions.presentation.allTransactions.AllTransactionsScreen
import dev.ridill.oar.transactions.presentation.allTransactions.AllTransactionsViewModel
import dev.ridill.oar.transactions.presentation.amountTransformation.AmountTransformationSheet
import dev.ridill.oar.transactions.presentation.amountTransformation.AmountTransformationViewModel
import java.util.Currency

@Suppress("LongMethod")
fun buildOarEntryProvider(
    navigator: OarNavigator,
    motionScheme: MotionScheme,
) = entryProvider<NavKey> {
    onboardingEntries(navigator = navigator)
    dashboardEntries(navigator = navigator)
    transactionEntries(navigator = navigator, motionScheme = motionScheme)
    folderEntries(navigator = navigator)
    tagEntries(navigator = navigator)
    scheduleEntries(navigator = navigator)
    settingsEntries(navigator = navigator)
}

// region Onboarding
@Suppress("LongMethod")
fun EntryProviderScope<NavKey>.onboardingEntries(navigator: OarNavigator) {
    entry<OnboardingRoute> {
        val viewModel: OnboardingViewModel = hiltViewModel()
        val pagerState = androidx.compose.foundation.pager.rememberPagerState(
            pageCount = { OnboardingPage.entries.size }
        )
        val state by viewModel.state.collectAsStateWithLifecycle()
        val budgetInputState = viewModel.budgetInputState

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current
        val activity = LocalActivity.current

        val permissionsState = rememberMultiplePermissionsState(
            permissions = buildList {
                if (BuildUtil.isNotificationRuntimePermissionNeeded())
                    add(Manifest.permission.POST_NOTIFICATIONS)
            },
            onPermissionResult = viewModel::onPermissionsRequestResult
        )

        val credentialService = rememberCredentialService(context)
        val currentPage by remember(pagerState) { derivedStateOf { pagerState.currentPage } }

        LaunchedEffect(currentPage) {
            viewModel.onPageChange(currentPage)
        }

        val authorizationResultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
            onResult = { result ->
                if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
                result.data?.let(viewModel::onAuthorizationResult)
            }
        )

        ResultEffect<Currency> { currency ->
            viewModel.onCurrencySelected(currency)
        }

        CollectFlowEffect(viewModel.events, snackbarController, context) { event ->
            when (event) {
                is OnboardingViewModel.OnboardingEvent.NavigateToPage -> {
                    if (!pagerState.isScrollInProgress)
                        pagerState.animateScrollToPage(event.page.ordinal)
                }

                OnboardingViewModel.OnboardingEvent.LaunchPermissionsRequest -> {
                    permissionsState.launchRequest()
                }

                OnboardingViewModel.OnboardingEvent.LaunchAlarmPermissionsSettings -> {
                    if (BuildUtil.isApiLevelAtLeast31) {
                        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    }
                }

                is OnboardingViewModel.OnboardingEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                OnboardingViewModel.OnboardingEvent.OnboardingConcluded -> {
                    navigator.replaceTop(DashboardRoute)
                }

                OnboardingViewModel.OnboardingEvent.RestartApplication -> {
                    context.restartApplication(editIntent = {
                        putExtra(
                            RUN_CONFIG_RESTORE_EXTRA,
                            true
                        )
                    })
                }

                is OnboardingViewModel.OnboardingEvent.StartAutoSignInFlow -> {
                    activity?.let {
                        val result = credentialService.startGetCredentialFlow(
                            filterByAuthorizedUsers = event.filterByAuthorizedAccounts,
                            activityContext = it
                        )
                        viewModel.onCredentialResult(result)
                    }
                }

                is OnboardingViewModel.OnboardingEvent.StartAuthorizationFlow -> {
                    authorizationResultLauncher.launch(
                        IntentSenderRequest.Builder(event.pendingIntent)
                            .build()
                    )
                }

                OnboardingViewModel.OnboardingEvent.StartManualSignInFlow -> {
                    activity?.let {
                        val result = credentialService.startManualGetCredentialFlow(it)
                        viewModel.onCredentialResult(result)
                    }
                }
            }
        }

        OnboardingScreen(
            snackbarController = snackbarController,
            pagerState = pagerState,
            permissionsState = permissionsState,
            state = state,
            budgetInputState = budgetInputState,
            navigateToCurrencySelection = {
                navigator.navigate(CurrencySelectionSheetRoute(preSelectedCurrCode = state.appCurrency.currencyCode))
            },
            actions = viewModel
        )
    }
}

// endregion

// region Dashboard
fun EntryProviderScope<NavKey>.dashboardEntries(navigator: OarNavigator) {
    entry<DashboardRoute> {
        val viewModel: DashboardViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val recentSpendsLazyPagingItems =
            viewModel.recentSpendsPagingData.collectAsLazyPagingItems()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        ResultEffect<AddEditTxResult> { result ->
            viewModel.onNavResult(result)
        }

        CollectFlowEffect(flow = viewModel.events, snackbarController, context) { event ->
            when (event) {
                DashboardViewModel.DashboardEvent.ScheduleSaved -> {
                    snackbarController.showSnackbar(
                        message = context.getString(R.string.schedule_saved),
                        actionLabel = context.getString(R.string.action_view),
                        onSnackbarResult = { result ->
                            if (result == SnackbarResult.ActionPerformed) {
                                navigator.navigate(AllSchedulesRoute)
                            }
                        }
                    )
                }

                is DashboardViewModel.DashboardEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(event.uiText.asString(context))
                }
            }
        }

        DashboardScreen(
            snackbarController = snackbarController,
            recentSpends = recentSpendsLazyPagingItems,
            state = state,
            navigateToAllTransactions = { navigator.navigate(AllTransactionsRoute) },
            navigateToAddEditTransaction = { id, isSchedule ->
                navigator.navigate(
                    AddEditTransactionRoute(
                        transactionId = id ?: INVALID_ID_LONG,
                        isScheduleMode = isSchedule
                    )
                )
            },
            navigateTo = { navigator.navigate(it) }
        )
    }
}

// endregion

// region Transactions
@Suppress("LongMethod")
fun EntryProviderScope<NavKey>.transactionEntries(
    navigator: OarNavigator,
    motionScheme: MotionScheme,
) {
    entry<AllTransactionsRoute> {
        val viewModel: AllTransactionsViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val transactionsLazyPagingItems =
            viewModel.transactionsPagingData.collectAsLazyPagingItems()
        val searchResults = viewModel.searchResults.collectAsLazyPagingItems()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        ResultEffect<FolderSelectedResult> { result ->
            viewModel.onFolderSelect(result.id)
        }
        ResultEffect<AddEditTxResult> { result ->
            viewModel.onAddEditTxNavResult(result)
        }
        ResultEffect<FolderSavedResult> { result ->
            navigator.navigate(FolderDetailsRoute(result.id))
        }
        ResultEffect<CycleSelectedResult> { result ->
            result.id?.let { viewModel.onCycleSelect(it) }
        }
        ResultEffect<TagSelectedResult> { result ->
            viewModel.onTagAssignmentSelectionResult(result.id)
        }

        CollectFlowEffect(viewModel.events, context, snackbarController) { event ->
            when (event) {
                is AllTransactionsViewModel.AllTransactionsEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                AllTransactionsViewModel.AllTransactionsEvent.NavigateToFolderSelection -> {
                    navigator.navigate(FolderSelectionSheetRoute())
                }

                is AllTransactionsViewModel.AllTransactionsEvent.NavigateToTagSelectionForAssignment -> {
                    navigator.navigate(TagSelectionSheetRoute())
                }

                AllTransactionsViewModel.AllTransactionsEvent.ScheduleSaved -> {
                    snackbarController.showSnackbar(
                        message = context.getString(R.string.schedule_saved),
                        actionLabel = context.getString(R.string.action_view),
                        onSnackbarResult = { result ->
                            if (result == SnackbarResult.ActionPerformed) {
                                navigator.navigate(AllSchedulesRoute)
                            }
                        }
                    )
                }

                is AllTransactionsViewModel.AllTransactionsEvent.NavigateToAddEditTx -> {
                    navigator.navigate(AddEditTransactionRoute(transactionId = event.id))
                }

                AllTransactionsViewModel.AllTransactionsEvent.ChooseCycleForTransactions -> {
                    navigator.navigate(CycleSelectionSheetRoute())
                }
            }
        }

        AllTransactionsScreen(
            snackbarController = snackbarController,
            transactionsLazyPagingItems = transactionsLazyPagingItems,
            searchQueryState = viewModel.searchQueryState,
            searchResultsLazyPagingItems = searchResults,
            state = state,
            actions = viewModel,
            navigateUp = navigator::goBack,
            navigateToAddEditTransaction = {
                navigator.navigate(
                    AddEditTransactionRoute(
                        transactionId = it ?: INVALID_ID_LONG
                    )
                )
            },
            navigateToCreateSchedule = { navigator.navigate(AddEditTransactionRoute(isScheduleMode = true)) },
            navigateToCreateFolder = { navigator.navigate(AddEditFolderSheetRoute()) }
        )
    }

    entry<AddEditTransactionRoute>(
        metadata = metadata {
            val slideAnimationSpec: FiniteAnimationSpec<IntOffset> = motionScheme.slowSpatialSpec()
            val fadeAnimationSpec: FiniteAnimationSpec<Float> = motionScheme.slowSpatialSpec()
            put(NavDisplay.TransitionKey) {
                slideInVerticallyWithFadeIn(
                    initialOffsetY = { it },
                    slideAnimationSpec = slideAnimationSpec,
                    fadeAnimationSpec = fadeAnimationSpec,
                ) togetherWith scaleOut(
                    animationSpec = fadeAnimationSpec,
                    targetScale = NAV_ANIM_SCALE,
                    transformOrigin = TransformOrigin.Center,
                ) + fadeOut(animationSpec = fadeAnimationSpec)
            }
            put(NavDisplay.PopTransitionKey) {
                scaleIn(
                    animationSpec = fadeAnimationSpec,
                    initialScale = NAV_ANIM_SCALE,
                    transformOrigin = TransformOrigin.Center,
                ) + fadeIn(animationSpec = fadeAnimationSpec) togetherWith slideOutVerticallyWithFadeOut(
                    targetOffsetY = { it },
                    slideAnimationSpec = slideAnimationSpec,
                    fadeAnimationSpec = fadeAnimationSpec,
                )
            }
            put(NavDisplay.PredictivePopTransitionKey) {
                scaleIn(
                    animationSpec = fadeAnimationSpec,
                    initialScale = NAV_ANIM_SCALE,
                    transformOrigin = TransformOrigin.Center,
                ) + fadeIn(animationSpec = fadeAnimationSpec) togetherWith slideOutVerticallyWithFadeOut(
                    targetOffsetY = { it },
                    slideAnimationSpec = slideAnimationSpec,
                    fadeAnimationSpec = fadeAnimationSpec,
                )
            }
        }
    ) { key ->
        val viewModel =
            hiltViewModel<AddEditTransactionViewModel, AddEditTransactionViewModel.Factory>(
                creationCallback = { it.create(key) }
            )
        val amountInputState = viewModel.amountInputState
        val noteInputState = viewModel.noteInputState
        val state by viewModel.state.collectAsStateWithLifecycle()

        val isEditMode = key.transactionId != INVALID_ID_LONG
        val isDuplicateMode = key.isDuplicateMode

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        val resultBus = LocalResultBus.current

        ResultEffect<FolderSelectedResult> { result ->
            viewModel.onFolderSelectionResult(result.id)
        }
        ResultEffect<TransformationResult> { result ->
            viewModel.onAmountTransformationResult(result)
        }
        ResultEffect<Currency> { currency ->
            viewModel.onCurrencySelect(currency)
        }
        ResultEffect<CycleSelectedResult> { result ->
            viewModel.onCycleSelect(result.id)
        }

        CollectFlowEffect(viewModel.events, snackbarController, context) { event ->
            when (event) {
                is AddEditTransactionViewModel.AddEditTransactionEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                is AddEditTransactionViewModel.AddEditTransactionEvent.LaunchFolderSelection -> {
                    navigator.navigate(
                        FolderSelectionSheetRoute(
                            preselectedId = event.preselectedId ?: INVALID_ID_LONG
                        )
                    )
                }

                is AddEditTransactionViewModel.AddEditTransactionEvent.NavigateUpWithResult -> {
                    resultBus.sendResult<AddEditTxResult>(event.result)
                    navigator.goBack()
                }

                is AddEditTransactionViewModel.AddEditTransactionEvent.NavigateToDuplicateTransactionCreation -> {
                    navigator.replaceTop(
                        AddEditTransactionRoute(
                            transactionId = event.id,
                            isDuplicateMode = true
                        )
                    )
                }
            }
        }

        AddEditTransactionScreen(
            isEditMode = isEditMode,
            isDuplicateMode = isDuplicateMode,
            snackbarController = snackbarController,
            amountInputState = amountInputState,
            noteInputState = noteInputState,
            state = state,
            actions = viewModel,
            navigateUp = navigator::goBack,
            navigateToAmountTransformation = { navigator.navigate(AmountTransformationSheetRoute) },
            navigateToCurrencySelection = {
                navigator.navigate(CurrencySelectionSheetRoute(preSelectedCurrCode = state.currency.currencyCode))
            },
            navigateToCycleSelection = {
                navigator.navigate(
                    CycleSelectionSheetRoute(
                        preselectedId = state.selectedCycleId ?: INVALID_ID_LONG
                    )
                )
            }
        )
    }

    entry<AmountTransformationSheetRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
        val viewModel: AmountTransformationViewModel = hiltViewModel()
        val selectedTransformation by viewModel.selectedTransformation.collectAsStateWithLifecycle()

        val resultBus = LocalResultBus.current

        AmountTransformationSheet(
            onDismiss = navigator::goBack,
            selectedTransformation = selectedTransformation,
            onTransformationSelect = viewModel::onTransformationSelect,
            factorInput = viewModel.factorInputState,
            onTransformClick = {
                resultBus.sendResult<TransformationResult>(
                    TransformationResult(
                        transformation = selectedTransformation,
                        factor = viewModel.factorInputState.text.toString()
                    )
                )
                navigator.goBack()
            }
        )
    }
}

// endregion

// region Folders

@Suppress("LongMethod")
fun EntryProviderScope<NavKey>.folderEntries(navigator: OarNavigator) {
    entry<AllFoldersRoute> {
        val viewModel: AllFoldersViewModel = hiltViewModel()
        val foldersPagingItems = viewModel.folderListPagingData.collectAsLazyPagingItems()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        ResultEffect<FolderSavedResult> { result ->
            navigator.navigate(FolderDetailsRoute(result.id))
        }
        ResultEffect<FolderDeletedResult> { _ ->
            snackbarController.showSnackbar(context.getString(R.string.transaction_folder_deleted))
        }

        AllFoldersScreen(
            snackbarController = snackbarController,
            foldersPagingItems = foldersPagingItems,
            navigateToFolderDetails = { navigator.navigate(FolderDetailsRoute(it)) },
            navigateUp = navigator::goBack,
            navigateToAddFolder = { navigator.navigate(AddEditFolderSheetRoute()) }
        )
    }

    entry<FolderDetailsRoute> { key ->
        val viewModel = hiltViewModel<FolderDetailsViewModel, FolderDetailsViewModel.Factory>(
            creationCallback = { it.create(key) }
        )
        val state by viewModel.state.collectAsStateWithLifecycle()
        val transactionPagingItems = viewModel.transactionPagingData.collectAsLazyPagingItems()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        val resultBus = LocalResultBus.current

        CollectFlowEffect(viewModel.events, context, snackbarController) { event ->
            when (event) {
                is FolderDetailsViewModel.FolderDetailsEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                FolderDetailsViewModel.FolderDetailsEvent.FolderDeleted -> {
                    resultBus.sendResult<FolderDeletedResult>(FolderDeletedResult)
                    navigator.goBack()
                }

                is FolderDetailsViewModel.FolderDetailsEvent.TransactionRemovedFromFolder -> {
                    snackbarController.showSnackbar(
                        message = context.resources.getQuantityString(
                            R.plurals.transaction_removed_from_this_folder, 1
                        ),
                        actionLabel = context.getString(R.string.action_undo),
                        onSnackbarResult = {
                            if (it == SnackbarResult.ActionPerformed) {
                                viewModel.onRemoveTransactionUndo(event.txId)
                            }
                        }
                    )
                }
            }
        }

        FolderDetailsScreen(
            snackbarController = snackbarController,
            transactionPagingItems = transactionPagingItems,
            state = state,
            actions = viewModel,
            navigateToAddEditTransaction = { transactionId ->
                navigator.navigate(
                    AddEditTransactionRoute(
                        transactionId = transactionId ?: INVALID_ID_LONG,
                        linkFolderId = key.folderId
                    )
                )
            },
            navigateToEditFolder = { navigator.navigate(AddEditFolderSheetRoute(folderId = key.folderId)) },
            navigateUp = navigator::goBack
        )
    }

    entry<AddEditFolderSheetRoute>(
        metadata = BottomSheetSceneStrategy.bottomSheet()
    ) { key ->
        val viewModel = hiltViewModel<AddEditFolderViewModel, AddEditFolderViewModel.Factory>(
            creationCallback = { it.create(key) }
        )
        val input = viewModel.folderInput.collectAsStateWithLifecycle()
        val folderNameState = viewModel.folderNameState
        val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

        val editMode = key.folderId != INVALID_ID_LONG
        val resultBus = LocalResultBus.current

        CollectFlowEffect(flow = viewModel.events) { event ->
            when (event) {
                is AddEditFolderViewModel.AddEditFolderEvent.FolderSaved -> {
                    resultBus.sendResult<FolderSavedResult>(FolderSavedResult(event.tagId))
                    navigator.goBack()
                }
            }
        }

        AddEditFolderSheet(
            isLoading = isLoading,
            nameState = folderNameState,
            excluded = { input.value.excluded },
            errorMessage = errorMessage,
            isEditMode = editMode,
            actions = viewModel,
            onDismiss = navigator::goBack
        )
    }

    entry<FolderSelectionSheetRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
        val viewModel = hiltViewModel<FolderSelectionViewModel, FolderSelectionViewModel.Factory>(
            creationCallback = { it.create(key) }
        )
        val searchQueryState = viewModel.searchQueryState
        val foldersList = viewModel.folderListPaged.collectAsLazyPagingItems()
        val selectedId by viewModel.selectedFolderId.collectAsStateWithLifecycle()

        val resultBus = LocalResultBus.current

        ResultEffect<FolderSavedResult> { result ->
            viewModel.onFolderSelect(result.id)
        }

        FolderSelectionSheet(
            queryState = searchQueryState,
            foldersListLazyPagingItems = foldersList,
            onFolderSelect = viewModel::onFolderSelect,
            onCreateNewClick = { navigator.navigate(AddEditFolderSheetRoute()) },
            onDismiss = navigator::goBack,
            onClearSelectionClick = {
                resultBus.sendResult<FolderSelectedResult>(FolderSelectedResult(INVALID_ID_LONG))
                navigator.goBack()
            },
            onConfirm = {
                resultBus.sendResult<FolderSelectedResult>(
                    FolderSelectedResult(
                        selectedId ?: INVALID_ID_LONG
                    )
                )
                navigator.goBack()
            },
            selectedId = selectedId
        )
    }
}

// endregion

// region Tags

fun EntryProviderScope<NavKey>.tagEntries(navigator: OarNavigator) {
    entry<AllTagsRoute> {
        val viewModel: AllTagsViewModel = hiltViewModel()
        val searchQueryState = viewModel.searchQueryState
        val tagsLazyPagingItems = viewModel.allTagsPagingData.collectAsLazyPagingItems()
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()

        AllTagsScreen(
            snackbarController = snackbarController,
            tagsLazyPagingItems = tagsLazyPagingItems,
            tagSearchQueryState = searchQueryState,
            state = state,
            actions = viewModel,
            navigateUp = navigator::goBack,
            navigateToAddEditTag = { tagId ->
                navigator.navigate(AddEditTagSheetRoute(tagId = tagId ?: INVALID_ID_LONG))
            }
        )
    }

    entry<AddEditTagSheetRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
        val viewModel = hiltViewModel<AddEditTagViewModel, AddEditTagViewModel.Factory>(
            creationCallback = { it.create(key) }
        )
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val input = viewModel.tagInput.collectAsStateWithLifecycle()
        val nameState = viewModel.nameInputState
        val error by viewModel.tagInputError.collectAsStateWithLifecycle()
        val showDeleteTagConfirmation by viewModel.showTagDeleteConfirmation.collectAsStateWithLifecycle()

        val isEditMode = key.tagId != INVALID_ID_LONG
        val resultBus = LocalResultBus.current

        CollectFlowEffect(flow = viewModel.events) { event ->
            when (event) {
                is AddEditTagViewModel.AddEditTagEvent.TagSaved -> {
                    resultBus.sendResult<TagSavedResult>(TagSavedResult(event.tagId))
                    navigator.goBack()
                }

                AddEditTagViewModel.AddEditTagEvent.TagDeleted -> {
                    navigator.goBack()
                }
            }
        }

        AddEditTagSheet(
            isLoading = isLoading,
            nameState = nameState,
            selectedColorCode = { input.value.colorCode },
            excluded = { input.value.excluded },
            errorMessage = error,
            isEditMode = isEditMode,
            onDismiss = navigator::goBack,
            showDeleteTagConfirmation = showDeleteTagConfirmation,
            actions = viewModel
        )
    }

    entry<TagSelectionSheetRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
        val preSelectedId = key.preselectedId.takeIf { it != INVALID_ID_LONG }
        val resultBus = LocalResultBus.current

        SingleTagSelectionSheet(
            preSelectedId = preSelectedId,
            onDismiss = navigator::goBack,
            onConfirm = { selectedId ->
                resultBus.sendResult<TagSelectedResult>(TagSelectedResult(selectedId))
                navigator.goBack()
            }
        )
    }
}

// endregion

// region Schedules

fun EntryProviderScope<NavKey>.scheduleEntries(navigator: OarNavigator) {
    entry<AllSchedulesRoute> {
        val viewModel: AllSchedulesViewModel = hiltViewModel()
        val allSchedulesPagingItems = viewModel.schedulesPagingData.collectAsLazyPagingItems()
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        val notificationPermissionState = if (BuildUtil.isNotificationRuntimePermissionNeeded())
            rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        else null

        CollectFlowEffect(flow = viewModel.events, snackbarController, context) { event ->
            when (event) {
                is AllSchedulesViewModel.AllSchedulesEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(event.uiText.asString(context))
                }

                AllSchedulesViewModel.AllSchedulesEvent.RequestNotificationPermission -> {
                    context.launchAppNotificationSettings()
                }
            }
        }

        OnLifecycleStartEffect(viewModel, block = viewModel::refreshCurrentDate)

        AllSchedulesScreen(
            context = context,
            snackbarController = snackbarController,
            notificationPermissionState = notificationPermissionState,
            allSchedulesPagingItems = allSchedulesPagingItems,
            state = state,
            actions = viewModel,
            navigateUp = navigator::goBack,
            navigateToAddEditSchedule = {
                navigator.navigate(
                    AddEditTransactionRoute(
                        transactionId = it ?: INVALID_ID_LONG,
                        isScheduleMode = true
                    )
                )
            }
        )
    }
}

// endregion

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
                dev.ridill.oar.core.ui.util.UiText.StringResource(R.string.budget_updated)
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
            onDismiss = navigator::goBack,
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
            onDismiss = navigator::goBack,
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
            onDismiss = navigator::goBack,
            onConfirm = {
                resultBus.sendResult<CycleSelectedResult>(CycleSelectedResult(selectedId))
                navigator.goBack()
            }
        )
    }
}

// endregion
