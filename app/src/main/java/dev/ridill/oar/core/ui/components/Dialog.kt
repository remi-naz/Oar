package dev.ridill.oar.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.IconSizeMedium
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.mergedContentDescription
import dev.ridill.oar.settings.domain.modal.BaseRadioOption

@Composable
fun ConfirmationDialog(
    @StringRes titleRes: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes contentRes: Int = R.string.action_irreversible_message,
    @StringRes confirmActionRes: Int = R.string.action_confirm,
    @StringRes dismissActionRes: Int = R.string.action_cancel,
    showDismissButton: Boolean = true,
    properties: DialogProperties = DialogProperties(),
    additionalNote: String? = null
) = ConfirmationDialog(
    title = stringResource(titleRes),
    content = stringResource(contentRes),
    onConfirm = onConfirm,
    onDismiss = onDismiss,
    modifier = modifier,
    confirmActionRes = confirmActionRes,
    dismissActionRes = dismissActionRes,
    showDismissButton = showDismissButton,
    properties = properties,
    additionalNote = additionalNote
)

@Composable
fun ConfirmationDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: String = stringResource(R.string.action_irreversible_message),
    @StringRes confirmActionRes: Int = R.string.action_confirm,
    @StringRes dismissActionRes: Int = R.string.action_cancel,
    showDismissButton: Boolean = true,
    properties: DialogProperties = DialogProperties(),
    additionalNote: String? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(confirmActionRes))
            }
        },
        dismissButton = {
            if (showDismissButton) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(dismissActionRes))
                }
            }
        },
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                Text(content)
                additionalNote?.let {
                    Text(
                        text = it,
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                    )
                }
            }
        },
        modifier = modifier,
        properties = properties
    )
}

@Composable
fun PermissionRationaleDialog(
    icon: ImageVector,
    rationaleText: String,
    onDismiss: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = {},
        modifier = modifier,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            contentColor = AlertDialogDefaults.titleContentColor,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(MaterialTheme.spacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(PermissionIconSize)
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(RationaleContentPadding)
                        .fillMaxWidth()
                ) {
                    Text(rationaleText)

                    SpacerMedium()

                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.action_not_now))
                        }
                        TextButton(onClick = onSettingsClick) {
                            Text(stringResource(R.string.settings))
                        }
                    }
                }
            }
        }
    }
}

private val RationaleContentPadding = 24.dp
private val PermissionIconSize = 40.dp

@Composable
fun <T : BaseRadioOption> RadioOptionListDialog(
    @StringRes titleRes: Int,
    options: List<T>,
    currentOption: T?,
    onDismiss: () -> Unit,
    onOptionSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes dismissActionRes: Int = R.string.action_cancel
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(dismissActionRes))
            }
        },
        title = { Text(stringResource(titleRes)) },
        text = {
            Column(
                modifier = Modifier
                    .selectableGroup()
            ) {
                options.forEach { option ->
                    LabelledRadioButton(
                        labelRes = option.labelRes,
                        selected = option == currentOption,
                        onClick = { onOptionSelect(option) },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun MultiActionConfirmationDialog(
    title: String,
    text: String,
    @StringRes primaryActionLabelRes: Int,
    onPrimaryActionClick: () -> Unit,
    @StringRes secondaryActionLabelRes: Int,
    onSecondaryActionClick: () -> Unit,
    @StringRes dismissActionLabelRes: Int = R.string.action_cancel,
    onDismiss: () -> Unit,
    additionalNote: String? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
            ) {
                Button(
                    onClick = onPrimaryActionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(stringResource(primaryActionLabelRes))
                }
                OutlinedButton(
                    onClick = onSecondaryActionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(stringResource(secondaryActionLabelRes))
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(stringResource(dismissActionLabelRes))
                }
            }
        },
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                Text(text)
                additionalNote?.let {
                    Text(
                        text = it,
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                    )
                }
            }
        }
    )
}

@Composable
fun FeatureInfoDialog(
    title: String,
    text: String,
    onAcknowledge: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isExperimental: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onAcknowledge) {
                Text(stringResource(R.string.action_acknowledge))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_dismiss))
            }
        },
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                Text(text)
                if (isExperimental) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        modifier = Modifier
                            .mergedContentDescription(
                                stringResource(R.string.feature_experimental_message)
                            )
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_filled_experiment),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(IconSizeMedium)
                        )
                        Text(
                            text = stringResource(R.string.feature_experimental_message),
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                        )
                    }
                }
            }
        },
        modifier = modifier
    )
}