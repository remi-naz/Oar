package dev.ridill.oar.transactions.presentation.nav

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.metadata
import androidx.navigation3.ui.NavDisplay
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.R
import dev.ridill.oar.application.NAV_ANIM_SCALE
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.components.slideInVerticallyWithFadeIn
import dev.ridill.oar.core.ui.components.slideOutVerticallyWithFadeOut
import dev.ridill.oar.core.ui.navigation.AddEditFolderSheetRoute
import dev.ridill.oar.core.ui.navigation.AddEditTransactionRoute
import dev.ridill.oar.core.ui.navigation.AddEditTxResult
import dev.ridill.oar.core.ui.navigation.AllSchedulesRoute
import dev.ridill.oar.core.ui.navigation.AllTransactionsRoute
import dev.ridill.oar.core.ui.navigation.AmountTransformationSheetRoute
import dev.ridill.oar.core.ui.navigation.BottomSheetSceneStrategy
import dev.ridill.oar.core.ui.navigation.CurrencySelectionSheetRoute
import dev.ridill.oar.core.ui.navigation.CycleSelectedResult
import dev.ridill.oar.core.ui.navigation.CycleSelectionSheetRoute
import dev.ridill.oar.core.ui.navigation.FolderSelectedResult
import dev.ridill.oar.core.ui.navigation.FolderSelectionSheetRoute
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.navigation.LocalResultBus
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.core.ui.navigation.ResultEffect
import dev.ridill.oar.core.ui.navigation.TagSelectedResult
import dev.ridill.oar.core.ui.navigation.TagSelectionSheetRoute
import dev.ridill.oar.core.ui.navigation.TransformationResult
import dev.ridill.oar.transactions.presentation.addEditTransaction.AddEditTransactionScreen
import dev.ridill.oar.transactions.presentation.addEditTransaction.AddEditTransactionViewModel
import dev.ridill.oar.transactions.presentation.allTransactions.AllTransactionsScreen
import dev.ridill.oar.transactions.presentation.allTransactions.AllTransactionsViewModel
import dev.ridill.oar.transactions.presentation.amountTransformation.AmountTransformationSheet
import dev.ridill.oar.transactions.presentation.amountTransformation.AmountTransformationViewModel
import java.util.Currency

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
