package dev.ridill.oar.settings.presentation.settings

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import dev.ridill.oar.R
import dev.ridill.oar.account.domain.model.AuthState
import dev.ridill.oar.core.domain.util.BuildUtil
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.ConfirmationDialog
import dev.ridill.oar.core.ui.components.FeatureInfoDialog
import dev.ridill.oar.core.ui.components.MultiActionConfirmationDialog
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.PermissionRationaleDialog
import dev.ridill.oar.core.ui.components.RadioOptionListDialog
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.icons.Message
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.settings.domain.modal.AppTheme
import dev.ridill.oar.settings.presentation.components.PreferenceIcon
import dev.ridill.oar.settings.presentation.components.SimpleSettingsPreference
import dev.ridill.oar.settings.presentation.components.SwitchPreference

@Composable
fun SettingsScreen(
    snackbarController: SnackbarController,
    state: SettingsState,
    actions: SettingsActions,
    navigateUp: () -> Unit,
    navigateToNotificationSettings: () -> Unit,
    navigateToCycles: () -> Unit,
    navigateToManageTags: () -> Unit,
    navigateToBackupSettings: () -> Unit,
    navigateToSecuritySettings: () -> Unit,
    launchUriInBrowser: (Uri) -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    OarScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.destination_settings)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        snackbarController = snackbarController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection)
                )
                .verticalScroll(rememberScrollState())
                .padding(bottom = PaddingScrollEnd)
        ) {
            SimpleSettingsPreference(
                titleRes = R.string.preference_app_theme,
                summary = stringResource(state.appTheme.labelRes),
                onClick = actions::onAppThemePreferenceClick,
                leadingIcon = ImageVector.vectorResource(R.drawable.ic_outlined_sun_medium)
            )

            if (BuildUtil.isDynamicColorsSupported()) {
                SwitchPreference(
                    titleRes = R.string.preference_dynamic_colors,
                    summary = stringResource(R.string.preference_dynamic_colors_summary),
                    value = state.dynamicColorsEnabled,
                    onValueChange = actions::onDynamicThemeEnabledChange,
                    leadingIcon = { PreferenceIcon(imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_palette)) }
                )
            }

            SimpleSettingsPreference(
                titleRes = R.string.preference_notifications,
                summary = stringResource(R.string.preference_notification_summary),
                onClick = navigateToNotificationSettings,
                leadingIcon = ImageVector.vectorResource(R.drawable.ic_outlined_notification_bell)
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = PreferenceDividerVerticalPadding)
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_budget_cycles,
                summary = stringResource(R.string.preference_budget_cycles_summary),
                onClick = navigateToCycles
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_tags,
                summary = stringResource(R.string.preference_tags_summary),
                onClick = navigateToManageTags
            )

            if (state.showAutoDetectTxOption) {
                SwitchPreference(
                    titleRes = R.string.preference_auto_detect_transactions,
                    summary = stringResource(R.string.preference_auto_detect_transactions_summary),
                    value = state.autoDetectTransactionEnabled,
                    onValueChange = actions::onToggleAutoAddTransactions
                )
            }

            AnimatedVisibility(state.authState is AuthState.Authenticated) {
                SimpleSettingsPreference(
                    titleRes = R.string.preference_backup,
                    summary = stringResource(R.string.preference_backup_summary),
                    onClick = navigateToBackupSettings,
                )
            }

            SimpleSettingsPreference(
                titleRes = R.string.preference_security,
                summary = stringResource(R.string.preference_security_summary),
                onClick = navigateToSecuritySettings
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = PreferenceDividerVerticalPadding)
            )

            Crossfade(state.authState) { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        SimpleSettingsPreference(
                            titleRes = R.string.preference_logout,
                            leadingIcon = ImageVector.vectorResource(R.drawable.ic_rounded_logout),
                            onClick = actions::onLogoutClick,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    }

                    AuthState.UnAuthenticated -> {
                        SimpleSettingsPreference(
                            titleRes = R.string.preference_login,
                            leadingIcon = ImageVector.vectorResource(R.drawable.ic_rounded_login),
                            onClick = actions::onLoginClick,
                        )
                    }
                }
            }

            if (state.hasValidSourceCodeUrl) {
                SimpleSettingsPreference(
                    titleRes = R.string.preference_source_code,
                    summary = stringResource(R.string.preference_source_code_summary),
                    leadingIcon = ImageVector.vectorResource(R.drawable.ic_rounded_code),
                    trailingIcon = ImageVector.vectorResource(R.drawable.ic_rounded_link_square),
                    onClick = {
                        state.sourceCodeUrl?.let {
                            launchUriInBrowser(it.toUri())
                        }
                    }
                )
            }

            SimpleSettingsPreference(
                titleRes = R.string.preference_export_logs,
                summary = stringResource(R.string.preference_export_logs_summary),
                leadingIcon = ImageVector.vectorResource(R.drawable.ic_outlined_tree),
                onClick = actions::onExportLogsClick
            )

            SimpleSettingsPreference(
                titleRes = R.string.preference_version,
                leadingIcon = ImageVector.vectorResource(R.drawable.ic_outlined_git_commit),
                summary = BuildUtil.versionName
            )
        }

        if (state.showAppThemeSelection) {
            RadioOptionListDialog(
                titleRes = R.string.choose_theme,
                options = AppTheme.entries,
                currentOption = state.appTheme,
                onDismiss = actions::onAppThemeSelectionDismiss,
                onOptionSelect = actions::onAppThemeSelectionConfirm
            )
        }

        when (state.logoutDialog) {
            LogoutDialog.CHOICE -> {
                MultiActionConfirmationDialog(
                    title = stringResource(R.string.logout_dialog_title),
                    text = stringResource(R.string.logout_dialog_message),
                    primaryActionLabelRes = R.string.logout_keep_local_data,
                    onPrimaryActionClick = actions::onLogoutKeepDataClick,
                    secondaryActionLabelRes = R.string.logout_clear_local_data,
                    onSecondaryActionClick = actions::onLogoutClearDataClick,
                    onDismiss = actions::onLogoutDialogDismiss
                )
            }

            LogoutDialog.CONFIRM_CLEAR_DATA -> {
                ConfirmationDialog(
                    title = stringResource(R.string.logout_clear_data_confirm_title),
                    content = stringResource(
                        R.string.logout_clear_data_confirm_message,
                        state.lastBackupDateFormatted?.asString()
                            ?: stringResource(R.string.never_backed_up)
                    ),
                    confirmActionRes = R.string.logout_clear_and_logout,
                    onConfirm = actions::onLogoutClearDataConfirm,
                    onDismiss = actions::onLogoutDialogDismiss
                )
            }

            null -> Unit
        }

        if (state.isClearingLocalData) {
            AlertDialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                ),
                title = { Text(stringResource(R.string.logout_clearing_data)) },
                text = { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) },
                confirmButton = {}
            )
        }

        if (state.showSmsPermissionRationale) {
            PermissionRationaleDialog(
                icon = Icons.Rounded.Message,
                rationaleText = stringResource(
                    R.string.permission_rationale_read_sms, stringResource(R.string.app_name)
                ),
                onDismiss = actions::onSmsPermissionRationaleDismiss,
                onSettingsClick = actions::onSmsPermissionRationaleSettingsClick
            )
        }

        if (state.showAutoDetectTransactionFeatureInfo) {
            FeatureInfoDialog(
                title = stringResource(R.string.feature_info_auto_detect_transaction_title),
                text = stringResource(
                    R.string.feature_info_auto_detect_transaction_message,
                    stringResource(R.string.app_name)
                ),
                onAcknowledge = actions::onAutoDetectTxFeatureInfoAcknowledge,
                onDismiss = actions::onAutoDetectTxFeatureInfoDismiss,
                isExperimental = true
            )
        }
    }
}

private val PreferenceDividerVerticalPadding = 12.dp

@Preview(showBackground = true)
@Composable
fun PreviewSimplePreference() {
    OarTheme {
        Surface {
            SimpleSettingsPreference(
                titleRes = R.string.preference_app_theme,
                modifier = Modifier
                    .fillMaxWidth(),
                summary = stringResource(AppTheme.SYSTEM_DEFAULT.labelRes),
                leadingIcon = ImageVector.vectorResource(R.drawable.ic_outlined_sun_medium)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewSettingsScreen() {
    OarTheme {
        SettingsScreen(
            snackbarController = rememberSnackbarController(),
            state = SettingsState(
                showAutoDetectTxOption = true,
                sourceCodeUrl = "https://github.com"
            ),
            actions = object : SettingsActions {
                override fun onAppThemePreferenceClick() {}
                override fun onAppThemeSelectionDismiss() {}
                override fun onAppThemeSelectionConfirm(appTheme: AppTheme) {}
                override fun onDynamicThemeEnabledChange(enabled: Boolean) {}
                override fun onToggleAutoAddTransactions(enabled: Boolean) {}
                override fun onAutoDetectTxFeatureInfoDismiss() {}
                override fun onAutoDetectTxFeatureInfoAcknowledge() {}
                override fun onSmsPermissionRationaleDismiss() {}
                override fun onSmsPermissionRationaleSettingsClick() {}
                override fun onLoginClick() {}
                override fun onLogoutClick() {}
                override fun onLogoutDialogDismiss() {}
                override fun onLogoutKeepDataClick() {}
                override fun onLogoutClearDataClick() {}
                override fun onLogoutClearDataConfirm() {}
                override fun onExportLogsClick() {}
            },
            navigateUp = {},
            navigateToNotificationSettings = {},
            navigateToCycles = {},
            navigateToManageTags = {},
            navigateToBackupSettings = {},
            navigateToSecuritySettings = {},
            launchUriInBrowser = {}
        )
    }
}
