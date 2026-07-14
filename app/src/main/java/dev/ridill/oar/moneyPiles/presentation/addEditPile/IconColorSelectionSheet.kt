package dev.ridill.oar.moneyPiles.presentation.addEditPile

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.HorizontalColorSelectionList
import dev.ridill.oar.core.ui.components.OarModalBottomSheet
import dev.ridill.oar.core.ui.components.SpacerMedium
import dev.ridill.oar.core.ui.components.SpacerSmall
import dev.ridill.oar.core.ui.theme.BorderWidthStandard
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.SelectableColorsList
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.moneyPiles.domain.model.PileIcon

@Composable
internal fun IconColorSelectionSheet(
    onDismissRequest: () -> Unit,
    selectedIcon: PileIcon,
    selectedColor: Color,
    onDoneClick: (PileIcon, Color) -> Unit,
    modifier: Modifier = Modifier,
    colorsList: List<Color> = remember { SelectableColorsList },
) {
    OarModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        IconColorSelectionSheetContent(
            selectedIcon = selectedIcon,
            selectedColor = selectedColor,
            onDoneClick = onDoneClick,
            modifier = modifier,
            colorsList = colorsList
        )
    }
}

@Composable
private fun IconColorSelectionSheetContent(
    selectedIcon: PileIcon,
    selectedColor: Color,
    onDoneClick: (PileIcon, Color) -> Unit,
    modifier: Modifier = Modifier,
    colorsList: List<Color> = remember { SelectableColorsList },
) {
    var iconState by remember(selectedIcon) { mutableStateOf(selectedIcon) }
    var colorState by remember(selectedColor) { mutableStateOf(selectedColor) }
    val animatedColor by animateColorAsState(colorState)
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val iconContainerShape = ContainerShape
        Box(
            modifier = Modifier
                .size(SelectionPreviewSize)
                .drawWithCache {
                    val outline = iconContainerShape
                        .createOutline(size, layoutDirection, Density(density))
                    onDrawBehind {
                        drawOutline(
                            outline = outline,
                            color = animatedColor,
                            style = Fill,
                            alpha = ContentAlpha.PERCENT_16
                        )

                        drawOutline(
                            outline = outline,
                            color = animatedColor,
                            style = Stroke(BorderWidthStandard.toPx()),
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(iconState.iconRes),
                contentDescription = stringResource(iconState.labelRes),
                tint = selectedColor,
                modifier = Modifier.size(32.dp)
            )
        }
        SpacerSmall()
        SectionLabel(stringResource(R.string.pile_icon_selection_label))
        FlowRow(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            PileIcon.entries.forEach { icon ->
                val selected = icon == iconState
                Surface(
                    onClick = { iconState = icon },
                    shape = MaterialTheme.shapes.medium,
                    color = if (selected) selectedColor.copy(alpha = ContentAlpha.PERCENT_32)
                    else MaterialTheme.colorScheme.surfaceContainerLow,
                    selected = selected,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(IconChipSize)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(icon.iconRes),
                            contentDescription = stringResource(icon.labelRes),
                            tint = if (selected) selectedColor
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        SpacerMedium()

        SectionLabel(text = stringResource(R.string.pile_color_selection_label))
        HorizontalColorSelectionList(
            onColorSelect = { colorState = it },
            selectedColorCode = { colorState.toArgb() },
            colorsList = colorsList
        )

        SpacerMedium()

        Button(
            onClick = { onDoneClick(iconState, colorState) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.action_done),
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val SelectionPreviewSize = 80.dp
private val ContainerShape: CornerBasedShape
    @Composable get() = MaterialTheme.shapes.medium

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.SUB_CONTENT),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
            .padding(bottom = MaterialTheme.spacing.small),
    )
}

private val IconChipSize = 40.dp

@PreviewLightDark
@Composable
private fun PreviewIconColorSelectionSheet() {
    OarTheme {
        Surface {
            IconColorSelectionSheetContent(
                selectedIcon = PileIcon.Travel,
                selectedColor = SelectableColorsList.first(),
                onDoneClick = { _, _ -> },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.spacing.medium)
            )
        }
    }
}
