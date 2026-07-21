package dev.ridill.oar.schedules.presentation.addEditSchedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.model.creditOrDebitLabel
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.One
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.ConfirmationDialog
import dev.ridill.oar.core.ui.components.OarDatePickerDialog
import dev.ridill.oar.core.ui.components.OarPlainTooltip
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.OarTextField
import dev.ridill.oar.core.ui.components.OarTimePickerDialog
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.Spacer
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.theme.IconSizeSmall
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.PaddingSide
import dev.ridill.oar.core.ui.util.exclude
import dev.ridill.oar.schedules.domain.model.ScheduleRepetition
import dev.ridill.oar.settings.presentation.components.SimplePreference
import dev.ridill.oar.tags.presentation.tagSelection.TagSelectionField
import dev.ridill.oar.transactions.presentation.components.AmountInput
import dev.ridill.oar.transactions.presentation.components.AmountRecommendationsRow
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Currency

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddEditScheduleScreen(
    isEditMode: Boolean,
    snackbarController: SnackbarController,
    amountInputState: TextFieldState,
    noteInputState: TextFieldState,
    state: AddEditScheduleState,
    actions: AddEditScheduleActions,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val amountFocusRequester = remember { FocusRequester() }
    LaunchedEffect(isEditMode) {
        if (!isEditMode) {
            delay(UtilConstants.FieldAutoFocusDelayDuration)
            amountFocusRequester.requestFocus()
        }
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val dateNowUtc = remember { DateUtil.dateNow(ZoneId.of(ZoneOffset.UTC.id)) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtil.toMillis(state.timestampUtc),
        yearRange = IntRange(dateNowUtc.year, DatePickerDefaults.YearRange.last),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis >= DateUtil.toMillis(
                    date = dateNowUtc.plusDays(1),
                    zoneId = ZoneId.of(ZoneOffset.UTC.id)
                )
        }
    )
    val timePickerState = rememberTimePickerState(
        initialHour = state.timestamp.hour,
        initialMinute = state.timestamp.minute
    )

    var toolbarExpanded by remember { mutableStateOf(true) }
    OarScaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            id = if (isEditMode) R.string.edit_schedule
                            else R.string.new_schedule
                        )
                    )
                },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .floatingToolbarVerticalNestedScroll(
                expanded = toolbarExpanded.takeIf { state.menuOptions.isNotEmpty() } ?: false,
                onExpand = { toolbarExpanded = true },
                onCollapse = { toolbarExpanded = false }
            )
            .imePadding()
            .then(modifier),
        snackbarController = snackbarController
    ) { paddingValues ->
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = MaterialTheme.spacing.medium,
                        bottom = PaddingScrollEnd
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                TransactionTypeSelector(
                    selectedType = state.fundMovement,
                    onValueChange = actions::onTypeChange,
                    modifier = Modifier
                        .fillMaxWidth(TRANSACTION_TYPE_SELECTOR_WIDTH_FRACTION)
                        .align(Alignment.CenterHorizontally)
                )

                AmountInput(
                    currency = state.currency,
                    onCurrencySelect = actions::onCurrencySelect,
                    inputState = amountInputState,
                    isInputAnExpression = state.isAmountInputAnExpression,
                    onExpressionEvalClick = actions::onEvaluateExpressionClick,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .focusRequester(amountFocusRequester)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) actions.onAmountFocusLost()
                        }
                )

                NoteInput(
                    inputState = noteInputState,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )

                if (!isEditMode) {
                    AmountRecommendationsRow(
                        recommendations = state.amountRecommendations,
                        onRecommendationClick = {
                            actions.onRecommendedAmountClick(it)
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                        modifier = Modifier
                            .fillMaxWidth(AMOUNT_RECOMMENDATION_WIDTH_FRACTION)
                    )
                }

                HorizontalDivider()

                ScheduleTimestamp(
                    timestamp = state.timestamp,
                    onClick = actions::onTimestampClick,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .align(Alignment.End)
                )

                RepetitionSelection(
                    selectedRepeatMode = state.selectedRepetition,
                    onSelect = actions::onRepetitionSelect,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )

                HorizontalDivider()

                FolderIndicator(
                    folderName = state.linkedFolderName,
                    onSelectFolderClick = actions::onSelectFolderClick,
                    modifier = Modifier
                        .align(Alignment.Start)
                )

                TagSelectionField(
                    selectedId = state.selectedTagId ?: OarDatabase.INVALID_ID_LONG,
                    onSelectedIdChange = actions::onTagSelect,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )
            }

            HorizontalFloatingToolbar(
                expanded = toolbarExpanded.takeIf { state.menuOptions.isNotEmpty() } ?: false,
                floatingActionButton = {
                    MediumFloatingActionButton(
                        onClick = actions::onSaveClick,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = stringResource(R.string.cd_save_schedule)
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(paddingValues.exclude(PaddingSide.TOP))
                    .padding(MaterialTheme.spacing.medium)
            ) {
                state.menuOptions.forEach { option ->
                    OarPlainTooltip(
                        tooltipText = stringResource(option.labelRes)
                    ) {
                        IconButton(
                            onClick = { actions.onOptionClick(option) }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(option.iconRes),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showDeleteConfirmation) {
        ConfirmationDialog(
            title = pluralStringResource(
                R.plurals.delete_schedules_confirmation_title,
                Int.One
            ),
            content = stringResource(R.string.action_irreversible_message),
            onConfirm = actions::onDeleteConfirm,
            onDismiss = actions::onDeleteDismiss
        )
    }

    if (state.showDatePicker) {
        OarDatePickerDialog(
            onDismiss = actions::onDateSelectionDismiss,
            onConfirm = actions::onDateSelectionConfirm,
            onPickTimeClick = actions::onPickTimeClick,
            state = datePickerState
        )
    }

    if (state.showTimePicker) {
        OarTimePickerDialog(
            onDismiss = actions::onTimeSelectionDismiss,
            onConfirm = actions::onTimeSelectionConfirm,
            onPickDateClick = actions::onPickDateClick,
            state = timePickerState
        )
    }
}

@Composable
private fun NoteInput(
    inputState: TextFieldState,
    modifier: Modifier = Modifier
) {
    OarTextField(
        state = inputState,
        modifier = modifier
            .defaultMinSize(minWidth = InputMinWidth),
        placeholder = {
            Text(
                text = stringResource(R.string.add_a_note),
                textAlign = TextAlign.Center,
                style = LocalTextStyle.current,
                modifier = Modifier
                    .defaultMinSize(minWidth = InputMinWidth),
            )
        },
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Default
        ),
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center
        ),
        lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = NOTE_MAX_LINES)
    )
}

private val InputMinWidth = 160.dp
private const val NOTE_MAX_LINES = 5

private const val TRANSACTION_TYPE_SELECTOR_WIDTH_FRACTION = 0.80f
private const val AMOUNT_RECOMMENDATION_WIDTH_FRACTION = 0.80f

@Composable
private fun ScheduleTimestamp(
    timestamp: LocalDateTime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stringResource(R.string.timestamp_label),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = timestamp.format(DateUtil.Formatters.localizedDateMediumTimeShort),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        FilledTonalIconButton(onClick = onClick) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_date_time),
                contentDescription = stringResource(R.string.cd_tap_to_pick_timestamp)
            )
        }
    }
}

@Composable
private fun FolderIndicator(
    folderName: String?,
    onSelectFolderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SimplePreference(
        title = folderName ?: stringResource(R.string.transaction_folder_indicator_label),
        leadingIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_folder_import),
                contentDescription = stringResource(R.string.cd_add_transaction_to_folder)
            )
        },
        onClick = onSelectFolderClick,
        modifier = modifier
            .fillMaxWidth(),
        summary = stringResource(R.string.tap_to_select_folder)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TransactionTypeSelector(
    selectedType: FundMovement,
    onValueChange: (FundMovement) -> Unit,
    modifier: Modifier = Modifier
) {
    val typeSelectorContentDescription = stringResource(
        R.string.cd_transaction_type_selector,
        stringResource(selectedType.creditOrDebitLabel)
    )

    Row(
        modifier = modifier
            .semantics(true) {
                contentDescription = typeSelectorContentDescription
            },
        horizontalArrangement = Arrangement.spacedBy(
            ButtonGroupDefaults.ConnectedSpaceBetween,
            Alignment.CenterHorizontally
        )
    ) {
        FundMovement.entries.forEachIndexed { index, type ->
            ToggleButton(
                checked = selectedType == type,
                onCheckedChange = { onValueChange(type) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    else -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                },
                modifier = Modifier
                    .weight(1f)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(type.iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(IconSizeSmall)
                )

                Spacer(ToggleButtonDefaults.IconSpacing)

                Text(stringResource(type.creditOrDebitLabel))
            }
        }
    }
}

@Composable
private fun RepetitionSelection(
    selectedRepeatMode: ScheduleRepetition,
    onSelect: (ScheduleRepetition) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_repeat),
                contentDescription = stringResource(R.string.cd_transaction_repeat_mode)
            )

            Text(
                text = stringResource(R.string.repeats),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            ScheduleRepetition.entries.forEachIndexed { index, repetition ->
                ToggleButton(
                    checked = repetition == selectedRepeatMode,
                    onCheckedChange = { onSelect(repetition) },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        ScheduleRepetition.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                ) {
                    Text(stringResource(repetition.labelRes))
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun PreviewScreenContent() {
    OarTheme {
        AddEditScheduleScreen(
            isEditMode = false,
            snackbarController = rememberSnackbarController(),
            amountInputState = rememberTextFieldState(),
            noteInputState = rememberTextFieldState(),
            state = AddEditScheduleState(
                selectedRepetition = ScheduleRepetition.MONTHLY
            ),
            actions = object : AddEditScheduleActions {
                override fun onCurrencySelect(currency: Currency) {}
                override fun refreshCurrentDateTime() {}
                override fun onAmountFocusLost() {}
                override fun onEvaluateExpressionClick() {}
                override fun onRecommendedAmountClick(amount: Long) {}
                override fun onTagSelect(tagId: Long?) {}
                override fun onTimestampClick() {}
                override fun onDateSelectionDismiss() {}
                override fun onPickTimeClick() {}
                override fun onPickDateClick() {}
                override fun onDateSelectionConfirm(millis: Long) {}
                override fun onTimeSelectionDismiss() {}
                override fun onTimeSelectionConfirm(hour: Int, minute: Int) {}
                override fun onTypeChange(type: FundMovement) {}
                override fun onDeleteDismiss() {}
                override fun onDeleteConfirm() {}
                override fun onSelectFolderClick() {}
                override fun onOptionClick(option: AddEditScheduleOption) {}
                override fun onRepetitionSelect(repetition: ScheduleRepetition) {}
                override fun onSaveClick() {}
            },
            navigateUp = {}
        )
    }
}
