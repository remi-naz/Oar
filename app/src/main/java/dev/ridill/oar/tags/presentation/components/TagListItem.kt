package dev.ridill.oar.tags.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.ExcludedIconSmall
import dev.ridill.oar.core.ui.components.ListItemLeadingContentContainer
import dev.ridill.oar.core.ui.theme.elevation
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.exclusionGraphicsLayer

@Composable
fun TagListItem(
    name: String,
    color: Color,
    excluded: Boolean,
    createdTimestamp: String,
    modifier: Modifier = Modifier,
    elevation: ListItemElevation = ListItemDefaults.elevation()
) {
    ListItem(
        modifier = modifier
            .exclusionGraphicsLayer(excluded),
        leadingContent = {
            ListItemLeadingContentContainer(
                tonalElevation = MaterialTheme.elevation.level0
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_tag),
                    contentDescription = null,
                    tint = color
                )
            }
        },
        supportingContent = {
            Text(
                text = stringResource(
                    R.string.created_colon_timestamp_value,
                    createdTimestamp
                )
            )
        },
        elevation = elevation,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            if (excluded) {
                ExcludedIconSmall()
            }
            Text(name)
        }
    }
}

@Composable
fun TagListItem(
    onClick: () -> Unit,
    name: String,
    color: Color,
    excluded: Boolean,
    createdTimestamp: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    elevation: ListItemElevation = ListItemDefaults.elevation(),
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
) {
    ListItem(
        onClick = onClick,
        modifier = modifier
            .exclusionGraphicsLayer(excluded),
        leadingContent = {
            ListItemLeadingContentContainer(
                tonalElevation = MaterialTheme.elevation.level0
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_tag),
                    contentDescription = null,
                    tint = color
                )
            }
        },
        supportingContent = {
            Text(
                text = stringResource(
                    R.string.created_colon_timestamp_value,
                    createdTimestamp
                )
            )
        },
        selected = selected,
        elevation = elevation,
        onLongClick = onLongClick,
        onLongClickLabel = onLongClickLabel,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            if (excluded) {
                ExcludedIconSmall()
            }
            Text(name)
        }
    }
}