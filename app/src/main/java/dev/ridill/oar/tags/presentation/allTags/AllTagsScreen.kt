package dev.ridill.oar.tags.presentation.allTags

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
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
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.listEmptyIndicator
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.isEmpty
import dev.ridill.oar.tags.domain.model.Tag
import dev.ridill.oar.tags.presentation.components.TagListItem
import kotlinx.coroutines.launch

@Composable
fun AllTagsScreen(
    snackbarController: SnackbarController,
    tagsLazyPagingItems: LazyPagingItems<Tag>,
    tagSearchQueryState: TextFieldState,
    searchResults: List<Tag>,
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

    val searchBarState = rememberSearchBarState()
    LaunchedEffect(searchBarState) {
        snapshotFlow { searchBarState.currentValue }
            .collect { actions.onSearchBarValueChange(it) }
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    OarScaffold(
        snackbarController = snackbarController,
        modifier = modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            AllTagsTopAppBar(
                searchBarState = searchBarState,
                searchQueryState = tagSearchQueryState,
                onClearSearchQuery = actions::onClearSearchQuery,
                searchResults = searchResults,
                onSearchItemClick = { navigateToAddEditTag(it) },
                multiSelectionModeActive = state.multiSelectionModeActive,
                selectionCount = state.selectedIds.size,
                onMultiSelectionModeDismiss = actions::onMultiSelectionModeDismiss,
                onDeleteTagsClick = actions::onDeleteTagsClick,
                navigateUp = navigateUp,
                scrollBehavior = topAppBarScrollBehavior,
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
                    contentType = tagsLazyPagingItems.itemContentType { Tag::class }
                ) { index ->
                    tagsLazyPagingItems[index]?.let { item ->
                        val selected = item.id in state.selectedIds
                        TagListItem(
                            onClick = {
                                if (state.multiSelectionModeActive) actions
                                    .onTagSelectionChange(item.id)
                                else navigateToAddEditTag(item.id)
                            },
                            onLongClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                actions.onTagLongPress(item.id)
                            },
                            onLongClickLabel = stringResource(R.string.cd_toggle_selection),
                            name = item.name,
                            color = item.color,
                            excluded = item.excluded,
                            createdTimestamp = item.createdTimestampFormatted,
                            selected = selected,
                            modifier = Modifier
                                .fillParentMaxWidth()
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

@Composable
private fun AllTagsTopAppBar(
    searchBarState: SearchBarState,
    searchQueryState: TextFieldState,
    onClearSearchQuery: () -> Unit,
    searchResults: List<Tag>,
    onSearchItemClick: (Long) -> Unit,
    multiSelectionModeActive: Boolean,
    selectionCount: Int,
    onMultiSelectionModeDismiss: () -> Unit,
    onDeleteTagsClick: () -> Unit,
    navigateUp: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {
    val isQueryNotEmpty by remember {
        derivedStateOf { searchQueryState.text.isNotEmpty() }
    }
    val coroutineScope = rememberCoroutineScope()
    val inputField = @Composable {
        SearchBarDefaults.InputField(
            textFieldState = searchQueryState,
            searchBarState = searchBarState,
            onSearch = {
                coroutineScope.launch { searchBarState.animateToCollapsed() }
            },
            placeholder = {
                Text(text = stringResource(R.string.search_tags))
            },
            trailingIcon = if (isQueryNotEmpty) {
                {
                    IconButton(onClick = onClearSearchQuery) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cd_clear_search_query)
                        )
                    }
                }
            } else null,
            leadingIcon = {
                BackArrowButton(
                    onClick = { coroutineScope.launch { searchBarState.animateToCollapsed() } }
                )
            }
        )
    }

    MediumFlexibleTopAppBar(
        title = {
            if (multiSelectionModeActive) {
                Text(stringResource(R.string.count_selected, selectionCount))
            } else {
                Text(stringResource(R.string.destination_all_tags))
            }
        },
        navigationIcon = {
            if (multiSelectionModeActive) {
                IconButton(onClick = onMultiSelectionModeDismiss) {
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
            if (multiSelectionModeActive) {
                IconButton(onClick = onDeleteTagsClick) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteForever,
                        contentDescription = stringResource(R.string.cd_delete_selected_tags)
                    )
                }
            } else {
                IconButton(onClick = { coroutineScope.launch { searchBarState.animateToExpanded() } }) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(R.string.search_tags)
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )

    if (!multiSelectionModeActive) {
        ExpandedFullScreenSearchBar(
            state = searchBarState,
            inputField = inputField,
        ) {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = MaterialTheme.spacing.medium,
                    bottom = PaddingScrollEnd
                )
            ) {
                items(
                    items = searchResults,
                    key = { it.id },
                    contentType = { Tag::class }
                ) { item ->
                    TagListItem(
                        onClick = { onSearchItemClick(item.id) },
                        name = item.name,
                        color = item.color,
                        excluded = item.excluded,
                        createdTimestamp = item.createdTimestampFormatted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    )
                }
            }
        }
    }
}
