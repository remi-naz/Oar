package dev.ridill.oar.statistics.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.statistics.domain.model.CycleSummary
import dev.ridill.oar.statistics.domain.model.TagSpend
import dev.ridill.oar.statistics.domain.repository.StatisticsRepository
import dev.ridill.oar.statistics.presentation.components.StatTile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import java.time.temporal.ChronoUnit
import java.util.Currency
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val cycleRepo: BudgetCycleRepository,
    private val statisticsRepo: StatisticsRepository
) : ViewModel(), StatisticsActions {

    private val currentDate = MutableStateFlow(DateUtil.dateNow())
    private val selectedCycleId = savedStateHandle.getStateFlow<Long?>(SELECTED_CYCLE_ID, null)
    private val selectedCurrency = savedStateHandle
        .getStateFlow<Currency?>(SELECTED_CURRENCY, LocaleUtil.defaultCurrency)
    private val showExcluded = savedStateHandle.getStateFlow(SHOW_EXCLUDED, false)

    private var lastResolvedCycleId: Long? = null
    private val resolvedCycle = selectedCycleId.flatMapLatest { id ->
        if (id != null) cycleRepo.getCycleByIdFlow(id)
        else cycleRepo.getActiveCycleFlow()
    }.distinctUntilChanged()
        .onEach { cycle ->
            // Default the currency to the cycle's currency only when the cycle changes,
            // so a user selected currency is kept across updates to the same cycle
            if (cycle == null || cycle.id == lastResolvedCycleId) return@onEach
            lastResolvedCycleId = cycle.id
            savedStateHandle[SELECTED_CURRENCY] = cycle.currency
        }
    private val resolvedCycleId = resolvedCycle
        .mapLatest { it?.id ?: OarDatabase.INVALID_ID_LONG }
        .distinctUntilChanged()
    private val resolvedCurrency = combineTuple(
        selectedCurrency,
        resolvedCycle
    ).mapLatest { (currency, cycle) -> currency ?: cycle?.currency ?: LocaleUtil.defaultCurrency }
        .distinctUntilChanged()

    // Cycle Summary
    private val cycleTotals = combineTuple(
        resolvedCycleId,
        resolvedCurrency,
        showExcluded
    ).flatMapLatest { (
                          cycleId,
                          currency,
                          addExcluded
                      ) ->
        if (cycleId == OarDatabase.INVALID_ID_LONG) flowOf(null)
        else statisticsRepo.getCycleTotals(
            cycleId = cycleId,
            currency = currency,
            addExcluded = addExcluded
        )
    }

    private val cycleAggregate = cycleTotals.mapLatest {
        if (it == null) Double.Zero
        else it.spent - it.received
    }.distinctUntilChanged()

    private val budgetUsageFraction = combineTuple(
        resolvedCycle.mapLatest { it?.budget.orZero() }.distinctUntilChanged(),
        cycleAggregate
    ).mapLatest { (budget, agg) ->
        if (budget <= 0) 0f
        else (agg / budget).toFloat().coerceAtLeast(0f)
    }.distinctUntilChanged()

    private val cycleElapsedDays = combineTuple(
        currentDate,
        resolvedCycle
    ).mapLatest { (currentDate, cycle) ->
        when {
            cycle == null -> 0
            currentDate.isBefore(cycle.startDate) -> 0
            currentDate.isAfter(cycle.endDate) -> ChronoUnit.DAYS.between(
                cycle.startDate,
                cycle.endDate
            ).toInt()

            else -> ChronoUnit.DAYS.between(cycle.startDate, currentDate).toInt()
        }
    }.distinctUntilChanged()
    private val cycleTotalDays = resolvedCycle
        .mapLatest {
            if (it == null) 0
            else ChronoUnit.DAYS.between(it.startDate, it.endDate).toInt()
        }.distinctUntilChanged()

    private val cycleSummary = combineTuple(
        resolvedCycle,
        cycleTotals,
        cycleAggregate,
        budgetUsageFraction,
        cycleElapsedDays,
        cycleTotalDays,
    ).mapLatest { (
                      cycle,
                      totals,
                      agg,
                      usageFraction,
                      cycleElapsedDays,
                      cycleTotalDays,
                  ) ->
        if (cycle == null || totals == null) null
        else CycleSummary(
            cycleId = cycle.id,
            startDate = cycle.startDate,
            endDate = cycle.endDate,
            spent = totals.spent,
            received = totals.received,
            budget = cycle.budget,
            currency = cycle.currency,
            aggregate = agg,
            usageFraction = usageFraction,
            totalDays = cycleTotalDays,
            elapsedDays = cycleElapsedDays,
        )
    }.distinctUntilChanged()

    // Tag spends
    private val creditsInclusiveBudget = combineTuple(
        resolvedCycleId,
        resolvedCurrency,
        showExcluded
    ).flatMapLatest { (cycleId, currency, addExcluded) ->
        statisticsRepo.getCreditsInclusiveBudget(
            cycleId = cycleId,
            currency = currency,
            addExcluded = addExcluded
        )
    }.distinctUntilChanged()

    private val tagSlices = combineTuple(
        resolvedCycleId,
        resolvedCurrency,
        showExcluded,
        creditsInclusiveBudget
    ).flatMapLatest { (
                          cycleId,
                          currency,
                          addExcluded,
                          total
                      ) ->
        statisticsRepo.getTagSpends(
            cycleId = cycleId,
            currency = currency,
            addExcluded = addExcluded,
            total = total
        ).mapLatest { list -> list.map(TagSpend::toChartSlice) }
    }

    // Stat tiles
    private val cycleDayAvg = cycleSummary.mapLatest {
        if (it == null) Double.Zero
        else it.spent / it.elapsedDays.coerceAtLeast(1)
    }.distinctUntilChanged()

    private val cycleLargest = combineTuple(
        resolvedCycleId,
        resolvedCurrency,
        showExcluded
    ).flatMapLatest { (cycleId, currency, addExcluded) ->
        if (cycleId == OarDatabase.INVALID_ID_LONG) flowOf(null)
        else statisticsRepo.getLargestSpend(cycleId, currency, addExcluded)
    }

    private val cycleTransactionCount = combineTuple(
        resolvedCycleId,
        resolvedCurrency,
        showExcluded
    ).flatMapLatest { (cycleId, currency, addExcluded) ->
        if (cycleId == OarDatabase.INVALID_ID_LONG) flowOf(0)
        else statisticsRepo.getTransactionCount(cycleId, currency, addExcluded)
    }.distinctUntilChanged()

    private val busiestTag = combineTuple(
        resolvedCycleId,
        resolvedCurrency,
        showExcluded
    ).flatMapLatest { (cycleId, currency, addExcluded) ->
        if (cycleId == OarDatabase.INVALID_ID_LONG) flowOf(null)
        else statisticsRepo.getBusiestTag(cycleId, currency, addExcluded)
    }.distinctUntilChanged()

    private val statTileRows = combineTuple(
        cycleSummary,
        cycleDayAvg,
        resolvedCurrency,
        cycleTransactionCount,
        cycleLargest,
        busiestTag,
        showExcluded
    ).mapLatest { (
                      cycle,
                      cycleDayAvg,
                      currency,
                      txCount,
                      largestSpend,
                      busiestTag,
                      addExcluded
                  ) ->
        if (cycle == null) return@mapLatest emptyList()

        buildList {
            add(
                StatTile(
                    key = "AveragePerDay",
                    label = UiText.StringResource(R.string.statistics_average_per_day),
                    value = TextFormat.currency(cycleDayAvg, currency),
                    sub = UiText.StringResource(
                        R.string.statistics_over_x_days,
                        args = listOf(TextFormat.number(cycle.elapsedDays))
                    )
                )
            )
            add(
                StatTile(
                    key = "Transactions",
                    label = UiText.StringResource(R.string.statistics_transactions),
                    value = TextFormat.number(txCount),
                    sub = UiText.StringResource(
                        if (addExcluded) R.string.statistics_including_excluded
                        else R.string.statistics_excluding_excluded
                    )
                )
            )
            largestSpend?.let {
                add(
                    StatTile(
                        key = "LargestSpend",
                        label = UiText.StringResource(R.string.statistics_largest_spend),
                        value = it.amountFormatted,
                        sub = UiText.DynamicString(it.note)
                    )
                )
            }
            busiestTag?.let {
                add(
                    StatTile(
                        key = "BusiestTag",
                        label = UiText.StringResource(R.string.statistics_busiest_tag),
                        value = it.name,
                        sub = UiText.StringResource(
                            R.string.statistics_x_transactions,
                            args = listOf(TextFormat.number(it.transactionCount))
                        )
                    )
                )
            }
        }.chunked(STAT_TILES_PER_ROW)
    }.distinctUntilChanged()
    
    private val isEmpty = cycleTransactionCount
        .mapLatest { it <= 0 }
        .distinctUntilChanged()

    val state = combineTuple(
        resolvedCycle,
        cycleSummary,
        tagSlices,
        statTileRows,
        resolvedCurrency,
        showExcluded,
        isEmpty
    ).mapLatest { (
                      cycle,
                      summary,
                      tagSlices,
                      statTileRows,
                      currency,
                      showExcluded,
                      isEmpty
                  ) ->
        StatisticsState(
            selectedCycle = cycle,
            summary = summary,
            tagSlices = tagSlices,
            statTileRows = statTileRows,
            currency = currency,
            showExcluded = showExcluded,
            isEmpty = isEmpty,
        )
    }.asStateFlow(viewModelScope, StatisticsState())

    override fun onExcludedToggle() {
        savedStateHandle[SHOW_EXCLUDED] = !showExcluded.value
    }

    override fun onCurrencySelect(currency: Currency) {
        savedStateHandle[SELECTED_CURRENCY] = currency
    }

    override fun onCycleSelect(
        cycleId: Long?
    ) {
        savedStateHandle[SELECTED_CYCLE_ID] = cycleId
    }
}

private const val STAT_TILES_PER_ROW = 2
private const val SELECTED_CYCLE_ID = "SELECTED_CYCLE_ID"
private const val SELECTED_CURRENCY = "SELECTED_CURRENCY"
private const val SHOW_EXCLUDED = "SHOW_EXCLUDED"
