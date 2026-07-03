package dev.ridill.oar.tags.presentation.nav

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.navigation.AddEditTagSheetRoute
import dev.ridill.oar.core.ui.navigation.AllTagsRoute
import dev.ridill.oar.core.ui.navigation.BottomSheetSceneStrategy
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.navigation.LocalResultBus
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.core.ui.navigation.TagSavedResult
import dev.ridill.oar.core.ui.navigation.TagSelectedResult
import dev.ridill.oar.core.ui.navigation.TagSelectionSheetRoute
import dev.ridill.oar.tags.presentation.addEditTag.AddEditTagSheet
import dev.ridill.oar.tags.presentation.addEditTag.AddEditTagViewModel
import dev.ridill.oar.tags.presentation.allTags.AllTagsScreen
import dev.ridill.oar.tags.presentation.allTags.AllTagsViewModel
import dev.ridill.oar.tags.presentation.tagSelection.SingleTagSelectionSheetContent

// region Tags

fun EntryProviderScope<NavKey>.tagEntries(navigator: OarNavigator) {
    entry<AllTagsRoute> {
        val viewModel: AllTagsViewModel = hiltViewModel()
        val searchQueryState = viewModel.searchQueryState
        val tagsLazyPagingItems = viewModel.allTagsPagingData.collectAsLazyPagingItems()
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()

        AllTagsScreen(
            snackbarController = snackbarController,
            tagsLazyPagingItems = tagsLazyPagingItems,
            tagSearchQueryState = searchQueryState,
            state = state,
            actions = viewModel,
            navigateUp = navigator::goBack,
            navigateToAddEditTag = { tagId ->
                navigator.navigate(AddEditTagSheetRoute(tagId = tagId ?: INVALID_ID_LONG))
            }
        )
    }

    entry<AddEditTagSheetRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
        val viewModel = hiltViewModel<AddEditTagViewModel, AddEditTagViewModel.Factory>(
            creationCallback = { it.create(key) }
        )
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val input = viewModel.tagInput.collectAsStateWithLifecycle()
        val nameState = viewModel.nameInputState
        val error by viewModel.tagInputError.collectAsStateWithLifecycle()
        val showDeleteTagConfirmation by viewModel.showTagDeleteConfirmation.collectAsStateWithLifecycle()

        val isEditMode = key.tagId != INVALID_ID_LONG
        val resultBus = LocalResultBus.current

        CollectFlowEffect(flow = viewModel.events) { event ->
            when (event) {
                is AddEditTagViewModel.AddEditTagEvent.TagSaved -> {
                    resultBus.sendResult<TagSavedResult>(TagSavedResult(event.tagId))
                    navigator.goBack()
                }

                AddEditTagViewModel.AddEditTagEvent.TagDeleted -> {
                    navigator.goBack()
                }
            }
        }

        AddEditTagSheet(
            isLoading = isLoading,
            nameState = nameState,
            selectedColorCode = { input.value.colorCode },
            excluded = { input.value.excluded },
            errorMessage = error,
            isEditMode = isEditMode,
            showDeleteTagConfirmation = showDeleteTagConfirmation,
            actions = viewModel
        )
    }

    entry<TagSelectionSheetRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
        val preSelectedId = key.preselectedId.takeIf { it != INVALID_ID_LONG }
        val resultBus = LocalResultBus.current

        SingleTagSelectionSheetContent(
            preSelectedId = preSelectedId,
            onConfirm = { selectedId ->
                resultBus.sendResult<TagSelectedResult>(TagSelectedResult(selectedId))
                navigator.goBack()
            }
        )
    }
}

// endregion
