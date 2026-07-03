package dev.ridill.oar.tags.presentation.addEditTag

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.One
import dev.ridill.oar.core.ui.components.ButtonWithLoadingIndicator
import dev.ridill.oar.core.ui.components.ConfirmationDialog
import dev.ridill.oar.core.ui.components.HorizontalColorSelectionList
import dev.ridill.oar.core.ui.components.MarkExcludedSwitch
import dev.ridill.oar.core.ui.components.OutlinedTextFieldSheetContent
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.UiText
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AddEditTagSheet(
    isLoading: Boolean,
    nameState: TextFieldState,
    selectedColorCode: () -> Int?,
    excluded: () -> Boolean?,
    errorMessage: UiText?,
    showDeleteTagConfirmation: Boolean,
    isEditMode: Boolean,
    actions: AddEditTagActions,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isEditMode) {
        if (!isEditMode) {
            delay(500.milliseconds)
            focusRequester.requestFocus()
        }
    }

    OutlinedTextFieldSheetContent(
        title = {
            Row(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) {
                Text(
                    text = stringResource(
                        id = if (!isEditMode) R.string.destination_new_tag
                        else R.string.destination_edit_tag
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .weight(Float.One)
                )

                if (isEditMode) {
                    IconButton(onClick = actions::onDeleteClick) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteForever,
                            contentDescription = stringResource(R.string.cd_delete_tag)
                        )
                    }
                }
            }
        },
        inputState = nameState,
        text = {
            if (!isEditMode) {
                Text(
                    text = stringResource(R.string.new_tag_input_text),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        ),
        label = stringResource(R.string.tag_name),
        errorMessage = errorMessage,
        focusRequester = focusRequester,
        contentAfterTextField = {
            HorizontalColorSelectionList(
                selectedColorCode = selectedColorCode,
                onColorSelect = actions::onColorSelect
            )

            MarkExcludedSwitch(
                excluded = excluded() == true,
                onToggle = actions::onExclusionChange,
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .align(Alignment.End)
            )
        },
        modifier = modifier,
        actionButton = {
            ButtonWithLoadingIndicator(
                onClick = actions::onConfirm,
                textRes = R.string.action_confirm,
                loading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
        }
    )

    if (showDeleteTagConfirmation) {
        ConfirmationDialog(
            title = pluralStringResource(
                R.plurals.delete_tags_confirmation_title,
                Int.One
            ),
            content = stringResource(R.string.action_irreversible_message),
            additionalNote = stringResource(R.string.delete_tag_confirmation_note),
            onConfirm = actions::onDeleteTagConfirm,
            onDismiss = actions::onDeleteTagDismiss
        )
    }
}