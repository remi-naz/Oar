package dev.ridill.oar.core.ui.navigation.destinations

import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.FloatingWindowNavigationResultEffect
import dev.ridill.oar.core.ui.components.NavigationResultEffect
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.transactions.presentation.allTransactions.AllTransactionsScreen
import dev.ridill.oar.transactions.presentation.allTransactions.AllTransactionsViewModel

data object AllTransactionsScreenSpec : ScreenSpec {

    override val route: String
        get() = "all_transactions"

    override val labelRes: Int
        get() = R.string.destination_all_transactions

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: AllTransactionsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()
        val transactionsLazyPagingItems = viewModel.transactionsPagingData
            .collectAsLazyPagingItems()
        val searchResults = viewModel.searchResults.collectAsLazyPagingItems()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        FloatingWindowNavigationResultEffect(
            resultKey = FolderSelectionSheetSpec.SELECTED_FOLDER_ID,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            snackbarController,
            context,
            onResult = viewModel::onFolderSelect
        )

        NavigationResultEffect(
            resultKey = AddEditTxResult::name.name,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            onResult = viewModel::onAddEditTxNavResult
        )

        FloatingWindowNavigationResultEffect<Long>(
            resultKey = AddEditFolderSheetSpec.ACTION_FOLDER_SAVED,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            navController
        ) { id ->
            navController.navigate(
                FolderDetailsScreenSpec.routeWithArgs(id)
            )
        }

        FloatingWindowNavigationResultEffect<Long>(
            resultKey = CycleSelectionSheetSpec.SELECTED_CYCLE_ID,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            onResult = viewModel::onCycleSelect
        )

        FloatingWindowNavigationResultEffect<Long?>(
            resultKey = TagSelectionSheetSpec.SELECTED_TAG_ID,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            onResult = viewModel::onTagAssignmentSelectionResult
        )

        CollectFlowEffect(viewModel.events, context, snackbarController) { event ->
            when (event) {
                is AllTransactionsViewModel.AllTransactionsEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                AllTransactionsViewModel.AllTransactionsEvent.NavigateToFolderSelection -> {
                    navController.navigate(FolderSelectionSheetSpec.routeWithArgs(null))
                }

                is AllTransactionsViewModel.AllTransactionsEvent.NavigateToTagSelectionForAssignment -> {
                    navController.navigate(
                        TagSelectionSheetSpec.routeWithArgs()
                    )
                }

                AllTransactionsViewModel.AllTransactionsEvent.ScheduleSaved -> {
                    snackbarController.showSnackbar(
                        message = context.getString(R.string.schedule_saved),
                        actionLabel = context.getString(R.string.action_view),
                        onSnackbarResult = { result ->
                            if (result == SnackbarResult.ActionPerformed) {
                                navController.navigate(SchedulesGraphSpec.route)
                            }
                        }
                    )
                }

                is AllTransactionsViewModel.AllTransactionsEvent.NavigateToAddEditTx -> {
                    navController.navigate(
                        AddEditTransactionScreenSpec.routeWithArg(event.id)
                    )
                }

                AllTransactionsViewModel.AllTransactionsEvent.ChooseCycleForTransactions -> {
                    navController.navigate(CycleSelectionSheetSpec.route)
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
            navigateUp = navController::navigateUp,
            navigateToAddEditTransaction = {
                navController.navigate(
                    AddEditTransactionScreenSpec.routeWithArg(transactionId = it)
                )
            },
            navigateToCreateSchedule = {
                navController.navigate(
                    AddEditTransactionScreenSpec.routeWithArg(
                        isScheduleTxMode = true
                    )
                )
            },
            navigateToCreateFolder = { navController.navigate(AddEditFolderSheetSpec.routeWithArg()) }
        )
    }
}