package dev.ridill.oar.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.theme.spacing

@Composable
fun OarDatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    onPickTimeClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: DatePickerState = rememberDatePickerState(),
    dateFormatter: DatePickerFormatter = remember { DatePickerDefaults.dateFormatter() },
    title: (@Composable () -> Unit)? = {
        DatePickerDefaults.DatePickerTitle(
            displayMode = state.displayMode,
            modifier = Modifier.padding(DatePickerTitlePadding)
        )
    },
    headline: (@Composable () -> Unit)? = {
        DatePickerDefaults.DatePickerHeadline(
            selectedDateMillis = state.selectedDateMillis,
            displayMode = state.displayMode,
            dateFormatter = dateFormatter,
            modifier = Modifier.padding(DatePickerHeadlinePadding)
        )
    },
    showModeToggle: Boolean = true,
    colors: DatePickerColors = DatePickerDefaults.colors()
) = DatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
        FilledTonalIconButton(
            onClick = {
                state.selectedDateMillis?.let(onConfirm)
                onPickTimeClick()
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_clock),
                contentDescription = stringResource(R.string.cd_pick_time)
            )
        }
    },
    dismissButton = {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }

            Button(onClick = {
                state.selectedDateMillis?.let(onConfirm)

            }) {
                Text(text = stringResource(R.string.action_confirm))
            }
        }
    },
    modifier = modifier
) {
    DatePicker(
        state = state,
        dateFormatter = dateFormatter,
        title = title,
        headline = headline,
        showModeToggle = showModeToggle,
        colors = colors
    )
}

private val DatePickerTitlePadding = PaddingValues(start = 24.dp, end = 12.dp, top = 16.dp)
private val DatePickerHeadlinePadding =
    PaddingValues(start = 24.dp, end = 12.dp, bottom = 12.dp)

@Composable
fun OarTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onPickDateClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: TimePickerState = rememberTimePickerState(),
    colors: TimePickerColors = TimePickerDefaults.colors(),
    layoutType: TimePickerLayoutType = TimePickerDefaults.layoutType(),
) = AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
        FilledTonalIconButton(
            onClick = {
                onConfirm(state.hour, state.minute)
                onPickDateClick()
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_calendar),
                contentDescription = stringResource(R.string.cd_pick_date)
            )
        }
    },
    dismissButton = {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }

            Button(onClick = {
                onConfirm(state.hour, state.minute)
            }) {
                Text(text = stringResource(R.string.action_confirm))
            }
        }
    },
    text = {
        TimePicker(
            state = state,
            colors = colors,
            layoutType = layoutType
        )
    },
    modifier = modifier
)