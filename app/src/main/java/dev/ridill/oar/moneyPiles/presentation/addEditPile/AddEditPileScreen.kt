package dev.ridill.oar.moneyPiles.presentation.addEditPile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.ArrangementTopWithFooter
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.ButtonWithLoadingIndicator
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.OarTextField
import dev.ridill.oar.core.ui.components.SpacerExtraSmall
import dev.ridill.oar.core.ui.theme.BorderWidthStandard
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.LocalCurrencyPreference
import dev.ridill.oar.core.ui.util.PaddingSide
import dev.ridill.oar.core.ui.util.exclude
import dev.ridill.oar.core.ui.util.only
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddEditPileScreen(
    isEditMode: Boolean,
    state: AddEditPileState,
    nameState: TextFieldState,
    starterAmountState: TextFieldState,
    targetAmountState: TextFieldState,
    reminderAmountState: TextFieldState,
    actions: AddEditPileActions,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    OarScaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (isEditMode) R.string.destination_edit_pile
                            else R.string.destination_new_pile
                        ),
                    )
                },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        modifier = modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        val maxWidthWithPaddingModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues.exclude(PaddingSide.BOTTOM))
                .verticalScroll(rememberScrollState())
                .padding(paddingValues.only(PaddingSide.BOTTOM))
                .padding(vertical = MaterialTheme.spacing.medium),
            verticalArrangement = ArrangementTopWithFooter(MaterialTheme.spacing.medium)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                modifier = maxWidthWithPaddingModifier,
            ) {
                Surface(
                    onClick = actions::onIconIndicatorClick,
                    shape = MaterialTheme.shapes.medium,
                    color = state.color.copy(alpha = ContentAlpha.PERCENT_16),
                    border = BorderStroke(BorderWidthStandard, state.color),
                    modifier = Modifier.size(PileIconAvatarSize)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                state.icon.iconRes ?: R.drawable.ic_outlined_money_pile
                            ),
                            contentDescription = stringResource(
                                state.icon.labelRes ?: R.string.select_pile_icon
                            ),
                            tint = state.color
                        )
                    }
                }

                OarTextField(
                    state = nameState,
                    label = { Text(stringResource(R.string.pile_name)) },
                    placeholder = { Text(stringResource(R.string.pile_name_placeholder)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    modifier = Modifier
                        .weight(1f)
                )
            }

//            OarTextField(
//                state = noteState,
//                label = { Text(stringResource(R.string.pile_note_optional)) },
//                placeholder = { Text(stringResource(R.string.pile_note_placeholder)) },
//                keyboardOptions = KeyboardOptions(
//                    capitalization = KeyboardCapitalization.Sentences,
//                    imeAction = ImeAction.Done
//                ),
//                lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 3),
//                modifier = maxWidthWithPaddingModifier,
//            )

            Column(
                modifier = maxWidthWithPaddingModifier,
            ) {
                Text(
                    text = stringResource(R.string.pile_contribution_mode_label),
                    style = MaterialTheme.typography.labelLarge,
                )
                SpacerExtraSmall()
                Row(
                    horizontalArrangement = Arrangement
                        .spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    PileContributionMode.entries.forEachIndexed { index, mode ->
                        ToggleButton(
                            checked = state.contributionMode == mode,
                            onCheckedChange = { actions.onModeChange(mode) },
                            shapes = if (index == 0) ButtonGroupDefaults.connectedLeadingButtonShapes()
                            else ButtonGroupDefaults.connectedTrailingButtonShapes(),
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text(stringResource(mode.labelRes))
                        }
                    }
                }

                SpacerExtraSmall()

                Crossfade(state.contributionMode.helpTextRes) { resId ->
                    Text(
                        text = stringResource(resId),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                modifier = maxWidthWithPaddingModifier,
            ) {
                if (!isEditMode) {
                    OarTextField(
                        state = starterAmountState,
                        label = { Text(stringResource(R.string.pile_starter_amount_optional)) },
                        prefix = { Text(LocalCurrencyPreference.current.symbol) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        lineLimits = TextFieldLineLimits.SingleLine,
                        modifier = Modifier
                            .weight(1f)
                    )
                }
                OarTextField(
                    state = targetAmountState,
                    label = { Text(stringResource(R.string.pile_target_amount_optional)) },
                    prefix = { Text(LocalCurrencyPreference.current.symbol) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                modifier = maxWidthWithPaddingModifier,
            ) {
                Text(
                    text = stringResource(R.string.pile_reminder_label),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    PileReminderCadence.entries.forEach { cadence ->
                        FilterChip(
                            selected = state.reminderCadence == cadence,
                            onClick = { actions.onCadenceChange(cadence) },
                            label = { Text(stringResource(cadence.labelRes)) }
                        )
                    }
                }
            }
            val showReminderBehavior by remember(state.reminderCadence) {
                derivedStateOf { state.reminderCadence != PileReminderCadence.NO_REPEAT }
            }
            AnimatedVisibility(
                visible = showReminderBehavior,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    modifier = maxWidthWithPaddingModifier
                ) {
                    PileReminderBehavior.entries.forEachIndexed { index, behavior ->
                        ToggleButton(
                            checked = state.reminderBehavior == behavior,
                            onCheckedChange = { actions.onBehaviorChange(behavior) },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                PileReminderBehavior.entries.lastIndex -> ButtonGroupDefaults
                                    .connectedTrailingButtonShapes()

                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(behavior.labelRes),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            val showReminderAmountInput by remember(state.reminderBehavior) {
                derivedStateOf { state.reminderBehavior != PileReminderBehavior.REMIND }
            }
            AnimatedVisibility(
                visible = showReminderAmountInput,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OarTextField(
                    state = reminderAmountState,
                    label = {
                        Text(
                            stringResource(
                                if (state.reminderBehavior == PileReminderBehavior.AUTO_ADD) {
                                    R.string.pile_reminder_amount_auto_label
                                } else {
                                    R.string.pile_reminder_amount_suggest_label
                                }
                            )
                        )
                    },
                    prefix = { Text(LocalCurrencyPreference.current.symbol) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    modifier = maxWidthWithPaddingModifier
                )
            }

            LockedIndicatorSwitch(
                locked = state.locked,
                onLockedToggle = actions::onLockToggle,
                modifier = maxWidthWithPaddingModifier,
            )

            ButtonWithLoadingIndicator(
                textRes = if (isEditMode) R.string.action_save_changes else R.string.pile_action_create,
                loading = state.isLoading,
                onClick = actions::onSaveClick,
                enabled = nameState.text.isNotBlank(),
                modifier = maxWidthWithPaddingModifier,
            )
        }
    }

    if (state.showIconColorSelection) {
        IconColorSelectionSheet(
            onDismissRequest = actions::onIconColorSelectionDismiss,
            selectedIcon = state.icon,
            selectedColor = state.color,
            onDoneClick = actions::onIconColorSelectionConfirm
        )
    }
}

private val PileIconAvatarSize = 56.dp

@Composable
private fun LockedIndicatorSwitch(
    locked: Boolean,
    onLockedToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = MaterialTheme.colorScheme.secondaryContainer
    val contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    OutlinedCard(
        modifier = modifier,
        onClick = onLockedToggle,
        colors = CardDefaults.outlinedCardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        )
    ) {
        ListItem(
            supportingContent = { Text(stringResource(R.string.pile_lock_help)) },
            leadingContent = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_circle_lock),
                    contentDescription = null,
                )
            },
            trailingContent = {
                Switch(
                    checked = locked,
                    onCheckedChange = null,
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = containerColor,
                contentColor = contentColor,
                supportingContentColor = contentColor,
                leadingContentColor = contentColor,
                trailingContentColor = contentColor
            )
        ) {
            Text(stringResource(R.string.pile_lock_label))
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewAddEditPileScreen() {
    OarTheme {
        AddEditPileScreen(
            isEditMode = false,
            state = AddEditPileState(
                reminderCadence = PileReminderCadence.MONTHLY,
                reminderBehavior = PileReminderBehavior.SUGGEST,
                locked = true
            ),
            nameState = rememberTextFieldState(),
            starterAmountState = rememberTextFieldState(),
            targetAmountState = rememberTextFieldState(),
            reminderAmountState = rememberTextFieldState(),
            actions = object : AddEditPileActions {
                override fun onIconIndicatorClick() {}
                override fun onIconColorSelectionDismiss() {}
                override fun onIconColorSelectionConfirm(icon: PileIcon, color: Color) {}
                override fun onModeChange(mode: PileContributionMode) {}
                override fun onCadenceChange(cadence: PileReminderCadence) {}
                override fun onBehaviorChange(behavior: PileReminderBehavior) {}
                override fun onLockToggle() {}
                override fun onSaveClick() {}
            },
            navigateUp = {}
        )
    }
}
