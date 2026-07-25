package dev.ridill.oar.moneyPiles.presentation.nav

import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.OnLifecycleStartEffect
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.navigation.AddEditMoneyPileResult
import dev.ridill.oar.core.ui.navigation.AddEditMoneyPileRoute
import dev.ridill.oar.core.ui.navigation.AllMoneyPilesRoute
import dev.ridill.oar.core.ui.navigation.BottomSheetSceneStrategy
import dev.ridill.oar.core.ui.navigation.LocalResultBus
import dev.ridill.oar.core.ui.navigation.MoneyPileDetailsRoute
import dev.ridill.oar.core.ui.navigation.MoneyPileFundMovementRoute
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.core.ui.navigation.PileFundMovementResult
import dev.ridill.oar.core.ui.navigation.ResultEffect
import dev.ridill.oar.moneyPiles.presentation.addEditPile.AddEditPileScreen
import dev.ridill.oar.moneyPiles.presentation.addEditPile.AddEditPileViewModel
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesScreen
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesViewModel
import dev.ridill.oar.moneyPiles.presentation.movePileFund.MovePileFundSheetContent
import dev.ridill.oar.moneyPiles.presentation.movePileFund.MovePileFundViewModel
import dev.ridill.oar.moneyPiles.presentation.pileDetails.PileDetailScreen
import dev.ridill.oar.moneyPiles.presentation.pileDetails.PileDetailViewModel

fun EntryProviderScope<NavKey>.moneyPileEntries(
    navigator: OarNavigator,
    motionScheme: MotionScheme,
) {
    entry<AllMoneyPilesRoute> {
        val viewModel: AllPilesViewModel = hiltViewModel()
        val pilesPagingItems = viewModel.pilesPagingData.collectAsLazyPagingItems()
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
        CollectFlowEffect(viewModel.events, snackbarController) { event ->
            when (event) {
                is PileDetailViewModel.PileDetailEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(event.text)
                }
            }
        }

        ResultEffect<AddEditMoneyPileResult> { result ->
            viewModel.onAddEditPileResult(result)
        }

        ResultEffect<PileFundMovementResult> { result ->
            viewModel.onPileFundMovementResult(result)
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
                    navigator.goBack()
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
        metadata = BottomSheetSceneStrategy.bottomSheet()
    ) { route ->
        val viewModel: MovePileFundViewModel =
            hiltViewModel<MovePileFundViewModel, MovePileFundViewModel.Factory>(
                creationCallback = { it.create(route) }
            )
        val state by viewModel.state.collectAsStateWithLifecycle()
        val resultBus = LocalResultBus.current
        CollectFlowEffect(viewModel.events, resultBus) { event ->
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
            }
        }
        MovePileFundSheetContent(
            movement = route.movement,
            state = state,
            amountInputState = viewModel.amountInputState,
            actions = viewModel,
        )
    }
}