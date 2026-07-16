package dev.ridill.oar.schedules.data.local

import dev.ridill.oar.core.data.db.KeysetPagingSource
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.data.db.PageLoadDirection
import dev.ridill.oar.schedules.data.local.entity.ScheduleEntity
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDate

/**
 * Keyset-paginated alternative to Room's generated OFFSET-based PagingSource, mirroring
 * TransactionPagingSource so cost shrinks with the remaining unseen rows instead of staying
 * constant on every page load.
 */
class SchedulePagingSource(
    private val dao: SchedulesDao,
    db: OarDatabase,
    applicationScope: CoroutineScope,
    private val dateNow: LocalDate
) : KeysetPagingSource<SchedulePageKey, ScheduleEntity>(
    db = db,
    applicationScope = applicationScope,
    invalidationTables = setOf("schedules_table")
) {

    override suspend fun fetch(
        cursor: SchedulePageKey?,
        direction: PageLoadDirection,
        loadSize: Int
    ): List<ScheduleEntity> {
        val rawQuery = SchedulePagedQueryBuilder.build(
            dateNow = dateNow,
            cursor = cursor,
            direction = direction,
            limit = loadSize
        )
        return dao.getSchedulesPagedRaw(rawQuery)
    }

    override fun keyOf(value: ScheduleEntity): SchedulePageKey = value.toPageKey(dateNow)
}
