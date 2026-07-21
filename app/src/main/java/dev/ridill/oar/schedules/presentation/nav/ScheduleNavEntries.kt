package dev.ridill.oar.schedules.presentation.nav

import android.Manifest
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.metadata
import androidx.navigation3.ui.NavDisplay
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.application.NAV_ANIM_SCALE
import dev.ridill.oar.core.domain.util.BuildUtil
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.OnLifecycleStartEffect
import dev.ridill.oar.core.ui.components.rememberPermissionState
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.components.slideInVerticallyWithFadeIn
import dev.ridill.oar.core.ui.components.slideOutVerticallyWithFadeOut
import dev.ridill.oar.core.ui.navigation.AddEditScheduleResult
import dev.ridill.oar.core.ui.navigation.AddEditScheduleRoute
import dev.ridill.oar.core.ui.navigation.AllSchedulesRoute
import dev.ridill.oar.core.ui.navigation.FolderSelectedResult
import dev.ridill.oar.core.ui.navigation.FolderSelectionSheetRoute
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.navigation.LocalResultBus
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.core.ui.navigation.ResultEffect
import dev.ridill.oar.core.ui.util.launchAppNotificationSettings
import dev.ridill.oar.schedules.presentation.addEditSchedule.AddEditScheduleScreen
import dev.ridill.oar.schedules.presentation.addEditSchedule.AddEditScheduleViewModel
import dev.ridill.oar.schedules.presentation.allSchedules.AllSchedulesScreen
import dev.ridill.oar.schedules.presentation.allSchedules.AllSchedulesViewModel

// region Schedules

fun EntryProviderScope<NavKey>.scheduleEntries(
    navigator: OarNavigator,
    motionScheme: MotionScheme,
) {
    entry<AllSchedulesRoute> {
        val viewModel: AllSchedulesViewModel = hiltViewModel()
        val allSchedulesPagingItems = viewModel.schedulesPagingData.collectAsLazyPagingItems()
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        val notificationPermissionState = if (BuildUtil.isNotificationRuntimePermissionNeeded())
            rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        else null

        ResultEffect<AddEditScheduleResult> { result ->
            viewModel.onAddEditScheduleNavResult(result)
        }

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
                    AddEditScheduleRoute(scheduleId = it ?: INVALID_ID_LONG)
                )
            }
        )
    }

    entry<AddEditScheduleRoute>(
        metadata = metadata {
            val slideAnimationSpec: FiniteAnimationSpec<IntOffset> = motionScheme.slowSpatialSpec()
            val fadeAnimationSpec: FiniteAnimationSpec<Float> = motionScheme.slowSpatialSpec()
            put(NavDisplay.TransitionKey) {
                slideInVerticallyWithFadeIn(
                    initialOffsetY = { it },
                    slideAnimationSpec = slideAnimationSpec,
                    fadeAnimationSpec = fadeAnimationSpec,
                ) togetherWith scaleOut(
                    animationSpec = fadeAnimationSpec,
                    targetScale = NAV_ANIM_SCALE,
                    transformOrigin = TransformOrigin.Center,
                ) + fadeOut(animationSpec = fadeAnimationSpec)
            }
            put(NavDisplay.PopTransitionKey) {
                scaleIn(
                    animationSpec = fadeAnimationSpec,
                    initialScale = NAV_ANIM_SCALE,
                    transformOrigin = TransformOrigin.Center,
                ) + fadeIn(animationSpec = fadeAnimationSpec) togetherWith slideOutVerticallyWithFadeOut(
                    targetOffsetY = { it },
                    slideAnimationSpec = slideAnimationSpec,
                    fadeAnimationSpec = fadeAnimationSpec,
                )
            }
            put(NavDisplay.PredictivePopTransitionKey) {
                scaleIn(
                    animationSpec = fadeAnimationSpec,
                    initialScale = NAV_ANIM_SCALE,
                    transformOrigin = TransformOrigin.Center,
                ) + fadeIn(animationSpec = fadeAnimationSpec) togetherWith slideOutVerticallyWithFadeOut(
                    targetOffsetY = { it },
                    slideAnimationSpec = slideAnimationSpec,
                    fadeAnimationSpec = fadeAnimationSpec,
                )
            }
        }
    ) { key ->
        val viewModel =
            hiltViewModel<AddEditScheduleViewModel, AddEditScheduleViewModel.Factory>(
                creationCallback = { it.create(key) }
            )

        OnLifecycleStartEffect(viewModel, block = viewModel::refreshCurrentDateTime)
        val amountInputState = viewModel.amountInputState
        val noteInputState = viewModel.noteInputState
        val state by viewModel.state.collectAsStateWithLifecycle()

        val isEditMode = key.scheduleId != INVALID_ID_LONG

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        val resultBus = LocalResultBus.current

        ResultEffect<FolderSelectedResult> { result ->
            viewModel.onFolderSelectionResult(result.id)
        }

        CollectFlowEffect(viewModel.events, snackbarController, context) { event ->
            when (event) {
                is AddEditScheduleViewModel.AddEditScheduleEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                is AddEditScheduleViewModel.AddEditScheduleEvent.LaunchFolderSelection -> {
                    navigator.navigate(
                        FolderSelectionSheetRoute(
                            preselectedId = event.preselectedId ?: INVALID_ID_LONG
                        )
                    )
                }

                is AddEditScheduleViewModel.AddEditScheduleEvent.NavigateUpWithResult -> {
                    resultBus.sendResult<AddEditScheduleResult>(event.result)
                    navigator.goBack()
                }
            }
        }

        AddEditScheduleScreen(
            isEditMode = isEditMode,
            snackbarController = snackbarController,
            amountInputState = amountInputState,
            noteInputState = noteInputState,
            state = state,
            actions = viewModel,
            navigateUp = navigator::goBack
        )
    }
}

// endregion
