package dev.ridill.oar.moneyPiles.presentation.movePileFund

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.OarDatePickerDialog
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.OarTimePickerDialog
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import dev.ridill.oar.settings.presentation.components.SimplePreference
import dev.ridill.oar.transactions.presentation.components.AmountInput
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun MovePileFundScreen(
    movement: FundMovement,
    amountInputState: TextFieldState,
    state: MovePileFundState,
    actions: MovePileFundActions,
    navigateUp: () -> Unit,
    navigateToCycleSelection: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarController: SnackbarController = rememberSnackbarController(),
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(300.milliseconds)
        focusRequester.requestFocus()
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val dateNowUtc = remember { DateUtil.dateNow(ZoneId.of(ZoneOffset.UTC.id)) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtil.toMillis(state.timestampUtc),
        yearRange = IntRange(DatePickerDefaults.YearRange.first, dateNowUtc.year),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis < DateUtil.toMillis(
                    date = dateNowUtc.plusDays(1),
                    zoneId = ZoneId.of(ZoneOffset.UTC.id)
                )
        }
    )
    val timePickerState = rememberTimePickerState(
        initialHour = state.timestamp.hour,
        initialMinute = state.timestamp.minute
    )

    OarScaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(movement.pileTransactionMovement)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior,
                subtitle = state.pile?.name?.let {
                    { Text(it) }
                },
            )
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .imePadding()
            .then(modifier),
        floatingActionButton = {
            MediumFloatingActionButton(
                onClick = actions::onConfirm,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Save,
                    contentDescription = stringResource(R.string.cd_save_transaction)
                )
            }
        },
        snackbarController = snackbarController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(
                    top = MaterialTheme.spacing.medium,
                    bottom = PaddingScrollEnd
                )
                .padding(horizontal = MaterialTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            AmountInput(
                inputState = amountInputState,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                currency = state.pile?.currency ?: LocaleUtil.defaultCurrency,
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .padding(vertical = MaterialTheme.spacing.small),
                colors = TextFieldDefaults.tonalColors()
            )

            if (state.pile != null && state.pile.reminderAmount.orZero() > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                ) {
                    SuggestionChip(
                        onClick = actions::onAddRecommendedAmountClick,
                        label = {
                            Text(
                                text = stringResource(
                                    R.string.recommended_colon_amount,
                                    TextFormat.currencyAmount(state.pile.reminderAmount.orZero())
                                )
                            )
                        }
                    )
                }
            }

            HorizontalDivider()

            MovePileFundTimestamp(
                timestamp = state.timestamp,
                onClick = actions::onTimestampClick,
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .align(Alignment.End)
            )

            SimplePreference(
                titleRes = R.string.cycle,
                summary = state.cycleDescription.orEmpty(),
                onClick = navigateToCycleSelection,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.pile != null) {
                HorizontalDivider()

                PileInfoField(
                    pile = state.pile,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
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

@get:StringRes
private val FundMovement.pileTransactionMovement: Int
    get() = when (this) {
        FundMovement.IN -> R.string.pile_action_add
        FundMovement.OUT -> R.string.pile_action_withdraw
    }

@Composable
private fun PileInfoField(
    pile: MoneyPileDetails,
    modifier: Modifier = Modifier
) {
    val contributionModeHelpText = stringResource(pile.contributionMode.helpTextRes)
    val targetLabel = pile.targetAmount?.let {
        stringResource(R.string.pile_target_label, TextFormat.currencyAmount(it))
    }
    val targetDateLabel = pile.targetDate
        ?.takeIf { targetLabel != null }
        ?.format(DateUtil.Formatters.localizedDateMedium)

    val summary = buildString {
        append(contributionModeHelpText)
        if (targetLabel != null) {
            append("\n")
            append(targetLabel)
            if (targetDateLabel != null) {
                append(" · ")
                append(targetDateLabel)
            }
        }
    }

    SimplePreference(
        title = stringResource(pile.contributionMode.labelRes),
        summary = summary,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null
            )
        },
        modifier = modifier
    )
}

@Composable
private fun MovePileFundTimestamp(
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
                contentDescription = stringResource(R.string.cd_pick_timestamp)
            )
        }
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun PreviewMovePileFundScreen() {
    OarTheme {
        MovePileFundScreen(
            movement = FundMovement.IN,
            state = MovePileFundState(
                pile = MoneyPileDetails(
                    id = 1L,
                    name = "Japan trip",
                    icon = PileIcon.LandProperty,
                    colorCode = 0xFFFF4CA6.toInt(),
                    contributionMode = PileContributionMode.FROM_BALANCE,
                    targetAmount = 5000.0,
                    locked = false,
                    reminderCadence = PileReminderCadence.WEEKLY,
                    reminderBehavior = PileReminderBehavior.AUTO_ADD,
                    reminderAmount = 150.0,
                    createdTimestamp = DateUtil.now(),
                    targetDate = null,
                    currency = LocaleUtil.defaultCurrency,
                    completionTimestamp = null,
                    nextReminderTimestamp = null,
                ),
                cycleDescription = "Aug 1 - Aug 31"
            ),
            amountInputState = rememberTextFieldState(),
            actions = object : MovePileFundActions {
                override fun onAddRecommendedAmountClick() {}
                override fun onTimestampClick() {}
                override fun onDateSelectionDismiss() {}
                override fun onDateSelectionConfirm(millis: Long) {}
                override fun onPickTimeClick() {}
                override fun onTimeSelectionDismiss() {}
                override fun onTimeSelectionConfirm(hour: Int, minute: Int) {}
                override fun onPickDateClick() {}
                override fun onConfirm() {}
            },
            navigateUp = {},
            navigateToCycleSelection = {},
        )
    }
}
