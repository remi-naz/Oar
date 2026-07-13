package dev.ridill.oar.budgetCycles.presentation.cycleHistory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.oar.R
import dev.ridill.oar.budgetCycles.domain.model.ActiveCycleOption
import dev.ridill.oar.budgetCycles.domain.model.CycleHistoryEntry
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.One
import dev.ridill.oar.core.domain.util.WhiteSpace
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.ConfirmationDialog
import dev.ridill.oar.core.ui.components.ItemListSheet
import dev.ridill.oar.core.ui.components.LabelLargeText
import dev.ridill.oar.core.ui.components.ListSeparator
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.OptionListItem
import dev.ridill.oar.core.ui.components.SpacerMedium
import dev.ridill.oar.core.ui.components.SwipeToAction
import dev.ridill.oar.core.ui.components.TitleLargeText
import dev.ridill.oar.core.ui.components.TitleMediumText
import dev.ridill.oar.core.ui.components.listEmptyIndicator
import dev.ridill.oar.core.ui.components.rememberSwipeToActionState
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.NegativeRed
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.PositiveGreen
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.isEmpty
import dev.ridill.oar.core.ui.util.isNotEmpty
import dev.ridill.oar.core.ui.util.plus
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BudgetCyclesScreenContent(
    state: BudgetCyclesState,
    history: LazyPagingItems<CycleHistoryEntry>,
    actions: BudgetCyclesActions,
    navigateUp: () -> Unit,
    navigateToUpdateBudget: () -> Unit,
    navigateToCurrencySelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    OarScaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.destination_budget_cycles)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        modifier = modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = paddingValues + PaddingValues(
                top = MaterialTheme.spacing.medium,
                bottom = PaddingScrollEnd
            )
        ) {
            state.activeCycle?.let { cycle ->
                item(
                    key = cycle.id,
                    contentType = "ActiveCycleCard"
                ) {
                    ActiveCycleDetails(
                        startDate = cycle.startDate,
                        endDate = cycle.endDate,
                        budget = cycle.budgetFormatted,
                        aggregate = cycle.aggregateFormatted,
                        onCycleOptionsClick = actions::onCycleOptionsClick,
                        cycleProgressFraction = { state.activeCycleProgressFraction },
                        showCompleteAction = state.showCycleCompleteAction,
                        onCompleteCycleSwiped = actions::onCompleteActiveCycleAction,
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.spacing.medium)
                            .animateItem()
                    )
                }
            }

            listEmptyIndicator(
                isListEmpty = history.isEmpty(),
                messageRes = R.string.your_completed_cycles_will_appear_here
            )

            if (history.isNotEmpty()) {
                stickyHeader(
                    key = "CompletedCyclesLabel",
                    contentType = "CompletedCyclesLabel"
                ) {
                    Column(
                        modifier = Modifier
                            .animateItem()
                    ) {
                        SpacerMedium()
                        ListSeparator(
                            label = stringResource(R.string.completed_cycles)
                        )
                        SpacerMedium()
                    }
                }

                items(
                    count = history.itemCount,
                    key = history.itemKey { it.id },
                    contentType = history.itemContentType { CycleHistoryEntry::class }
                ) { index ->
                    history[index]?.let { cycle ->
                        CycleHistoryItem(
                            description = cycle.description,
                            budget = cycle.budgetFormatted,
                            aggregate = cycle.aggregateFormatted,
                            isUnderBudget = cycle.isWithinBudget,
                            modifier = Modifier
                                .animateItem()
                        )
                    }
                }
            }
        }
    }

    if (state.showCycleCompletionWarning) {
        ConfirmationDialog(
            titleRes = R.string.complete_cycle_confirmation_title,
            contentRes = R.string.complete_cycle_confirmation_message,
            onDismiss = actions::onCompleteActiveCycleDismiss,
            onConfirm = actions::onCompleteActiveCycleConfirm
        )
    }

    if (state.showCycleOptions) {
        CycleOptionsSheet(
            onDismiss = actions::onCycleOptionsDismiss,
            onOptionSelect = { option ->
                actions.onCycleOptionsDismiss()
                when (option) {
                    ActiveCycleOption.UPDATE_BUDGET -> {
                        navigateToUpdateBudget()
                    }

                    ActiveCycleOption.CHANGE_BASE_CURRENCY -> {
                        navigateToCurrencySelection()
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActiveCycleDetails(
    startDate: LocalDate,
    endDate: LocalDate,
    budget: String,
    aggregate: String,
    onCycleOptionsClick: () -> Unit,
    cycleProgressFraction: () -> Float,
    showCompleteAction: Boolean,
    onCompleteCycleSwiped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeToActionState = rememberSwipeToActionState(
        onSwiped = onCompleteCycleSwiped
    )
    val contentColor = LocalContentColor.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            CircularWavyProgressIndicator(
                progress = { cycleProgressFraction() + ((Float.One - cycleProgressFraction()) * swipeToActionState.swipeProgress) },
                modifier = Modifier
                    .size(ProgressIndicatorSize)
            )

            Column(
                modifier = Modifier
                    .weight(Float.One)
            ) {
                TitleLargeText(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = contentColor)) {
                            append(aggregate)
                        }

                        withStyle(SpanStyle(color = contentColor.copy(alpha = ContentAlpha.SUB_CONTENT))) {
                            append("/")
                            append(budget)
                        }
                    }
                )

                LabelLargeText(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = contentColor.copy(alpha = ContentAlpha.SUB_CONTENT))) {
                            append(stringResource(R.string.cycle))
                            append(String.WhiteSpace)
                        }
                        withStyle(SpanStyle(color = contentColor)) {
                            append(DateUtil.prettyDateRange(startDate, endDate))
                        }
                    }
                )
            }

            IconButton(
                onClick = onCycleOptionsClick,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_gears),
                    contentDescription = null
                )
            }
        }

        SpacerMedium()

        AnimatedVisibility(showCompleteAction) {
            SwipeToAction(
                state = swipeToActionState,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

private val ProgressIndicatorSize = 48.dp

@PreviewLightDark
@Composable
private fun PreviewActiveCycleInfo() {
    OarTheme {
        Surface {
            ActiveCycleDetails(
                startDate = DateUtil.dateNow(),
                endDate = DateUtil.dateNow().plusMonths(1L),
                budget = 1000L.toString(),
                aggregate = 500L.toString(),
                cycleProgressFraction = { 0.5f },
                onCompleteCycleSwiped = {},
                onCycleOptionsClick = {},
                showCompleteAction = true,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CycleHistoryItem(
    description: String,
    budget: String,
    aggregate: String,
    isUnderBudget: Boolean,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier,
        trailingContent = {
            TitleMediumText(
                text = stringResource(R.string.amounts_divided, aggregate, budget),
                color = if (isUnderBudget) PositiveGreen else NegativeRed
            )
        },
    ) {
        Text(description)
    }
}

@Composable
private fun CycleOptionsSheet(
    onDismiss: () -> Unit,
    onOptionSelect: (ActiveCycleOption) -> Unit,
    modifier: Modifier = Modifier
) {
    ItemListSheet(
        onDismiss = onDismiss,
        items = ActiveCycleOption.entries,
        key = { it.name },
        contentType = { ActiveCycleOption::class },
        modifier = modifier
    ) { option ->
        OptionListItem(
            iconRes = option.iconRes,
            label = stringResource(option.labelRes),
            onClick = { onOptionSelect(option) },
            onClickLabel = null,
            modifier = Modifier
                .fillMaxWidth()
                .animateItem()
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewCycleHistoryScreenContent() {
    OarTheme {
        BudgetCyclesScreenContent(
            state = BudgetCyclesState(
                activeCycle = CycleHistoryEntry(
                    id = 1L,
                    startDate = DateUtil.dateNow(),
                    endDate = DateUtil.dateNow().plusMonths(1L),
                    budget = 1000L,
                    currency = LocaleUtil.defaultCurrency,
                    active = true,
                    aggregate = 500.0
                )
            ),
            history = flowOf(
                PagingData.from(
                    List(10) {
                        CycleHistoryEntry(
                            id = it.toLong(),
                            startDate = DateUtil.dateNow(),
                            endDate = DateUtil.dateNow().plusMonths(1L),
                            budget = 1000L,
                            currency = LocaleUtil.defaultCurrency,
                            active = true,
                            aggregate = 500.0
                        )
                    }
                )
            ).collectAsLazyPagingItems(),
            actions = object : BudgetCyclesActions {
                override fun onCycleOptionsClick() {}
                override fun onCycleOptionsDismiss() {}
                override fun onCompleteActiveCycleAction() {}
                override fun onCompleteActiveCycleDismiss() {}
                override fun onCompleteActiveCycleConfirm() {}
            },
            navigateUp = {},
            navigateToUpdateBudget = {},
            navigateToCurrencySelection = {}
        )
    }
}