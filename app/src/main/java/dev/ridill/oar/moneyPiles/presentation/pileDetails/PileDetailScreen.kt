package dev.ridill.oar.moneyPiles.presentation.pileDetails

import androidx.compose.animation.Crossfade
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
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.ui.components.AmountWithMovementIndicator
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.BodyMediumText
import dev.ridill.oar.core.ui.components.DisplaySmallText
import dev.ridill.oar.core.ui.components.ListLabel
import dev.ridill.oar.core.ui.components.OarPlainTooltip
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.SpacerExtraSmall
import dev.ridill.oar.core.ui.components.SpacerMedium
import dev.ridill.oar.core.ui.components.SpacerSmall
import dev.ridill.oar.core.ui.components.SwipeActionsContainer
import dev.ridill.oar.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.theme.IconSizeMedium
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.SelectableColorsList
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.core.ui.util.excludeTop
import dev.ridill.oar.core.ui.util.isNotEmpty
import dev.ridill.oar.core.ui.util.onlyTop
import dev.ridill.oar.core.ui.util.plus
import dev.ridill.oar.moneyPiles.domain.model.ContributionSource
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import dev.ridill.oar.moneyPiles.domain.model.PileTransactionEntry
import dev.ridill.oar.moneyPiles.presentation.components.PileIconIndicator
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Currency

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PileDetailScreen(
    state: PileDetailState,
    actions: PileDetailActions,
    transactionPagingItems: LazyPagingItems<PileTransactionEntry>,
    navigateUp: () -> Unit,
    navigateToEditPile: () -> Unit,
    navigateToFundMovement: (FundMovement) -> Unit,
    navigateToSweepOut: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarController: SnackbarController = rememberSnackbarController(),
) {
    val pile = state.pile
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    OarScaffold(
        isLoading = state.loading,
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.destination_money_pile_details)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                actions = {
                    if (pile != null) {
                        IconButton(onClick = navigateToEditPile) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.cd_tap_to_edit_pile)
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
                    canWithdraw = state.canWithdraw,
                    onWithdrawClick = { navigateToFundMovement(FundMovement.OUT) },
                    onAddClick = { navigateToFundMovement(FundMovement.IN) },
                    onSweepOutClick = navigateToSweepOut,
                    modifier = Modifier
                        .padding(vertical = MaterialTheme.spacing.large)
                )
            }
        },
        snackbarController = snackbarController,
        modifier = modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues.onlyTop()),
            contentPadding = paddingValues.excludeTop() + PaddingValues(bottom = PaddingScrollEnd)
        ) {
            pile?.let { pile ->
                item(
                    key = "PileHeader",
                    contentType = "PileHeader"
                ) {
                    PileHeroSection(
                        icon = pile.icon,
                        color = pile.color,
                        locked = pile.locked,
                        currency = pile.currency,
                        savedAmount = state.savedAmount,
                        progressState = state.progressState,
                        name = pile.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.spacing.medium)
                            .animateItem()
                    )
                }

                item(
                    key = "PileStatRow",
                    contentType = "PileStatRow"
                ) {
                    PileStatRow(
                        progressPercent = state.progressFraction,
                        projectedCompletion = state.projectedCompletion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.spacing.medium)
                            .padding(top = MaterialTheme.spacing.medium)
                            .animateItem()
                    )
                }

                item(
                    key = "PileReminderRow",
                    contentType = "PileReminderRow",
                ) {
                    PileReminderRow(
                        cadence = pile.reminderCadence,
                        reminderLabel = when {
                            pile.reminderCadence == PileReminderCadence.NO_REMIND ->
                                stringResource(R.string.pile_reminder_none)

                            pile.reminderBehavior == PileReminderBehavior.AUTO_ADD ->
                                stringResource(
                                    R.string.pile_reminder_auto_add,
                                    TextFormat.currencyAmount(
                                        pile.reminderAmount.orZero(),
                                        pile.currency
                                    ),
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
                            .padding(top = MaterialTheme.spacing.small)
                            .animateItem()
                    )
                }
            }

            if (transactionPagingItems.isNotEmpty()) {
                stickyHeader(
                    key = "Div_Header_History",
                    contentType = "Divider",
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.spacing.medium)
                            .padding(top = MaterialTheme.spacing.medium)
                            .animateItem()

                    ) {
                        ListLabel(
                            text = stringResource(R.string.pile_activity_label),
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        SpacerExtraSmall()
                        HorizontalDivider()
                    }
                }

                items(
                    count = transactionPagingItems.itemCount,
                    key = transactionPagingItems.itemKey { it.id },
                    contentType = transactionPagingItems.itemContentType { PileTransactionEntry::class },
                ) { index ->
                    transactionPagingItems[index]?.let { item ->
                        PileHistoryItem(
                            source = item.contributionSource,
                            movement = item.movement,
                            amount = item.amount,
                            timestamp = item.timestamp,
                            currency = pile?.currency ?: LocaleUtil.defaultCurrency,
                            onDelete = { actions.onTransactionDelete(item.id) },
                            onActionRevealed = actions::onTransactionActionRevealed,
                            locked = item.locked,
                            modifier = Modifier
                                .animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PileHeroSection(
    icon: PileIcon,
    color: Color,
    locked: Boolean,
    currency: Currency,
    savedAmount: Double,
    progressState: PileProgressState,
    name: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            PileIconIndicator(
                icon = icon,
                color = color,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .size(PileHeroAvatarSize)
            )

            if (locked) {
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

        VerticalNumberSpinnerContent(savedAmount) { amount ->
            DisplaySmallText(
                text = TextFormat.currencyAmount(amount, currency),
                fontWeight = FontWeight.SemiBold
            )
        }

        Crossfade(progressState) { state ->
            BodyMediumText(
                text = when (state) {
                    is PileProgressState.AmountToGo -> stringResource(
                        R.string.pile_remaining_label,
                        TextFormat.currency(state.amount, currency)
                    )

                    PileProgressState.GoalReached -> stringResource(R.string.pile_goal_reached)
                    PileProgressState.SavingFreely -> stringResource(R.string.pile_saving_freely_label)
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SpacerMedium()

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.medium,
        ) {
            BodyMediumText(
                text = name,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.small,
                    )
            )
        }
    }
}

@Composable
private fun PileStatRow(
    progressPercent: Float?,
    projectedCompletion: LocalDate?,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        PileStatCard(
            label = stringResource(R.string.pile_progress_label),
            value = progressPercent?.let(TextFormat::percent)
                ?: stringResource(R.string.pile_no_goal_label),
            modifier = Modifier
                .fillMaxWidth(0.50f)
        )
        PileStatCard(
            label = stringResource(R.string.pile_projected_completion_label),
            value = projectedCompletion?.format(DateUtil.Formatters.MMM_ddth_spaceSep)
                ?: stringResource(R.string.pile_no_goal_label),
            modifier = Modifier
                .fillMaxWidth()
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
    onSweepOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .then(modifier)
        ) {
            if (canWithdraw) {
                FilledTonalIconButton(
                    onClick = onSweepOutClick,
                    modifier = Modifier
                        .weight(0.5f)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_sweep),
                        contentDescription = stringResource(R.string.cd_tap_to_sweep_out_pile)
                    )
                }

                OutlinedButton(
                    onClick = onWithdrawClick,
                    modifier = Modifier.weight(1.5f)
                ) {
                    Text(stringResource(R.string.withdraw))
                }
            }
            Button(
                onClick = onAddClick,
                modifier = Modifier.weight(2f)
            ) {
                Text(stringResource(R.string.pile_action_add))
            }
        }
    }
}

@Composable
private fun PileHistoryItem(
    source: ContributionSource,
    movement: FundMovement,
    amount: Double,
    currency: Currency,
    timestamp: LocalDateTime,
    onDelete: () -> Unit,
    onActionRevealed: () -> Unit,
    locked: Boolean,
    modifier: Modifier = Modifier
) {
    var isRevealed by remember { mutableStateOf(false) }
    SwipeActionsContainer(
        isRevealed = isRevealed,
        onRevealedChange = { revealed ->
            isRevealed = revealed
            if (revealed) {
                onActionRevealed()
            }
        },
        actions = {
            OarPlainTooltip(
                tooltipText = stringResource(R.string.delete)
            ) {
                IconButton(
                    onClick = {
                        onDelete()
                        isRevealed = false
                    },
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_delete),
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }
        },
        gesturesEnabled = !locked,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ListItem(
            modifier = modifier,
            supportingContent = {
                Text(timestamp.format(DateUtil.Formatters.MMM_ddth_spaceSep))
            },
            enabled = !locked,
            trailingContent = {
                AmountWithMovementIndicator(
                    value = TextFormat.currency(amount, currency),
                    movement = movement,
                )
            }
        ) {
            Text(
                text = stringResource(
                    if (movement == FundMovement.OUT) R.string.pile_action_withdraw
                    else source.labelRes
                ),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private val PileHeroAvatarSize = 88.dp
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
                    timestamp = LocalDateTime.now().minusMonths(2),
                    locked = false,
                ),
                PileTransactionEntry(
                    id = 2L,
                    amount = 150.0,
                    movement = FundMovement.IN,
                    contributionSource = ContributionSource.AUTO,
                    timestamp = LocalDateTime.now().minusWeeks(1),
                    locked = false,
                ),
                PileTransactionEntry(
                    id = 3L,
                    amount = 100.0,
                    movement = FundMovement.OUT,
                    contributionSource = ContributionSource.MANUAL,
                    timestamp = LocalDateTime.now(),
                    locked = false,
                ),
            )
        )
    ).collectAsLazyPagingItems()

    OarTheme {
        PileDetailScreen(
            state = PileDetailState(
                pile = MoneyPileDetails(
                    id = 1L,
                    name = "Japan trip",
                    icon = PileIcon.Travel,
                    colorCode = SelectableColorsList.random().toArgb(),
                    currency = LocaleUtil.defaultCurrency,
                    contributionMode = PileContributionMode.FROM_BALANCE,
                    targetAmount = 5000.0,
                    locked = false,
                    reminderCadence = PileReminderCadence.WEEKLY,
                    reminderBehavior = PileReminderBehavior.REMIND,
                    reminderAmount = 150.0,
                    createdTimestamp = DateUtil.now(),
                    targetDate = null,
                    completionTimestamp = null,
                ),
                progressState = PileProgressState.SavingFreely,
                canWithdraw = true,
            ),
            actions = object : PileDetailActions {
                override fun onTransactionActionRevealed() {}
                override fun onTransactionDelete(id: Long) {}
            },
            transactionPagingItems = transactionPagingItems,
            navigateUp = {},
            navigateToEditPile = {},
            navigateToFundMovement = {},
            navigateToSweepOut = {},
        )
    }
}
