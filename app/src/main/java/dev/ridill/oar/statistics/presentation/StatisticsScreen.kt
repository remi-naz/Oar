package dev.ridill.oar.statistics.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.ridill.oar.R
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleEntry
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.SelectableColorsList
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.statistics.domain.model.CycleBarEntry
import dev.ridill.oar.statistics.domain.model.CycleSummary
import dev.ridill.oar.statistics.domain.model.LargestSpend
import dev.ridill.oar.statistics.domain.model.StatisticsChartMode
import dev.ridill.oar.statistics.domain.model.TagSpendEntry
import dev.ridill.oar.statistics.presentation.components.CycleBarChart
import dev.ridill.oar.statistics.presentation.components.CycleSummaryCard
import dev.ridill.oar.statistics.presentation.components.StatTile
import dev.ridill.oar.statistics.presentation.components.StatTileGrid
import dev.ridill.oar.statistics.presentation.components.StatisticsEmptyState
import dev.ridill.oar.statistics.presentation.components.TagDonutChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    snackbarController: SnackbarController,
    state: StatisticsState,
    actions: StatisticsActions,
    navigateUp: () -> Unit,
    navigateToCycleSelection: () -> Unit,
    navigateToAddEditTransaction: () -> Unit
) {
    val cycle = state.selectedCycle
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    OarScaffold(
        snackbarController = snackbarController,
        isLoading = cycle == null,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.destination_statistics)) },
                    navigationIcon = { BackArrowButton(onClick = navigateUp) },
                    actions = {
                        FilterChip(
                            selected = state.showExcluded,
                            onClick = actions::onExcludedToggle,
                            label = {
                                Text(
                                    stringResource(
                                        if (state.showExcluded) R.string.excluded_on
                                        else R.string.excluded_off
                                    )
                                )
                            },
                            modifier = Modifier
                                .padding(end = MaterialTheme.spacing.small)
                        )
                    },
                    scrollBehavior = topAppBarScrollBehavior
                )

                if (cycle != null) {
                    Row(
                        modifier = Modifier
                            .padding(
                                horizontal = MaterialTheme.spacing.medium,
                                vertical = MaterialTheme.spacing.extraSmall
                            )
                    ) {
                        AssistChip(
                            onClick = navigateToCycleSelection,
                            label = { Text(cycle.description) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = stringResource(R.string.destination_budget_cycle_selection)
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (cycle == null) return@OarScaffold
        LazyColumn(
            contentPadding = PaddingValues(
                start = MaterialTheme.spacing.medium,
                end = MaterialTheme.spacing.medium,
                top = paddingValues.calculateTopPadding() + MaterialTheme.spacing.small,
                bottom = PaddingScrollEnd
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (state.isEmpty) {
                item(
                    key = "EmptyState",
                    contentType = "EmptyState"
                ) {
                    StatisticsEmptyState(
                        onAddTransactionClick = navigateToAddEditTransaction,
                        onChooseCycleClick = navigateToCycleSelection,
                        modifier = Modifier
                            .fillParentMaxHeight(EmptyStateHeightFraction)
                    )
                }
            }

            state.summary?.let { summary ->
                item(
                    key = "Summary",
                    contentType = "Summary"
                ) {
                    CycleSummaryCard(
                        summary = summary,
                        isOnPace = state.isCycleOnPace,
                        currentDay = state.cycleElapsedDays + 1,
                        daysTotal = state.cycleTotalDays,
                        modifier = Modifier
                            .animateItem()
                    )
                }
            }

            if (state.cycleBars.isNotEmpty()) {
                item(
                    key = "BarChart",
                    contentType = "BarChart"
                ) {
                    CycleBarChart(
                        bars = state.cycleBars,
                        selectedBar = state.selectedCycleBar,
                        chartMode = state.chartMode,
                        cycleBarChartSummaryText = state.cycleBarsSummaryText,
                        onModeChange = actions::onChartModeChange,
                        onBarSelect = actions::onBarSelect,
                        modifier = Modifier
                            .animateItem()
                    )
                }
            }

            if (state.tagBreakdown.isNotEmpty()) {
                item(key = "TagDonut", contentType = "TagDonut") {
                    TagDonutChart(
                        entries = state.tagBreakdown,
                        currency = cycle.currency,
                        onSelect = {},
                        selectedEntry = null,
                        totalSpend = 1000.0,
                    )
                }
            }

            state.summary?.let { summary ->
                item(key = "StatTiles", contentType = "StatTiles") {
                    StatTileGrid(tiles = buildStatTiles(summary, state))
                }
            }
        }
    }
}

@Composable
private fun buildStatTiles(
    summary: CycleSummary,
    state: StatisticsState
): List<StatTile> {
    val busiestTag = state.tagBreakdown.maxByOrNull { it.transactionCount }
    val untaggedLabel = stringResource(R.string.statistics_untagged)

    return listOf(
        StatTile(
            label = stringResource(R.string.statistics_average_per_day),
            value = "summary.averagePerDayFormatted",
            sub = stringResource(R.string.statistics_over_x_days, 10)
        ),
        StatTile(
            label = stringResource(R.string.statistics_transactions),
            value = summary.transactionCount.toString(),
            sub = stringResource(
                if (state.showExcluded) R.string.statistics_including_excluded
                else R.string.statistics_excluding_excluded
            )
        ),
        StatTile(
            label = stringResource(R.string.statistics_largest_spend),
            value = state.largestSpend?.amountFormatted.orEmpty(),
            sub = state.largestSpend?.note.orEmpty()
        ),
        StatTile(
            label = stringResource(R.string.statistics_busiest_tag),
            value = busiestTag?.name ?: untaggedLabel,
            sub = stringResource(
                R.string.statistics_x_transactions,
                busiestTag?.transactionCount ?: 0
            )
        )
    )
}

private const val EmptyStateHeightFraction = 0.85f

private val PreviewActions = object : StatisticsActions {
    override fun onExcludedToggle() {}
    override fun onChartModeChange(mode: StatisticsChartMode) {}
    override fun onBarSelect(cycleId: Long) {}
    override fun onTagSelect(tagId: Long) {}
    override fun onCycleSelect(cycleId: Long?) {}
}

private fun previewStatisticsState(): StatisticsState {
    val currency = LocaleUtil.currencyForCode("INR")
    val startDate = DateUtil.dateNow().withDayOfMonth(1)
    val endDate = startDate.plusMonths(1).minusDays(1)

    val cycle = BudgetCycleEntry(
        id = 5L,
        startDate = startDate,
        endDate = endDate,
        budget = 60_000L,
        currency = currency,
        active = true
    )

    val summary = CycleSummary(
        cycleId = cycle.id,
        startDate = startDate,
        endDate = endDate,
        spent = 41_280.0,
        received = 8_400.0,
        budget = cycle.budget,
        currency = currency,
        transactionCount = 63,
    )

    val cycleAmounts = listOf(
        52_100.0 to 5_200.0,
        47_300.0 to 12_000.0,
        61_900.0 to 4_800.0,
        44_800.0 to 9_600.0,
        55_200.0 to 3_400.0,
        41_280.0 to 8_400.0
    )
    val cycleBars = cycleAmounts.mapIndexed { index, (spent, received) ->
        val barStart = startDate.minusMonths((cycleAmounts.size - 1 - index).toLong())
        CycleBarEntry(
            cycleId = index.toLong(),
            startDate = barStart,
            endDate = barStart.plusMonths(1).minusDays(1),
            spent = spent,
            received = received,
            budget = 60_000L,
            currency = LocaleUtil.currencyForCode("INR"),
        )
    }

    val tagAmounts = listOf(
        "Rent & Bills" to 11_000.0,
        "Food & Drink" to 9_860.0,
        "Transport" to 6_420.0,
        "Shopping" to 5_980.0,
        "Health" to 3_540.0
    )
    val tagTotal = tagAmounts.sumOf { it.second }
    val tagBreakdown = tagAmounts.mapIndexed { index, (name, amount) ->
        TagSpendEntry(
            tagId = index.toLong(),
            name = name,
            colorCode = SelectableColorsList[index].toArgb(),
            amount = amount,
            transactionCount = (25 - index * 4),
            fraction = (amount / tagTotal).toFloat()
        )
    }

    return StatisticsState(
        selectedCycle = cycle,
        summary = summary,
        cycleBars = cycleBars,
        chartMode = StatisticsChartMode.SPEND,
        tagBreakdown = tagBreakdown,
        largestSpend = LargestSpend(amount = 9_450.0, note = "Rent — August", currency = currency),
        showExcluded = false,
        isEmpty = false
    )
}

@PreviewLightDark
@Composable
private fun PreviewStatisticsScreen() {
    OarTheme {
        StatisticsScreen(
            snackbarController = rememberSnackbarController(),
            state = previewStatisticsState(),
            actions = PreviewActions,
            navigateUp = {},
            navigateToCycleSelection = {},
            navigateToAddEditTransaction = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewStatisticsScreenEmpty() {
    val base = previewStatisticsState()
    OarTheme {
        StatisticsScreen(
            snackbarController = rememberSnackbarController(),
            state = base.copy(
                summary = base.summary?.copy(
                    spent = 0.0,
                    received = 0.0,
                    transactionCount = 0
                ),
                cycleBars = emptyList(),
                tagBreakdown = emptyList(),
                largestSpend = null,
                isEmpty = true
            ),
            actions = PreviewActions,
            navigateUp = {},
            navigateToCycleSelection = {},
            navigateToAddEditTransaction = {}
        )
    }
}
