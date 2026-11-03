package dev.ridill.oar.folders.presentation.allFolders

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
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.ui.components.BackArrowButton
import dev.ridill.oar.core.ui.components.EmptyListIndicator
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.isEmpty
import dev.ridill.oar.folders.domain.model.Folder
import dev.ridill.oar.folders.domain.model.FolderListItemUiModel
import kotlinx.coroutines.flow.flowOf
import kotlin.random.Random

@Composable
fun AllFoldersScreen(
    snackbarController: SnackbarController,
    foldersPagingItems: LazyPagingItems<FolderListItemUiModel>,
    showArchived: Boolean,
    archivedFoldersExist: Boolean,
    onShowArchivedToggle: (Boolean) -> Unit,
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
                title = { Text(stringResource(R.string.destination_all_folders)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                actions = {
                    var showOptionsMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.cd_more_options)
                        )
                    }
                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(
                                        if (showArchived) R.string.hide_archived
                                        else R.string.show_archived
                                    )
                                )
                            },
                            onClick = {
                                onShowArchivedToggle(!showArchived)
                                showOptionsMenu = false
                            }
                        )
                    }
                },
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
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    verticalItemSpacing = MaterialTheme.spacing.medium
                ) {
                    repeat(foldersPagingItems.itemCount) { index ->
                        when (val entry = foldersPagingItems[index]) {
                            FolderListItemUiModel.ArchivedSeparator -> {
                                item(
                                    key = "ArchivedSeparator",
                                    contentType = "ArchivedSeparator",
                                    span = StaggeredGridItemSpan.FullLine
                                ) {
                                    ArchivedSeparator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem()
                                    )
                                }
                            }

                            is FolderListItemUiModel.FolderItem -> {
                                val item = entry.folder
                                item(
                                    key = item.id,
                                    contentType = "FolderCard"
                                ) {
                                    FolderCard(
                                        name = item.name,
                                        created = item.createdTimestamp.format(DateUtil.Formatters.localizedDateMedium),
                                        excluded = item.excluded,
                                        archived = item.isArchived,
                                        onClick = { navigateToFolderDetails(item.id) },
                                        modifier = Modifier
                                            .animateItem()
                                    )
                                }
                            }

                            null -> Unit
                        }
                    }
                    if (archivedFoldersExist && !showArchived) {
                        item(
                            key = "ArchivedFoldersPresent",
                            contentType = "ArchivedFoldersPresent",
                            span = StaggeredGridItemSpan.FullLine
                        ) {
                            Text(
                                text = stringResource(R.string.archived_folders_present),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchivedSeparator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.archived),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small)
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@PreviewLightDark
@Composable
private fun PreviewAllFoldersScreen() {
    OarTheme {
        AllFoldersScreen(
            snackbarController = rememberSnackbarController(),
            foldersPagingItems = flowOf(
                PagingData.from(
                    List<FolderListItemUiModel>(5) {
                        FolderListItemUiModel.FolderItem(
                            Folder(
                                id = it.toLong(),
                                name = "Folder $it",
                                createdTimestamp = DateUtil.now(),
                                excluded = Random.nextBoolean()
                            )
                        )
                    }
                )).collectAsLazyPagingItems(),
            showArchived = false,
            archivedFoldersExist = true,
            onShowArchivedToggle = {},
            navigateToAddFolder = {},
            navigateToFolderDetails = {},
            navigateUp = {}
        )
    }
}