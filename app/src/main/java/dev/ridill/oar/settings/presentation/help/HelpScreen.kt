package dev.ridill.oar.settings.presentation.help

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.core.net.toUri
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.BuildUtil
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.settings.presentation.components.SimpleSettingsPreference

@Composable
fun HelpScreen(
    snackbarController: SnackbarController,
    state: HelpState,
    actions: HelpActions,
    navigateUp: () -> Unit,
    launchUriInBrowser: (Uri) -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    OarScaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.destination_help)) },
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
                .padding(bottom = PaddingScrollEnd)
        ) {
            if (state.hasValidWebsiteUrl) {
                SimpleSettingsPreference(
                    title = stringResource(
                        R.string.preference_about,
                        stringResource(R.string.app_name)
                    ),
                    summary = stringResource(R.string.preference_about_summary),
                    leadingIcon = ImageVector.vectorResource(R.drawable.ic_outlined_globe),
                    trailingIcon = ImageVector.vectorResource(R.drawable.ic_rounded_link_square),
                    onClick = {
                        state.websiteUrl?.let { launchUriInBrowser(it.toUri()) }
                    }
                )
            }

            if (state.hasValidPrivacyPolicyUrl) {
                SimpleSettingsPreference(
                    titleRes = R.string.preference_privacy_policy,
                    summary = stringResource(R.string.preference_privacy_policy_summary),
                    leadingIcon = ImageVector.vectorResource(R.drawable.ic_outlined_contracts),
                    trailingIcon = ImageVector.vectorResource(R.drawable.ic_rounded_link_square),
                    onClick = {
                        state.privacyPolicyUrl?.let { launchUriInBrowser(it.toUri()) }
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
    }
}

@PreviewLightDark
@Composable
private fun PreviewHelpScreen() {
    OarTheme {
        HelpScreen(
            snackbarController = rememberSnackbarController(),
            state = HelpState(
                websiteUrl = "https://example.com",
                privacyPolicyUrl = "https://example.com/privacy"
            ),
            actions = object : HelpActions {
                override fun onExportLogsClick() {}
            },
            navigateUp = {},
            launchUriInBrowser = {}
        )
    }
}
