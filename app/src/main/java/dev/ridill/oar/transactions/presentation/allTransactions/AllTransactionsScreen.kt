package dev.ridill.oar.transactions.presentation.allTransactions

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.oar.R
import dev.ridill.oar.aggregations.presentation.AmountAggregatesList
import dev.ridill.oar.budgetCycles.domain.model.CycleIndicator
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.ConfirmationDialog
import dev.ridill.oar.core.ui.components.CreateFloatingActionMenu
import dev.ridill.oar.core.ui.components.CreateOption
import dev.ridill.oar.core.ui.components.DisplaySmallText
import dev.ridill.oar.core.ui.components.FadedVisibility
import dev.ridill.oar.core.ui.components.ItemListSheet
import dev.ridill.oar.core.ui.components.ListLabel
import dev.ridill.oar.core.ui.components.ListSeparator
import dev.ridill.oar.core.ui.components.OarModalBottomSheet
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.OptionListItem
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.listEmptyIndicator
import dev.ridill.oar.core.ui.components.slideInHorizontallyWithFadeIn
import dev.ridill.oar.core.ui.components.slideInVerticallyWithFadeIn
import dev.ridill.oar.core.ui.components.slideOutHorizontallyWithFadeOut
import dev.ridill.oar.core.ui.components.slideOutVerticallyWithFadeOut
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.elevation
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.core.ui.util.isEmpty
import dev.ridill.oar.settings.presentation.components.SwitchPreference
import dev.ridill.oar.tags.presentation.tagSelection.TagSelectionField
import dev.ridill.oar.transactions.domain.model.AllTransactionsMultiSelectionOption
import dev.ridill.oar.transactions.domain.model.TransactionEntry
import dev.ridill.oar.transactions.domain.model.TransactionListItemUIModel
import dev.ridill.oar.transactions.domain.model.TransactionTypeFilter
import dev.ridill.oar.transactions.presentation.components.TransactionListItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun AllTransactionsScreen(
    snackbarController: SnackbarController,
    transactionsLazyPagingItems: LazyPagingItems<TransactionListItemUIModel>,
    searchQueryState: TextFieldState,
    searchResultsLazyPagingItems: LazyPagingItems<TransactionEntry>,
    state: AllTransactionsState,
    actions: AllTransactionsActions,
    navigateToAddEditTransaction: (Long?) -> Unit,
    navigateToCreateSchedule: () -> Unit,
    navigateToCreateFolder: () -> Unit,
    navigateUp: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val areTransactionsEmpty by remember {
        derivedStateOf { transactionsLazyPagingItems.isEmpty() }
    }

    BackHandler(
        enabled = state.transactionMultiSelectionModeActive,
        onBack = actions::onDismissMultiSelectionMode
    )

    BackHandler(
        enabled = state.searchModeActive,
        onBack = { actions.onSearchModeToggle(false) }
    )

    val coroutineScope = rememberCoroutineScope()
    var listScrollJob: Job? = remember { null }
    val transactionListState = rememberLazyListState()
    val showScrollUpButton by remember {
        derivedStateOf { transactionListState.firstVisibleItemIndex >= 5 }
    }

    OarScaffold(
        snackbarController = snackbarController,
        topBar = {
            AllTransactionsTopAppBar(
                searchModeActive = state.searchModeActive,
                onSearchModeToggle = actions::onSearchModeToggle,
                searchQueryState = searchQueryState,
                onClearSearchQuery = actions::onClearSearchQuery,
                searchResults = searchResultsLazyPagingItems,
                onFilterOptionsClick = actions::onFilterOptionsClick,
                multiSelectionModeActive = state.transactionMultiSelectionModeActive,
                selectionCount = state.selectedTransactionIds.size,
                onDismissMultiSelectionMode = actions::onDismissMultiSelectionMode,
                onMultiSelectionOptionsClick = actions::onMultiSelectionOptionsClick,
                onSearchItemClick = { navigateToAddEditTransaction(it) },
                navigateUp = navigateUp
            )
        },
        floatingActionButton = {
            FadedVisibility(!state.searchModeActive) {
                CreateFloatingActionMenu(
                    onOptionClick = { option ->
                        when (option) {
                            CreateOption.CREATE_TRANSACTION -> {
                                navigateToAddEditTransaction(null)
                            }

                            CreateOption.CREATE_SCHEDULE -> {
                                navigateToCreateSchedule()
                            }

                            CreateOption.CREATE_FOLDER -> {
                                navigateToCreateFolder()
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = state.showAggregates,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                AmountAggregatesList(
                    aggregatesList = state.aggregatesList,
                    modifier = Modifier
                )
            }
        }
    ) { paddingValues ->
        val localLayoutDirection = LocalLayoutDirection.current
        Column(
            modifier = Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(localLayoutDirection),
                    end = paddingValues.calculateEndPadding(localLayoutDirection)
                )
        ) {
            TransactionListLabel(
                listLabel = state.transactionListLabel,
                modifier = Modifier
                    .padding(top = MaterialTheme.spacing.medium)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    state = transactionListState,
                    modifier = Modifier
                        .matchParentSize(),
                    contentPadding = PaddingValues(
                        top = MaterialTheme.spacing.medium,
                        bottom = paddingValues.calculateBottomPadding() + PaddingScrollEnd
                    )
                ) {
                    listEmptyIndicator(
                        isListEmpty = areTransactionsEmpty,
                        messageRes = R.string.all_transactions_list_empty_message
                    )

                    repeat(transactionsLazyPagingItems.itemCount) { index ->
                        transactionsLazyPagingItems[index]?.let { item ->
                            when (item) {
                                is TransactionListItemUIModel.CycleSeparator -> {
                                    stickyHeader(
                                        key = "CycleId-${item.cycle.id}",
                                        contentType = CycleIndicator::class
                                    ) {
                                        ListSeparator(
                                            label = item.cycle.description,
                                            modifier = Modifier
                                                .animateItem()
                                        )
                                    }
                                }

                                is TransactionListItemUIModel.TransactionItem -> {
                                    item(
                                        key = item.id,
                                        contentType = TransactionEntry::class
                                    ) {
                                        val selected = item.id in state.selectedTransactionIds
                                        TransactionListItem(
                                            onClick = {
                                                if (state.transactionMultiSelectionModeActive) actions
                                                    .onTransactionSelectionChange(item.id)
                                                else navigateToAddEditTransaction(item.id)
                                            },
                                            onLongClick = {
                                                hapticFeedback
                                                    .performHapticFeedback(HapticFeedbackType.LongPress)
                                                actions.onTransactionLongPress(item.id)
                                            },
                                            onLongClickLabel = stringResource(R.string.cd_long_press_to_toggle_selection),
                                            note = item.note,
                                            amount = TextFormat.currency(
                                                item.amount,
                                                item.currency
                                            ),
                                            timeStamp = item.timestamp,
                                            leadingContentLine1 = item.timestamp.format(DateUtil.Formatters.ddth),
                                            leadingContentLine2 = item.timestamp.format(DateUtil.Formatters.EEE),
                                            type = item.type,
                                            tag = item.tag,
                                            folder = item.folder,
                                            excluded = item.excluded,
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
                }

                FadedVisibility(
                    showScrollUpButton,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = paddingValues.calculateBottomPadding() + MaterialTheme.spacing.medium)
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            listScrollJob?.cancel()
                            listScrollJob =
                                if (transactionListState.isScrollInProgress) coroutineScope.launch {
                                    transactionListState.scrollToItem(0)
                                } else coroutineScope.launch {
                                    transactionListState.animateScrollToItem(0)
                                }
                        },
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = stringResource(R.string.cd_scroll_to_top),
                        )
                    }
                }
            }
        }
    }

    if (state.showDeleteTransactionConfirmation) {
        ConfirmationDialog(
            title = pluralStringResource(
                R.plurals.delete_transactions_confirmation_title,
                state.selectedTransactionIds.size
            ),
            content = stringResource(R.string.action_irreversible_message),
            onConfirm = actions::onDeleteTransactionConfirm,
            onDismiss = actions::onDeleteTransactionDismiss
        )
    }

    if (state.showAggregationConfirmation) {
        ConfirmationDialog(
            titleRes = R.string.transaction_aggregation_confirmation_title,
            contentRes = R.string.transaction_aggregation_confirmation_message,
            onConfirm = actions::onAggregationConfirm,
            onDismiss = actions::onAggregationDismiss
        )
    }

    if (state.showMultiSelectionOptions) {
        MultiSelectionOptionsSheet(
            onDismiss = actions::onMultiSelectionOptionsDismiss,
            onOptionClick = actions::onMultiSelectionOptionSelect
        )
    }

    if (state.showFilterOptions) {
        FilterOptionsSheet(
            onDismissRequest = actions::onFilterOptionsDismiss,
            onClearAllFiltersClick = actions::onClearAllFiltersClick,
            selectedTypeFilter = state.selectedTransactionTypeFilter,
            onTypeFilterSelect = actions::onTypeFilterSelect,
            showExcluded = state.showExcludedTransactions,
            onShowExcludedToggle = actions::onShowExcludedToggle,
            selectedTagFilterIds = state.selectedTagFilterIds,
            onTagFilterIdsChange = actions::onTagFilterIdsChange,
        )
    }
}

@Composable
private fun AllTransactionsTopAppBar(
    searchModeActive: Boolean,
    onSearchModeToggle: (Boolean) -> Unit,
    searchQueryState: TextFieldState,
    onClearSearchQuery: () -> Unit,
    searchResults: LazyPagingItems<TransactionEntry>,
    onFilterOptionsClick: () -> Unit,
    multiSelectionModeActive: Boolean,
    selectionCount: Int,
    onDismissMultiSelectionMode: () -> Unit,
    onMultiSelectionOptionsClick: () -> Unit,
    onSearchItemClick: (Long) -> Unit,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isQueryNotEmpty by remember {
        derivedStateOf { searchQueryState.text.isNotEmpty() }
    }
    val searchBarHorizontalPadding by animateDpAsState(
        targetValue = if (searchModeActive) Dp.Zero else MaterialTheme.spacing.medium,
        label = "SearchBarHorizontalPadding"
    )
    AnimatedContent(
        targetState = multiSelectionModeActive,
        transitionSpec = {
            if (targetState) {
                slideInVerticallyWithFadeIn { it / 2 }
                    .togetherWith(
                        slideOutVerticallyWithFadeOut { -it / 2 }
                    )
            } else {
                slideInVerticallyWithFadeIn { -it / 2 }
                    .togetherWith(
                        slideOutVerticallyWithFadeOut { it / 2 }
                    )
            }
        },
        label = "MultiSelectionModeActive",
        modifier = modifier
    ) { active ->
        if (active) {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            R.string.count_selected,
                            selectionCount
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismissMultiSelectionMode) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cd_clear_transaction_selection)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onMultiSelectionOptionsClick) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.cd_tap_for_more_options)
                        )
                    }
                }
            )
        } else {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        state = searchQueryState,
                        onSearch = {},
                        expanded = searchModeActive,
                        onExpandedChange = onSearchModeToggle,
                        leadingIcon = {
                            BackArrowButton(
                                onClick = {
                                    if (searchModeActive) onSearchModeToggle(false)
                                    else navigateUp()
                                }
                            )
                        },
                        trailingIcon = {
                            if (searchModeActive) {
                                FadedVisibility(
                                    visible = isQueryNotEmpty,
                                    label = "ClearQueryButton"
                                ) {
                                    IconButton(onClick = onClearSearchQuery) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = stringResource(R.string.cd_clear_search_query)
                                        )
                                    }
                                }
                            } else {
                                IconButton(onClick = onFilterOptionsClick) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = stringResource(id = R.string.cd_filter_options)
                                    )
                                }
                            }
                        },
                        placeholder = { Text(stringResource(R.string.destination_all_transactions)) },
                    )
                },
                expanded = searchModeActive,
                onExpandedChange = onSearchModeToggle,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .layout { measurable, constraints ->
                        val searchbarWidth =
                            constraints.maxWidth - (searchBarHorizontalPadding * 2f).roundToPx()
                        val searchBarPlaceable = measurable.measure(
                            constraints = constraints.copy(
                                maxWidth = searchbarWidth,
                                minWidth = searchbarWidth
                            )
                        )
                        layout(searchBarPlaceable.width, searchBarPlaceable.height) {
                            searchBarPlaceable.placeRelative(0, 0)
                        }
                    }
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = MaterialTheme.spacing.medium,
                        bottom = PaddingScrollEnd
                    )
                ) {
                    items(
                        count = searchResults.itemCount,
                        key = searchResults.itemKey { it.id },
                        contentType = searchResults.itemContentType { "SearchResultTransactionItem" }
                    ) { index ->
                        searchResults[index]?.let { item ->
                            TransactionListItem(
                                note = item.note,
                                amount = item.amountFormatted,
                                timeStamp = item.timestamp,
                                leadingContentLine1 = item.timestamp.format(DateUtil.Formatters.ddth),
                                leadingContentLine2 = item.timestamp.format(DateUtil.Formatters.MMM),
                                type = item.type,
                                tag = item.tag,
                                folder = item.folder,
                                colors = ListItemDefaults.colors(
                                    containerColor = SearchBarDefaults.colors().containerColor
                                ),
                                elevation = ListItemDefaults.elevation(
                                    elevation = MaterialTheme.elevation.level1
                                ),
                                onClick = { onSearchItemClick(item.id) },
                                modifier = Modifier
                                    .animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionListLabel(
    listLabel: UiText,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = Modifier
            .then(modifier)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Crossfade(
                targetState = listLabel,
                label = "ListLabel",
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) { label ->
                Text(
                    text = label.asString(),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun MultiSelectionOptionsSheet(
    onDismiss: () -> Unit,
    onOptionClick: (AllTransactionsMultiSelectionOption) -> Unit,
    modifier: Modifier = Modifier
) {
    ItemListSheet(
        onDismiss = onDismiss,
        items = AllTransactionsMultiSelectionOption.entries,
        key = { it.name },
        contentType = { AllTransactionsMultiSelectionOption::class },
        modifier = modifier
    ) { option ->
        OptionListItem(
            iconRes = option.iconRes,
            label = stringResource(option.labelRes),
            onClick = { onOptionClick(option) },
            onClickLabel = stringResource(R.string.cd_option_name, stringResource(option.labelRes)),
            modifier = Modifier
                .fillParentMaxWidth()
                .animateItem()
        )
    }
}

@Composable
private fun FilterOptionsSheet(
    onDismissRequest: () -> Unit,
    onClearAllFiltersClick: () -> Unit,
    selectedTypeFilter: TransactionTypeFilter,
    onTypeFilterSelect: (TransactionTypeFilter) -> Unit,
    selectedTagFilterIds: Set<Long>,
    onTagFilterIdsChange: (Set<Long>) -> Unit,
    showExcluded: Boolean,
    onShowExcludedToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    OarModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = MaterialTheme.spacing.medium,
                    bottom = PaddingScrollEnd
                ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) {
                DisplaySmallText(stringResource(R.string.filter))
                TextButton(
                    onClick = onClearAllFiltersClick
                ) {
                    Text(
                        text = stringResource(R.string.clear_all),
                        textDecoration = TextDecoration.Underline
                    )
                }
            }

            TypeFilterSection(
                selectedTypeFilter = selectedTypeFilter,
                onTypeFilterSelect = onTypeFilterSelect
            )

            FilterSectionTitle(
                resId = R.string.filter_section_tags,
                showClearOption = selectedTagFilterIds.isNotEmpty(),
                onClearClick = { onTagFilterIdsChange(emptySet()) }
            )

            TagSelectionField(
                selectedIds = selectedTagFilterIds,
                onSelectedIdsChange = onTagFilterIdsChange,
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )

            FilterSectionTitle(resId = R.string.filter_section_more)
            SwitchPreference(
                titleRes = R.string.show_excluded_transactions,
                value = showExcluded,
                onValueChange = onShowExcludedToggle
            )
        }
    }
}

@Composable
private fun FilterSectionTitle(
    @StringRes resId: Int,
    modifier: Modifier = Modifier,
    showClearOption: Boolean = false,
    onClearClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .padding(horizontal = MaterialTheme.spacing.medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ButtonDefaults.MinHeight * 1.2f)
        ) {
            ListLabel(stringResource(resId))
            AnimatedVisibility(
                visible = showClearOption,
                enter = slideInHorizontallyWithFadeIn(),
                exit = slideOutHorizontallyWithFadeOut()
            ) {
                TextButton(onClick = onClearClick) {
                    Text(
                        text = stringResource(R.string.clear),
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .padding(vertical = MaterialTheme.spacing.small)
        )
    }
}

@Composable
private fun TypeFilterSection(
    selectedTypeFilter: TransactionTypeFilter,
    onTypeFilterSelect: (TransactionTypeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterSectionTitle(R.string.filter_section_transaction_type)
    val filterEntries = remember { TransactionTypeFilter.entries }
    val filterEntriesSize = remember(filterEntries) { filterEntries.size }
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
    ) {
        filterEntries.forEachIndexed { index, filter ->
            SegmentedButton(
                selected = filter == selectedTypeFilter,
                onClick = { onTypeFilterSelect(filter) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = filterEntriesSize
                ),
                label = { Text(stringResource(filter.labelRes)) }
            )
        }
    }
}

