package dev.ridill.oar.schedules.presentation.addEditSchedule

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.service.ExpEvalService
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.domain.util.ifInfinite
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.domain.util.textAsFlow
import dev.ridill.oar.core.ui.navigation.AddEditScheduleResult
import dev.ridill.oar.core.ui.navigation.AddEditScheduleRoute
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.navigation.TransformationResult
import dev.ridill.oar.core.ui.navigation.toSchedule
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.schedules.domain.model.Schedule
import dev.ridill.oar.schedules.domain.model.ScheduleRepetition
import dev.ridill.oar.schedules.domain.repository.AddEditScheduleRepository
import dev.ridill.oar.transactions.domain.model.AmountTransformation
import dev.ridill.oar.transactions.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import java.util.Currency

@HiltViewModel(assistedFactory = AddEditScheduleViewModel.Factory::class)
class AddEditScheduleViewModel @AssistedInject constructor(
    @Assisted val route: AddEditScheduleRoute,
    private val savedStateHandle: SavedStateHandle,
    private val cycleRepo: BudgetCycleRepository,
    private val repo: AddEditScheduleRepository,
    private val evalService: ExpEvalService,
    private val eventBus: EventBus<AddEditScheduleEvent>
) : ViewModel(), AddEditScheduleActions {

    @AssistedFactory
    interface Factory {
        fun create(route: AddEditScheduleRoute): AddEditScheduleViewModel
    }

    private val isLoading = MutableStateFlow(false)

    private val coercedIdArg: Long
        get() = route.scheduleId.coerceAtLeast(OarDatabase.DEFAULT_ID_LONG)

    private val scheduleInput = savedStateHandle.getStateFlow<Schedule?>(SCHEDULE_INPUT, null)
    private val currency = scheduleInput
        .mapLatest { it?.currency ?: LocaleUtil.defaultCurrency }
        .distinctUntilChanged()

    val amountInputState = savedStateHandle.saveable(
        key = "AMOUNT_INPUT_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )

    private val isAmountInputAnExpression = amountInputState.textAsFlow()
        .mapLatest { evalService.isExpression(it) }
        .distinctUntilChanged()

    val noteInputState = savedStateHandle.saveable(
        key = "NOTE_INPUT_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )

    private val selectedTagId = scheduleInput
        .mapLatest { it?.tagId }
        .distinctUntilChanged()

    private val timestamp = scheduleInput
        .mapLatest { it?.nextPaymentTimestamp ?: DateUtil.now().plusDays(1) }
        .distinctUntilChanged()

    private val scheduleFolderId = scheduleInput
        .mapLatest { it?.folderId }
        .distinctUntilChanged()

    private val transactionType = scheduleInput
        .mapLatest { it?.type ?: TransactionType.DEBIT }
        .distinctUntilChanged()

    private val selectedRepetition = scheduleInput
        .mapLatest { it?.repetition ?: ScheduleRepetition.NO_REPEAT }
        .distinctUntilChanged()

    private val showDeleteConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_CONFIRMATION, false)

    private val amountRecommendations = repo.getAmountRecommendations()

    private val showDatePicker = savedStateHandle.getStateFlow(SHOW_DATE_PICKER, false)
    private val showTimePicker = savedStateHandle.getStateFlow(SHOW_TIME_PICKER, false)

    private val linkedFolderName = scheduleFolderId.flatMapLatest { selectedId ->
        repo.getFolderNameForId(selectedId)
    }.distinctUntilChanged()

    private val menuOptions = MutableStateFlow<Set<AddEditScheduleOption>>(emptySet())

    val state = combineTuple(
        isLoading,
        currency,
        transactionType,
        isAmountInputAnExpression,
        amountRecommendations,
        timestamp,
        showDatePicker,
        showTimePicker,
        selectedTagId,
        showDeleteConfirmation,
        linkedFolderName,
        selectedRepetition,
        menuOptions,
    ).mapLatest { (
                      isLoading,
                      currency,
                      transactionType,
                      isAmountInputAnExpression,
                      amountRecommendations,
                      timestamp,
                      showDatePicker,
                      showTimePicker,
                      selectedTagId,
                      showDeleteConfirmation,
                      linkedFolderName,
                      selectedRepetition,
                      menuOptions,
                  ) ->
        AddEditScheduleState(
            menuOptions = menuOptions,
            currency = currency,
            isLoading = isLoading,
            transactionType = transactionType,
            isAmountInputAnExpression = isAmountInputAnExpression,
            amountRecommendations = amountRecommendations,
            timestamp = timestamp,
            showDatePicker = showDatePicker,
            showTimePicker = showTimePicker,
            selectedTagId = selectedTagId,
            showDeleteConfirmation = showDeleteConfirmation,
            linkedFolderName = linkedFolderName,
            selectedRepetition = selectedRepetition,
        )
    }
        .onStart { buildMenuOptions() }
        .asStateFlow(viewModelScope, AddEditScheduleState())

    val events = eventBus.eventFlow

    init {
        onInit()
    }

    private fun onInit() {
        if (scheduleInput.value != null) return

        viewModelScope.launch {
            val activeCycle = cycleRepo.getActiveCycle()
            val loadedSchedule: Schedule? = when {
                // ID not invalid means we're editing an existing schedule
                route.scheduleId != INVALID_ID_LONG -> repo.getScheduleById(route.scheduleId)

                // Creating a new with/without prior inputs from a transaction
                else -> route.inputs?.toSchedule()
            }

            val isFreshSchedule = loadedSchedule == null
            val schedule = loadedSchedule ?: Schedule.DEFAULT.copy(
                currency = activeCycle?.currency ?: LocaleUtil.defaultCurrency
            )

            val dateNow = DateUtil.now()
            val nextPaymentTimestamp = schedule.nextPaymentTimestamp
                ?.takeIf { it.isAfter(dateNow) }
                ?: dateNow.plusDays(1)

            savedStateHandle[SCHEDULE_INPUT] = schedule.copy(
                nextPaymentTimestamp = nextPaymentTimestamp
            )
            amountInputState.setTextAndPlaceCursorAtEnd(
                if (isFreshSchedule) "" else TextFormat.number(
                    schedule.amount,
                    isGroupingUsed = false
                )
            )
            noteInputState.setTextAndPlaceCursorAtEnd(schedule.note.orEmpty())
        }
    }

    private fun buildMenuOptions() {
        menuOptions.update {
            when {
                // New schedule has no actions
                route.scheduleId == INVALID_ID_LONG -> emptySet()
                // Existing schedule has all actions
                else -> AddEditScheduleOption.entries.toSet()
            }
        }
    }

    fun onCurrencySelect(currency: Currency) {
        savedStateHandle[SCHEDULE_INPUT] = scheduleInput.value?.copy(currency = currency)
    }

    override fun refreshCurrentDateTime() {
        repo.refreshCurrentDateTime()
    }

    override fun onAmountFocusLost() {
        evaluateAmountInput()
    }

    override fun onEvaluateExpressionClick() {
        evaluateAmountInput()
    }

    private fun evaluateAmountInput() {
        val amountInput = amountInputState.text
            .trim()
            .ifEmpty { return }
            .toString()

        val isExpression = evalService.isExpression(amountInput)
        val result = if (isExpression) evalService.evalOrNull(amountInput)
        else TextFormat.parseNumber(amountInput)
        amountInputState.setTextAndPlaceCursorAtEnd(result.orZero().toString())
    }

    override fun onRecommendedAmountClick(amount: Long) {
        savedStateHandle[SCHEDULE_INPUT] = scheduleInput.value?.copy(
            amount = amount.toDouble()
        )
    }

    override fun onTagSelect(tagId: Long?) {
        savedStateHandle[SCHEDULE_INPUT] = scheduleInput.value?.copy(
            tagId = tagId?.takeIf { it != scheduleInput.value?.tagId }
        )
    }

    override fun onTimestampClick() {
        savedStateHandle[SHOW_DATE_PICKER] = true
    }

    override fun onDateSelectionDismiss() {
        savedStateHandle[SHOW_DATE_PICKER] = false
    }

    override fun onDateSelectionConfirm(millis: Long) {
        savedStateHandle[SCHEDULE_INPUT] = scheduleInput.value?.copy(
            nextPaymentTimestamp = DateUtil.dateFromMillisWithTime(
                millis = millis,
                time = scheduleInput.value?.nextPaymentTimestamp?.toLocalTime()
                    ?: DateUtil.timeNow(),
                zoneId = ZoneOffset.UTC
            )
        )
        savedStateHandle[SHOW_DATE_PICKER] = false
    }

    override fun onPickTimeClick() {
        savedStateHandle[SHOW_DATE_PICKER] = false
        savedStateHandle[SHOW_TIME_PICKER] = true
    }

    override fun onTimeSelectionDismiss() {
        savedStateHandle[SHOW_TIME_PICKER] = false
    }

    override fun onTimeSelectionConfirm(hour: Int, minute: Int) {
        savedStateHandle[SCHEDULE_INPUT] = scheduleInput.value?.copy(
            nextPaymentTimestamp = scheduleInput.value?.nextPaymentTimestamp
                ?.withHour(hour)
                ?.withMinute(minute)
                ?: DateUtil.now().plusDays(1)
        )
        savedStateHandle[SHOW_TIME_PICKER] = false
    }

    override fun onPickDateClick() {
        savedStateHandle[SHOW_TIME_PICKER] = false
        savedStateHandle[SHOW_DATE_PICKER] = true
    }

    override fun onTypeChange(type: TransactionType) {
        savedStateHandle[SCHEDULE_INPUT] = scheduleInput.value?.copy(
            type = type
        )
    }

    fun onAmountTransformationResult(result: TransformationResult) {
        val amount = amountInputState.text.toString()
            .toDoubleOrNull() ?: return
        val transformedAmount = when (result.transformation) {
            AmountTransformation.DIVIDE_BY -> amount / result.factor.toDoubleOrNull()
                .orZero()

            AmountTransformation.MULTIPLIER -> amount * result.factor.toDoubleOrNull()
                .orZero()

            AmountTransformation.PERCENT -> amount * (result.factor.toFloatOrNull()
                .orZero() / 100f)
        }
        amountInputState.setTextAndPlaceCursorAtEnd(
            text = transformedAmount
                .ifInfinite { Double.Zero }
                .toString()
        )
    }

    override fun onOptionClick(option: AddEditScheduleOption) {
        when (option) {
            AddEditScheduleOption.DELETE -> {
                savedStateHandle[SHOW_DELETE_CONFIRMATION] = true
            }
        }
    }

    override fun onDeleteDismiss() {
        savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
    }

    override fun onDeleteConfirm() {
        viewModelScope.launch {
            isLoading.update { true }
            repo.deleteSchedule(coercedIdArg)
            isLoading.update { false }
            savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
            eventBus.send(AddEditScheduleEvent.NavigateUpWithResult(AddEditScheduleResult.SCHEDULE_DELETED))
        }
    }

    override fun onSelectFolderClick() {
        viewModelScope.launch {
            eventBus.send(AddEditScheduleEvent.LaunchFolderSelection(scheduleInput.value?.folderId))
        }
    }

    fun onFolderSelectionResult(id: Long) {
        savedStateHandle[SCHEDULE_INPUT] = scheduleInput.value?.copy(
            folderId = id.takeIf { it != INVALID_ID_LONG }
        )
    }

    override fun onRepetitionSelect(repetition: ScheduleRepetition) {
        savedStateHandle[SCHEDULE_INPUT] = scheduleInput.value?.copy(repetition = repetition)
    }

    override fun onSaveClick() {
        viewModelScope.launch {
            val noteInput = noteInputState.text.trim().toString()
            val input = scheduleInput.value?.copy(
                note = noteInput.ifEmpty { null }
            ) ?: return@launch

            val amountInput = amountInputState.text.trim().toString()
            if (amountInput.isEmpty()) {
                eventBus.send(
                    AddEditScheduleEvent.ShowUiMessage(
                        UiText.StringResource(R.string.error_invalid_amount, true)
                    )
                )
                return@launch
            }

            val isExp = evalService.isExpression(amountInput)
            val evaluatedAmount = (if (isExp) evalService.evalOrNull(amountInput)
            else TextFormat.parseNumber(amountInput)) ?: -1.0
            if (evaluatedAmount < Double.Zero) {
                eventBus.send(
                    AddEditScheduleEvent.ShowUiMessage(
                        UiText.StringResource(
                            R.string.error_invalid_amount,
                            true
                        )
                    )
                )
                return@launch
            }

            isLoading.update { true }
            repo.saveSchedule(input.copy(amount = evaluatedAmount))
            isLoading.update { false }
            eventBus.send(AddEditScheduleEvent.NavigateUpWithResult(AddEditScheduleResult.SCHEDULE_SAVED))
        }
    }

    sealed interface AddEditScheduleEvent {
        data class ShowUiMessage(val uiText: UiText) : AddEditScheduleEvent
        data class NavigateUpWithResult(val result: AddEditScheduleResult) : AddEditScheduleEvent
        data class LaunchFolderSelection(val preselectedId: Long?) : AddEditScheduleEvent
    }
}

private const val SCHEDULE_INPUT = "SCHEDULE_INPUT"
private const val SHOW_DELETE_CONFIRMATION = "SHOW_DELETE_CONFIRMATION"
private const val SHOW_DATE_PICKER = "SHOW_DATE_PICKER"
private const val SHOW_TIME_PICKER = "SHOW_TIME_PICKER"
