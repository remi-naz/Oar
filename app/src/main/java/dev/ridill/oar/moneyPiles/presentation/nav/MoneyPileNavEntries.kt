package dev.ridill.oar.moneyPiles.presentation.nav

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.navigation.AddEditPileResult
import dev.ridill.oar.core.ui.navigation.AddEditPileRoute
import dev.ridill.oar.core.ui.navigation.AllPilesRoute
import dev.ridill.oar.core.ui.navigation.LocalResultBus
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.core.ui.navigation.ResultEffect
import dev.ridill.oar.moneyPiles.presentation.addEditPile.AddEditPileScreen
import dev.ridill.oar.moneyPiles.presentation.addEditPile.AddEditPileViewModel
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesScreen
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesViewModel

fun EntryProviderScope<NavKey>.moneyPileEntries(navigator: OarNavigator) {
    entry<AllPilesRoute> {
        val viewModel: AllPilesViewModel = hiltViewModel()
        val pilesPagingItems = viewModel.pilesPagingData.collectAsLazyPagingItems()
        val snackbarController = rememberSnackbarController()

        ResultEffect<AddEditPileResult> { result ->
            viewModel.onAddEditPileResult(result)
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
            navigateToPileDetails = { navigator.navigate(AddEditPileRoute(it)) },
            navigateToAddToPile = {},
            navigateUp = navigator::goBack,
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
}
