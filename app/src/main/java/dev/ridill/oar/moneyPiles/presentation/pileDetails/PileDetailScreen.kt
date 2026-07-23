package dev.ridill.oar.moneyPiles.presentation.pileDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.ListItemLeadingContentContainer
import dev.ridill.oar.core.ui.components.ListLabel
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.SpacerExtraSmall
import dev.ridill.oar.core.ui.components.SpacerSmall
import dev.ridill.oar.core.ui.components.listEmptyIndicator
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.IconSizeMedium
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.SelectableColorsList
import dev.ridill.oar.core.ui.theme.adjustedContentColor
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.ui.util.isEmpty
import dev.ridill.oar.moneyPiles.domain.model.ContributionSource
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileDetail
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import dev.ridill.oar.moneyPiles.domain.model.PileTransactionEntry
import dev.ridill.oar.moneyPiles.domain.model.labelRes
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime
import java.util.Currency

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PileDetailScreen(
    state: PileDetailState,
    transactionPagingItems: LazyPagingItems<PileTransactionEntry>,
    navigateUp: () -> Unit,
    navigateToEditPile: () -> Unit,
    navigateToAddToPile: (FundMovement) -> Unit,
    modifier: Modifier = Modifier,
    snackbarController: SnackbarController = rememberSnackbarController(),
) {
    val pile = state.pile
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    OarScaffold(
        isLoading = state.isLoading,
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(pile?.name.orEmpty()) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                actions = {
                    if (pile != null) {
                        IconButton(onClick = navigateToEditPile) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.cd_edit_pile)
                            )
                        }
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        bottomBar = {
            if (pile != null) {
                PileActionsBar(
                    canWithdraw = !pile.locked && pile.savedAmount > 0,
                    onWithdrawClick = { navigateToAddToPile(FundMovement.OUT) },
                    onAddClick = { navigateToAddToPile(FundMovement.IN) },
                )
            }
        },
        snackbarController = snackbarController,
        modifier = modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        if (pile != null) {
            val isHistoryEmpty by remember {
                derivedStateOf { transactionPagingItems.isEmpty() }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                PileHeroSection(
                    pile = pile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )

                SpacerSmall()

                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium)
                ) {
                    PileStatCard(
                        label = stringResource(R.string.pile_progress_label),
                        value = pile.targetAmount?.let {
                            "${(pile.progressFraction.coerceIn(0f, 1f) * 100).toInt()}%"
                        } ?: stringResource(R.string.pile_no_goal_label),
                        modifier = Modifier.weight(1f)
                    )
                    val projectedCompletionDate = state.projectedCompletionDate
                    PileStatCard(
                        label = stringResource(R.string.pile_projected_completion_label),
                        value = when {
                            pile.targetAmount == null -> stringResource(R.string.pile_projection_no_goal)
                            pile.isGoalReached -> stringResource(R.string.pile_projection_reached)
                            projectedCompletionDate == null ->
                                stringResource(R.string.pile_projection_set_pace)

                            else -> projectedCompletionDate
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                SpacerSmall()

                PileReminderRow(
                    cadence = pile.reminderCadence,
                    reminderLabel = when {
                        pile.reminderCadence == PileReminderCadence.NO_REMIND ->
                            stringResource(R.string.pile_reminder_none)

                        pile.reminderBehavior == PileReminderBehavior.AUTO_ADD ->
                            stringResource(
                                R.string.pile_reminder_auto_add,
                                TextFormat.currencyAmount(pile.reminderAmount.orZero(), pile.currency),
                                stringResource(pile.reminderCadence.labelRes)
                            )

                        else -> stringResource(
                            R.string.pile_reminder_remind,
                            stringResource(pile.reminderCadence.labelRes)
                        )
                    },
                    modeHelpText = stringResource(pile.contributionMode.helpTextRes),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )

                SpacerSmall()

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        top = MaterialTheme.spacing.small,
                        bottom = paddingValues.calculateBottomPadding() + PaddingScrollEnd
                    )
                ) {
                    stickyHeader(
                        key = "PileActivityHeader",
                        contentType = "PileActivityHeader"
                    ) {
                        ListLabel(
                            text = stringResource(R.string.pile_activity_label),
                            modifier = Modifier
                                .padding(
                                    horizontal = MaterialTheme.spacing.medium,
                                    vertical = MaterialTheme.spacing.small
                                )
                                .animateItem()
                        )
                    }

                    listEmptyIndicator(
                        isListEmpty = isHistoryEmpty,
                        messageRes = R.string.pile_activity_empty_message
                    )

                    repeat(transactionPagingItems.itemCount) { index ->
                        transactionPagingItems[index]?.let { entry ->
                            item(
                                key = entry.id,
                                contentType = PileTransactionEntry::class
                            ) {
                                PileHistoryItem(
                                    entry = entry,
                                    currency = pile.currency,
                                    modifier = Modifier
                                        .fillParentMaxWidth()
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

@Composable
private fun PileHeroSection(
    pile: PileDetail,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = pile.color,
                modifier = Modifier.size(PileHeroAvatarSize)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = ImageVector.vectorResource(pile.icon.iconRes),
                        contentDescription = stringResource(pile.icon.labelRes),
                        tint = pile.color.adjustedContentColor(),
                        modifier = Modifier.size(PileHeroIconSize)
                    )
                }
            }

            if (pile.locked) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.size(PileLockBadgeSize)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_circle_lock),
                            contentDescription = stringResource(R.string.pile_locked),
                            modifier = Modifier.size(IconSizeMedium)
                        )
                    }
                }
            }
        }

        SpacerSmall()

        Text(
            text = TextFormat.currencyAmount(pile.savedAmount, pile.currency),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = when {
                pile.isGoalReached -> stringResource(R.string.pile_goal_reached)
                pile.targetAmount != null -> stringResource(
                    R.string.pile_remaining_label,
                    TextFormat.currency(pile.targetAmount - pile.savedAmount, pile.currency)
                )

                else -> stringResource(R.string.pile_saving_freely_label)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PileStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SpacerExtraSmall()
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PileReminderRow(
    cadence: PileReminderCadence,
    reminderLabel: String,
    modeHelpText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
        ) {
            Icon(
                imageVector = if (cadence == PileReminderCadence.NO_REMIND) Icons.Rounded.NotificationsOff
                else Icons.Rounded.Notifications,
                contentDescription = null
            )
            Column {
                Text(
                    text = reminderLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = modeHelpText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PileActionsBar(
    canWithdraw: Boolean,
    onWithdrawClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.medium)
    ) {
        if (canWithdraw) {
            OutlinedButton(
                onClick = onWithdrawClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.pile_action_withdraw))
            }
        }
        Button(
            onClick = onAddClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.pile_action_add))
        }
    }
}

@Composable
private fun PileHistoryItem(
    entry: PileTransactionEntry,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        modifier = modifier
            .padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.small
            )
    ) {
        ListItemLeadingContentContainer(
            containerColor = entry.movement.color.copy(alpha = ContentAlpha.PERCENT_16),
            contentColor = entry.movement.color
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(entry.movement.iconRes),
                contentDescription = null,
                modifier = Modifier.size(IconSizeMedium)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(entry.labelRes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = entry.timestamp.format(DateUtil.Formatters.MMM_ddth_spaceSep),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = (if (entry.movement == FundMovement.OUT) "− " else "+ ") +
                    TextFormat.currency(entry.amount, currency),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = entry.movement.color
        )
    }
}

private val PileHeroAvatarSize = 88.dp
private val PileHeroIconSize = 36.dp
private val PileLockBadgeSize = 26.dp

@PreviewLightDark
@Composable
private fun PreviewPileDetailScreen() {
    val transactionPagingItems = flowOf(
        PagingData.from(
            listOf(
                PileTransactionEntry(
                    id = 1L,
                    amount = 1000.0,
                    movement = FundMovement.IN,
                    contributionSource = ContributionSource.STARTER,
                    timestamp = LocalDateTime.now().minusMonths(2)
                ),
                PileTransactionEntry(
                    id = 2L,
                    amount = 150.0,
                    movement = FundMovement.IN,
                    contributionSource = ContributionSource.AUTO,
                    timestamp = LocalDateTime.now().minusWeeks(1)
                ),
                PileTransactionEntry(
                    id = 3L,
                    amount = 100.0,
                    movement = FundMovement.OUT,
                    contributionSource = ContributionSource.MANUAL,
                    timestamp = LocalDateTime.now()
                ),
            )
        )
    ).collectAsLazyPagingItems()

    OarTheme {
        PileDetailScreen(
            state = PileDetailState(
                pile = PileDetail(
                    id = 1L,
                    name = "Japan trip",
                    icon = PileIcon.Travel,
                    color = SelectableColorsList.random(),
                    currency = LocaleUtil.defaultCurrency,
                    contributionMode = PileContributionMode.FROM_BALANCE,
                    targetAmount = 5000.0,
                    savedAmount = 3200.0,
                    locked = true,
                    reminderCadence = PileReminderCadence.WEEKLY,
                    reminderBehavior = PileReminderBehavior.AUTO_ADD,
                    reminderAmount = 150.0,
                    createdTimestamp = DateUtil.now()
                )
            ),
            transactionPagingItems = transactionPagingItems,
            navigateUp = {},
            navigateToEditPile = {},
            navigateToAddToPile = {},
        )
    }
}
