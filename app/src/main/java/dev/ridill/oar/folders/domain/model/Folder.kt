package dev.ridill.oar.folders.domain.model

import android.os.Parcelable
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.Empty
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class Folder(
    val id: Long,
    val name: String,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean,
    val archivedTimestamp: LocalDateTime? = null
) : Parcelable {
    val isArchived: Boolean get() = archivedTimestamp != null

    companion object {
        val NEW get() = Folder(
            id = OarDatabase.DEFAULT_ID_LONG,
            name = String.Empty,
            createdTimestamp = DateUtil.now(),
            excluded = false
        )
    }
}