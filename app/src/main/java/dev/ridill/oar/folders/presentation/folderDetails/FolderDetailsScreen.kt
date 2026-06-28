package dev.ridill.oar.folders.presentation.folderDetails

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.R
import dev.ridill.oar.aggregations.presentation.AmountAggregatesList
import dev.ridill.oar.budgetCycles.domain.model.CycleIndicator
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.One
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.ConfirmationDialog
import dev.ridill.oar.core.ui.components.ExcludedIcon
import dev.ridill.oar.core.ui.components.ItemListSheet
import dev.ridill.oar.core.ui.components.ListLabel
import dev.ridill.oar.core.ui.components.ListSeparator
import dev.ridill.oar.core.ui.components.MultiActionConfirmationDialog
import dev.ridill.oar.core.ui.components.OarPlainTooltip
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.OptionListItem
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.SpacerSmall
import dev.ridill.oar.core.ui.components.SwipeActionsContainer
import dev.ridill.oar.core.ui.components.icons.CalendarClock
import dev.ridill.oar.core.ui.components.listEmptyIndicator
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.elevation
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.ui.util.isEmpty
import dev.ridill.oar.folders.domain.model.FolderTransactionsMultiSelectionOption
import dev.ridill.oar.transactions.domain.model.TagIndicator
import dev.ridill.oar.transactions.domain.model.TransactionEntry
import dev.ridill.oar.transactions.domain.model.TransactionListItemUIModel
import dev.ridill.oar.transactions.domain.model.TransactionType
import dev.ridill.oar.transactions.presentation.components.NewTransactionFab
import dev.ridill.oar.transactions.presentation.components.TransactionListItem
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FolderDetailsScreen(
    snackbarController: SnackbarController,
    state: FolderDetailsState,
    transactionPagingItems: LazyPagingItems<TransactionListItemUIModel>,
    actions: FolderDetailsActions,
    navigateToEditFolder: () -> Unit,
    navigateToAddEditTransaction: (Long?) -> Unit,
    navigateUp: () -> Unit
) {
    val areTransactionsEmpty by remember {
        derivedStateOf { transactionPagingItems.isEmpty() }
    }

    BackHandler(
        enabled = state.transactionMultiSelectionModeActive,
        onBack = actions::onMultiSelectionModeDismiss
    )

    val layoutDirection = LocalLayoutDirection.current
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    OarScaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    if (state.transactionMultiSelectionModeActive) {
                        Text(
                            stringResource(
                                R.string.count_selected,
                                state.selectedTransactionIds.size
                            )
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                        ) {
                            AnimatedVisibility(state.isExcluded) {
                                ExcludedIcon()
                            }
                            Text(state.folderName)
                        }
                    }
                },
                navigationIcon = {
                    if (state.transactionMultiSelectionModeActive) {
                        IconButton(onClick = actions::onMultiSelectionModeDismiss) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.cd_clear_transaction_selection)
                            )
                        }
                    } else {
                        BackArrowButton(onClick = navigateUp)
                    }
                },
                actions = {
                    if (state.transactionMultiSelectionModeActive) {
                        IconButton(onClick = actions::onMultiSelectionOptionsClick) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.cd_tap_for_more_options)
                            )
                        }
                    } else {
                        IconButton(onClick = navigateToEditFolder) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.cd_edit_folder)
                            )
                        }

                        IconButton(onClick = actions::onDeleteClick) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteForever,
                                contentDescription = stringResource(R.string.cd_delete_folder)
                            )
                        }
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        floatingActionButton = {
            NewTransactionFab(onClick = { navigateToAddEditTransaction(null) })
        },
        bottomBar = {
            AnimatedVisibility(
                visible = state.transactionMultiSelectionModeActive,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                AmountAggregatesList(
                    aggregatesList = state.aggregatesList,
                    modifier = Modifier
                )
            }
        },
        snackbarController = snackbarController,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                )
        ) {
            SpacerSmall()

            FolderDetails(
                createdTimestamp = state.createdTimestampFormatted,
                modifier = Modifier
                    .fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    top = MaterialTheme.spacing.medium,
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                    bottom = paddingValues.calculateBottomPadding() + PaddingScrollEnd
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                stickyHeader(
                    key = "TransactionListHeader",
                    contentType = "TransactionListHeader"
                ) {
                    ListLabel(
                        text = stringResource(R.string.transactions),
                        modifier = Modifier
                            .padding(
                                horizontal = MaterialTheme.spacing.medium,
                                vertical = MaterialTheme.spacing.small
                            )
                            .animateItem()
                    )
                }

                listEmptyIndicator(
                    isListEmpty = areTransactionsEmpty,
                    messageRes = R.string.transactions_in_folder_list_empty_message
                )

                repeat(transactionPagingItems.itemCount) { index ->
                    transactionPagingItems[index]?.let { item ->
                        when (item) {
                            is TransactionListItemUIModel.CycleSeparator -> {
                                stickyHeader(
                                    key = "CycleId-${item.cycle.id}",
                                    contentType = CycleIndicator::class
                                ) {
                                    ListSeparator(
                                        label = item.cycle.description,
                                        modifier = Modifier
                                            .clickable(
                                                onClick = { actions.onCycleSelect(item.cycle.id) },
                                                onClickLabel = stringResource(R.string.cd_tap_to_see_cycle_aggregate)
                                            )
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
                                    TransactionInFolderItem(
                                        note = item.note,
                                        amount = TextFormat.currency(item.amount, item.currency),
                                        timestamp = item.timestamp,
                                        type = item.type,
                                        tag = item.tag,
                                        excluded = item.excluded,
                                        multiSelectionActive = state.transactionMultiSelectionModeActive,
                                        selected = selected,
                                        onSelectionChange = {
                                            actions.onTransactionSelectionChange(item.id)
                                        },
                                        onLongPress = { actions.onTransactionLongPress(item.id) },
                                        onClick = { navigateToAddEditTransaction(item.id) },
                                        onRevealed = actions::onTransactionSwipeActionRevealed,
                                        onRemoveFromFolderClick = {
                                            actions.onRemoveTransactionFromFolderClick(item.id)
                                        },
                                        showSwipePreview = state.shouldShowActionPreview && index == 1,
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
        }

        if (state.showDeleteConfirmation) {
            if (transactionPagingItems.itemCount == 0) {
                ConfirmationDialog(
                    title = pluralStringResource(
                        R.plurals.delete_folders_confirmation_title,
                        Int.One
                    ),
                    content = stringResource(R.string.action_irreversible_message),
                    onConfirm = actions::onDeleteFolderOnlyClick,
                    onDismiss = actions::onDeleteDismiss
                )
            } else {
                MultiActionConfirmationDialog(
                    title = pluralStringResource(
                        R.plurals.delete_folders_confirmation_title,
                        Int.One
                    ),
                    text = stringResource(R.string.action_irreversible_message),
                    primaryActionLabelRes = R.string.delete_folder,
                    additionalNote = stringResource(R.string.delete_folder_confirmation_note),
                    onPrimaryActionClick = actions::onDeleteFolderOnlyClick,
                    secondaryActionLabelRes = R.string.delete_folder_and_transactions,
                    onSecondaryActionClick = actions::onDeleteFolderAndTransactionsClick,
                    onDismiss = actions::onDeleteDismiss
                )
            }
        }
    }

    if (state.showMultiSelectionOptions) {
        FolderTransactionsOptionsSheet(
            onDismiss = actions::onMultiSelectionOptionDismiss,
            onOptionClick = actions::onMultiSelectionOptionClick
        )
    }

    if (state.showDeleteTransactionsConfirmation) {
        ConfirmationDialog(
            title = pluralStringResource(
                R.plurals.delete_transactions_confirmation_title,
                state.selectedTransactionIds.size
            ),
            content = stringResource(R.string.action_irreversible_message),
            onConfirm = actions::onDeleteTransactionsConfirm,
            onDismiss = actions::onDeleteTransactionsDismiss
        )
    }

    if (state.showRemoveTransactionsConfirmation) {
        ConfirmationDialog(
            title = pluralStringResource(
                R.plurals.remove_transactions_from_folder_confirmation_title,
                state.selectedTransactionIds.size
            ),
            content = stringResource(R.string.action_irreversible_message),
            onConfirm = actions::onRemoveTransactionsFromFolderConfirm,
            onDismiss = actions::onRemoveTransactionsFromFolderDismiss
        )
    }
}

@Composable
private fun FolderDetails(
    createdTimestamp: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = MaterialTheme.spacing.medium),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        FolderCreatedDate(date = createdTimestamp)

        HorizontalDivider()
    }
}

@Composable
private fun FolderCreatedDate(
    date: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stringResource(R.string.created),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        SpacerSmall()
        Icon(
            imageVector = Icons.Outlined.CalendarClock,
            contentDescription = stringResource(R.string.cd_folder_created_date)
        )
    }
}

@Composable
private fun TransactionInFolderItem(
    note: String,
    amount: String,
    timestamp: LocalDateTime,
    type: TransactionType,
    tag: TagIndicator?,
    excluded: Boolean,
    multiSelectionActive: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onSelectionChange: () -> Unit,
    onLongPress: () -> Unit,
    onRemoveFromFolderClick: () -> Unit,
    onRevealed: () -> Unit,
    showSwipePreview: Boolean,
    modifier: Modifier = Modifier,
    hapticFeedback: HapticFeedback = LocalHapticFeedback.current,
) {
    val clickableModifier = if (multiSelectionActive) Modifier
        .selectable(
            selected = selected,
            onClick = onSelectionChange
        )
    else Modifier.combinedClickable(
        onClick = onClick,
        onClickLabel = stringResource(R.string.cd_tap_to_edit_transaction),
        onLongClick = {
            hapticFeedback.performHapticFeedback(
                HapticFeedbackType.LongPress
            )
            onLongPress()
        },
        onLongClickLabel = stringResource(R.string.cd_long_press_to_toggle_selection)
    )
    var isRevealed by remember { mutableStateOf(false) }
    LaunchedEffect(multiSelectionActive) {
        if (multiSelectionActive) {
            isRevealed = false
        }
    }
    SwipeActionsContainer(
        isRevealed = isRevealed,
        onRevealedChange = { revealed ->
            isRevealed = revealed
            if (revealed) {
                onRevealed()
            }
        },
        actions = {
            OarPlainTooltip(
                tooltipText = stringResource(R.string.cd_remove_from_folder)
            ) {
                IconButton(
                    onClick = {
                        isRevealed = false
                        onRemoveFromFolderClick()
                    }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_folder_export),
                        contentDescription = stringResource(R.string.cd_remove_from_folder)
                    )
                }
            }
        },
        animatePreview = showSwipePreview,
        gesturesEnabled = !multiSelectionActive
    ) {
        TransactionListItem(
            note = note,
            amount = amount,
            timeStamp = timestamp,
            leadingContentLine1 = timestamp.format(DateUtil.Formatters.ddth),
            leadingContentLine2 = timestamp.format(DateUtil.Formatters.EEE),
            type = type,
            tonalElevation = if (selected) MaterialTheme.elevation.level1 else MaterialTheme.elevation.level0,
            tag = tag,
            excluded = excluded,
            modifier = modifier
                .then(clickableModifier)
        )
    }
}

@Composable
private fun FolderTransactionsOptionsSheet(
    onDismiss: () -> Unit,
    onOptionClick: (FolderTransactionsMultiSelectionOption) -> Unit,
    modifier: Modifier = Modifier
) {
    ItemListSheet(
        onDismiss = onDismiss,
        items = FolderTransactionsMultiSelectionOption.entries,
        key = { it.name },
        contentType = { FolderTransactionsMultiSelectionOption::class },
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

@PreviewLightDark
@Composable
private fun PreviewFolderDetailsScreen() {
    val transactionPagingItems = flowOf(
        PagingData.from(
            listOf(
                TransactionListItemUIModel.CycleSeparator(
                    CycleIndicator(1L, "August 2023")
                ),
                TransactionListItemUIModel.TransactionItem(
                    id = 1L,
                    note = "Sample Transaction",
                    amount = 100.0,
                    currency = java.util.Currency.getInstance("INR"),
                    timestamp = LocalDateTime.now(),
                    type = TransactionType.DEBIT,
                    excluded = false,
                    cycleEntry = CycleIndicator(1L, "August 2023"),
                    tag = null,
                    folder = null,
                    scheduleId = null
                )
            )
        )
    ).collectAsLazyPagingItems()

    OarTheme {
        FolderDetailsScreen(
            snackbarController = rememberSnackbarController(),
            state = FolderDetailsState(
                folderName = "Sample Folder",
                createdTimestamp = LocalDateTime.now()
            ),
            transactionPagingItems = transactionPagingItems,
            actions = object : FolderDetailsActions {
                override fun onCycleSelect(id: Long) {}
                override fun onDeleteClick() {}
                override fun onDeleteDismiss() {}
                override fun onDeleteFolderOnlyClick() {}
                override fun onDeleteFolderAndTransactionsClick() {}
                override fun onTransactionSwipeActionRevealed() {}
                override fun onRemoveTransactionFromFolderClick(id: Long) {}
                override fun onTransactionLongPress(id: Long) {}
                override fun onTransactionSelectionChange(id: Long) {}
                override fun onMultiSelectionModeDismiss() {}
                override fun onMultiSelectionOptionDismiss() {}
                override fun onMultiSelectionOptionsClick() {}
                override fun onMultiSelectionOptionClick(option: FolderTransactionsMultiSelectionOption) {}
                override fun onDeleteTransactionsDismiss() {}
                override fun onDeleteTransactionsConfirm() {}
                override fun onRemoveTransactionsFromFolderDismiss() {}
                override fun onRemoveTransactionsFromFolderConfirm() {}
            },
            navigateToEditFolder = {},
            navigateToAddEditTransaction = {},
            navigateUp = {}
        )
    }
}
