package dev.ridill.oar.core.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

const val INVALID_ID_LONG = -1L

// --- Onboarding ---
@Serializable
data object OnboardingRoute : NavKey

// --- Dashboard ---
@Serializable
data object DashboardRoute : NavKey

// --- Transactions ---
@Serializable
data class AddEditTransactionRoute(
    val transactionId: Long = INVALID_ID_LONG,
    val linkFolderId: Long? = null,
    val isDuplicateMode: Boolean = false,
) : NavKey

@Serializable
data object AllTransactionsRoute : NavKey

// --- Amount transformation (sheet launched from AddEditTransaction) ---
@Serializable
data object AmountTransformationSheetRoute : NavKey

// --- Folders ---
@Serializable
data object AllFoldersRoute : NavKey

@Serializable
data class FolderDetailsRoute(val folderId: Long) : NavKey

@Serializable
data class AddEditFolderSheetRoute(val folderId: Long = INVALID_ID_LONG) : NavKey

@Serializable
data class FolderSelectionSheetRoute(val preselectedId: Long = INVALID_ID_LONG) : NavKey

// --- Tags ---
@Serializable
data object AllTagsRoute : NavKey

@Serializable
data class AddEditTagSheetRoute(
    val tagId: Long = INVALID_ID_LONG,
    val prefilledName: String = "",
) : NavKey

@Serializable
data class TagSelectionSheetRoute(val preselectedId: Long = INVALID_ID_LONG) : NavKey

// --- Schedules ---
@Serializable
data object AllSchedulesRoute : NavKey

@Serializable
data class AddEditScheduleRoute(
    val scheduleId: Long = INVALID_ID_LONG,
    val inputs: ScheduleInputs? = null,
) : NavKey

// --- Settings ---
@Serializable
data object SettingsRoute : NavKey

@Serializable
data object UpdateBudgetSheetRoute : NavKey

@Serializable
data object BackupSettingsRoute : NavKey

@Serializable
data object BackupEncryptionRoute : NavKey

@Serializable
data object SecuritySettingsRoute : NavKey

@Serializable
data object BudgetCyclesRoute : NavKey

// --- Shared sheets ---
@Serializable
data class CurrencySelectionSheetRoute(val preSelectedCurrCode: String? = null) : NavKey

@Serializable
data class CycleSelectionSheetRoute(val preselectedId: Long = INVALID_ID_LONG) : NavKey
