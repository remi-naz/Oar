package dev.ridill.oar.onboarding.presentation.components

import android.Manifest
import androidx.annotation.StringRes
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
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.One
import dev.ridill.oar.core.ui.components.DisplayMediumText
import dev.ridill.oar.core.ui.components.MultiplePermissionsState
import dev.ridill.oar.core.ui.components.SpacerMedium
import dev.ridill.oar.core.ui.components.icons.Message
import dev.ridill.oar.core.ui.theme.spacing

@Composable
fun PermissionsPage(
    permissionsState: MultiplePermissionsState,
    onGivePermissionClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val areAllPermissionsGranted by remember {
        derivedStateOf { permissionsState.areAllPermissionsGranted }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large),
    ) {
        DisplayMediumText(
            text = stringResource(R.string.onboarding_page_permissions_title),
            modifier = Modifier
                .padding(vertical = MaterialTheme.spacing.medium)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(Float.One)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            permissionsState.permissions.forEach { permission ->
                val icon = when (permission) {
                    Manifest.permission.POST_NOTIFICATIONS -> Icons.Rounded.Notifications
                    Manifest.permission.RECEIVE_SMS -> Icons.Rounded.Message
                    else -> null
                }
                val titleRes = when (permission) {
                    Manifest.permission.POST_NOTIFICATIONS -> R.string.permission_notification
                    Manifest.permission.RECEIVE_SMS -> R.string.permission_sms
                    else -> null
                }
                val messageRes = when (permission) {
                    Manifest.permission.POST_NOTIFICATIONS -> R.string.permission_rationale_notification
                    Manifest.permission.RECEIVE_SMS -> R.string.permission_rationale_read_sms
                    else -> null
                }
                if (icon != null && titleRes != null && messageRes != null) {
                    PermissionDetails(
                        icon = icon,
                        titleRes = titleRes,
                        message = stringResource(messageRes, stringResource(R.string.app_name)),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }

        if (!areAllPermissionsGranted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OnboardingTextButton(
                    onClick = onSkipClick,
                ) {
                    Text(stringResource(R.string.action_skip))
                }

                OnboardingButton(
                    onClick = onGivePermissionClick,
                ) {
                    Text(stringResource(R.string.give_permission))
                }
            }
        } else {
            OnboardingButton(
                onClick = onGivePermissionClick,
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                Text(stringResource(R.string.i_understand))
            }
        }
    }
}

@Composable
private fun PermissionDetails(
    icon: ImageVector,
    @StringRes titleRes: Int,
    message: String,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(
//        LocalContentColor provides MaterialTheme.colorScheme.onPrimary
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
            )

            SpacerMedium()

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}