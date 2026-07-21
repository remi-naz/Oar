package dev.ridill.oar.moneyPiles.presentation.allPiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.EmptyListIndicator
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.SelectableColorsList
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.isEmpty
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileWithSavedAmount
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AllPilesScreen(
    snackbarController: SnackbarController,
    pilesPagingItems: LazyPagingItems<MoneyPileWithSavedAmount>,
    navigateToAddPile: () -> Unit,
    navigateToPileDetails: (Long) -> Unit,
    navigateToAddToPile: (Long) -> Unit,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val refreshLoadState = pilesPagingItems.loadState.refresh
    val appendLoadState = pilesPagingItems.loadState.append
    val retryLabel = stringResource(R.string.action_retry)
    val loadErrorMessage = stringResource(R.string.piles_list_load_error_message)

    LaunchedEffect(appendLoadState, pilesPagingItems.itemCount) {
        if (appendLoadState is LoadState.Error && pilesPagingItems.itemCount > 0) {
            snackbarController.showSnackbar(
                message = loadErrorMessage,
                isError = true,
                actionLabel = retryLabel,
                onSnackbarResult = { result ->
                    if (result == SnackbarResult.ActionPerformed) pilesPagingItems.retry()
                }
            )
        }
    }

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
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                if (pilesPagingItems.isEmpty()) {
                    if (refreshLoadState is LoadState.Error) {
                        EmptyListIndicator(
                            rawResId = R.raw.lottie_empty_list_ghost,
                            messageRes = R.string.piles_list_load_error_message,
                            actionLabel = retryLabel,
                            onActionClick = pilesPagingItems::retry
                        )
                    } else {
                        EmptyListIndicator(
                            rawResId = R.raw.lottie_empty_list_ghost,
                            messageRes = R.string.piles_list_empty_message
                        )
                    }
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
                        contentType = pilesPagingItems.itemContentType { MoneyPileWithSavedAmount::class }
                    ) { index ->
                        pilesPagingItems[index]?.let { pile ->
                            PileGridItem(
                                icon = pile.icon,
                                name = pile.name,
                                accent = pile.color,
                                locked = pile.locked,
                                currency = pile.currency,
                                savedAmount = pile.savedAmount,
                                targetAmount = pile.targetAmount,
                                progressFraction = pile.progressFraction,
                                onClick = { navigateToPileDetails(pile.id) },
                                onQuickAddClick = { navigateToAddToPile(pile.id) },
                                animationSeed = index,
                                modifier = Modifier
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
            pilesPagingItems = flowOf(PagingData.from(List(5) {
                MoneyPileWithSavedAmount(
                    id = it.toLong(),
                    name = "Pile $it",
                    icon = PileIcon.LandProperty,
                    color = SelectableColorsList.random(),
                    currency = LocaleUtil.defaultCurrency,
                    targetAmount = 5000.0,
                    savedAmount = 3200.0,
                    locked = it % 2 == 0,
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
