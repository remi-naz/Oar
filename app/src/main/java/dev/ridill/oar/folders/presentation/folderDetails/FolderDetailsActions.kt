package dev.ridill.oar.folders.presentation.folderDetails

import dev.ridill.oar.folders.domain.model.FolderTransactionsMultiSelectionOption

interface FolderDetailsActions {
    fun onCycleSelect(id: Long)
    fun onDeleteClick()
    fun onDeleteDismiss()
    fun onDeleteFolderOnlyClick()
    fun onDeleteFolderAndTransactionsClick()
    fun onTransactionSwipeActionRevealed()
    fun onRemoveTransactionFromFolderClick(id: Long)
    fun onTransactionLongPress(id: Long)
    fun onTransactionSelectionChange(id: Long)
    fun onMultiSelectionModeDismiss()
    fun onMultiSelectionOptionDismiss()
    fun onMultiSelectionOptionsClick()
    fun onMultiSelectionOptionClick(option: FolderTransactionsMultiSelectionOption)
    fun onDeleteTransactionsDismiss()
    fun onDeleteTransactionsConfirm()
    fun onRemoveTransactionsFromFolderDismiss()
    fun onRemoveTransactionsFromFolderConfirm()
}