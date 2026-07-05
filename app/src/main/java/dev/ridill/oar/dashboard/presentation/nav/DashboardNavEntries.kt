package dev.ridill.oar.dashboard.presentation.nav

import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.navigation.AddEditScheduleResult
import dev.ridill.oar.core.ui.navigation.AddEditScheduleRoute
import dev.ridill.oar.core.ui.navigation.AddEditTransactionRoute
import dev.ridill.oar.core.ui.navigation.AddEditTxResult
import dev.ridill.oar.core.ui.navigation.AllSchedulesRoute
import dev.ridill.oar.core.ui.navigation.AllTransactionsRoute
import dev.ridill.oar.core.ui.navigation.DashboardRoute
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.core.ui.navigation.ResultEffect
import dev.ridill.oar.dashboard.presentation.DashboardScreen
import dev.ridill.oar.dashboard.presentation.DashboardViewModel

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
        ResultEffect<AddEditScheduleResult> { result ->
            viewModel.onAddEditScheduleNavResult(result)
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
            navigateToAddEditTransaction = { id ->
                navigator.navigate(AddEditTransactionRoute(transactionId = id ?: INVALID_ID_LONG))
            },
            navigateToAddEditSchedule = { id ->
                navigator.navigate(AddEditScheduleRoute(scheduleId = id))
            },
            navigateTo = { navigator.navigate(it) }
        )
    }
}

// endregion
