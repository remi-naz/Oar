package dev.ridill.oar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ridill.oar.aggregations.data.local.AggregationsDao
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.statistics.data.repository.StatisticsRepositoryImpl
import dev.ridill.oar.statistics.domain.repository.StatisticsRepository

@Module
@InstallIn(SingletonComponent::class)
object StatisticsModule {

    @Provides
    fun provideStatisticsRepository(
        dao: AggregationsDao,
        cycleRepo: BudgetCycleRepository
    ): StatisticsRepository = StatisticsRepositoryImpl(
        dao = dao,
        cycleRepo = cycleRepo
    )
}
