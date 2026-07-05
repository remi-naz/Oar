package dev.ridill.oar.transactions.presentation.addEditTransaction

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.ridill.oar.R

enum class AddEditTxOption(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int
) {
    DELETE(
        labelRes = R.string.delete,
        iconRes = R.drawable.ic_outlined_delete
    ),
    CREATE_SCHEDULE_FROM_TRANSACTION(
        labelRes = R.string.create_schedule_from_transaction,
        iconRes = R.drawable.ic_rounded_calendar_entries
    ),
    DUPLICATE(
        labelRes = R.string.duplicate,
        iconRes = R.drawable.ic_outlined_duplicate
    ),
}