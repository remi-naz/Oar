package dev.ridill.oar.core.ui.navigation.destinations

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.core.domain.util.NewLine
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.FloatingWindowNavigationResultEffect
import dev.ridill.oar.core.ui.components.navigateUpWithResult
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.transactions.presentation.addEditTransaction.AddEditTransactionScreen
import dev.ridill.oar.transactions.presentation.addEditTransaction.AddEditTransactionViewModel
import java.util.Currency

data object AddEditTransactionScreenSpec : ScreenSpec {
    override val route: String
        get() = """
        add_edit_transaction/
        {$ARG_TRANSACTION_ID}
        ?$ARG_LINK_FOLDER_ID={$ARG_LINK_FOLDER_ID}
        &$ARG_IS_SCHEDULE_MODE_ACTIVE={$ARG_IS_SCHEDULE_MODE_ACTIVE}
        &$ARG_IS_DUPLICATE_MODE={$ARG_IS_DUPLICATE_MODE}
    """.trimIndent()
            .replace(String.NewLine, String.Empty)

    override val labelRes: Int
        get() = R.string.destination_add_edit_transaction

    override val arguments: List<NamedNavArgument>
        get() = listOf(
            navArgument(ARG_TRANSACTION_ID) {
                type = NavType.LongType
                nullable = false
                defaultValue = NavDestination.ARG_INVALID_ID_LONG
            },
            navArgument(ARG_LINK_FOLDER_ID) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(ARG_IS_SCHEDULE_MODE_ACTIVE) {
                type = NavType.BoolType
                nullable = false
                defaultValue = false
            },
            navArgument(ARG_IS_DUPLICATE_MODE) {
                type = NavType.BoolType
                nullable = false
                defaultValue = false
            }
        )

    override val deepLinks: List<NavDeepLink>
        get() = listOf(
            navDeepLink { uriPattern = DEEPLINK_URI_PATTERN },
        )

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
        get() = { slideInVertically { it } }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?
        get() = { slideOutVertically { it } }

    fun routeWithArg(
        transactionId: Long? = null,
        folderId: Long? = null,
        isScheduleTxMode: Boolean = false,
        isDuplicateMode: Boolean = false
    ): String = route
        .replace(
            oldValue = "{$ARG_TRANSACTION_ID}",
            newValue = (transactionId ?: NavDestination.ARG_INVALID_ID_LONG).toString()
        )
        .replace(
            oldValue = "{$ARG_LINK_FOLDER_ID}",
            newValue = folderId?.toString().orEmpty()
        )
        .replace(
            oldValue = "{$ARG_IS_SCHEDULE_MODE_ACTIVE}",
            newValue = isScheduleTxMode.toString()
        )
        .replace(
            oldValue = "{$ARG_IS_DUPLICATE_MODE}",
            newValue = isDuplicateMode.toString()
        )

    fun getTransactionIdFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long =
        savedStateHandle.get<Long>(ARG_TRANSACTION_ID) ?: NavDestination.ARG_INVALID_ID_LONG

    fun getFolderIdToLinkFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long? =
        savedStateHandle.get<String?>(ARG_LINK_FOLDER_ID)?.toLongOrNull()

    fun getIsScheduleModeFromSavedStateHandle(savedStateHandle: SavedStateHandle): Boolean =
        savedStateHandle.get<Boolean>(ARG_IS_SCHEDULE_MODE_ACTIVE) == true

    fun getIsDuplicateModeFromSavedStateHandle(savedStateHandle: SavedStateHandle): Boolean =
        savedStateHandle.get<Boolean>(ARG_IS_DUPLICATE_MODE) == true

    private fun isArgDuplicateMode(navBackStackEntry: NavBackStackEntry): Boolean =
        navBackStackEntry.arguments?.getBoolean(ARG_IS_DUPLICATE_MODE) == true

    private fun isArgEditMode(navBackStackEntry: NavBackStackEntry): Boolean =
        navBackStackEntry.arguments?.getLong(ARG_TRANSACTION_ID) != NavDestination.ARG_INVALID_ID_LONG

    fun buildDeeplink(id: Long?): Uri = DEEPLINK_URI_PATTERN
        .replace(
            oldValue = "{$ARG_TRANSACTION_ID}",
            newValue = (id ?: NavDestination.ARG_INVALID_ID_LONG).toString()
        )
        .toUri()

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: AddEditTransactionViewModel = hiltViewModel(navBackStackEntry)
        val amountInputState = viewModel.amountInputState
        val noteInputState = viewModel.noteInputState
        val state by viewModel.state.collectAsStateWithLifecycle()

        val isEditMode = isArgEditMode(navBackStackEntry)
        val isDuplicateMode = isArgDuplicateMode(navBackStackEntry)

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        FloatingWindowNavigationResultEffect(
            resultKey = FolderSelectionSheetSpec.SELECTED_FOLDER_ID,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            onResult = viewModel::onFolderSelectionResult
        )

        FloatingWindowNavigationResultEffect(
            resultKey = AmountTransformationSheetSpec.TRANSFORMATION_RESULT,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            onResult = viewModel::onAmountTransformationResult
        )

        FloatingWindowNavigationResultEffect<Currency>(
            resultKey = CurrencySelectionSheetSpec.SELECTED_CURRENCY,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            onResult = viewModel::onCurrencySelect
        )

        FloatingWindowNavigationResultEffect<Long?>(
            resultKey = CycleSelectionSheetSpec.SELECTED_CYCLE_ID,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            onResult = viewModel::onCycleSelect
        )

        CollectFlowEffect(viewModel.events, snackbarController, context) { event ->
            when (event) {
                is AddEditTransactionViewModel.AddEditTransactionEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        message = event.uiText.asString(context),
                        isError = event.uiText.isErrorText
                    )
                }

                is AddEditTransactionViewModel.AddEditTransactionEvent.LaunchFolderSelection -> {
                    navController.navigate(
                        FolderSelectionSheetSpec.routeWithArgs(event.preselectedId)
                    )
                }

                is AddEditTransactionViewModel.AddEditTransactionEvent.NavigateUpWithResult -> {
                    navController.navigateUpWithResult<AddEditTxResult>(
                        AddEditTxResult::name.name,
                        event.result
                    )
                }

                is AddEditTransactionViewModel.AddEditTransactionEvent.NavigateToDuplicateTransactionCreation -> {
                    navController.navigate(
                        routeWithArg(
                            transactionId = event.id,
                            isDuplicateMode = true
                        )
                    ) {
                        navBackStackEntry.destination.route
                            ?.let {
                                popUpTo(it) {
                                    this.inclusive = true
                                }
                            }
                    }
                }
            }
        }

        AddEditTransactionScreen(
            isEditMode = isEditMode,
            isDuplicateMode = isDuplicateMode,
            snackbarController = snackbarController,
            amountInputState = amountInputState,
            noteInputState = noteInputState,
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp,
            navigateToAmountTransformation = {
                navController.navigate(AmountTransformationSheetSpec.route)
            },
            navigateToCurrencySelection = {
                navController.navigate(CurrencySelectionSheetSpec.routeWithArg(state.currency.currencyCode))
            },
            navigateToCycleSelection = {
                navController.navigate(CycleSelectionSheetSpec.routeWithArgs(state.selectedCycleId))
            }
        )
    }
}

enum class AddEditTxResult {
    TRANSACTION_DELETED,
    TRANSACTION_SAVED,
    SCHEDULE_SAVED
}

const val ARG_TRANSACTION_ID = "ARG_TRANSACTION_ID"
private const val ARG_LINK_FOLDER_ID = "ARG_LINK_FOLDER_ID"
private const val ARG_IS_SCHEDULE_MODE_ACTIVE = "ARG_IS_SCHEDULE_MODE_ACTIVE"
private const val ARG_IS_DUPLICATE_MODE = "ARG_IS_DUPLICATE_MODE"

private const val DEEPLINK_URI_PATTERN =
    "${NavDestination.DEEP_LINK_URI}/add_edit_transaction?$ARG_TRANSACTION_ID={$ARG_TRANSACTION_ID}"