package dev.ridill.oar.folders.presentation.allFolders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.times
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.ExcludedIconSmall
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.spacing
import kotlin.math.roundToInt

@Composable
internal fun FolderCard(
    name: String,
    created: String,
    excluded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nameStyle = MaterialTheme.typography.titleMedium
    val createdDateStyle = MaterialTheme.typography.bodySmall.copy(
        color = LocalContentColor.current
            .copy(alpha = ContentAlpha.SUB_CONTENT)
    )

    val folderContentDescription = stringResource(
        R.string.cd_folder_list_item,
        name,
        created
    )

    val border = CardDefaults.outlinedCardBorder()
    val cardShape = CardDefaults.shape
    val cardColors = CardDefaults.outlinedCardColors()
    val folderLip = @Composable {
        val lipAccent = MaterialTheme.colorScheme.tertiary
            .copy(alpha = ContentAlpha.PERCENT_08)
        Box(
            modifier = Modifier
                .height(FolderLipHeight * 2)
                .drawWithCache {
                    val contentOutline = cardShape
                        .createOutline(
                            size = size.times(ScaleFactor(scaleX = 1f, scaleY = 2f)),
                            layoutDirection = layoutDirection,
                            density = Density(density)
                        )
                    onDrawBehind {
                        drawOutline(
                            outline = contentOutline,
                            color = lipAccent,
                            style = Fill
                        )
                        drawOutline(
                            outline = contentOutline,
                            brush = border.brush,
                            style = Stroke(border.width.toPx())
                        )
                    }
                }
                .layoutId(FolderLip)
        )
    }
    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                )
                .drawWithCache {
                    val contentOutline = cardShape
                        .createOutline(
                            size = size,
                            layoutDirection = layoutDirection,
                            density = Density(density)
                        )
                    onDrawBehind {
                        drawOutline(
                            outline = contentOutline,
                            color = cardColors.containerColor,
                            style = Fill
                        )
                        drawOutline(
                            outline = contentOutline,
                            brush = border.brush,
                            style = Stroke(border.width.toPx())
                        )
                    }
                }
                .padding(MaterialTheme.spacing.medium)
                .layoutId(FolderContent)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                if (excluded) {
                    ExcludedIconSmall()
                }
                Text(
                    text = name,
                    style = nameStyle,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = created,
                style = createdDateStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    Layout(
        content = {
            folderLip()
            content()
        },
        modifier = modifier
            .semantics(true) {
                contentDescription = folderContentDescription
            },
        measurePolicy = rememberMeasurePolicy()
    )
}

private const val FolderContent = "FolderContent"
private const val FolderLip = "FolderLip"
private val FolderLipHeight = 12.dp
private const val LIP_WIDTH_FRACTION = 0.40f

private class FolderCardMeasurePolicy : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints
    ): MeasureResult {
        val content = measurables
            .fastFirst { it.layoutId == FolderContent }
            .measure(constraints)
        val lipWidth = (content.width * LIP_WIDTH_FRACTION).roundToInt()
        val lip = measurables
            .fastFirst { it.layoutId == FolderLip }
            .measure(
                constraints.copy(
                    minWidth = lipWidth,
                    maxWidth = lipWidth,
                )
            )

        val width = content.width
        val height = content.height + (lip.height / 2)
        return layout(width = width, height = height) {
            lip.place(0, 0)
            content.place(0, lip.height / 2)
        }
    }
}

@Composable
private fun rememberMeasurePolicy(): MeasurePolicy = remember {
    FolderCardMeasurePolicy()
}