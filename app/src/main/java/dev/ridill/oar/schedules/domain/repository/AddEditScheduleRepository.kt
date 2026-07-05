package dev.ridill.oar.schedules.domain.repository

import dev.ridill.oar.schedules.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

interface AddEditScheduleRepository {
    suspend fun getScheduleById(id: Long): Schedule?
    fun getAmountRecommendations(): Flow<List<Long>>
    suspend fun saveSchedule(schedule: Schedule)
    suspend fun deleteSchedule(id: Long)
    fun getFolderNameForId(id: Long?): Flow<String?>
}
