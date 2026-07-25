package dev.ridill.oar.moneyPiles.presentation.nav

import androidx.compose.material3.MotionScheme
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.navigation.AddEditMoneyPileRoute
import dev.ridill.oar.core.ui.navigation.AllMoneyPilesRoute
import dev.ridill.oar.core.ui.navigation.MoneyPileDetailsRoute
import dev.ridill.oar.core.ui.navigation.MoneyPileFundMovementRoute
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesScreen
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesViewModel

fun EntryProviderScope<NavKey>.moneyPileEntries(
    navigator: OarNavigator,
    motionScheme: MotionScheme,
) {
    entry<AllMoneyPilesRoute> {
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
    entry<MoneyPileDetailsRoute> {}
    entry<AddEditMoneyPileRoute> {}
    entry<MoneyPileFundMovementRoute> {}
}