package dev.ridill.oar.schedules.presentation.allSchedules

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.ui.navigation.AddEditScheduleResult
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.core.ui.util.UiText.StringResource
import dev.ridill.oar.schedules.domain.repository.AllSchedulesRepository
import dev.ridill.oar.schedules.presentation.allSchedules.AllSchedulesViewModel.AllSchedulesEvent.ShowUiMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllSchedulesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: AllSchedulesRepository,
    private val eventBus: EventBus<AllSchedulesEvent>
) : ViewModel(), AllSchedulesActions {

    private val showNotificationRationale = savedStateHandle
        .getStateFlow(SHOW_NOTIFICATION_RATIONALE, false)

    val schedulesPagingData = repo.getSchedulesPagingData()
        .cachedIn(viewModelScope)

    private val selectedScheduleIds = savedStateHandle
        .getStateFlow(SELECTED_SCHEDULE_IDS, emptySet<Long>())
    private val multiSelectionModeActive = selectedScheduleIds.mapLatest { it.isNotEmpty() }
        .distinctUntilChanged()
        .asStateFlow(viewModelScope, false)

    private val showDeleteSelectedSchedulesConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION, false)

    private val showActionPreview = repo.shouldShowActionPreview()

    val state = combineTuple(
        showActionPreview,
        showNotificationRationale,
        multiSelectionModeActive,
        selectedScheduleIds,
        showDeleteSelectedSchedulesConfirmation
    ).mapLatest { (
                      showActionPreview,
                      showNotificationRationale,
                      multiSelectionModeActive,
                      selectedScheduleIds,
                      showDeleteSelectedSchedulesConfirmation
                  ) ->
        AllSchedulesState(
            showActionPreview = showActionPreview,
            showNotificationRationale = showNotificationRationale,
            multiSelectionModeActive = multiSelectionModeActive,
            selectedScheduleIds = selectedScheduleIds,
            showDeleteSelectedSchedulesConfirmation = showDeleteSelectedSchedulesConfirmation
        )
    }
        .asStateFlow(viewModelScope, AllSchedulesState())

    val events = eventBus.eventFlow

    fun refreshCurrentDate() = repo.refreshCurrentDate()

    fun onAddEditScheduleNavResult(result: AddEditScheduleResult) = viewModelScope.launch {
        if (result == AddEditScheduleResult.SCHEDULE_SAVED) {
            eventBus.send(ShowUiMessage(StringResource(R.string.schedule_saved)))
        }
    }

    override fun onNotificationWarningClick() {
        savedStateHandle[SHOW_NOTIFICATION_RATIONALE] = true
    }

    override fun onNotificationRationaleDismiss() {
        savedStateHandle[SHOW_NOTIFICATION_RATIONALE] = false
    }

    override fun onNotificationRationaleAgree() {
        viewModelScope.launch {
            savedStateHandle[SHOW_NOTIFICATION_RATIONALE] = false
            eventBus.send(AllSchedulesEvent.RequestNotificationPermission)
        }
    }

    private var actionPreviewDisableJob: Job? = null
    override fun onScheduleActionRevealed() {
        actionPreviewDisableJob?.cancel()
        actionPreviewDisableJob = viewModelScope.launch {
            if (showActionPreview.first()) {
                repo.disableActionPreview()
            }
        }
    }

    override fun onMarkSchedulePaidClick(id: Long) {
        viewModelScope.launch {
            when (val result = repo.markScheduleAsPaid(id)) {
                is Result.Error -> {
                    eventBus.send(ShowUiMessage(result.message))

                }

                is Result.Success -> {
                    eventBus.send(ShowUiMessage(StringResource(R.string.schedule_marked_as_paid)))
                }
            }
        }
    }

    override fun onScheduleLongPress(id: Long) {
        savedStateHandle[SELECTED_SCHEDULE_IDS] = selectedScheduleIds.value + id
    }

    override fun onScheduleSelectionToggle(id: Long) {
        val selectedIds = selectedScheduleIds.value
        savedStateHandle[SELECTED_SCHEDULE_IDS] = if (id in selectedIds) selectedIds - id
        else selectedIds + id
    }

    override fun onMultiSelectionModeDismiss() {
        savedStateHandle[SELECTED_SCHEDULE_IDS] = emptySet<Long>()
    }

    override fun onDeleteSelectedSchedulesClick() {
        savedStateHandle[SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION] = true
    }

    override fun onDeleteSelectedSchedulesDismiss() {
        savedStateHandle[SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION] = false
    }

    override fun onDeleteSelectedSchedulesConfirm() {
        viewModelScope.launch {
            repo.deleteSchedulesById(selectedScheduleIds.value)
            savedStateHandle[SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION] = false
            savedStateHandle[SELECTED_SCHEDULE_IDS] = emptySet<Long>()
        }
    }

    sealed interface AllSchedulesEvent {
        data class ShowUiMessage(val uiText: UiText) : AllSchedulesEvent
        data object RequestNotificationPermission : AllSchedulesEvent
    }
}

private const val SHOW_NOTIFICATION_RATIONALE = "SHOW_NOTIFICATION_RATIONALE"
private const val SELECTED_SCHEDULE_IDS = "SELECTED_SCHEDULE_IDS"
private const val SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION =
    "SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION"