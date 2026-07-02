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
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.ridill.oar.R
import dev.ridill.oar.account.domain.model.AuthState
import dev.ridill.oar.core.domain.util.BuildUtil
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.FeatureInfoDialog
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.PermissionRationaleDialog
import dev.ridill.oar.core.ui.components.RadioOptionListDialog
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.icons.Message
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.settings.domain.modal.AppTheme
import dev.ridill.oar.settings.presentation.components.PreferenceIcon
import dev.ridill.oar.settings.presentation.components.SimpleSettingsPreference
import dev.ridill.oar.settings.presentation.components.SwitchPreference

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
                leadingIcon = Icons.Rounded.BrightnessMedium
            )

            if (BuildUtil.isDynamicColorsSupported()) {
                SwitchPreference(
                    titleRes = R.string.preference_dynamic_colors,
                    summary = stringResource(R.string.preference_dynamic_colors_summary),
                    value = state.dynamicColorsEnabled,
                    onValueChange = actions::onDynamicThemeEnabledChange,
                    leadingIcon = { PreferenceIcon(imageVector = Icons.Rounded.Palette) }
                )
            }

            SimpleSettingsPreference(
                titleRes = R.string.preference_notifications,
                summary = stringResource(R.string.preference_notification_summary),
                onClick = navigateToNotificationSettings,
                leadingIcon = Icons.Rounded.Notifications
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
                            leadingIcon = Icons.AutoMirrored.Rounded.Logout,
                            onClick = actions::onLogoutClick,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    }

                    AuthState.UnAuthenticated -> {
                        SimpleSettingsPreference(
                            titleRes = R.string.preference_login,
                            leadingIcon = Icons.AutoMirrored.Rounded.Logout,
                            onClick = actions::onLoginClick,
                        )
                    }
                }
            }

            if (state.hasValidSourceCodeUrl) {
                SimpleSettingsPreference(
                    titleRes = R.string.preference_source_code,
                    summary = stringResource(R.string.preference_source_code_summary),
                    leadingIcon = ImageVector.vectorResource(R.drawable.ic_filled_source_code),
                    trailingIcon = Icons.AutoMirrored.Filled.Launch,
                    onClick = {
                        state.sourceCodeUrl?.let {
                            launchUriInBrowser(it.toUri())
                        }
                    }
                )
            }

            SimpleSettingsPreference(
                titleRes = R.string.preference_version,
                leadingIcon = Icons.Rounded.Info,
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
                leadingIcon = Icons.Rounded.BrightnessMedium
            )
        }
    }
}