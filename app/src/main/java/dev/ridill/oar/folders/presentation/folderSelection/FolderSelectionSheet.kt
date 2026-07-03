package dev.ridill.oar.folders.presentation.folderSelection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.ui.components.ExcludedIconSmall
import dev.ridill.oar.core.ui.components.ListItemLeadingContentContainer
import dev.ridill.oar.core.ui.components.ListSearchSheetContent
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.exclusionGraphicsLayer
import dev.ridill.oar.folders.domain.model.Folder

@Composable
fun FolderSelectionSheet(
    queryState: TextFieldState,
    foldersListLazyPagingItems: LazyPagingItems<Folder>,
    selectedId: Long?,
    onFolderSelect: (Long) -> Unit,
    onCreateNewClick: () -> Unit,
    onClearSelectionClick: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListSearchSheetContent(
        title = stringResource(R.string.select_folder),
        inputState = queryState,
        placeholder = stringResource(R.string.search_folder),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        additionalEndContent = {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) {
                Text(stringResource(R.string.action_confirm))
            }
        }
    ) {
        stickyHeader(
            key = "ClearSelection",
            contentType = "ClearSelection"
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) {
                TextButton(
                    onClick = onClearSelectionClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = stringResource(R.string.clear_selection),
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
        item(
            key = "CreateNewFolderItem",
            contentType = "CreateNewFolderItem"
        ) {
            NewFolderItem(
                onClick = onCreateNewClick,
                modifier = Modifier
                    .animateItem()
            )
        }
        items(
            count = foldersListLazyPagingItems.itemCount,
            key = foldersListLazyPagingItems.itemKey { it.id },
            contentType = foldersListLazyPagingItems.itemContentType { "FolderCard" }
        ) { index ->
            foldersListLazyPagingItems[index]?.let { folder ->
                FolderSelectionCard(
                    name = folder.name,
                    excluded = folder.excluded,
                    onClick = { onFolderSelect(folder.id) },
                    createdTimestamp = folder.createdTimestamp.format(DateUtil.Formatters.localizedDateMedium),
                    selected = folder.id == selectedId,
                    modifier = Modifier
                        .animateItem()
                )
            }
        }
    }
}

@Composable
private fun NewFolderItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.create_new_folder)) },
        trailingContent = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_folder_add),
                contentDescription = stringResource(R.string.cd_create_new_folder)
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = BottomSheetDefaults.ContainerColor
        ),
        tonalElevation = BottomSheetDefaults.Elevation,
        modifier = Modifier
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
            .then(modifier)
    )
}

@Composable
private fun FolderSelectionCard(
    name: String,
    excluded: Boolean,
    selected: Boolean,
    createdTimestamp: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                if (excluded) {
                    ExcludedIconSmall()
                }
                Text(
                    text = name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        supportingContent = { Text(createdTimestamp) },
        leadingContent = {
            ListItemLeadingContentContainer(
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_outlined_folder),
                    contentDescription = null
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (selected) MaterialTheme.colorScheme.surfaceContainerHigh
            else BottomSheetDefaults.ContainerColor
        ),
        modifier = Modifier
            .clickable(
                onClick = onClick,
                onClickLabel = stringResource(R.string.cd_tap_to_select_folder, name)
            )
            .exclusionGraphicsLayer(excluded)
            .then(modifier)
    )
}