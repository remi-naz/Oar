package dev.ridill.oar.onboarding.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import dev.ridill.oar.R
import dev.ridill.oar.account.domain.model.AuthState
import dev.ridill.oar.core.domain.util.One
import dev.ridill.oar.core.domain.util.isAnyOf
import dev.ridill.oar.core.ui.components.DisplayMediumText
import dev.ridill.oar.core.ui.components.GoogleSignInButton
import dev.ridill.oar.core.ui.components.OutlinedPasswordFieldSheet
import dev.ridill.oar.core.ui.components.SecureTextFieldKeyboardOptions
import dev.ridill.oar.core.ui.components.Spacer
import dev.ridill.oar.core.ui.components.SpacerMedium
import dev.ridill.oar.core.ui.components.SpacerSmall
import dev.ridill.oar.core.ui.components.TitleLargeText
import dev.ridill.oar.core.ui.components.slideInVerticallyWithFadeIn
import dev.ridill.oar.core.ui.components.slideOutVerticallyWithFadeOut
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.onboarding.domain.model.DataRestoreState
import dev.ridill.oar.onboarding.domain.model.SignInAndDataRestoreState
import dev.ridill.oar.settings.presentation.components.LoggedInAccountInfo
import kotlin.time.Duration

@Composable
fun SignInAndDataRestore(
    state: SignInAndDataRestoreState,
    authState: AuthState,
    onSignInClick: () -> Unit,
    onSignInSkip: () -> Unit,
    restoreState: DataRestoreState,
    onCheckForBackupClick: () -> Unit,
    onSkipClick: () -> Unit,
    showEncryptionPasswordInput: Boolean,
    onEncryptionPasswordInputDismiss: () -> Unit,
    onEncryptionPasswordSubmit: (String) -> Unit,
    appRestartTimer: Duration,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large)
    ) {
        Crossfade(state) { screenState ->
            DisplayMediumText(
                text = stringResource(
                    id = when (screenState) {
                        SignInAndDataRestoreState.SIGN_IN -> R.string.onboarding_page_sign_in_title
                        SignInAndDataRestoreState.DATA_RESTORE -> R.string.onboarding_page_restore_data_title
                    }
                ),
                modifier = Modifier
                    .padding(vertical = MaterialTheme.spacing.medium)
            )
        }
        Crossfade(state) { screenState ->
            TitleLargeText(
                text = stringResource(
                    id = when (screenState) {
                        SignInAndDataRestoreState.SIGN_IN -> R.string.onboarding_page_sign_in_message
                        SignInAndDataRestoreState.DATA_RESTORE -> R.string.onboarding_page_restore_data_message
                    }
                ),
                fontWeight = FontWeight.Normal
            )
        }

        SpacerMedium()

        AnimatedVisibility(
            visible = state == SignInAndDataRestoreState.DATA_RESTORE,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_filled_cloud_download),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight(DOWNLOAD_ICON_HEIGHT_FRACTION)
                    .aspectRatio(Float.One)
            )
        }

        Spacer(weight = Float.One)

        AnimatedContent(
            targetState = state,
            label = "SignInAndDataRestoreContent"
        ) { state ->
            when (state) {
                SignInAndDataRestoreState.SIGN_IN -> {
                    AccountSignInSection(
                        authState = authState,
                        onSignInClick = onSignInClick,
                        onSignInSkip = onSignInSkip,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                SignInAndDataRestoreState.DATA_RESTORE -> {
                    DataRestoreSection(
                        restoreState = restoreState,
                        onCheckForBackupClick = onCheckForBackupClick,
                        onSkipClick = onSkipClick,
                        showEncryptionPasswordInput = showEncryptionPasswordInput,
                        onEncryptionPasswordInputDismiss = onEncryptionPasswordInputDismiss,
                        onEncryptionPasswordSubmit = onEncryptionPasswordSubmit,
                        appRestartTimer = appRestartTimer
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountSignInSection(
    authState: AuthState,
    onSignInClick: () -> Unit,
    onSignInSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(authState) { state ->
            when (state) {
                is AuthState.Authenticated -> {
                    LoggedInAccountInfo(
                        account = state.account,
                        modifier = Modifier
                            .clickable(
                                onClick = onSignInClick,
                                onClickLabel = stringResource(R.string.cd_sign_in)
                            )
                            .padding(MaterialTheme.spacing.small)
                    )
                }

                AuthState.UnAuthenticated -> {
                    GoogleSignInActions(
                        authState = authState,
                        onSignInClick = onSignInClick,
                        onSkipClick = onSignInSkip
                    )
                }
            }
        }
    }
}

@Composable
private fun GoogleSignInActions(
    authState: AuthState,
    onSignInClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showSignInOption by remember {
        derivedStateOf { authState is AuthState.UnAuthenticated }
    }
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AnimatedVisibility(visible = showSignInOption) {
            GoogleSignInButton(
                onClick = onSignInClick
            )
        }

        OnboardingTextButton(
            onClick = onSkipClick,
        ) {
            Text(
                stringResource(
                    id = when (authState) {
                        is AuthState.Authenticated -> R.string.action_next
                        AuthState.UnAuthenticated -> R.string.action_skip
                    }
                )
            )
        }
    }
}

@Composable
private fun DataRestoreSection(
    restoreState: DataRestoreState,
    onCheckForBackupClick: () -> Unit,
    onSkipClick: () -> Unit,
    showEncryptionPasswordInput: Boolean,
    onEncryptionPasswordInputDismiss: () -> Unit,
    onEncryptionPasswordSubmit: (String) -> Unit,
    appRestartTimer: Duration,
    modifier: Modifier = Modifier
) {
    val showRestoreStatus by remember(restoreState) {
        derivedStateOf {
            restoreState != DataRestoreState.IDLE
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = showRestoreStatus,
            enter = slideInVerticallyWithFadeIn(),
            exit = slideOutVerticallyWithFadeOut()
        ) {
            RestoreStatus(
                dataRestoreState = restoreState,
                restartTimer = appRestartTimer,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        val isBackupDownloaded by remember(restoreState) {
            derivedStateOf {
                restoreState.isAnyOf(
                    DataRestoreState.PASSWORD_VERIFICATION,
                    DataRestoreState.RESTORE_IN_PROGRESS
                )
            }
        }
        val isRestoreNotCompleted by remember(restoreState) {
            derivedStateOf {
                restoreState != DataRestoreState.COMPLETED
            }
        }
        val isRestoreInProgress by remember(restoreState) {
            derivedStateOf {
                restoreState.isAnyOf(
                    DataRestoreState.DOWNLOADING_DATA,
                    DataRestoreState.RESTORE_IN_PROGRESS
                )
            }
        }

        AnimatedVisibility(
            visible = isRestoreNotCompleted,
            enter = slideInVerticallyWithFadeIn(),
            exit = slideOutVerticallyWithFadeOut()
        ) {
            RestoreBackupActions(
                isBackupDownloaded = isBackupDownloaded,
                isRestoreInProgress = isRestoreInProgress,
                onRestoreClick = onCheckForBackupClick,
                onSkipClick = onSkipClick
            )
        }

        if (showEncryptionPasswordInput) {
            val passwordInput = rememberTextFieldState()
            OutlinedPasswordFieldSheet(
                titleRes = R.string.enter_password,
                inputState = passwordInput,
                onDismiss = onEncryptionPasswordInputDismiss,
                onConfirm = {
                    onEncryptionPasswordSubmit(passwordInput.text.toString())
                    passwordInput.clearText()
                },
                keyboardOptions = SecureTextFieldKeyboardOptions.copy(
                    imeAction = ImeAction.Done
                ),
                textFieldModifier = Modifier
                    .semantics {
                        contentType = ContentType.Password
                    },
                label = stringResource(R.string.enter_password)
            )
        }
    }
}

@Composable
private fun RestoreStatus(
    dataRestoreState: DataRestoreState,
    restartTimer: Duration,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Crossfade(
            targetState = dataRestoreState,
            label = "RestoreWorkerStateText"
        ) { restoreState ->
            val uiText = when (restoreState) {
                DataRestoreState.IDLE -> null
                DataRestoreState.CHECKING_FOR_BACKUP -> UiText.StringResource(R.string.checking_for_backups)
                DataRestoreState.DOWNLOADING_DATA -> UiText.StringResource(R.string.downloading_app_data)
                DataRestoreState.PASSWORD_VERIFICATION -> UiText.StringResource(R.string.verify_your_password)
                DataRestoreState.RESTORE_IN_PROGRESS -> UiText.StringResource(R.string.data_restore_in_progress)
                DataRestoreState.COMPLETED -> UiText.StringResource(
                    R.string.restarting_app_in_time,
                    args = listOf(restartTimer.toString())
                )

                DataRestoreState.FAILED -> UiText.StringResource(R.string.error_app_data_restore_failed)
            }

            Text(
                text = uiText?.asString().orEmpty(),
                color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
            )
        }

        SpacerSmall()

        Text(
            text = stringResource(R.string.data_restore_caution_message),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.large),
            color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
        )
    }
}

@Composable
private fun RestoreBackupActions(
    isBackupDownloaded: Boolean,
    isRestoreInProgress: Boolean,
    onRestoreClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnboardingButton(
            onClick = onRestoreClick,
            enabled = !isRestoreInProgress
        ) {
            Text(
                text = stringResource(
                    id = if (isBackupDownloaded) R.string.restore_backup
                    else R.string.check_for_backups
                )
            )
        }

        OnboardingTextButton(
            onClick = onSkipClick,
            enabled = !isRestoreInProgress
        ) {
            Text(stringResource(R.string.do_not_restore))
        }
    }
}

private const val DOWNLOAD_ICON_HEIGHT_FRACTION = 0.16f