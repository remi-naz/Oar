package dev.ridill.oar.statistics.presentation.nav

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.navigation.AddEditTransactionRoute
import dev.ridill.oar.core.ui.navigation.CycleSelectedResult
import dev.ridill.oar.core.ui.navigation.CycleSelectionSheetRoute
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.core.ui.navigation.ResultEffect
import dev.ridill.oar.core.ui.navigation.StatisticsRoute
import dev.ridill.oar.statistics.presentation.StatisticsScreen
import dev.ridill.oar.statistics.presentation.StatisticsViewModel

fun EntryProviderScope<NavKey>.statisticsEntries(
    navigator: OarNavigator
) {
    entry<StatisticsRoute> {
        val viewModel: StatisticsViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val snackbarController = rememberSnackbarController()

        ResultEffect<CycleSelectedResult> { result ->
            viewModel.onCycleSelect(result.id)
        }

        StatisticsScreen(
            snackbarController = snackbarController,
            state = state,
            actions = viewModel,
            navigateUp = navigator::goBack,
            navigateToCycleSelection = {
                navigator.navigate(
                    CycleSelectionSheetRoute(
                        preselectedId = state.selectedCycle?.id ?: INVALID_ID_LONG
                    )
                )
            },
            navigateToAddEditTransaction = {
                navigator.navigate(AddEditTransactionRoute())
            }
        )
    }
}
