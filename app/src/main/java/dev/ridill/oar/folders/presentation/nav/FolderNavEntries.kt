package dev.ridill.oar.folders.presentation.nav

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
import dev.ridill.oar.core.ui.navigation.AddEditFolderSheetRoute
import dev.ridill.oar.core.ui.navigation.AddEditTransactionRoute
import dev.ridill.oar.core.ui.navigation.AllFoldersRoute
import dev.ridill.oar.core.ui.navigation.BottomSheetSceneStrategy
import dev.ridill.oar.core.ui.navigation.FolderDeletedResult
import dev.ridill.oar.core.ui.navigation.FolderDetailsRoute
import dev.ridill.oar.core.ui.navigation.FolderSavedResult
import dev.ridill.oar.core.ui.navigation.FolderSelectedResult
import dev.ridill.oar.core.ui.navigation.FolderSelectionSheetRoute
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.navigation.LocalResultBus
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.core.ui.navigation.ResultEffect
import dev.ridill.oar.folders.presentation.addEditFolder.AddEditFolderSheet
import dev.ridill.oar.folders.presentation.addEditFolder.AddEditFolderViewModel
import dev.ridill.oar.folders.presentation.allFolders.AllFoldersScreen
import dev.ridill.oar.folders.presentation.allFolders.AllFoldersViewModel
import dev.ridill.oar.folders.presentation.folderDetails.FolderDetailsScreen
import dev.ridill.oar.folders.presentation.folderDetails.FolderDetailsViewModel
import dev.ridill.oar.folders.presentation.folderSelection.FolderSelectionSheet
import dev.ridill.oar.folders.presentation.folderSelection.FolderSelectionViewModel

// region Folders

@Suppress("LongMethod")
fun EntryProviderScope<NavKey>.folderEntries(navigator: OarNavigator) {
    entry<AllFoldersRoute> {
        val viewModel: AllFoldersViewModel = hiltViewModel()
        val foldersPagingItems = viewModel.folderListPagingData.collectAsLazyPagingItems()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        ResultEffect<FolderSavedResult> { result ->
            navigator.navigate(FolderDetailsRoute(result.id))
        }
        ResultEffect<FolderDeletedResult> { _ ->
            snackbarController.showSnackbar(context.getString(R.string.transaction_folder_deleted))
        }

        AllFoldersScreen(
            snackbarController = snackbarController,
            foldersPagingItems = foldersPagingItems,
            navigateToFolderDetails = { navigator.navigate(FolderDetailsRoute(it)) },
            navigateUp = navigator::goBack,
            navigateToAddFolder = { navigator.navigate(AddEditFolderSheetRoute()) }
        )
    }

    entry<FolderDetailsRoute> { key ->
        val viewModel = hiltViewModel<FolderDetailsViewModel, FolderDetailsViewModel.Factory>(
            creationCallback = { it.create(key) }
        )
        val state by viewModel.state.collectAsStateWithLifecycle()
        val transactionPagingItems = viewModel.transactionPagingData.collectAsLazyPagingItems()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        val resultBus = LocalResultBus.current

        CollectFlowEffect(viewModel.events, context, snackbarController) { event ->
            when (event) {
                is FolderDetailsViewModel.FolderDetailsEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                FolderDetailsViewModel.FolderDetailsEvent.FolderDeleted -> {
                    resultBus.sendResult<FolderDeletedResult>(FolderDeletedResult)
                    navigator.goBack()
                }

                is FolderDetailsViewModel.FolderDetailsEvent.TransactionRemovedFromFolder -> {
                    snackbarController.showSnackbar(
                        message = context.resources.getQuantityString(
                            R.plurals.transaction_removed_from_this_folder, 1
                        ),
                        actionLabel = context.getString(R.string.action_undo),
                        onSnackbarResult = {
                            if (it == SnackbarResult.ActionPerformed) {
                                viewModel.onRemoveTransactionUndo(event.txId)
                            }
                        }
                    )
                }
            }
        }

        FolderDetailsScreen(
            snackbarController = snackbarController,
            transactionPagingItems = transactionPagingItems,
            state = state,
            actions = viewModel,
            navigateToAddEditTransaction = { transactionId ->
                navigator.navigate(
                    AddEditTransactionRoute(
                        transactionId = transactionId ?: INVALID_ID_LONG,
                        linkFolderId = key.folderId
                    )
                )
            },
            navigateToEditFolder = { navigator.navigate(AddEditFolderSheetRoute(folderId = key.folderId)) },
            navigateUp = navigator::goBack
        )
    }

    entry<AddEditFolderSheetRoute>(
        metadata = BottomSheetSceneStrategy.bottomSheet()
    ) { key ->
        val viewModel = hiltViewModel<AddEditFolderViewModel, AddEditFolderViewModel.Factory>(
            creationCallback = { it.create(key) }
        )
        val input = viewModel.folderInput.collectAsStateWithLifecycle()
        val folderNameState = viewModel.folderNameState
        val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

        val editMode = key.folderId != INVALID_ID_LONG
        val resultBus = LocalResultBus.current

        CollectFlowEffect(flow = viewModel.events) { event ->
            when (event) {
                is AddEditFolderViewModel.AddEditFolderEvent.FolderSaved -> {
                    resultBus.sendResult<FolderSavedResult>(FolderSavedResult(event.tagId))
                    navigator.goBack()
                }
            }
        }

        AddEditFolderSheet(
            isLoading = isLoading,
            nameState = folderNameState,
            excluded = { input.value.excluded },
            errorMessage = errorMessage,
            isEditMode = editMode,
            actions = viewModel
        )
    }

    entry<FolderSelectionSheetRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
        val viewModel = hiltViewModel<FolderSelectionViewModel, FolderSelectionViewModel.Factory>(
            creationCallback = { it.create(key) }
        )
        val searchQueryState = viewModel.searchQueryState
        val foldersList = viewModel.folderListPaged.collectAsLazyPagingItems()
        val selectedId by viewModel.selectedFolderId.collectAsStateWithLifecycle()

        val resultBus = LocalResultBus.current

        ResultEffect<FolderSavedResult> { result ->
            viewModel.onFolderSelect(result.id)
        }

        FolderSelectionSheet(
            queryState = searchQueryState,
            foldersListLazyPagingItems = foldersList,
            onFolderSelect = viewModel::onFolderSelect,
            onCreateNewClick = { navigator.navigate(AddEditFolderSheetRoute()) },
            onClearSelectionClick = {
                resultBus.sendResult<FolderSelectedResult>(FolderSelectedResult(INVALID_ID_LONG))
                navigator.goBack()
            },
            onConfirm = {
                resultBus.sendResult<FolderSelectedResult>(
                    FolderSelectedResult(
                        selectedId ?: INVALID_ID_LONG
                    )
                )
                navigator.goBack()
            },
            selectedId = selectedId
        )
    }
}

// endregion
