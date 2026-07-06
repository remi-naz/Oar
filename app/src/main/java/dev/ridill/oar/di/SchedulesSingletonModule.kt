package dev.ridill.oar.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.service.ReceiverService
import dev.ridill.oar.schedules.data.local.SchedulesDao
import dev.ridill.oar.schedules.data.repository.SchedulesRepositoryImpl
import dev.ridill.oar.schedules.domain.model.Schedule
import dev.ridill.oar.schedules.domain.notification.ScheduleReminderNotificationHelper
import dev.ridill.oar.schedules.domain.repository.SchedulesRepository
import dev.ridill.oar.schedules.domain.scheduleReminder.AlarmManagerScheduleReminder
import dev.ridill.oar.schedules.domain.scheduleReminder.ScheduleReminder
import dev.ridill.oar.transactions.data.local.TransactionDao

@Module
@InstallIn(SingletonComponent::class)
object SchedulesSingletonModule {
    @Provides
    fun provideSchedulesDao(database: OarDatabase): SchedulesDao =
        database.schedulesDao()

    @Provides
    fun provideSchedulesRepository(
        db: OarDatabase,
        schedulesDao: SchedulesDao,
        transactionDao: TransactionDao,
        scheduler: ScheduleReminder,
        cycleRepo: BudgetCycleRepository
    ): SchedulesRepository = SchedulesRepositoryImpl(
        db = db,
        schedulesDao = schedulesDao,
        transactionDao = transactionDao,
        scheduler = scheduler,
        cycleRepo = cycleRepo
    )

    @Provides
    fun provideScheduleReminder(
        @ApplicationContext context: Context,
        receiverService: ReceiverService,
    ): ScheduleReminder = AlarmManagerScheduleReminder(
        context = context,
        receiverService = receiverService
    )

    @Provides
    fun provideScheduleReminderNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<Schedule> = ScheduleReminderNotificationHelper(context)
}