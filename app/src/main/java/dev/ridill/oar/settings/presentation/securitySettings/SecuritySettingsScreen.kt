package dev.ridill.oar.settings.presentation.securitySettings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.BuildUtil
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.LabelledRadioButton
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.PermissionRationaleDialog
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.oar.settings.presentation.components.SimpleSettingsPreference
import dev.ridill.oar.settings.presentation.components.SwitchPreference

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SecuritySettingsScreen(
    snackbarController: SnackbarController,
    state: SecuritySettingsState,
    actions: SecuritySettingsActions,
    navigateUp: () -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    OarScaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.destination_security_settings)) },
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
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SwitchPreference(
                titleRes = R.string.preference_app_lock,
                summary = stringResource(
                    R.string.preference_app_lock_summary,
                    stringResource(R.string.app_name)
                ),
                value = state.appLockEnabled,
                onValueChange = actions::onAppLockToggle
            )

            HorizontalDivider()

            AnimatedVisibility(visible = state.appLockEnabled) {
                Column {
                    AutoLockIntervalSelection(
                        selectedInterval = state.autoLockInterval,
                        onIntervalSelect = actions::onAutoLockIntervalSelect
                    )

                    HorizontalDivider()
                }
            }

            SwitchPreference(
                titleRes = R.string.preference_screen_security,
                summary = stringResource(R.string.preference_screen_security_summary),
                value = state.screenSecurityEnabled,
                onValueChange = actions::onScreenSecurityToggle
            )
        }
    }

    if (state.showNotificationPermissionRationale) {
        PermissionRationaleDialog(
            icon = Icons.Rounded.Notifications,
            rationaleText = stringResource(
                R.string.permission_rationale_notification,
                stringResource(R.string.app_name)
            ),
            onDismiss = actions::onNotificationPermissionRationaleDismiss,
            onSettingsClick = actions::onNotificationPermissionRationaleConfirm,
        )
    }
}

@Composable
private fun AutoLockIntervalSelection(
    selectedInterval: AppAutoLockInterval,
    onIntervalSelect: (AppAutoLockInterval) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = remember {
        AppAutoLockInterval.entries
            .filter { BuildUtil.isDebug || !it.debugOnly }
    }
    Column(
        modifier = modifier
    ) {
        SimpleSettingsPreference(titleRes = R.string.auto_lock_after)
        entries.forEach { interval ->
            LabelledRadioButton(
                label = stringResource(interval.labelRes),
                selected = interval == selectedInterval,
                onClick = { onIntervalSelect(interval) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
        }
    }
}