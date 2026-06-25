package dev.ridill.oar.settings.presentation.backupEncryption

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.ArrangementTopWithFooter
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.ButtonWithLoadingIndicator
import dev.ridill.oar.core.ui.components.DisplayMediumText
import dev.ridill.oar.core.ui.components.DisplaySmallText
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.PasswordField
import dev.ridill.oar.core.ui.components.SecureTextFieldKeyboardOptions
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.SpacerMedium
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.navigation.destinations.BackupEncryptionScreenSpec
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackupEncryptionScreen(
    snackbarController: SnackbarController,
    currentPasswordState: TextFieldState,
    newPasswordState: TextFieldState,
    confirmNewPasswordState: TextFieldState,
    state: BackupEncryptionState,
    actions: BackupEncryptionActions,
    navigateUp: () -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    OarScaffold(
        isLoading = state.isLoading,
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(BackupEncryptionScreenSpec.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        snackbarController = snackbarController,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(MaterialTheme.spacing.medium)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = ArrangementTopWithFooter(MaterialTheme.spacing.medium)
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudDone,
                contentDescription = null,
                modifier = Modifier
                    .size(IconSize)
            )
            DisplaySmallText(stringResource(BackupEncryptionScreenSpec.labelRes))

            SpacerMedium()

            Text(
                text = stringResource(R.string.backup_encryption_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.large)
            )

            OutlinedButton(
                onClick = actions::onUpdatePasswordClick,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.update_password))
            }
        }

        if (state.showPasswordInput) {
            PasswordUpdateSheet(
                hasExistingPassword = state.hasExistingPassword,
                onDismissRequest = actions::onPasswordInputDismiss,
                currentPasswordState = currentPasswordState,
                newPasswordState = newPasswordState,
                confirmNewPasswordState = confirmNewPasswordState,
                onForgotPasswordClick = actions::onForgotCurrentPasswordClick,
                isLoading = state.isPasswordUpdateButtonLoading,
                onConfirmClick = actions::onPasswordUpdateConfirm
            )
        }
    }
}

private val IconSize = 80.dp

@Composable
private fun PasswordUpdateSheet(
    hasExistingPassword: Boolean,
    onDismissRequest: () -> Unit,
    currentPasswordState: TextFieldState,
    newPasswordState: TextFieldState,
    confirmNewPasswordState: TextFieldState,
    onForgotPasswordClick: () -> Unit,
    onConfirmClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val confirmEnabled by remember(hasExistingPassword) {
        derivedStateOf {
            (!hasExistingPassword || currentPasswordState.text.isNotEmpty())
                    && newPasswordState.text.isNotEmpty()
                    && confirmNewPasswordState.text.isNotEmpty()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    ) {
        Column(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            DisplayMediumText(text = stringResource(R.string.update_password))
            AnimatedVisibility(hasExistingPassword) {
                Column {
                    PasswordField(
                        state = currentPasswordState,
                        label = stringResource(R.string.current_password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentType = ContentType.Password
                            },
                        keyboardOptions = SecureTextFieldKeyboardOptions.copy(
                            imeAction = ImeAction.Next
                        )
                    )
                    TextButton(
                        onClick = onForgotPasswordClick,
                        modifier = Modifier
                            .align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.forgot_password))
                    }
                }
            }

            PasswordField(
                state = newPasswordState,
                label = stringResource(R.string.new_password),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentType = ContentType.NewPassword
                    },
                keyboardOptions = SecureTextFieldKeyboardOptions.copy(
                    imeAction = ImeAction.Next
                )
            )

            PasswordField(
                state = confirmNewPasswordState,
                label = stringResource(R.string.confirm_new_password),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentType = ContentType.NewPassword
                    },
                keyboardOptions = SecureTextFieldKeyboardOptions.copy(
                    imeAction = ImeAction.Done
                )
            )

            ButtonWithLoadingIndicator(
                textRes = R.string.action_confirm,
                loading = isLoading,
                onClick = onConfirmClick,
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = confirmEnabled
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewBackupEncryptionScreen() {
    OarTheme {
        BackupEncryptionScreen(
            snackbarController = rememberSnackbarController(),
            currentPasswordState = TextFieldState(),
            newPasswordState = TextFieldState(),
            confirmNewPasswordState = TextFieldState(),
            state = BackupEncryptionState(),
            actions = object : BackupEncryptionActions {
                override fun onUpdatePasswordClick() {}
                override fun onPasswordInputDismiss() {}
                override fun onForgotCurrentPasswordClick() {}
                override fun onPasswordUpdateConfirm() {}
            },
            navigateUp = {}
        )
    }
}