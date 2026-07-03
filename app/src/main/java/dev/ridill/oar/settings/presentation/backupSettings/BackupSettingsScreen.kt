package dev.ridill.oar.settings.presentation.backupSettings

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddToDrive
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.RadioOptionListDialog
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.icons.Google
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.settings.domain.modal.BackupInterval
import dev.ridill.oar.settings.domain.repositoty.FatalBackupError
import dev.ridill.oar.settings.presentation.components.BasicPreference
import dev.ridill.oar.settings.presentation.components.EmptyPreferenceIconSpacer
import dev.ridill.oar.settings.presentation.components.PreferenceIcon
import dev.ridill.oar.settings.presentation.components.PreferenceIconSize
import dev.ridill.oar.settings.presentation.components.SimplePreference
import dev.ridill.oar.settings.presentation.components.SimpleSettingsPreference

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackupSettingsScreen(
    context: Context,
    snackbarController: SnackbarController,
    state: BackupSettingsState,
    actions: BackupSettingsActions,
    navigateUp: () -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    OarScaffold(
        snackbarController = snackbarController,
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.destination_backup_settings)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            val showBackupError by remember(state.fatalBackupError) {
                derivedStateOf { state.fatalBackupError != null }
            }
            AnimatedVisibility(visible = showBackupError) {
                state.fatalBackupError?.let {
                    FatalBackupErrorMessage(
                        error = it,
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.spacing.medium)
                    )
                }
            }

            val infoVerticalDp by animateDpAsState(
                targetValue = if (showBackupError) Dp.Zero
                else MaterialTheme.spacing.large,
                label = "VerticalSpacingDpForInfo"
            )
            BackupInfoText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = infoVerticalDp)
            )

            HorizontalDivider()

            SimpleSettingsPreference(
                titleRes = R.string.preference_google_account,
                leadingIcon = Icons.Default.Google,
                summary = state.userEmail
            )

            SimplePreference(
                titleRes = R.string.preference_backup_interval,
                summary = stringResource(state.backupInterval.labelRes),
                onClick = actions::onBackupIntervalPreferenceClick,
                leadingIcon = { EmptyPreferenceIconSpacer() }
            )

            PreviousBackupDetails(
                lastBackupDate = state.lastBackupDateFormatted?.asString(),
                lastBackupTime = state.getBackupTimeFormatted(
                    is24HourFormat = DateFormat.is24HourFormat(context)
                ),
                onBackupNowClick = actions::onBackupNowClick,
                isBackupRunning = state.isBackupRunning
            )

            SimplePreference(
                titleRes = R.string.preference_backup_encryption_title,
                summary = stringResource(R.string.preference_backup_encryption_summary),
                onClick = actions::onEncryptionPreferenceClick,
                leadingIcon = {
                    if (!state.isEncryptionPasswordAvailable) {
                        PreferenceIcon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }

        if (state.showBackupIntervalSelection) {
            RadioOptionListDialog(
                titleRes = R.string.choose_backup_interval,
                options = BackupInterval.entries,
                currentOption = state.backupInterval,
                onDismiss = actions::onBackupIntervalSelectionDismiss,
                onOptionSelect = actions::onBackupIntervalSelected
            )
        }
    }
}

@Composable
private fun BackupInfoText(
    modifier: Modifier = Modifier
) {
    BasicPreference(
        titleContent = { Text(stringResource(R.string.preference_title_google_drive)) },
        summaryContent = {
            Text(
                text = stringResource(
                    R.string.preference_google_drive_backup_message,
                    stringResource(R.string.app_name)
                )
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.AddToDrive,
                contentDescription = null,
                modifier = Modifier
                    .size(PreferenceIconSize)
            )
        },
        titleTextStyle = MaterialTheme.typography.titleMedium,
        summaryTextStyle = MaterialTheme.typography.bodyLarge,
        modifier = modifier,
        verticalAlignment = Alignment.Top
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PreviousBackupDetails(
    lastBackupDate: String?,
    lastBackupTime: String?,
    onBackupNowClick: () -> Unit,
    isBackupRunning: Boolean,
    modifier: Modifier = Modifier
) {
    BasicPreference(
        titleContent = {
            Text(
                text = stringResource(R.string.last_backup),
                fontWeight = FontWeight.SemiBold
            )
        },
        leadingIcon = { EmptyPreferenceIconSpacer() },
        summaryContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                modifier = Modifier
                    .padding(vertical = MaterialTheme.spacing.small)
            ) {
                lastBackupDate?.let { Text(stringResource(R.string.date_label, it)) }
                lastBackupTime?.let { Text(stringResource(R.string.time_label, it)) }

                Crossfade(
                    targetState = isBackupRunning,
                    label = "BackupProgressBarAnimation"
                ) { loading ->
                    if (loading) {
                        LinearWavyProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    } else {
                        Button(onClick = onBackupNowClick) {
                            Text(stringResource(R.string.backup_now))
                        }
                    }
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun FatalBackupErrorMessage(
    error: FatalBackupError,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null
            )

            Text(
                text = stringResource(
                    id = when (error) {
                        FatalBackupError.PASSWORD_CORRUPTED -> R.string.error_fatal_backup_failure_password_corruption
                        FatalBackupError.GOOGLE_AUTH_FAILURE -> R.string.error_fatal_backup_failure_auth_failure
                        FatalBackupError.STORAGE_QUOTA_EXCEEDED -> R.string.error_fatal_backup_failure_storage_quota_exceeded
                    }
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewBackupSettingsScreen() {
    OarTheme {
        BackupSettingsScreen(
            context = LocalContext.current,
            snackbarController = rememberSnackbarController(),
            state = BackupSettingsState(),
            actions = object : BackupSettingsActions {
                override fun onBackupIntervalPreferenceClick() {}
                override fun onBackupIntervalSelected(interval: BackupInterval) {}
                override fun onBackupIntervalSelectionDismiss() {}
                override fun onBackupNowClick() {}
                override fun onEncryptionPreferenceClick() {}
            },
            navigateUp = {}
        )
    }
}