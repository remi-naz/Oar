package dev.ridill.oar.tags.presentation.allTags

import androidx.compose.material3.SearchBarValue

interface AllTagsActions {
    fun onTagLongPress(id: Long)
    fun onTagSelectionChange(id: Long)
    fun onMultiSelectionModeDismiss()
    fun onDeleteTagsClick()
    fun onDeleteDismiss()
    fun onDeleteConfirm()
    fun onSearchBarValueChange(value: SearchBarValue)
    fun onClearSearchQuery()
}