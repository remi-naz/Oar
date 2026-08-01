package dev.ridill.oar.statistics.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.statistics.domain.model.StatisticsChartMode
import dev.ridill.oar.statistics.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import java.time.Period
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val cycleRepo: BudgetCycleRepository,
    private val statisticsRepo: StatisticsRepository
) : ViewModel(), StatisticsActions {

    private val currentDate = MutableStateFlow(DateUtil.dateNow())
    private val selectedCycleId = savedStateHandle.getStateFlow<Long?>(SELECTED_CYCLE_ID, null)
    private val showExcluded = savedStateHandle.getStateFlow(SHOW_EXCLUDED, false)
    private val chartMode = savedStateHandle.getStateFlow(CHART_MODE, StatisticsChartMode.SPEND)
    private val selectedBarCycleId = savedStateHandle
        .getStateFlow<Long?>(SELECTED_BAR_CYCLE_ID, null)
    private val selectedTagId = savedStateHandle.getStateFlow<Long?>(SELECTED_TAG_ID, null)

    private val resolvedCycle = selectedCycleId.flatMapLatest { id ->
        if (id != null) cycleRepo.getCycleByIdFlow(id)
        else cycleRepo.getActiveCycleFlow()
    }.distinctUntilChanged()
    private val cycleElapsedDays = combineTuple(
        currentDate,
        resolvedCycle
    ).mapLatest { (currentDate, cycle) ->
        when {
            cycle == null -> 0
            currentDate.isBefore(cycle.startDate) -> 0
            currentDate.isAfter(cycle.endDate) -> Period.between(
                cycle.startDate,
                cycle.endDate
            ).days

            else -> Period.between(cycle.startDate, currentDate).days
        }
    }.distinctUntilChanged()
    private val cycleTotalDays = resolvedCycle
        .mapLatest {
            if (it == null) 0
            else Period.between(it.startDate, it.endDate).days
        }.distinctUntilChanged()

    private val resolvedCycleId = resolvedCycle
        .mapLatest { it?.id ?: OarDatabase.INVALID_ID_LONG }
        .distinctUntilChanged()

    private val summary = combineTuple(resolvedCycleId, showExcluded)
        .flatMapLatest { (cycleId, addExcluded) ->
            if (cycleId == OarDatabase.INVALID_ID_LONG) flowOf(null)
            else statisticsRepo.getCycleSummary(cycleId, addExcluded)
        }

    private val isCycleOnPace = combineTuple(
        summary,
        cycleElapsedDays,
        cycleTotalDays
    ).mapLatest { (summary, elapsedDays, totalDays) ->
        if (summary == null || totalDays <= 0) return@mapLatest true
        val projectedSpend = (summary.budget.toDouble() / totalDays) * elapsedDays
        summary.spent <= projectedSpend
    }.distinctUntilChanged()

    private val cycleBars = combineTuple(resolvedCycleId, showExcluded)
        .flatMapLatest { (cycleId, addExcluded) ->
            if (cycleId == OarDatabase.INVALID_ID_LONG) flowOf(emptyList())
            else statisticsRepo.getRecentCycleBars(
                cycleId = cycleId,
                addExcluded = addExcluded
            )
        }

    private val selectedCycleBar = combineTuple(selectedBarCycleId, cycleBars)
        .mapLatest { (selectedBarCycleId, cycleBars) ->
            cycleBars.find { it.cycleId == selectedBarCycleId }
        }

    private val tagBreakdown = combineTuple(resolvedCycleId, showExcluded)
        .flatMapLatest { (cycleId, addExcluded) ->
            if (cycleId == OarDatabase.INVALID_ID_LONG) flowOf(emptyList())
            else statisticsRepo.getTagBreakdown(cycleId, addExcluded)
        }

    private val selectedTagEntry = combineTuple(selectedTagId, tagBreakdown)
        .mapLatest { (selectedTagId, tagBreakdown) ->
            tagBreakdown.find { it.tagId == selectedTagId }
        }

    private val largestSpend = combineTuple(resolvedCycleId, showExcluded)
        .flatMapLatest { (cycleId, addExcluded) ->
            if (cycleId == OarDatabase.INVALID_ID_LONG) flowOf(null)
            else statisticsRepo.getLargestSpend(cycleId, addExcluded)
        }

    val state = combineTuple(
        resolvedCycle,
        summary,
        cycleElapsedDays,
        cycleTotalDays,
        isCycleOnPace,
        cycleBars,
        selectedCycleBar,
        chartMode,
        tagBreakdown,
        selectedTagEntry,
        largestSpend,
        showExcluded
    ).mapLatest { (
                      cycle,
                      summary,
                      cycleElapsedDays,
                      cycleTotalDays,
                      isCycleOnPace,
                      cycleBars,
                      selectedCycleBar,
                      chartMode,
                      tagBreakdown,
                      selectedTagEntry,
                      largestSpend,
                      showExcluded
                  ) ->
        StatisticsState(
            selectedCycle = cycle,
            summary = summary,
            cycleElapsedDays = cycleElapsedDays,
            cycleTotalDays = cycleTotalDays,
            isCycleOnPace = isCycleOnPace,
            cycleBars = cycleBars,
            selectedCycleBar = selectedCycleBar,
            chartMode = chartMode,
            tagBreakdown = tagBreakdown,
            selectedTagEntry = selectedTagEntry,
            largestSpend = largestSpend,
            showExcluded = showExcluded,
            isEmpty = summary?.transactionCount == 0
        )
    }.asStateFlow(viewModelScope, StatisticsState())

    override fun onExcludedToggle() {
        savedStateHandle[SHOW_EXCLUDED] = !showExcluded.value
    }

    override fun onChartModeChange(mode: StatisticsChartMode) {
        savedStateHandle[CHART_MODE] = mode
    }

    override fun onBarSelect(cycleId: Long) {
        savedStateHandle[SELECTED_BAR_CYCLE_ID] = cycleId
    }

    override fun onTagSelect(tagId: Long) {
        savedStateHandle[SELECTED_TAG_ID] = tagId
            .takeIf { selectedTagId.value != tagId }
    }

    override fun onCycleSelect(
        cycleId: Long?
    ) {
        savedStateHandle[SELECTED_CYCLE_ID] = cycleId
        savedStateHandle[SELECTED_BAR_CYCLE_ID] = null
        savedStateHandle[SELECTED_TAG_ID] = null
    }
}

private const val SELECTED_CYCLE_ID = "SELECTED_CYCLE_ID"
private const val SHOW_EXCLUDED = "SHOW_EXCLUDED"
private const val CHART_MODE = "CHART_MODE"
private const val SELECTED_BAR_CYCLE_ID = "SELECTED_BAR_CYCLE_ID"
private const val SELECTED_TAG_ID = "SELECTED_TAG_ID"
