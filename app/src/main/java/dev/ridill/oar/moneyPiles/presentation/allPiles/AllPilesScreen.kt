package dev.ridill.oar.moneyPiles.presentation.allPiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.EmptyListIndicator
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.ui.util.isEmpty
import dev.ridill.oar.core.ui.util.isNotEmpty
import dev.ridill.oar.moneyPiles.domain.model.MoneyPile
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AllPilesScreen(
    snackbarController: SnackbarController,
    state: AllPilesState,
    pilesPagingItems: LazyPagingItems<MoneyPile>,
    navigateToAddPile: () -> Unit,
    navigateToPileDetails: (Long) -> Unit,
    navigateToAddToPile: (Long) -> Unit,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    OarScaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.destination_all_piles)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        modifier = modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            FloatingActionButton(onClick = navigateToAddPile) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.cd_new_pile)
                )
            }
        },
        snackbarController = snackbarController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (pilesPagingItems.isNotEmpty()) {
                Text(
                    text = stringResource(
                        R.string.pile_list_subtitle,
                        TextFormat.currencyAmount(state.totalSavedAmount),
                        pilesPagingItems.itemCount
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.small
                    )
                )
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                if (pilesPagingItems.isEmpty()) {
                    EmptyListIndicator(
                        rawResId = R.raw.lottie_empty_list_ghost,
                        messageRes = R.string.piles_list_empty_message
                    )
                }
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = MaterialTheme.spacing.small,
                        bottom = PaddingScrollEnd,
                        start = MaterialTheme.spacing.medium,
                        end = MaterialTheme.spacing.medium
                    ),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    verticalItemSpacing = MaterialTheme.spacing.medium
                ) {
                    items(
                        count = pilesPagingItems.itemCount,
                        key = pilesPagingItems.itemKey { it.id },
                        contentType = pilesPagingItems.itemContentType { MoneyPile::class }
                    ) { index ->
                        pilesPagingItems[index]?.let { pile ->
                            val progressFraction = pile.targetAmount
                                ?.takeIf { it > 0 }
                                ?.let { (pile.currentAmount / it).toFloat() }
                            val complete = progressFraction != null && progressFraction >= 1f
                            PileGridItem(
                                icon = pile.icon,
                                name = pile.name,
                                accent = Color(pile.color),
                                accumulatedAmountLabel = TextFormat.currencyAmount(pile.currentAmount),
                                targetLabel = pile.targetAmount?.let {
                                    stringResource(
                                        R.string.pile_target_label,
                                        TextFormat.currencyAmount(it)
                                    )
                                } ?: stringResource(R.string.pile_no_goal_label),
                                progressFraction = progressFraction,
                                locked = pile.locked,
                                complete = complete,
                                onClick = { navigateToPileDetails(pile.id) },
                                onQuickAddClick = { navigateToAddToPile(pile.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewAllPilesScreen() {
    OarTheme {
        AllPilesScreen(
            snackbarController = rememberSnackbarController(),
            state = AllPilesState(),
            pilesPagingItems = flowOf(PagingData.from(List(5) {
                MoneyPile(
                    id = it.toLong(),
                    name = "Pile $it",
                    note = "",
                    icon = "🌸",
                    color = 0xFFFF4CA6.toInt(),
                    contributionMode = PileContributionMode.TRACK_ONLY,
                    targetAmount = 5000.0,
                    currentAmount = 3200.0,
                    locked = it % 2 == 0,
                    reminderCadence = PileReminderCadence.MONTHLY,
                    reminderBehavior = PileReminderBehavior.REMIND,
                    reminderAmount = 0.0,
                    createdTimestamp = DateUtil.now()
                )
            })).collectAsLazyPagingItems(),
            navigateToAddPile = {},
            navigateToPileDetails = {},
            navigateToAddToPile = {},
            navigateUp = {}
        )
    }
}
