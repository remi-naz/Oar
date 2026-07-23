package dev.ridill.oar.moneyPiles.presentation.nav

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.navigation.AddEditPileResult
import dev.ridill.oar.core.ui.navigation.AddEditPileRoute
import dev.ridill.oar.core.ui.navigation.AddToPileRoute
import dev.ridill.oar.core.ui.navigation.AllPilesRoute
import dev.ridill.oar.core.ui.navigation.BottomSheetSceneStrategy
import dev.ridill.oar.core.ui.navigation.FundAddedToPile
import dev.ridill.oar.core.ui.navigation.LocalResultBus
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.core.ui.navigation.PileDetailRoute
import dev.ridill.oar.core.ui.navigation.ResultEffect
import dev.ridill.oar.moneyPiles.presentation.addEditPile.AddEditPileScreen
import dev.ridill.oar.moneyPiles.presentation.addEditPile.AddEditPileViewModel
import dev.ridill.oar.moneyPiles.presentation.addToPile.AddToPileSheetContent
import dev.ridill.oar.moneyPiles.presentation.addToPile.AddToPileViewModel
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesScreen
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesViewModel
import dev.ridill.oar.moneyPiles.presentation.pileDetails.PileDetailScreen
import dev.ridill.oar.moneyPiles.presentation.pileDetails.PileDetailViewModel

fun EntryProviderScope<NavKey>.moneyPileEntries(navigator: OarNavigator) {
    entry<AllPilesRoute> {
        val viewModel: AllPilesViewModel = hiltViewModel()
        val pilesPagingItems = viewModel.pilesPagingData.collectAsLazyPagingItems()
        val snackbarController = rememberSnackbarController()

        ResultEffect<AddEditPileResult> { result ->
            viewModel.onAddEditPileResult(result)
        }

        ResultEffect<FundAddedToPile> {
            viewModel.onFundAddedToPile()
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
            navigateToAddPile = { navigator.navigate(AddEditPileRoute()) },
            navigateToPileDetails = { navigator.navigate(PileDetailRoute(it)) },
            navigateToAddToPile = {
                navigator.navigate(
                    AddToPileRoute(
                        pileId = it,
                        movement = FundMovement.IN
                    )
                )
            },
            navigateUp = navigator::goBack,
        )
    }

    entry<PileDetailRoute> { key ->
        val viewModel = hiltViewModel<PileDetailViewModel, PileDetailViewModel.Factory>(
            creationCallback = { it.create(key) }
        )
        val state by viewModel.state.collectAsStateWithLifecycle()
        val transactionPagingItems = viewModel.transactionPagingData.collectAsLazyPagingItems()

        val snackbarController = rememberSnackbarController()
        val resultBus = LocalResultBus.current

        ResultEffect<FundAddedToPile> {
            viewModel.onFundAddedToPile()
        }

        ResultEffect<AddEditPileResult> { result ->
            when (result) {
                AddEditPileResult.PILE_SAVED -> viewModel.onPileSaved()
                AddEditPileResult.PILE_DELETED -> {
                    resultBus.sendResult(AddEditPileResult.PILE_DELETED)
                    navigator.goBack()
                }
            }
        }

        CollectFlowEffect(viewModel.events) { event ->
            when (event) {
                is PileDetailViewModel.PileDetailEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(event.text)
                }
            }
        }

        PileDetailScreen(
            state = state,
            transactionPagingItems = transactionPagingItems,
            snackbarController = snackbarController,
            navigateUp = navigator::goBack,
            navigateToEditPile = { navigator.navigate(AddEditPileRoute(key.pileId)) },
            navigateToAddToPile = { movement ->
                navigator.navigate(AddToPileRoute(pileId = key.pileId, movement = movement))
            },
        )
    }

    entry<AddEditPileRoute> { route ->
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
                    resultBus.sendResult(AddEditPileResult.PILE_SAVED)
                    navigator.goBack()
                }

                is AddEditPileViewModel.AddEditPileEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(event.text)
                }

                AddEditPileViewModel.AddEditPileEvent.PileDeleted -> {
                    resultBus.sendResult(AddEditPileResult.PILE_DELETED)
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

    entry<AddToPileRoute>(
        metadata = BottomSheetSceneStrategy.bottomSheet()
    ) { route ->
        val viewModel: AddToPileViewModel =
            hiltViewModel<AddToPileViewModel, AddToPileViewModel.Factory>(
                creationCallback = { it.create(route) }
            )
        val state by viewModel.state.collectAsStateWithLifecycle()
        val resultBus = LocalResultBus.current
        CollectFlowEffect(viewModel.events, resultBus) { event ->
            when (event) {
                AddToPileViewModel.AddToPileEvent.Deposited -> {
                    resultBus.sendResult(FundAddedToPile)
                    navigator.goBack()
                }
            }
        }
        AddToPileSheetContent(
            movement = route.movement,
            state = state,
            amountInputState = viewModel.amountInputState,
            actions = viewModel,
        )
    }
}
