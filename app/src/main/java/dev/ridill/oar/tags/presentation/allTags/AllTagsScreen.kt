package dev.ridill.oar.tags.presentation.allTags

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.ConfirmationDialog
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.SearchField
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.listEmptyIndicator
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.elevation
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.isEmpty
import dev.ridill.oar.tags.domain.model.Tag
import dev.ridill.oar.tags.presentation.components.TagListItem

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AllTagsScreen(
    snackbarController: SnackbarController,
    tagsLazyPagingItems: LazyPagingItems<Tag>,
    tagSearchQueryState: TextFieldState,
    state: AllTagsState,
    actions: AllTagsActions,
    navigateUp: () -> Unit,
    navigateToAddEditTag: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val isTagsListEmpty by remember(tagsLazyPagingItems) {
        derivedStateOf { tagsLazyPagingItems.isEmpty() }
    }

    BackHandler(
        enabled = state.multiSelectionModeActive,
        onBack = actions::onMultiSelectionModeDismiss
    )

    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    OarScaffold(
        snackbarController = snackbarController,
        modifier = modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    if (state.multiSelectionModeActive) {
                        Text(stringResource(R.string.count_selected, state.selectedIds.size))
                    } else {
                        Text(stringResource(R.string.destination_all_tags))
                    }
                },
                navigationIcon = {
                    if (state.multiSelectionModeActive) {
                        IconButton(onClick = actions::onMultiSelectionModeDismiss) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.cd_clear_tag_selection)
                            )
                        }
                    } else {
                        BackArrowButton(onClick = navigateUp)
                    }
                },
                actions = {
                    if (state.multiSelectionModeActive) {
                        IconButton(onClick = actions::onDeleteTagsClick) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteForever,
                                contentDescription = stringResource(R.string.cd_delete_selected_tags)
                            )
                        }
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navigateToAddEditTag(null) }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.cd_create_new_tag)
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchField(
                state = tagSearchQueryState,
                placeholder = stringResource(R.string.search_tags),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    top = MaterialTheme.spacing.medium,
                    bottom = PaddingScrollEnd
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                listEmptyIndicator(
                    isListEmpty = isTagsListEmpty,
                    messageRes = R.string.all_tags_list_empty_message
                )

                items(
                    count = tagsLazyPagingItems.itemCount,
                    key = tagsLazyPagingItems.itemKey { it.id },
                    contentType = tagsLazyPagingItems.itemContentType { "TagListItem" }
                ) { index ->
                    tagsLazyPagingItems[index]?.let { item ->
                        val selected = item.id in state.selectedIds
                        val clickableModifier = if (state.multiSelectionModeActive) Modifier
                            .toggleable(
                                value = selected,
                                onValueChange = { actions.onTagSelectionChange(item.id) }
                            )
                        else Modifier.combinedClickable(
                            onClick = { navigateToAddEditTag(item.id) },
                            onClickLabel = stringResource(R.string.cd_tap_to_edit_transaction),
                            onLongClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                actions.onTagLongPress(item.id)
                            },
                            onLongClickLabel = stringResource(R.string.cd_long_press_to_toggle_selection)
                        )

                        TagListItem(
                            name = item.name,
                            color = item.color,
                            excluded = item.excluded,
                            createdTimestamp = item.createdTimestampFormatted,
                            tonalElevation = if (selected) MaterialTheme.elevation.level1 else MaterialTheme.elevation.level0,
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .then(clickableModifier)
                                .animateItem()
                        )
                    }
                }
            }
        }
    }

    if (state.showDeleteConfirmation) {
        ConfirmationDialog(
            title = pluralStringResource(
                R.plurals.delete_tags_confirmation_title,
                state.selectedIds.size
            ),
            content = stringResource(R.string.action_irreversible_message),
            onConfirm = actions::onDeleteConfirm,
            onDismiss = actions::onDeleteDismiss,
            additionalNote = stringResource(R.string.delete_tag_confirmation_note)
        )
    }
}