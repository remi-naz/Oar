package dev.ridill.oar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.oar.core.data.preferences.animPreferences.AnimPreferencesManager
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.folders.domain.repository.FolderDetailsRepository
import dev.ridill.oar.schedules.data.local.SchedulesDao
import dev.ridill.oar.schedules.data.repository.AddEditScheduleRepositoryImpl
import dev.ridill.oar.schedules.data.repository.AllSchedulesRepositoryImpl
import dev.ridill.oar.schedules.domain.repository.AddEditScheduleRepository
import dev.ridill.oar.schedules.domain.repository.AllSchedulesRepository
import dev.ridill.oar.schedules.domain.repository.SchedulesRepository
import dev.ridill.oar.schedules.presentation.addEditSchedule.AddEditScheduleViewModel
import dev.ridill.oar.schedules.presentation.allSchedules.AllSchedulesViewModel
import dev.ridill.oar.transactions.data.local.TransactionDao

@Module
@InstallIn(ViewModelComponent::class)
object SchedulesViewModelModule {
    @Provides
    fun provideAllSchedulesRepository(
        dao: SchedulesDao,
        schedulesRepository: SchedulesRepository,
        animPreferencesManager: AnimPreferencesManager
    ): AllSchedulesRepository = AllSchedulesRepositoryImpl(
        dao = dao,
        repo = schedulesRepository,
        animPreferencesManager = animPreferencesManager
    )

    @Provides
    fun provideAllSchedulesEventBuss(): EventBus<AllSchedulesViewModel.AllSchedulesEvent> =
        EventBus()

    @Provides
    fun provideAddEditScheduleRepository(
        schedulesRepo: SchedulesRepository,
        transactionDao: TransactionDao,
        folderRepo: FolderDetailsRepository
    ): AddEditScheduleRepository = AddEditScheduleRepositoryImpl(
        schedulesRepo = schedulesRepo,
        transactionDao = transactionDao,
        folderRepo = folderRepo
    )

    @Provides
    fun provideAddEditScheduleEventBus(): EventBus<AddEditScheduleViewModel.AddEditScheduleEvent> =
        EventBus()
}