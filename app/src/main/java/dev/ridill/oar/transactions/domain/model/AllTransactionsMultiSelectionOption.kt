package dev.ridill.oar.transactions.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.ridill.oar.R

enum class AllTransactionsMultiSelectionOption(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int
) {
    DELETE(
        iconRes = R.drawable.ic_outlined_delete,
        labelRes = R.string.action_delete
    ),
    CHANGE_CYCLE(
        iconRes = R.drawable.ic_outlined_cycle,
        labelRes = R.string.all_transactions_multi_selection_option_change_cycle
    ),
    ASSIGN_TAG(
        iconRes = R.drawable.ic_outlined_tag,
        labelRes = R.string.all_transactions_multi_selection_option_assign_tag
    ),
    REMOVE_TAG(
        iconRes = R.drawable.ic_outlined_tag_remove,
        labelRes = R.string.all_transactions_multi_selection_option_remove_tag
    ),
    EXCLUDE_FROM_EXPENDITURE(
        iconRes = R.drawable.ic_excluded,
        labelRes = R.string.all_transactions_multi_selection_option_mark_excluded
    ),
    INCLUDE_IN_EXPENDITURE(
        iconRes = R.drawable.ic_outlined_add_circle,
        labelRes = R.string.all_transactions_multi_selection_option_un_mark_excluded
    ),
    ADD_TO_FOLDER(
        iconRes = R.drawable.ic_outlined_folder_import,
        labelRes = R.string.all_transactions_multi_selection_option_add_to_folder
    ),
    REMOVE_FROM_FOLDERS(
        iconRes = R.drawable.ic_outlined_folder_export,
        labelRes = R.string.all_transactions_multi_selection_option_remove_from_folders
    ),
    AGGREGATE_TOGETHER(
        iconRes = R.drawable.ic_outlined_summation_circle,
        labelRes = R.string.all_transactions_multi_selection_option_aggregate_together
    )
}