package dev.ridill.oar.schedules.data.repository

import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.folders.domain.repository.FolderDetailsRepository
import dev.ridill.oar.schedules.domain.model.Schedule
import dev.ridill.oar.schedules.domain.repository.AddEditScheduleRepository
import dev.ridill.oar.schedules.domain.repository.SchedulesRepository
import dev.ridill.oar.transactions.data.local.TransactionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import kotlin.math.roundToLong

class AddEditScheduleRepositoryImpl(
    private val schedulesRepo: SchedulesRepository,
    private val transactionDao: TransactionDao,
    private val folderRepo: FolderDetailsRepository
) : AddEditScheduleRepository {

    override suspend fun getScheduleById(id: Long): Schedule? = withContext(Dispatchers.IO) {
        schedulesRepo.getScheduleById(id)?.let { schedule ->
            val nextPaymentTimestamp = schedule.nextPaymentTimestamp
                ?: schedule.lastPaymentTimestamp?.let {
                    schedulesRepo.calculateNextPaymentTimestampFromDate(
                        anchor = it,
                        repetition = schedule.repetition,
                    )
                }

            schedule.copy(
                nextPaymentTimestamp = nextPaymentTimestamp
            )
        }
    }

    override fun getAmountRecommendations(): Flow<List<Long>> = transactionDao
        .getTransactionAmountRange()
        .mapLatest { (upperLimit, lowerLimit) ->
            val roundedUpper = ((upperLimit.roundToLong() / 10) * 10)
                .coerceAtLeast(RANGE_MIN_VALUE)
            val roundedLower = ((lowerLimit.roundToLong() / 10) * 10)
                .coerceAtLeast(RANGE_MIN_VALUE)

            val range = roundedUpper - roundedLower

            if (range == 0L) buildList {
                repeat(3) {
                    add(RANGE_MIN_VALUE * (it + 1))
                }
            }
            else listOf(roundedLower, roundedLower + (range / 2), roundedUpper)
        }

    override suspend fun saveSchedule(
        schedule: Schedule,
        setReminder: Boolean
    ) = schedulesRepo.saveSchedule(
        schedule = schedule,
        setReminder = setReminder
    )

    override suspend fun deleteSchedule(id: Long) = schedulesRepo.deleteScheduleById(id)

    override fun getFolderNameForId(id: Long?): Flow<String?> = folderRepo
        .getFolderDetailsById(id ?: OarDatabase.INVALID_ID_LONG)
        .mapLatest { it?.name }
        .distinctUntilChanged()
}

private const val RANGE_MIN_VALUE = 50L
