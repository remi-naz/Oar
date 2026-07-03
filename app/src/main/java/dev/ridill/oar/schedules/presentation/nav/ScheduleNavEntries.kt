package dev.ridill.oar.schedules.presentation.nav

import android.Manifest
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.core.domain.util.BuildUtil
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.OnLifecycleStartEffect
import dev.ridill.oar.core.ui.components.rememberPermissionState
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.navigation.AddEditTransactionRoute
import dev.ridill.oar.core.ui.navigation.AllSchedulesRoute
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.core.ui.util.launchAppNotificationSettings
import dev.ridill.oar.schedules.presentation.allSchedules.AllSchedulesScreen
import dev.ridill.oar.schedules.presentation.allSchedules.AllSchedulesViewModel

// region Schedules

fun EntryProviderScope<NavKey>.scheduleEntries(navigator: OarNavigator) {
    entry<AllSchedulesRoute> {
        val viewModel: AllSchedulesViewModel = hiltViewModel()
        val allSchedulesPagingItems = viewModel.schedulesPagingData.collectAsLazyPagingItems()
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        val notificationPermissionState = if (BuildUtil.isNotificationRuntimePermissionNeeded())
            rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        else null

        CollectFlowEffect(flow = viewModel.events, snackbarController, context) { event ->
            when (event) {
                is AllSchedulesViewModel.AllSchedulesEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(event.uiText.asString(context))
                }

                AllSchedulesViewModel.AllSchedulesEvent.RequestNotificationPermission -> {
                    context.launchAppNotificationSettings()
                }
            }
        }

        OnLifecycleStartEffect(viewModel, block = viewModel::refreshCurrentDate)

        AllSchedulesScreen(
            context = context,
            snackbarController = snackbarController,
            notificationPermissionState = notificationPermissionState,
            allSchedulesPagingItems = allSchedulesPagingItems,
            state = state,
            actions = viewModel,
            navigateUp = navigator::goBack,
            navigateToAddEditSchedule = {
                navigator.navigate(
                    AddEditTransactionRoute(
                        transactionId = it ?: INVALID_ID_LONG,
                        isScheduleMode = true
                    )
                )
            }
        )
    }
}

// endregion
