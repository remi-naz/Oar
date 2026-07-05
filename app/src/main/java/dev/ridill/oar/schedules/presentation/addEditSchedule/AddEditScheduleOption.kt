package dev.ridill.oar.schedules.presentation.addEditSchedule

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.ridill.oar.R

enum class AddEditScheduleOption(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int
) {
    DELETE(
        labelRes = R.string.delete,
        iconRes = R.drawable.ic_outlined_delete
    )
}
