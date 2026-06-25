package dev.ridill.oar.tags.presentation.tagSelection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.ComponentViewModelScope
import dev.ridill.oar.core.ui.components.slideInVerticallyWithFadeIn
import dev.ridill.oar.core.ui.components.slideOutVerticallyWithFadeOut
import dev.ridill.oar.core.ui.theme.BorderWidthStandard
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.isNotEmpty
import dev.ridill.oar.tags.domain.model.Tag
import dev.ridill.oar.tags.domain.model.TagSelectionEntry
import dev.ridill.oar.tags.presentation.components.TagChip
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime

@Composable
fun TagSelectionField(
    selectedIds: Set<Long>,
    onSelectedIdsChange: (Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.tags),
    placeholder: String = stringResource(R.string.search_tag_placeholder),
    shape: Shape = MaterialTheme.shapes.medium,
) {
    if (LocalInspectionMode.current) return
    ComponentViewModelScope(key = "MultiTagSelectionField") {
        val viewModel: TagSelectionViewModel = hiltViewModel()
        val queryState = viewModel.searchQueryState
        val tagsLazyPagingItems = viewModel.tagsPagingData.collectAsLazyPagingItems()
        val state by viewModel.state.collectAsStateWithLifecycle()
        SideEffect {
            viewModel.updateSelectedIds(selectedIds)
        }

        CollectFlowEffect(viewModel.events) { event ->
            when (event) {
                is TagSelectionViewModel.TagSelectionEvent.TagSelectionChange -> {
                    onSelectedIdsChange(event.ids)
                }
            }
        }

        TagSelectionField(
            queryState = queryState,
            selectedIds = state.selectedIds,
            selectedTags = state.selectedTags,
            suggestedTags = tagsLazyPagingItems,
            onTagRemove = { viewModel.onTagRemove(it, false) },
            onTagSelect = { viewModel.onTagSelect(it, false) },
            onNewTagIndicatorClick = { viewModel.onNewTagClick(it, false) },
            modifier = modifier,
            label = label,
            placeholder = placeholder,
            shape = shape,
        )
    }
}

/**
 * Overload of [TagSelectionField] for single tag selection.
 */
@Composable
fun TagSelectionField(
    selectedId: Long?,
    onSelectedIdChange: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.tags),
    placeholder: String = stringResource(R.string.search_tag_placeholder),
    shape: Shape = MaterialTheme.shapes.medium,
) {
    if (LocalInspectionMode.current) return
    ComponentViewModelScope(key = "SingleTagSelectionField") {
        val viewModel: TagSelectionViewModel = hiltViewModel()
        val queryState = viewModel.searchQueryState
        val tagsLazyPagingItems = viewModel.tagsPagingData.collectAsLazyPagingItems()
        val state by viewModel.state.collectAsStateWithLifecycle()
        SideEffect {
            viewModel.updateSelectedIds(selectedId?.let { setOf(it) }.orEmpty())
        }

        CollectFlowEffect(viewModel.events, viewModel) { event ->
            when (event) {
                is TagSelectionViewModel.TagSelectionEvent.TagSelectionChange -> {
                    onSelectedIdChange(event.ids.firstOrNull())
                }
            }
        }

        TagSelectionField(
            queryState = queryState,
            selectedIds = state.selectedIds,
            selectedTags = state.selectedTags,
            suggestedTags = tagsLazyPagingItems,
            onTagRemove = { viewModel.onTagRemove(it, true) },
            onTagSelect = { viewModel.onTagSelect(it, true) },
            onNewTagIndicatorClick = { viewModel.onNewTagClick(it, true) },
            modifier = modifier,
            label = label,
            placeholder = placeholder,
            shape = shape,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TagSelectionField(
    queryState: TextFieldState,
    selectedIds: Set<Long>,
    selectedTags: List<Tag>,
    suggestedTags: LazyPagingItems<TagSelectionEntry>,
    onTagRemove: (Long) -> Unit,
    onTagSelect: (Long) -> Unit,
    onNewTagIndicatorClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.tags),
    placeholder: String = stringResource(R.string.search_tag_placeholder),
    shape: Shape = MaterialTheme.shapes.medium,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.bodySmall
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline

    val labelMeasured = remember(label, labelStyle) {
        textMeasurer.measure(label, labelStyle)
    }
    val labelHalfHeightDp = with(LocalDensity.current) {
        (labelMeasured.size.height / 2).toDp()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = OutlinedTextFieldDefaults.MinHeight)
                .tagSelectionContainer(
                    shape = shape,
                    borderColor = borderColor,
                    borderWidth = BorderWidthStandard,
                    labelMeasured = labelMeasured,
                    labelColor = labelColor,
                ),
        ) {
            val contentColor = LocalContentColor.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = labelHalfHeightDp + MaterialTheme.spacing.small,
                        start = MaterialTheme.spacing.medium,
                        end = MaterialTheme.spacing.medium,
                        bottom = MaterialTheme.spacing.small
                    ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
            ) {
                val showSelectedTags by remember(selectedTags) {
                    derivedStateOf { selectedTags.isNotEmpty() }
                }
                AnimatedVisibility(
                    visible = showSelectedTags,
                    enter = slideInVerticallyWithFadeIn { -it },
                    exit = slideOutVerticallyWithFadeOut { -it }
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                    ) {
                        selectedTags.forEach { tag ->
                            TagChip(
                                name = tag.name,
                                color = tag.color,
                                excluded = tag.excluded,
                                selected = tag.id in selectedIds,
                                onClick = { onTagRemove(tag.id) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(InputChipDefaults.IconSize)
                                    )
                                },
                            )
                        }
                    }
                }

                BasicTextField(
                    state = queryState,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium
                        .copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = remember { SolidColor(contentColor) },
                    decorator = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (queryState.text.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        val showSuggestions by remember {
            derivedStateOf { suggestedTags.isNotEmpty() }
        }
        AnimatedVisibility(
            visible = showSuggestions,
            enter = slideInVerticallyWithFadeIn { it },
            exit = slideOutVerticallyWithFadeOut { it }
        ) {
            Column {
                Text(
                    text = stringResource(R.string.suggested).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                ) {
                    repeat(suggestedTags.itemCount) { index ->
                        suggestedTags[index]?.let { tag ->
                            when (tag) {
                                is TagSelectionEntry.NewTagIndicator -> {
                                    AssistChip(
                                        onClick = { onNewTagIndicatorClick(tag.label) },
                                        label = {
                                            Text(
                                                stringResource(
                                                    R.string.create_colon_label,
                                                    tag.label
                                                )
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }

                                is TagSelectionEntry.Tag -> {
                                    SuggestionChip(
                                        onClick = { onTagSelect(tag.id) },
                                        label = { Text(tag.name) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.tagSelectionContainer(
    shape: Shape,
    borderColor: Color,
    borderWidth: Dp,
    labelMeasured: TextLayoutResult,
    labelColor: Color,
    labelStartOffset: Dp = 16.dp,
    labelHorizontalPadding: Dp = 4.dp,
): Modifier = drawWithContent {
    val bw = borderWidth.toPx()
    val lh = labelMeasured.size.height.toFloat()
    val lhPad = labelHorizontalPadding.toPx()
    val lw = labelMeasured.size.width.toFloat() + lhPad * 2f
    val lx = labelStartOffset.toPx()
    val rx = lx + lw
    val borderTop = lh / 2f

    val cr = (shape as? CornerBasedShape)
        ?.topStart
        ?.toPx(Size(size.width, size.height - borderTop), this)
        ?: 0f
    val safeRadius = cr.coerceAtMost(minOf(size.width, size.height - borderTop) / 2f)

    drawContent()

    val path = Path().apply {
        moveTo(rx, borderTop)
        lineTo(size.width - safeRadius, borderTop)
        if (safeRadius > 0f) {
            arcTo(
                rect = Rect(
                    size.width - 2 * safeRadius, borderTop,
                    size.width, borderTop + 2 * safeRadius
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
        }
        lineTo(size.width, size.height - safeRadius)
        if (safeRadius > 0f) {
            arcTo(
                rect = Rect(
                    size.width - 2 * safeRadius, size.height - 2 * safeRadius,
                    size.width, size.height
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
        }
        lineTo(safeRadius, size.height)
        if (safeRadius > 0f) {
            arcTo(
                rect = Rect(0f, size.height - 2 * safeRadius, 2 * safeRadius, size.height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
        }
        lineTo(0f, borderTop + safeRadius)
        if (safeRadius > 0f) {
            arcTo(
                rect = Rect(0f, borderTop, 2 * safeRadius, borderTop + 2 * safeRadius),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
        }
        lineTo(lx, borderTop)
    }

    drawPath(path = path, color = borderColor, style = Stroke(width = bw))
    drawText(
        textLayoutResult = labelMeasured,
        color = labelColor,
        topLeft = Offset(lx + lhPad, 0f)
    )
}

@PreviewLightDark
@Composable
private fun PreviewTagSelectionField() {
    OarTheme {
        Surface {
            TagSelectionField(
                queryState = rememberTextFieldState(),
                selectedIds = setOf(1L),
                selectedTags = listOf(
                    Tag(
                        id = 1L,
                        name = LoremIpsum(1).values.joinToString(),
                        colorCode = Color.Red.toArgb(),
                        createdTimestamp = LocalDateTime.now(),
                        excluded = false
                    )
                ),
                suggestedTags = flowOf(
                    PagingData.from(
                        listOf(
                            TagSelectionEntry.Tag(
                                id = 2L,
                                name = LoremIpsum(1).values.joinToString(),
                                colorCode = Color.Red.toArgb(),
                                createdTimestamp = LocalDateTime.now(),
                                excluded = false
                            ) as TagSelectionEntry
                        )
                    )
                ).collectAsLazyPagingItems(),
                onTagSelect = {},
                onTagRemove = {},
                onNewTagIndicatorClick = {},
                placeholder = "Select Tag",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
