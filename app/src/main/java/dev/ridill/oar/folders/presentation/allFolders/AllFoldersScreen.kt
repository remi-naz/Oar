package dev.ridill.oar.folders.presentation.allFolders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.EmptyListIndicator
import dev.ridill.oar.core.ui.components.ExcludedIndicatorSmall
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.navigation.destinations.AllFoldersScreenSpec
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.exclusionGraphicsLayer
import dev.ridill.oar.core.ui.util.isEmpty
import dev.ridill.oar.core.ui.util.mergedContentDescription
import dev.ridill.oar.folders.domain.model.Folder
import kotlinx.coroutines.flow.flowOf
import kotlin.random.Random

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AllFoldersScreen(
    snackbarController: SnackbarController,
    foldersPagingItems: LazyPagingItems<Folder>,
    navigateToAddFolder: () -> Unit,
    navigateToFolderDetails: (Long) -> Unit,
    navigateUp: () -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val areFoldersListEmpty by remember {
        derivedStateOf { foldersPagingItems.isEmpty() }
    }
    OarScaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(AllFoldersScreenSpec.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            FloatingActionButton(onClick = navigateToAddFolder) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_folder_add),
                    contentDescription = stringResource(R.string.cd_new_folder)
                )
            }
        },
        snackbarController = snackbarController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                if (areFoldersListEmpty) {
                    EmptyListIndicator(
                        rawResId = R.raw.lottie_empty_list_ghost,
                        messageRes = R.string.folders_list_empty_message
                    )
                }
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = MaterialTheme.spacing.medium,
                        bottom = PaddingScrollEnd,
                        start = MaterialTheme.spacing.medium,
                        end = MaterialTheme.spacing.medium
                    ),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                    verticalItemSpacing = MaterialTheme.spacing.small
                ) {
                    repeat(foldersPagingItems.itemCount) { index ->
                        foldersPagingItems[index]?.let { item ->
                            item(
                                key = item.id,
                                contentType = "FolderCard"
                            ) {
                                FolderCard(
                                    name = item.name,
                                    created = item.createdTimestamp.format(DateUtil.Formatters.localizedDateMedium),
                                    excluded = item.excluded,
                                    onClick = { navigateToFolderDetails(item.id) },
                                    modifier = Modifier
                                        .animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderCard(
    name: String,
    created: String,
    excluded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nameStyle = MaterialTheme.typography.titleMedium
    val createdDateStyle = MaterialTheme.typography.bodySmall.copy(
        color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
    )

    val folderContentDescription = stringResource(
        R.string.cd_folder_list_item,
        name,
        created
    )

    val border = CardDefaults.outlinedCardBorder()

    val folderShape = remember { FolderShape() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawOutline(
                    outline = folderShape.createOutline(size, layoutDirection, Density(density)),
                    brush = border.brush,
                    style = Stroke(border.width.toPx())
                )
            }
            .clip(folderShape)
            .clickable(onClick = onClick)
            .padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.extraLarge
            )
            .mergedContentDescription(folderContentDescription)
            .exclusionGraphicsLayer(excluded),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                if (excluded) {
                    ExcludedIndicatorSmall()
                }
                Text(
                    text = name,
                    style = nameStyle,
                    maxLines = 2,
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
}

private class FolderShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = Outline.Generic(
        path = Path().apply {
            val scaleX = size.width / 24f
            val scaleY = size.height / 24f

            moveTo(10f * scaleX, 4f * scaleY)
            lineTo(4f * scaleX, 4f * scaleY)
            cubicTo(
                2.9f * scaleX,
                4f * scaleY,
                2.01f * scaleX,
                4.9f * scaleY,
                2.01f * scaleX,
                6f * scaleY
            )
            lineTo(2f * scaleX, 18f * scaleY)
            cubicTo(
                2f * scaleX,
                19.1f * scaleY,
                2.9f * scaleX,
                20f * scaleY,
                4f * scaleX,
                20f * scaleY
            )
            lineTo(20f * scaleX, 20f * scaleY)
            cubicTo(
                21.1f * scaleX,
                20f * scaleY,
                22f * scaleX,
                19.1f * scaleY,
                22f * scaleX,
                18f * scaleY
            )
            lineTo(22f * scaleX, 8f * scaleY)
            cubicTo(
                22f * scaleX,
                6.9f * scaleY,
                21.1f * scaleX,
                6f * scaleY,
                20f * scaleX,
                6f * scaleY
            )
            lineTo(12f * scaleX, 6f * scaleY)
            lineTo(10f * scaleX, 4f * scaleY)
            close()
        }
    )
}

@PreviewLightDark
@Composable
private fun PreviewAllFoldersScreen() {
    OarTheme {
        AllFoldersScreen(
            snackbarController = rememberSnackbarController(),
            foldersPagingItems = flowOf(
                PagingData.from(
                    List(5) {
                        Folder(
                            id = it.toLong(),
                            name = "Folder $it",
                            createdTimestamp = DateUtil.now(),
                            excluded = Random.nextBoolean()
                        )
                    }
                )).collectAsLazyPagingItems(),
            navigateToAddFolder = {},
            navigateToFolderDetails = {},
            navigateUp = {}
        )
    }
}