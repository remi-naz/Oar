package dev.ridill.oar.schedules.domain.model

import android.os.Parcelable
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.domain.model.FundMovement
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.util.Currency

@Parcelize
data class Schedule(
    val id: Long,
    val amount: Double,
    val note: String?,
    val currency: Currency,
    val type: FundMovement,
    val tagId: Long?,
    val folderId: Long?,
    val repetition: ScheduleRepetition,
    val nextPaymentTimestamp: LocalDateTime?,
    val lastPaymentTimestamp: LocalDateTime?
) : Parcelable {
    companion object {
        val DEFAULT = Schedule(
            id = OarDatabase.DEFAULT_ID_LONG,
            amount = Double.Zero,
            note = null,
            currency = LocaleUtil.defaultCurrency,
            type = FundMovement.OUT,
            tagId = null,
            folderId = null,
            repetition = ScheduleRepetition.NO_REPEAT,
            nextPaymentTimestamp = null,
            lastPaymentTimestamp = null
        )
    }
}