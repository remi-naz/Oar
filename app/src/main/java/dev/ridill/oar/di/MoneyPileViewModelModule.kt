package dev.ridill.oar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileTransactionDao
import dev.ridill.oar.moneyPiles.data.repository.AddEditPileRepositoryImpl
import dev.ridill.oar.moneyPiles.data.repository.AllPilesRepositoryImpl
import dev.ridill.oar.moneyPiles.domain.repository.AddEditPileRepository
import dev.ridill.oar.moneyPiles.domain.repository.AllPilesRepository
import dev.ridill.oar.moneyPiles.presentation.addEditPile.AddEditPileViewModel
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesViewModel
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(ViewModelComponent::class)
object MoneyPileViewModelModule {

    @Provides
    fun provideMoneyPileDao(db: OarDatabase): MoneyPileDao = db.moneyPileDao()

    @Provides
    fun provideMoneyPileTransactionDao(db: OarDatabase): MoneyPileTransactionDao =
        db.moneyPileTransactionsDao()

    @Provides
    fun provideAllPileRepository(
        db: OarDatabase,
        @ApplicationScope applicationScope: CoroutineScope,
        dao: MoneyPileDao,
    ): AllPilesRepository = AllPilesRepositoryImpl(
        db = db,
        applicationScope = applicationScope,
        dao = dao,
    )

    @Provides
    fun provideAllPilesEventBus(): EventBus<AllPilesViewModel.AllPilesEvent> = EventBus()

    @Provides
    fun provideAddEditPileRepository(
        db: OarDatabase,
        pileDao: MoneyPileDao,
        pileTransactionDao: MoneyPileTransactionDao,
    ): AddEditPileRepository = AddEditPileRepositoryImpl(
        db = db,
        pileDao = pileDao,
        pileTransactionDao = pileTransactionDao,
    )

    @Provides
    fun provideAddEditPileEventBus(): EventBus<AddEditPileViewModel.AddEditPileEvent> = EventBus()
}