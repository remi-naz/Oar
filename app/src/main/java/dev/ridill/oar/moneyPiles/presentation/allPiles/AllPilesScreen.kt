package dev.ridill.oar.moneyPiles.presentation.allPiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
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
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileEntryUiModel
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AllPilesScreen(
    snackbarController: SnackbarController,
    pilesPagingItems: LazyPagingItems<MoneyPileEntryUiModel>,
    includeCompletedPiles: Boolean,
    onIncludeLockedPilesToggle: (Boolean) -> Unit,
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
                title = { Text(stringResource(R.string.destination_all_money_piles)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                actions = {
                    var showOptionsMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.cd_tap_for_more_options)
                        )
                    }
                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(
                                        if (includeCompletedPiles) R.string.hide_completed
                                        else R.string.show_completed
                                    )
                                )
                            },
                            onClick = {
                                onIncludeLockedPilesToggle(!includeCompletedPiles)
                                showOptionsMenu = false
                            }
                        )
                    }
                },
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
                    repeat(pilesPagingItems.itemCount) { index ->
                        pilesPagingItems[index]?.let { entry ->
                            when (entry) {
                                MoneyPileEntryUiModel.CompletedSeparator -> {
                                    item(
                                        key = "CompletedSeparator",
                                        contentType = MoneyPileEntryUiModel.CompletedSeparator::class,
                                        span = StaggeredGridItemSpan.FullLine
                                    ) {
                                        CompletedSeparator(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .animateItem()
                                        )
                                    }
                                }

                                is MoneyPileEntryUiModel.MoneyPileWithSavedAmount -> {
                                    item(
                                        key = entry.id,
                                        contentType = MoneyPileEntryUiModel.MoneyPileWithSavedAmount::class
                                    ) {
                                        PileGridItem(
                                            icon = entry.icon,
                                            name = entry.name,
                                            accent = entry.color,
                                            locked = entry.locked,
                                            currency = entry.currency,
                                            savedAmount = entry.savedAmount,
                                            targetAmount = entry.targetAmount,
                                            progressFraction = entry.progressFraction,
                                            onClick = { navigateToPileDetails(entry.id) },
                                            onQuickAddClick = { navigateToAddToPile(entry.id) },
                                            animationSeed = index,
                                            completionTimestamp = entry.completionTimestamp,
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
        }
    }
}

@Composable
private fun CompletedSeparator(
    modifier: Modifier = Modifier,
) {
    val dotColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = SeparatorDotsWidth, height = SeparatorDotSize)
                .drawBehind { drawSeparatorDots(dotColor) }
        )
        Text(
            text = stringResource(R.string.completed),
            style = MaterialTheme.typography.labelLarge,
            color = dotColor,
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.small)
        )
        Box(
            modifier = Modifier
                .size(width = SeparatorDotsWidth, height = SeparatorDotSize)
                .drawBehind { drawSeparatorDots(dotColor) }
        )
    }
}

private const val SeparatorDotCount = 3
private val SeparatorDotSize = 4.dp
private val SeparatorDotSpacing = 4.dp
private val SeparatorDotsWidth = SeparatorDotSize * SeparatorDotCount +
        SeparatorDotSpacing * (SeparatorDotCount - 1)

private fun DrawScope.drawSeparatorDots(color: Color) {
    val radius = SeparatorDotSize.toPx() / 2
    val step = SeparatorDotSize.toPx() + SeparatorDotSpacing.toPx()
    repeat(SeparatorDotCount) { index ->
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(x = radius + index * step, y = size.height / 2)
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewAllPilesScreen() {
    OarTheme {
        AllPilesScreen(
            snackbarController = rememberSnackbarController(),
            pilesPagingItems = flowOf(PagingData.from(List(5) {
                MoneyPileEntryUiModel.MoneyPileWithSavedAmount(
                    id = it.toLong(),
                    name = "Pile $it",
                    icon = PileIcon.LandProperty,
                    color = SelectableColorsList.random(),
                    currency = LocaleUtil.defaultCurrency,
                    targetAmount = 5000.0,
                    savedAmount = 3200.0,
                    locked = it % 2 == 0,
                    createdTimestamp = DateUtil.now(),
                    completionTimestamp = null,
                ) as MoneyPileEntryUiModel
            })).collectAsLazyPagingItems(),
            includeCompletedPiles = false,
            onIncludeLockedPilesToggle = {},
            navigateToAddPile = {},
            navigateToPileDetails = {},
            navigateToAddToPile = {},
            navigateUp = {}
        )
    }
}
