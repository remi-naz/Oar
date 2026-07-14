package dev.ridill.oar.moneyPiles.presentation.pileDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.OarProgressBar
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.moneyPiles.domain.model.MoneyPile
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileHistoryEntry
import dev.ridill.oar.moneyPiles.domain.model.PileHistoryEntryType
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PileDetailsScreen(
    snackbarController: SnackbarController,
    state: PileDetailsState,
    navigateToEditPile: () -> Unit,
    navigateToAddToPile: () -> Unit,
    navigateToWithdrawFromPile: () -> Unit,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pile = state.pile
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    OarScaffold(
        snackbarController = snackbarController,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .then(modifier)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackArrowButton(onClick = navigateUp)
                if (pile != null) {
                    IconButton(onClick = navigateToEditPile) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.cd_edit_pile)
                        )
                    }
                }
            }

            if (pile != null) {
                val accent = Color(pile.color)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.spacing.small)
                ) {
                    PileJarVisual(
                        icon = pile.icon,
                        accent = accent,
                        progressFraction = state.progressPercent?.let { it / 100f }
                    )

                    Text(
                        text = TextFormat.currencyAmount(pile.currentAmount),
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
                    )
                    Text(
                        text = remainingLabel(pile, state.isGoalReached),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        modifier = Modifier.padding(top = MaterialTheme.spacing.small)
                    ) {
                        InfoPill(text = pile.name)
                        if (pile.locked) {
                            InfoPill(
                                text = stringResource(R.string.pile_locked),
                                icon = Icons.Rounded.Lock
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = MaterialTheme.spacing.medium,
                        end = MaterialTheme.spacing.medium,
                        bottom = paddingValues.calculateBottomPadding() + PaddingScrollEnd
                    ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    if (pile.note.isNotBlank()) {
                        item(key = "PileNote", contentType = "PileNote") {
                            Text(
                                text = pile.note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .padding(MaterialTheme.spacing.medium)
                            )
                        }
                    }

                    item(key = "PileStats", contentType = "PileStats") {
                        val progressPercent = state.progressPercent
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatCard(
                                labelRes = R.string.pile_progress_label,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = progressPercent?.let { "$it%" }
                                        ?: stringResource(R.string.pile_no_goal_label),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                if (progressPercent != null) {
                                    OarProgressBar(
                                        progress = { progressPercent / 100f },
                                        startColor = accent,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = MaterialTheme.spacing.small)
                                    )
                                }
                            }
                            StatCard(
                                labelRes = R.string.pile_projected_completion_label,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = state.projectedCompletionLabel.asString(),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }

                    item(key = "PileReminder", contentType = "PileReminder") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(MaterialTheme.spacing.medium)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = null
                            )
                            Column {
                                Text(
                                    text = reminderLabel(pile),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = stringResource(
                                        if (pile.contributionMode == PileContributionMode.FROM_BALANCE) {
                                            R.string.pile_contribution_mode_from_balance_help
                                        } else {
                                            R.string.pile_contribution_mode_track_only_help
                                        }
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (state.history.isNotEmpty()) {
                        item(key = "ActivityLabel", contentType = "ActivityLabel") {
                            Text(
                                text = stringResource(R.string.pile_activity_label),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    top = MaterialTheme.spacing.small,
                                    bottom = MaterialTheme.spacing.extraSmall
                                )
                            )
                        }

                        items(
                            items = state.history,
                            key = { it.id },
                            contentType = { PileHistoryEntry::class }
                        ) { entry ->
                            PileHistoryItem(entry = entry)
                        }
                    }
                }
            }
        }

        if (pile != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
            ) {
                if (state.canWithdraw) {
                    OutlinedButton(
                        onClick = navigateToWithdrawFromPile,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.pile_action_withdraw))
                    }
                }
                Button(
                    onClick = navigateToAddToPile,
                    modifier = Modifier.weight(if (state.canWithdraw) 1f else 2f)
                ) {
                    Text(stringResource(R.string.pile_action_add))
                }
            }
        }
    }
}

@Composable
private fun remainingLabel(pile: MoneyPile, isGoalReached: Boolean): String = when {
    isGoalReached -> stringResource(R.string.pile_goal_reached)
    pile.targetAmount != null -> stringResource(
        R.string.pile_remaining_label,
        TextFormat.currencyAmount(pile.targetAmount - pile.currentAmount)
    )

    else -> stringResource(R.string.pile_saving_freely_label)
}

@Composable
private fun reminderLabel(pile: MoneyPile): String {
    if (pile.reminderCadence == PileReminderCadence.NO_REPEAT) {
        return stringResource(R.string.pile_reminder_none)
    }
    val cadenceLabel = stringResource(pile.reminderCadence.labelRes)
    return when (pile.reminderBehavior) {
        PileReminderBehavior.AUTO_ADD -> stringResource(
            R.string.pile_reminder_auto_add,
            TextFormat.currencyAmount(pile.reminderAmount),
            cadenceLabel
        )

        PileReminderBehavior.SUGGEST -> stringResource(
            R.string.pile_reminder_suggest,
            TextFormat.currencyAmount(pile.reminderAmount),
            cadenceLabel
        )

        PileReminderBehavior.REMIND -> stringResource(
            R.string.pile_reminder_remind,
            cadenceLabel
        )
    }
}

@Composable
private fun InfoPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = MaterialTheme.spacing.small, vertical = MaterialTheme.spacing.extraSmall)
    ) {
        icon?.let {
            Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(14.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun StatCard(
    labelRes: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(MaterialTheme.spacing.medium)
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
                .copy(alpha = ContentAlpha.SUB_CONTENT)
        )
        content()
    }
}

@Composable
private fun PileHistoryItem(
    entry: PileHistoryEntry,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(MaterialTheme.spacing.medium)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Text(text = entry.type.icon, style = MaterialTheme.typography.bodyMedium)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(entry.type.labelRes),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = entry.timestamp.format(DateUtil.Formatters.localizedDateMedium),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = (if (entry.type.isWithdrawal) "−" else "+") +
                TextFormat.currencyAmount(entry.amount),
            style = MaterialTheme.typography.bodyLarge,
            color = if (entry.type.isWithdrawal) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewPileDetailsScreen() {
    OarTheme {
        PileDetailsScreen(
            snackbarController = rememberSnackbarController(),
            state = PileDetailsState(
                pile = MoneyPile(
                    id = 1L,
                    name = "Japan trip",
                    note = "Two weeks in April — Tokyo, Kyoto, Osaka.",
                    icon = "🌸",
                    color = 0xFFFF4CA6.toInt(),
                    contributionMode = PileContributionMode.FROM_BALANCE,
                    targetAmount = 5000.0,
                    currentAmount = 3200.0,
                    locked = true,
                    reminderCadence = PileReminderCadence.WEEKLY,
                    reminderBehavior = PileReminderBehavior.AUTO_ADD,
                    reminderAmount = 150.0,
                    createdTimestamp = DateUtil.now()
                ),
                progressPercent = 64,
                isGoalReached = false,
                history = listOf(
                    PileHistoryEntry(1L, PileHistoryEntryType.STARTER, 1000.0, DateUtil.now()),
                    PileHistoryEntry(2L, PileHistoryEntryType.AUTO_CONTRIBUTION, 150.0, DateUtil.now())
                ),
                canWithdraw = true
            ),
            navigateToEditPile = {},
            navigateToAddToPile = {},
            navigateToWithdrawFromPile = {},
            navigateUp = {}
        )
    }
}
