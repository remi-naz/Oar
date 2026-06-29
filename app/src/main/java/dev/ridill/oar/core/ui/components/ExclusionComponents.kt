package dev.ridill.oar.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.theme.spacing

@Composable
fun ExcludedIcon(
    modifier: Modifier = Modifier,
    tint: Color = DefaultTint,
) {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.ic_excluded),
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .defaultMinSize(
                minWidth = DefaultIndicatorSize,
                minHeight = DefaultIndicatorSize,
            )
    )
}

@Composable
fun ExcludedIconSmall(
    modifier: Modifier = Modifier,
    tint: Color = DefaultTint,
) = ExcludedIcon(
    modifier = modifier
        .size(DefaultIndicatorSizeSmall),
    tint = tint
)

private val DefaultIndicatorSize = 16.dp
private val DefaultIndicatorSizeSmall = 14.dp
private val DefaultTint: Color
    @Composable get() = MaterialTheme.colorScheme.error

@Composable
fun MarkExcludedSwitch(
    excluded: Boolean,
    onToggle: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = Modifier
            .toggleable(
                value = excluded,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = onToggle != null,
                role = Role.Switch,
                onValueChange = { onToggle?.invoke(it) }
            )
            .then(modifier)
    ) {
        ExcludedIcon(
            modifier = Modifier
                .size(SwitchDefaults.IconSize)
        )
        Text(
            text = stringResource(R.string.mark_excluded_question),
            style = style
        )
        Switch(
            checked = excluded,
            onCheckedChange = null
        )
    }
}