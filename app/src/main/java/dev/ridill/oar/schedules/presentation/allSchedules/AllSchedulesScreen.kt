package dev.ridill.oar.schedules.presentation.allSchedules

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.paging.compose.LazyPagingItems
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.logD
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.CancelButton
import dev.ridill.oar.core.ui.components.ConfirmationDialog
import dev.ridill.oar.core.ui.components.ListSeparator
import dev.ridill.oar.core.ui.components.OarPlainTooltip
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.PermissionRationaleDialog
import dev.ridill.oar.core.ui.components.PermissionState
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.SwipeActionsContainer
import dev.ridill.oar.core.ui.components.listEmptyIndicator
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.isEmpty
import dev.ridill.oar.schedules.domain.model.ScheduleListItemUiModel
import dev.ridill.oar.schedules.presentation.components.ScheduleListItem
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AllSchedulesScreen(
    context: Context = LocalContext.current,
    snackbarController: SnackbarController,
    notificationPermissionState: PermissionState?,
    state: AllSchedulesState,
    allSchedulesPagingItems: LazyPagingItems<ScheduleListItemUiModel>,
    actions: AllSchedulesActions,
    navigateUp: () -> Unit,
    navigateToAddEditSchedule: (Long?) -> Unit
) {
    val isNotificationPermissionGranted by remember(notificationPermissionState) {
        derivedStateOf { notificationPermissionState?.isGranted != false }
    }
    val areSchedulesEmpty by remember {
        derivedStateOf { allSchedulesPagingItems.isEmpty() }
    }

    val hapticFeedback = LocalHapticFeedback.current

    BackHandler(
        enabled = state.multiSelectionModeActive,
        onBack = actions::onMultiSelectionModeDismiss
    )

    val schedulesListState = rememberLazyListState()
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    OarScaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    Text(
                        text = if (state.multiSelectionModeActive) stringResource(
                            R.string.count_selected,
                            state.selectedScheduleIds.size
                        )
                        else stringResource(R.string.destination_all_schedules)
                    )
                },
                navigationIcon = {
                    if (state.multiSelectionModeActive) {
                        CancelButton(onClick = actions::onMultiSelectionModeDismiss)
                    } else {
                        BackArrowButton(onClick = navigateUp)
                    }
                },
                scrollBehavior = topAppBarScrollBehavior,
                actions = {
                    if (state.multiSelectionModeActive) {
                        IconButton(onClick = actions::onDeleteSelectedSchedulesClick) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteForever,
                                contentDescription = stringResource(R.string.cd_delete_selected_schedules)
                            )
                        }
                    }
                    if (!state.multiSelectionModeActive && !isNotificationPermissionGranted) {
                        NotificationPermissionWarning(
                            onClick = actions::onNotificationWarningClick
                        )
                    }
                }
            )
        },
        snackbarController = snackbarController,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            FloatingActionButton(onClick = { navigateToAddEditSchedule(null) }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.cd_new_schedule_fab)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = schedulesListState,
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier
                .fillMaxSize()
        ) {
            listEmptyIndicator(
                isListEmpty = areSchedulesEmpty,
                messageRes = R.string.schedules_empty_message
            )

            repeat(allSchedulesPagingItems.itemCount) { index ->
                allSchedulesPagingItems[index]?.let { item ->
                    when (item) {
                        is ScheduleListItemUiModel.TypeSeparator -> {
                            stickyHeader(
                                key = item.label.asString(context),
                                contentType = "TypeSeparator"
                            ) {
                                ListSeparator(
                                    label = item.label.asString(),
                                    modifier = Modifier
                                        .animateItem()
                                )
                            }
                        }

                        is ScheduleListItemUiModel.ScheduleItem -> {
                            item(
                                key = item.id,
                                contentType = "ScheduleListItem"
                            ) {
                                logD { "Index = $index" }
                                val selected by remember(state.selectedScheduleIds) {
                                    derivedStateOf { item.id in state.selectedScheduleIds }
                                }
                                ScheduleItem(
                                    amount = item.amountFormatted,
                                    note = item.note,
                                    type = item.type,
                                    nextPaymentTimestamp = item.nextPaymentTimestamp,
                                    lastPaymentTimestamp = item.lastPaymentTimestamp,
                                    canMarkPaid = item.canMarkPaid,
                                    onActionRevealed = actions::onScheduleActionRevealed,
                                    onMarkPaidClick = { actions.onMarkSchedulePaidClick(item.id) },
                                    onClick = { navigateToAddEditSchedule(item.id) },
                                    onLongPress = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        actions.onScheduleLongPress(item.id)
                                    },
                                    selectionModeActive = state.multiSelectionModeActive,
                                    selected = selected,
                                    onSelectionToggle = { actions.onScheduleSelectionToggle(item.id) },
                                    showPreview = state.showActionPreview && item.canMarkPaid && index == 1,
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

        if (state.showDeleteSelectedSchedulesConfirmation) {
            ConfirmationDialog(
                titleRes = R.string.delete_selected_schedules_confirmation_title,
                contentRes = R.string.action_irreversible_message,
                onConfirm = actions::onDeleteSelectedSchedulesConfirm,
                onDismiss = actions::onDeleteSelectedSchedulesDismiss
            )
        }

        if (state.showNotificationRationale) {
            PermissionRationaleDialog(
                icon = Icons.Outlined.Notifications,
                rationaleText = stringResource(
                    R.string.permission_rationale_notification,
                    stringResource(R.string.app_name)
                ),
                onDismiss = actions::onNotificationRationaleDismiss,
                onSettingsClick = actions::onNotificationRationaleAgree
            )
        }
    }
}

@Composable
private fun NotificationPermissionWarning(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Rounded.NotificationsOff,
            contentDescription = stringResource(R.string.cd_notification_off_warning)
        )
    }
}

@Composable
private fun ScheduleItem(
    selectionModeActive: Boolean,
    amount: String,
    note: String?,
    type: FundMovement,
    nextPaymentTimestamp: LocalDateTime?,
    lastPaymentTimestamp: LocalDateTime?,
    canMarkPaid: Boolean,
    onActionRevealed: () -> Unit,
    onMarkPaidClick: () -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onSelectionToggle: () -> Unit,
    selected: Boolean,
    showPreview: Boolean,
    modifier: Modifier = Modifier
) {
    // Launched effect added to hide actions whenever some key state changes
    var isRevealed by remember { mutableStateOf(false) }
    LaunchedEffect(selectionModeActive, canMarkPaid) {
        isRevealed = false
    }

    SwipeActionsContainer(
        isRevealed = isRevealed,
        onRevealedChange = { revealed ->
            isRevealed = revealed
            if (revealed) {
                onActionRevealed()
            }
        },
        actions = {
            OarPlainTooltip(
                tooltipText = stringResource(R.string.cd_mark_as_paid)
            ) {
                IconButton(
                    onClick = {
                        onMarkPaidClick()
                        isRevealed = false
                    },
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_double_tick),
                        contentDescription = stringResource(R.string.cd_mark_as_paid)
                    )
                }
            }
        },
        modifier = modifier,
        gesturesEnabled = !selectionModeActive && canMarkPaid,
        animatePreview = showPreview
    ) {
        ScheduleListItem(
            onClick = if (selectionModeActive) onSelectionToggle else onClick,
            onLongClick = onLongPress,
            onLongClickLabel = stringResource(R.string.cd_long_press_to_toggle_selection),
            note = note,
            amount = amount,
            type = type,
            nextPaymentTimestamp = nextPaymentTimestamp,
            lastPaymentTimestamp = lastPaymentTimestamp,
            selected = selected,
        )
    }
}