package dev.ridill.oar.moneyPiles.presentation.nav

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.navigation.AllPilesRoute
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesScreen
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesViewModel

fun EntryProviderScope<NavKey>.moneyPileEntries(navigator: OarNavigator) {
    entry<AllPilesRoute> {
        val viewModel: AllPilesViewModel = hiltViewModel()
        val pilesPagingItems = viewModel.pilesPagingData.collectAsLazyPagingItems()
        val snackbarController = rememberSnackbarController()

        AllPilesScreen(
            snackbarController = snackbarController,
            pilesPagingItems = pilesPagingItems,
            navigateToAddPile = {},
            navigateToPileDetails = {},
            navigateToAddToPile = {},
            navigateUp = navigator::goBack,
        )
    }
}
