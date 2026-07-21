package dev.ridill.oar.core.ui.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Transaction add/edit nav result
enum class AddEditTxResult {
    TRANSACTION_DELETED,
    TRANSACTION_SAVED,
}

// Schedule add/edit nav result
enum class AddEditScheduleResult {
    SCHEDULE_SAVED,
    SCHEDULE_DELETED
}

// Folder nav results
@Parcelize data class FolderSelectedResult(val id: Long) : Parcelable
@Parcelize data class FolderSavedResult(val id: Long) : Parcelable
@Parcelize data object FolderDeletedResult : Parcelable

// Cycle selection result
@Parcelize data class CycleSelectedResult(val id: Long?) : Parcelable

// Tag nav results
@Parcelize data class TagSelectedResult(val id: Long?) : Parcelable
@Parcelize data class TagSavedResult(val id: Long) : Parcelable

// Budget update result
@Parcelize data object BudgetUpdatedResult : Parcelable

// Backup encryption result
@Parcelize data object EncryptionPasswordUpdatedResult : Parcelable
