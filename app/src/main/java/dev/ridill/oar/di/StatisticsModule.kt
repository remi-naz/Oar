package dev.ridill.oar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ridill.oar.aggregations.data.local.AggregationsDao
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.statistics.data.local.StatisticsDao
import dev.ridill.oar.statistics.data.repository.StatisticsRepositoryImpl
import dev.ridill.oar.statistics.domain.repository.StatisticsRepository

@Module
@InstallIn(SingletonComponent::class)
object StatisticsModule {

    @Provides
    fun provideStatisticsDao(database: OarDatabase): StatisticsDao = database.statisticsDao()

    @Provides
    fun provideStatisticsRepository(
        statisticsDao: StatisticsDao,
        aggDao: AggregationsDao,
        cycleRepo: BudgetCycleRepository
    ): StatisticsRepository = StatisticsRepositoryImpl(
        dao = statisticsDao,
        aggDao = aggDao,
        cycleRepo = cycleRepo
    )
}
