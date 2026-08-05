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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.WhiteSpace
import dev.ridill.oar.core.ui.components.BooleanPreviewParameterProvider
import dev.ridill.oar.core.ui.components.ExcludedIconSmall
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.exclusionGraphicsLayer
import dev.ridill.oar.core.ui.util.mergedContentDescription

@Composable
private fun LeadingContent(
    color: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_tag),
        contentDescription = null,
        tint = color,
        modifier = modifier
    )
}

@Composable
private fun OverlineContent(
    createdTimestamp: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(
            R.string.created_colon_timestamp_value,
            createdTimestamp
        ),
        modifier = modifier
    )
}

@Composable
private fun Content(
    name: String,
    excluded: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = modifier
    ) {
        if (excluded) {
            ExcludedIconSmall()
        }
        Text(name)
    }
}

@Composable
private fun buildTagContentDesc(
    name: String,
    excluded: Boolean,
    createdTimestamp: String,
): String = buildString {
    append(stringResource(R.string.cd_tag_list_item, name, createdTimestamp))

    if (excluded) {
        append(",")
        append(String.WhiteSpace)
        append(stringResource(R.string.cd_excluded_append))
    }
}

@Composable
internal fun TagListItem(
    name: String,
    color: Color,
    excluded: Boolean,
    createdTimestamp: String,
    modifier: Modifier = Modifier,
    elevation: ListItemElevation = ListItemDefaults.elevation()
) {
    val tagContentDescription = buildTagContentDesc(
        name = name,
        excluded = excluded,
        createdTimestamp = createdTimestamp,
    )
    ListItem(
        modifier = modifier
            .mergedContentDescription(tagContentDescription)
            .exclusionGraphicsLayer(excluded),
        leadingContent = {
            LeadingContent(color = color)
        },
        overlineContent = {
            OverlineContent(createdTimestamp = createdTimestamp)
        },
        elevation = elevation,
    ) {
        Content(
            name = name,
            excluded = excluded
        )
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
    val tagContentDescription = buildTagContentDesc(
        name = name,
        excluded = excluded,
        createdTimestamp = createdTimestamp,
    )
    ListItem(
        onClick = onClick,
        modifier = modifier
            .mergedContentDescription(tagContentDescription)
            .exclusionGraphicsLayer(excluded),
        leadingContent = {
            LeadingContent(color = color)
        },
        overlineContent = {
            OverlineContent(createdTimestamp = createdTimestamp)
        },
        selected = selected,
        elevation = elevation,
        onLongClick = onLongClick,
        onLongClickLabel = onLongClickLabel,
    ) {
        Content(
            name = name,
            excluded = excluded
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewTagListItem(
    @PreviewParameter(BooleanPreviewParameterProvider::class) excluded: Boolean
) {
    OarTheme {
        TagListItem(
            name = LoremIpsum(2).values.joinToString(),
            color = Color.Red,
            excluded = excluded,
            createdTimestamp = DateUtil.dateNow().format(DateUtil.Formatters.localizedDateMedium)
        )
    }
}
