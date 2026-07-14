package dev.ridill.oar.moneyPiles.presentation.nav

import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.navigation.AddEditPileRoute
import dev.ridill.oar.core.ui.navigation.AllPilesRoute
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.moneyPiles.presentation.addEditPile.AddEditPileScreen
import dev.ridill.oar.moneyPiles.presentation.addEditPile.AddEditPileViewModel
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesScreen
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesState
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesViewModel

fun EntryProviderScope<NavKey>.moneyPileEntries(
    navigator: OarNavigator,
    motionScheme: MotionScheme,
) {
    entry<AllPilesRoute> {
        val viewModel: AllPilesViewModel = hiltViewModel()
        val pilesPagingItems = viewModel.pilesPagingData.collectAsLazyPagingItems()
        val snackbarController = rememberSnackbarController()

        AllPilesScreen(
            snackbarController = snackbarController,
            state = AllPilesState(),
            pilesPagingItems = pilesPagingItems,
            navigateToAddPile = { navigator.navigate(AddEditPileRoute()) },
            navigateToPileDetails = {},
            navigateToAddToPile = {},
            navigateUp = navigator::goBack,
        )
    }

    entry<AddEditPileRoute> { key ->
        val viewModel: AddEditPileViewModel =
            hiltViewModel<AddEditPileViewModel, AddEditPileViewModel.Factory>(
                creationCallback = { it.create(key) }
            )

        val state by viewModel.state.collectAsStateWithLifecycle()
        AddEditPileScreen(
            state = state,
            actions = viewModel,
            navigateUp = navigator::goBack,
            isEditMode = false,
            nameState = viewModel.nameState,
            starterAmountState = viewModel.starterAmountState,
            targetAmountState = viewModel.targetAmountState,
            reminderAmountState = viewModel.reminderAmountState,
        )
    }
}