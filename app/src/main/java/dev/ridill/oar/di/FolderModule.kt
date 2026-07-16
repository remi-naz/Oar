package dev.ridill.oar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.data.preferences.animPreferences.AnimPreferencesManager
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.folders.data.local.FolderDao
import dev.ridill.oar.folders.data.repository.AddEditFolderRepositoryImpl
import dev.ridill.oar.folders.data.repository.FolderDetailsRepositoryImpl
import dev.ridill.oar.folders.data.repository.FolderListRepositoryImpl
import dev.ridill.oar.folders.domain.repository.AddEditFolderRepository
import dev.ridill.oar.folders.domain.repository.FolderDetailsRepository
import dev.ridill.oar.folders.domain.repository.FolderListRepository
import dev.ridill.oar.folders.presentation.addEditFolder.AddEditFolderViewModel
import dev.ridill.oar.folders.presentation.folderDetails.FolderDetailsViewModel
import dev.ridill.oar.transactions.data.local.TransactionDao
import dev.ridill.oar.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(ViewModelComponent::class)
object FolderModule {

    @Provides
    fun provideFolderDao(db: OarDatabase): FolderDao = db.folderDao()

    @Provides
    fun provideFolderListsRepository(
        folderDao: FolderDao,
        db: OarDatabase,
        @ApplicationScope applicationScope: CoroutineScope
    ): FolderListRepository = FolderListRepositoryImpl(
        folderDao = folderDao,
        db = db,
        applicationScope = applicationScope
    )

    @Provides
    fun provideFolderDetailsRepository(
        folderDao: FolderDao,
        transactionDao: TransactionDao,
        transactionRepo: TransactionRepository,
        animPreferencesManager: AnimPreferencesManager
    ): FolderDetailsRepository = FolderDetailsRepositoryImpl(
        dao = folderDao,
        transactionDao = transactionDao,
        transactionRepo = transactionRepo,
        animPreferencesManager = animPreferencesManager
    )

    @Provides
    fun provideFolderDetailsEventBus(): EventBus<FolderDetailsViewModel.FolderDetailsEvent> =
        EventBus()

    @Provides
    fun provideAddEditFolderRepository(
        dao: FolderDao
    ): AddEditFolderRepository = AddEditFolderRepositoryImpl(
        dao = dao
    )

    @Provides
    fun provideAddEditFolderEventBus(): EventBus<AddEditFolderViewModel.AddEditFolderEvent> =
        EventBus()
}