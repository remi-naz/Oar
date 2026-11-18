package dev.ridill.oar.settings.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import dev.ridill.oar.core.domain.util.One
import dev.ridill.oar.core.ui.components.HorizontalSpacer
import dev.ridill.oar.core.ui.theme.spacing

@Composable
fun SimpleSettingsPreference(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PreferenceContentPadding,
    contentColor: Color = LocalContentColor.current
) = SimpleSettingsPreference(
    title = stringResource(titleRes),
    modifier = modifier,
    summary = summary,
    onClick = onClick,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    contentPadding = contentPadding,
    contentColor = contentColor
)

@Composable
fun SimpleSettingsPreference(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PreferenceContentPadding,
    contentColor: Color = LocalContentColor.current
) = BasicPreference(
    titleContent = { Text(text = title) },
    summaryContent = summary?.let {
        { Text(text = it) }
    },
    leadingIcon = leadingIcon?.let {
        { PreferenceIcon(imageVector = it) }
    },
    trailingContent = trailingIcon?.let {
        { PreferenceIcon(imageVector = it) }
    },
    modifier = Modifier
        .fillMaxWidth()
        .clickable(
            onClick = { onClick?.invoke() },
            enabled = onClick != null
        )
        .then(modifier),
    contentPadding = contentPadding,
    contentColor = contentColor
)

@Composable
fun SimplePreference(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PreferenceContentPadding
) = BasicPreference(
    titleContent = { Text(text = stringResource(titleRes)) },
    summaryContent = summary?.let {
        { Text(text = it) }
    },
    leadingIcon = leadingIcon,
    modifier = Modifier
        .fillMaxWidth()
        .clickable(
            onClick = { onClick?.invoke() },
            enabled = onClick != null
        )
        .then(modifier),
    trailingContent = trailingIcon,
    contentPadding = contentPadding
)

@Composable
fun SimplePreference(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PreferenceContentPadding
) = BasicPreference(
    titleContent = { Text(text = title) },
    summaryContent = summary?.let {
        { Text(text = it) }
    },
    leadingIcon = leadingIcon,
    modifier = Modifier
        .fillMaxWidth()
        .clickable(
            onClick = { onClick?.invoke() },
            enabled = onClick != null
        )
        .then(modifier),
    trailingContent = trailingIcon,
    contentPadding = contentPadding
)

@Composable
fun SwitchPreference(
    @StringRes titleRes: Int,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PreferenceContentPadding
) = BasicPreference(
    titleContent = { Text(text = stringResource(titleRes)) },
    summaryContent = summary?.let {
        { Text(text = it) }
    },
    leadingIcon = leadingIcon,
    modifier = Modifier
        .fillMaxWidth()
        .toggleable(
            value = value,
            role = Role.Switch,
            onValueChange = onValueChange
        )
        .then(modifier),
    contentPadding = contentPadding,
    trailingContent = {
        Switch(
            checked = value,
            onCheckedChange = null,
            modifier = Modifier
                .clearAndSetSemantics {}
        )
    }
)

@Composable
fun BasicPreference(
    titleContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = MaterialTheme.shapes.extraSmall,
    summaryContent: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PreferenceContentPadding,
    titleTextStyle: TextStyle = MaterialTheme.typography.titleMedium,
    summaryTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    contentColor: Color = LocalContentColor.current,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Row(
            modifier = Modifier
                .clip(shape)
                .minimumInteractiveComponentSize()
                .then(modifier)
                .padding(contentPadding),
            verticalAlignment = verticalAlignment,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            leadingIcon?.invoke()
            Column(
                modifier = Modifier
                    .weight(Float.One)
            ) {
                ProvideTextStyle(titleTextStyle) {
                    titleContent()
                }
                summaryContent?.let { content ->
                    CompositionLocalProvider(
                        LocalTextStyle provides summaryTextStyle,
                        LocalContentColor provides LocalContentColor.current.copy(alpha = 0.64f)
                    ) {
                        content()
                    }
                }
            }
            trailingContent?.invoke()
        }
    }
}

@Composable
fun PreferenceIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) = Icon(
    imageVector = imageVector,
    contentDescription = null,
    tint = tint,
    modifier = Modifier
        .size(PreferenceIconSize)
        .then(modifier)
)

@Composable
fun EmptyPreferenceIconSpacer() = HorizontalSpacer(spacing = PreferenceIconSize)

val PreferenceIconSize = 24.dp
val PreferenceContentPadding
    @Composable
    @ReadOnlyComposable
    get() = PaddingValues(
        horizontal = MaterialTheme.spacing.medium,
        vertical = MaterialTheme.spacing.small
    )