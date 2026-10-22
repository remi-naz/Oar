package dev.ridill.oar.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.oar.aggregations.data.local.AggregationsDao
import dev.ridill.oar.budgetCycles.data.local.BudgetCycleDao
import dev.ridill.oar.budgetCycles.data.repository.BudgetCycleRepositoryImpl
import dev.ridill.oar.budgetCycles.domain.cycleManager.CycleManager
import dev.ridill.oar.budgetCycles.domain.cycleManager.CycleAlarmManager
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleEntry
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.budgetCycles.domain.util.CycleDateCalculator
import dev.ridill.oar.budgetCycles.presentation.notification.CycleNotificationHelper
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.service.ReceiverService
import dev.ridill.oar.settings.data.local.ConfigDao

@Module
@InstallIn(SingletonComponent::class)
object BudgetCycleSingletonModule {
    @Provides
    fun provideBudgetCycleDao(
        database: OarDatabase
    ): BudgetCycleDao = database.budgetCycleDao()

    @Provides
    fun provideCycleManager(
        @ApplicationContext context: Context,
        receiverService: ReceiverService
    ): CycleManager = CycleAlarmManager(context, receiverService)

    @Provides
    fun provideCycleDateCalculator(): CycleDateCalculator = CycleDateCalculator()

    @Provides
    fun provideCycleRepository(
        database: OarDatabase,
        cycleDao: BudgetCycleDao,
        aggregationsDao: AggregationsDao,
        cycleManager: CycleManager,
        configDao: ConfigDao,
        cycleDateCalculator: CycleDateCalculator
    ): BudgetCycleRepository = BudgetCycleRepositoryImpl(
        db = database,
        cycleDao = cycleDao,
        aggDao = aggregationsDao,
        manager = cycleManager,
        configDao = configDao,
        cycleDateCalculator = cycleDateCalculator
    )

    @Provides
    fun provideBudgetCycleNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<BudgetCycleEntry> = CycleNotificationHelper(context)
}