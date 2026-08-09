package dev.ridill.oar.moneyPiles.presentation.nav

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.metadata
import androidx.navigation3.ui.NavDisplay
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.application.NAV_ANIM_SCALE
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.OnLifecycleStartEffect
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.components.slideInVerticallyWithFadeIn
import dev.ridill.oar.core.ui.components.slideOutVerticallyWithFadeOut
import dev.ridill.oar.core.ui.navigation.AddEditMoneyPileResult
import dev.ridill.oar.core.ui.navigation.AddEditMoneyPileRoute
import dev.ridill.oar.core.ui.navigation.AllMoneyPilesRoute
import dev.ridill.oar.core.ui.navigation.BottomSheetSceneStrategy
import dev.ridill.oar.core.ui.navigation.CycleSelectedResult
import dev.ridill.oar.core.ui.navigation.CycleSelectionSheetRoute
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.navigation.LocalResultBus
import dev.ridill.oar.core.ui.navigation.MoneyPileDetailsRoute
import dev.ridill.oar.core.ui.navigation.MoneyPileFundMovementRoute
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.core.ui.navigation.PileFundMovementResult
import dev.ridill.oar.core.ui.navigation.PileSweepOutConfirmationSheetRoute
import dev.ridill.oar.core.ui.navigation.PileSweptOutResult
import dev.ridill.oar.core.ui.navigation.ResultEffect
import dev.ridill.oar.moneyPiles.presentation.addEditPile.AddEditPileScreen
import dev.ridill.oar.moneyPiles.presentation.addEditPile.AddEditPileViewModel
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesScreen
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesViewModel
import dev.ridill.oar.moneyPiles.presentation.movePileFund.MovePileFundScreen
import dev.ridill.oar.moneyPiles.presentation.movePileFund.MovePileFundViewModel
import dev.ridill.oar.moneyPiles.presentation.pileDetails.PileDetailScreen
import dev.ridill.oar.moneyPiles.presentation.pileDetails.PileDetailViewModel
import dev.ridill.oar.moneyPiles.presentation.sweepout.PileSweepOutConfirmationSheet
import dev.ridill.oar.moneyPiles.presentation.sweepout.PileSweepOutViewModel

fun EntryProviderScope<NavKey>.moneyPileEntries(
    navigator: OarNavigator,
    motionScheme: MotionScheme,
) {
    entry<AllMoneyPilesRoute> {
        val viewModel: AllPilesViewModel = hiltViewModel()
        val pilesPagingItems = viewModel.pilesPagingData.collectAsLazyPagingItems()
        val includeCompletedPiles by viewModel.includeCompletedPiles.collectAsStateWithLifecycle()
        val snackbarController = rememberSnackbarController()

        ResultEffect<AddEditMoneyPileResult> { result ->
            viewModel.onAddEditPileResult(result)
        }

        ResultEffect<PileFundMovementResult> { result ->
            viewModel.onPileFundMovementResult(result)
        }

        CollectFlowEffect(viewModel.events) { event ->
            when (event) {
                is AllPilesViewModel.AllPilesEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(event.text)
                }
            }
        }

        AllPilesScreen(
            snackbarController = snackbarController,
            pilesPagingItems = pilesPagingItems,
            includeCompletedPiles = includeCompletedPiles,
            onIncludeLockedPilesToggle = viewModel::onIncludeLockedPilesToggle,
            navigateToAddPile = { navigator.navigate(AddEditMoneyPileRoute()) },
            navigateToPileDetails = { navigator.navigate(MoneyPileDetailsRoute(it)) },
            navigateToAddToPile = {
                navigator.navigate(
                    MoneyPileFundMovementRoute(
                        pileId = it,
                        movement = FundMovement.IN
                    )
                )
            },
            navigateUp = navigator::goBack,
        )
    }

    entry<MoneyPileDetailsRoute> { route ->
        val viewModel: PileDetailViewModel =
            hiltViewModel<PileDetailViewModel, PileDetailViewModel.Factory>(
                creationCallback = { it.create(route) }
            )

        val transactionPagingItems = viewModel.transactionPagingData.collectAsLazyPagingItems()
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val resultBus = LocalResultBus.current
        CollectFlowEffect(viewModel.events, resultBus, snackbarController) { event ->
            when (event) {
                is PileDetailViewModel.PileDetailEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(event.text)
                }

                PileDetailViewModel.PileDetailEvent.PileDeleted -> {
                    resultBus.sendResult(AddEditMoneyPileResult.PILE_DELETED)
                    navigator.goBack()
                }
            }
        }

        ResultEffect<AddEditMoneyPileResult> { result ->
            viewModel.onAddEditPileResult(result)
        }

        ResultEffect<PileFundMovementResult> { result ->
            viewModel.onPileFundMovementResult(result)
        }

        ResultEffect<PileSweptOutResult> {
            viewModel.onPileSweptOut()
        }

        OnLifecycleStartEffect(
            viewModel,
            block = viewModel::refreshDateNow
        )

        PileDetailScreen(
            state = state,
            actions = viewModel,
            transactionPagingItems = transactionPagingItems,
            navigateUp = navigator::goBack,
            navigateToEditPile = { navigator.navigate(AddEditMoneyPileRoute(route.pileId)) },
            navigateToFundMovement = {
                navigator.navigate(
                    MoneyPileFundMovementRoute(
                        pileId = route.pileId,
                        movement = it
                    )
                )
            },
            navigateToSweepOut = {
                navigator.navigate(PileSweepOutConfirmationSheetRoute(route.pileId))
            },
            snackbarController = snackbarController
        )
    }

    entry<AddEditMoneyPileRoute> { route ->
        val viewModel: AddEditPileViewModel =
            hiltViewModel<AddEditPileViewModel, AddEditPileViewModel.Factory>(
                creationCallback = { it.create(route) }
            )
        val resultBus = LocalResultBus.current
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        CollectFlowEffect(viewModel.events) { event ->
            when (event) {
                AddEditPileViewModel.AddEditPileEvent.PileSaved -> {
                    resultBus.sendResult(AddEditMoneyPileResult.PILE_SAVED)
                    navigator.goBack()
                }

                is AddEditPileViewModel.AddEditPileEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(event.text)
                }

                AddEditPileViewModel.AddEditPileEvent.PileDeleted -> {
                    resultBus.sendResult(AddEditMoneyPileResult.PILE_DELETED)
                    navigator.goBack() // Pop AddEdit screen
                    navigator.goBack() // Pop Detail screen
                }
            }
        }

        AddEditPileScreen(
            state = state,
            isEditMode = viewModel.isEditMode,
            nameState = viewModel.nameState,
            starterAmountState = viewModel.starterAmountState,
            targetAmountState = viewModel.targetAmountState,
            reminderAmountState = viewModel.reminderAmountState,
            actions = viewModel,
            navigateUp = navigator::goBack,
            snackbarController = snackbarController,
        )
    }

    entry<MoneyPileFundMovementRoute>(
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
    ) { route ->
        val viewModel: MovePileFundViewModel =
            hiltViewModel<MovePileFundViewModel, MovePileFundViewModel.Factory>(
                creationCallback = { it.create(route) }
            )
        val state by viewModel.state.collectAsStateWithLifecycle()
        val resultBus = LocalResultBus.current
        val snackbarController = rememberSnackbarController()
        CollectFlowEffect(viewModel.events, resultBus, snackbarController) { event ->
            when (event) {
                is MovePileFundViewModel.MovePileFundEvent.FundMoved -> {
                    resultBus.sendResult(
                        when (event.movement) {
                            FundMovement.IN -> PileFundMovementResult.FUND_ADDED
                            FundMovement.OUT -> PileFundMovementResult.FUND_WITHDRAWN
                        }
                    )
                    navigator.goBack()
                }

                is MovePileFundViewModel.MovePileFundEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(event.uiText)
                }
            }
        }

        ResultEffect<CycleSelectedResult> { result ->
            viewModel.onCycleSelect(result.id)
        }

        MovePileFundScreen(
            movement = route.movement,
            state = state,
            amountInputState = viewModel.amountInputState,
            actions = viewModel,
            navigateUp = navigator::goBack,
            navigateToCycleSelection = {
                navigator.navigate(
                    CycleSelectionSheetRoute(
                        preselectedId = state.selectedCycleId ?: INVALID_ID_LONG
                    )
                )
            },
            snackbarController = snackbarController
        )
    }

    entry<PileSweepOutConfirmationSheetRoute>(
        metadata = BottomSheetSceneStrategy.bottomSheet()
    ) { route ->
        val viewModel: PileSweepOutViewModel =
            hiltViewModel<PileSweepOutViewModel, PileSweepOutViewModel.Factory>(
                creationCallback = { it.create(route) }
            )
        val state by viewModel.state.collectAsStateWithLifecycle()
        val resultBus = LocalResultBus.current
        val snackbarController = rememberSnackbarController()

        CollectFlowEffect(viewModel.events, resultBus, snackbarController) { event ->
            when (event) {
                is PileSweepOutViewModel.PileSweepOutEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(event.uiText)
                }

                PileSweepOutViewModel.PileSweepOutEvent.PileSweptOut -> {
                    resultBus.sendResult(PileSweptOutResult)
                    navigator.goBack()
                }
            }
        }

        OnLifecycleStartEffect(viewModel) {
            viewModel.refreshTimestampNow()
        }

        PileSweepOutConfirmationSheet(
            sweepAmountState = viewModel.sweepAmountInput,
            state = state,
            actions = viewModel,
            onCancel = navigator::goBack,
        )
    }
}