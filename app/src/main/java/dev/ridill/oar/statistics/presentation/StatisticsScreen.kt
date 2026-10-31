package dev.ridill.oar.statistics.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.ridill.oar.R
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleEntry
import dev.ridill.oar.budgetCycles.presentation.currencyUpdate.CurrencySelectionButton
import dev.ridill.oar.core.domain.model.ChartSlice
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
import dev.ridill.oar.core.ui.util.PaddingSide
import dev.ridill.oar.core.ui.util.exclude
import dev.ridill.oar.statistics.domain.model.CycleSummary
import dev.ridill.oar.statistics.presentation.components.CycleSummaryCard
import dev.ridill.oar.statistics.presentation.components.StatTileGrid
import dev.ridill.oar.statistics.presentation.components.StatisticsEmptyState
import dev.ridill.oar.statistics.presentation.components.TagDonutChart
import java.util.Currency

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
            TopAppBar(
                title = { Text(stringResource(R.string.destination_statistics)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                actions = {
                    CurrencySelectionButton(
                        currency = state.currency,
                        onCurrencySelect = actions::onCurrencySelect
                    )
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.cd_more_options)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(
                                            if (state.showExcluded) R.string.hide_excluded_transactions
                                            else R.string.show_excluded_transactions
                                        )
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    actions.onExcludedToggle()
                                }
                            )
                        }
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        }
    ) { paddingValues ->
        if (cycle == null) return@OarScaffold
        LazyColumn(
            contentPadding = PaddingValues(
                start = MaterialTheme.spacing.medium,
                end = MaterialTheme.spacing.medium,
                top = MaterialTheme.spacing.small,
                bottom = PaddingScrollEnd
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues.exclude(PaddingSide.BOTTOM))
        ) {
            item(
                key = "CycleSelector",
                contentType = "CycleSelector",
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = MaterialTheme.spacing.extraSmall)
                        .animateItem()
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
                            .animateItem()
                    )
                }
            }

            state.summary?.let { summary ->
                item(
                    key = "Summary",
                    contentType = "Summary"
                ) {
                    CycleSummaryCard(
                        net = summary.aggregate,
                        budget = summary.budget,
                        usageFraction = summary.usageFraction,
                        elapsedDays = summary.elapsedDays,
                        totalDays = summary.totalDays,
                        spent = summary.spent,
                        received = summary.received,
                        baseCurrency = summary.currency,
                        currency = state.currency,
                        modifier = Modifier
                            .animateItem()
                    )
                }
            }

            if (state.tagSlices.isNotEmpty()) {
                item(
                    key = "TagDonut",
                    contentType = "TagDonut"
                ) {
                    TagDonutChart(
                        entries = state.tagSlices,
                        currency = state.currency,
                        modifier = Modifier
                            .animateItem(),
                    )
                }
            }

            if (state.tagSlices.isNotEmpty()) {
                item(
                    key = "StatTiles",
                    contentType = "StatTiles"
                ) {
                    StatTileGrid(
                        tileRows = state.statTileRows,
                        modifier = Modifier
                            .animateItem(),
                    )
                }
            }
        }
    }
}

private const val EmptyStateHeightFraction = 0.85f

private val PreviewActions = object : StatisticsActions {
    override fun onExcludedToggle() {}
    override fun onCycleSelect(cycleId: Long?) {}
    override fun onCurrencySelect(currency: Currency) {}
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
        aggregate = 10_000.0,
        usageFraction = 0.50f,
        totalDays = 30,
        elapsedDays = 10,
    )

    val tagAmounts = listOf(
        "Rent & Bills" to 11_000.0,
        "Food & Drink" to 9_860.0,
        "Transport" to 6_420.0,
        "Shopping" to 5_980.0,
        "Health" to 3_540.0
    )
    val tagTotal = tagAmounts.sumOf { it.second }
    val tagBreakdown = tagAmounts.mapIndexed { index, (name, amount) ->
        ChartSlice(
            id = index.toLong(),
            label = name,
            amount = amount,
            fraction = (amount / tagTotal).toFloat(),
            colorCode = SelectableColorsList[index].toArgb()
        )
    }

    return StatisticsState(
        selectedCycle = cycle,
        summary = summary,
        tagSlices = tagBreakdown,
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
                ),
                tagSlices = emptyList(),
                isEmpty = true
            ),
            actions = PreviewActions,
            navigateUp = {},
            navigateToCycleSelection = {},
            navigateToAddEditTransaction = {}
        )
    }
}
