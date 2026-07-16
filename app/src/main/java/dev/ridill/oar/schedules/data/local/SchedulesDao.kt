package dev.ridill.oar.schedules.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import dev.ridill.oar.core.data.db.BaseDao
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.schedules.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface SchedulesDao : BaseDao<ScheduleEntity> {

    @RawQuery
    suspend fun getSchedulesPagedRaw(query: RoomRawQuery): List<ScheduleEntity>

    @Query("SELECT * FROM schedules_table WHERE id = :id")
    suspend fun getScheduleById(id: Long): ScheduleEntity?

    @Query("SELECT * FROM schedules_table WHERE DATETIME(next_payment_timestamp) > DATETIME(:timestamp)")
    suspend fun getAllSchedulesAfterTimestamp(timestamp: LocalDateTime): List<ScheduleEntity>

    @Query("SELECT MIN(DATETIME(timestamp)) FROM transaction_table WHERE schedule_id = :id")
    suspend fun getMinTxTimestampForSchedule(id: Long): LocalDateTime?

    @Query("SELECT MAX(DATETIME(timestamp)) FROM transaction_table WHERE schedule_id = :id")
    suspend fun getMaxTxTimestampForSchedule(id: Long): LocalDateTime?

    @Query(
        """
        SELECT *
        FROM schedules_table
        WHERE strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', next_payment_timestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :date)
        ORDER BY DATETIME(next_payment_timestamp) ASC
    """
    )
    fun getSchedulesActiveAtMonth(date: LocalDate): Flow<List<ScheduleEntity>>

    @Query("DELETE FROM schedules_table WHERE id IN (:ids)")
    suspend fun deleteSchedulesById(ids: Set<Long>)
}