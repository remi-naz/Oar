package dev.ridill.oar.folders.domain.model

sealed interface FolderListItemUiModel {
    data class FolderItem(val folder: Folder) : FolderListItemUiModel
    data object ArchivedSeparator : FolderListItemUiModel
}
