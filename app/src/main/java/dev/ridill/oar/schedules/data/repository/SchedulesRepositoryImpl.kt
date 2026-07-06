package dev.ridill.oar.schedules.data.repository

import androidx.room.withTransaction
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.data.util.trySuspend
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.schedules.data.local.SchedulesDao
import dev.ridill.oar.schedules.data.local.entity.ScheduleEntity
import dev.ridill.oar.schedules.data.toEntity
import dev.ridill.oar.schedules.data.toSchedule
import dev.ridill.oar.schedules.domain.model.Schedule
import dev.ridill.oar.schedules.domain.model.ScheduleRepetition
import dev.ridill.oar.schedules.domain.repository.SchedulesRepository
import dev.ridill.oar.schedules.domain.scheduleReminder.ScheduleReminder
import dev.ridill.oar.transactions.data.local.TransactionDao
import dev.ridill.oar.transactions.data.local.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.Year

class SchedulesRepositoryImpl(
    private val db: OarDatabase,
    private val schedulesDao: SchedulesDao,
    private val transactionDao: TransactionDao,
    private val cycleRepo: BudgetCycleRepository,
    private val scheduler: ScheduleReminder,
) : SchedulesRepository {
    override suspend fun getScheduleById(
        id: Long
    ): Schedule? = withContext(Dispatchers.IO) {
        schedulesDao.getScheduleById(id)?.toSchedule()
    }

    override fun calculateNextPaymentTimestampFromDate(
        anchor: LocalDateTime,
        repetition: ScheduleRepetition,
        expectedTimestamp: LocalDateTime?
    ): LocalDateTime? = when (repetition) {
        ScheduleRepetition.NO_REPEAT -> null
        ScheduleRepetition.WEEKLY -> {
            val nextIntervalDateTime = anchor.plusWeeks(1)
            val dayCount = DayOfWeek.entries.size.toLong()
            val difference = nextIntervalDateTime.dayOfWeek.value % dayCount

            if (expectedTimestamp == null) return nextIntervalDateTime
            if (anchor.isAfter(expectedTimestamp)) nextIntervalDateTime.minusDays(difference)
            else nextIntervalDateTime.plusDays(difference)
        }

        ScheduleRepetition.MONTHLY -> {
            val nextIntervalDateTime = anchor.plusMonths(1)
            val isLeapYear = Year.isLeap(nextIntervalDateTime.year.toLong())
            val dayCount = anchor.month.length(isLeapYear)
            val difference = nextIntervalDateTime.dayOfMonth.toLong() % dayCount

            if (expectedTimestamp == null) return nextIntervalDateTime
            if (anchor.isAfter(expectedTimestamp)) nextIntervalDateTime.minusDays(difference)
            else nextIntervalDateTime.plusDays(difference)
        }

        ScheduleRepetition.BI_MONTHLY -> {
            val nextIntervalDateTime = anchor.plusMonths(2)
            val isLeapYear = Year.isLeap(nextIntervalDateTime.year.toLong())
            val dayCount = anchor.month.length(isLeapYear) + anchor.month.length(isLeapYear)
            val difference = nextIntervalDateTime.dayOfMonth.toLong() % dayCount

            if (expectedTimestamp == null) return nextIntervalDateTime
            if (anchor.isAfter(expectedTimestamp)) nextIntervalDateTime.minusDays(difference)
            else nextIntervalDateTime.plusDays(difference)
        }

        ScheduleRepetition.YEARLY -> {
            val nextIntervalDateTime = anchor.plusYears(1)
            val dayCount = Year.of(nextIntervalDateTime.year).length()
            val difference = nextIntervalDateTime.dayOfYear.toLong() % dayCount

            if (expectedTimestamp == null) return nextIntervalDateTime
            if (anchor.isAfter(expectedTimestamp)) nextIntervalDateTime.minusDays(difference)
            else nextIntervalDateTime.plusDays(difference)
        }
    }

    override fun calculateLastPaymentTimestampFromDate(
        anchor: LocalDateTime,
        repetition: ScheduleRepetition,
        expectedTimestamp: LocalDateTime?
    ): LocalDateTime? = when (repetition) {
        ScheduleRepetition.NO_REPEAT -> null
        ScheduleRepetition.WEEKLY -> {
            val prevIntervalDateTime = anchor.minusWeeks(1)
            val dayCount = DayOfWeek.entries.size.toLong()
            val difference = prevIntervalDateTime.dayOfWeek.value % dayCount

            if (expectedTimestamp == null) return prevIntervalDateTime
            if (anchor.isAfter(expectedTimestamp)) prevIntervalDateTime.minusDays(difference)
            else prevIntervalDateTime.plusDays(difference)
        }

        ScheduleRepetition.MONTHLY -> {
            val prevIntervalDateTime = anchor.minusMonths(1)
            val isLeapYear = Year.isLeap(prevIntervalDateTime.year.toLong())
            val dayCount = anchor.month.length(isLeapYear)
            val difference = prevIntervalDateTime.dayOfMonth.toLong() % dayCount

            if (expectedTimestamp == null) return prevIntervalDateTime
            if (anchor.isAfter(expectedTimestamp)) prevIntervalDateTime.minusDays(difference)
            else prevIntervalDateTime.plusDays(difference)
        }

        ScheduleRepetition.BI_MONTHLY -> {
            val prevIntervalDateTime = anchor.minusMonths(2)
            val isLeapYear = Year.isLeap(prevIntervalDateTime.year.toLong())
            val dayCount = anchor.month.length(isLeapYear) + anchor.month.length(isLeapYear)
            val difference = prevIntervalDateTime.dayOfMonth.toLong() % dayCount

            if (expectedTimestamp == null) return prevIntervalDateTime
            if (anchor.isAfter(expectedTimestamp)) prevIntervalDateTime.minusDays(difference)
            else prevIntervalDateTime.plusDays(difference)
            anchor.minusMonths(2)
        }

        ScheduleRepetition.YEARLY -> {
            val prevIntervalDateTime = anchor.minusYears(1)
            val dayCount = Year.of(prevIntervalDateTime.year).length()
            val difference = prevIntervalDateTime.dayOfYear.toLong() % dayCount

            if (expectedTimestamp == null) return prevIntervalDateTime
            if (anchor.isAfter(expectedTimestamp)) prevIntervalDateTime.minusDays(difference)
            else prevIntervalDateTime.plusDays(difference)
        }
    }

    override suspend fun saveSchedule(
        schedule: Schedule,
        setReminder: Boolean
    ) {
        withContext(Dispatchers.IO) {
            val insertedId = schedulesDao.upsert(schedule.toEntity()).first()
                .takeIf { it > OarDatabase.DEFAULT_ID_LONG }
                ?: schedule.id

            if (setReminder) {
                scheduler.setReminder(schedule.copy(id = insertedId))
            }
        }
    }

    override suspend fun addPaymentToSchedule(
        schedule: Schedule
    ) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val activeCycle = cycleRepo.getActiveCycle()
            val timestampNow = DateUtil.now()
            val transaction = TransactionEntity(
                amount = schedule.amount,
                note = schedule.note.orEmpty(),
                timestamp = timestampNow,
                type = schedule.type,
                tagId = schedule.tagId,
                folderId = schedule.folderId,
                scheduleId = schedule.id,
                isExcluded = false,
                currencyCode = schedule.currency.currencyCode,
                cycleId = activeCycle?.id ?: OarDatabase.DEFAULT_ID_LONG
            )
            transactionDao.upsert(transaction)
            val nextReminderDate = calculateNextPaymentTimestampFromDate(
                anchor = timestampNow,
                repetition = schedule.repetition,
                expectedTimestamp = schedule.nextPaymentTimestamp
            )
            saveSchedule(
                schedule = schedule.copy(
                    nextPaymentTimestamp = nextReminderDate,
                    lastPaymentTimestamp = timestampNow
                )
            )
        }
    }

    override suspend fun getOldestTxTimestampForSchedule(
        id: Long
    ): LocalDateTime? = withContext(Dispatchers.IO) {
        schedulesDao.getMinTxTimestampForSchedule(id)
    }

    override suspend fun getLatestTxTimestampForSchedule(
        id: Long
    ): LocalDateTime? = withContext(Dispatchers.IO) {
        schedulesDao.getMaxTxTimestampForSchedule(id)
    }

    override suspend fun deleteScheduleById(id: Long) = withContext(Dispatchers.IO) {
        scheduler.cancel(id)
        schedulesDao.deleteSchedulesById(setOf(id))
    }

    override suspend fun setAllFutureScheduleReminders() = withContext(Dispatchers.IO) {
        schedulesDao.getAllSchedulesAfterTimestamp(DateUtil.now())
            .map(ScheduleEntity::toSchedule)
            .forEach(scheduler::setReminder)
    }

    override suspend fun deleteSchedulesByIds(ids: Set<Long>) {
        withContext(Dispatchers.IO) {
            trySuspend {
                ids.forEach { scheduler.cancel(it) }
                schedulesDao.deleteSchedulesById(ids)
            }
        }
    }

    override suspend fun updateSchedules(vararg schedule: Schedule) = withContext(Dispatchers.IO) {
        schedulesDao.update(*schedule.map(Schedule::toEntity).toTypedArray())
    }
}