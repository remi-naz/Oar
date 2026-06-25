package dev.ridill.oar.tags.presentation.components

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.ridill.oar.core.ui.components.ExcludedIcon
import dev.ridill.oar.core.ui.theme.contentColor

@Composable
fun TagChip(
    name: String,
    color: Color,
    excluded: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) = FilterChip(
    selected = selected,
    onClick = onClick,
    label = {
        Text(
            text = name,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    },
    trailingIcon = trailingIcon,
    colors = tagChipColors(color = color),
    modifier = Modifier
        .widthIn(max = TagChipMaxWidth)
        .then(modifier),
//        .exclusionGraphicsLayer(excluded),
    enabled = enabled,
    leadingIcon = if (excluded) {
        { ExcludedIcon() }
    } else null
)

@Composable
fun ElevatedTagChip(
    name: String,
    color: Color,
    excluded: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true
) = ElevatedFilterChip(
    selected = true,
    onClick = onClick,
    label = {
        Text(
            text = name,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    },
    colors = tagChipColors(color = color),
    modifier = Modifier
        .widthIn(max = TagChipMaxWidth)
        .then(modifier),
//        .exclusionGraphicsLayer(excluded),
    enabled = enabled,
    leadingIcon = if (excluded) {
        { ExcludedIcon() }
    } else null
)

private val TagChipMaxWidth = 150.dp

@Composable
private fun tagChipColors(color: Color): SelectableChipColors = FilterChipDefaults.filterChipColors(
    selectedContainerColor = color,
    selectedLabelColor = color.contentColor(),
    selectedLeadingIconColor = color.contentColor(),
    iconColor = color
)