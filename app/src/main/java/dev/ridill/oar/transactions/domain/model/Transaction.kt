package dev.ridill.oar.transactions.domain.model

import android.os.Parcelable
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.core.domain.util.LocaleUtil
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.util.Currency

@Parcelize
data class Transaction(
    val id: Long,
    val amount: String,
    val note: String,
    val currency: Currency,
    val timestamp: LocalDateTime,
    val type: FundMovement,
    val tagId: Long?,
    val folderId: Long?,
    val scheduleId: Long?,
    val excluded: Boolean,
    val cycleId: Long
) : Parcelable {
    companion object {
        val DEFAULT = Transaction(
            id = OarDatabase.DEFAULT_ID_LONG,
            amount = String.Empty,
            note = String.Empty,
            timestamp = DateUtil.now(),
            type = FundMovement.OUT,
            currency = LocaleUtil.defaultCurrency,
            tagId = null,
            folderId = null,
            excluded = false,
            scheduleId = null,
            cycleId = OarDatabase.INVALID_ID_LONG
        )
    }
}