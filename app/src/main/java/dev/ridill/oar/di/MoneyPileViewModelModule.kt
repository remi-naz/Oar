package dev.ridill.oar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.oar.aggregations.domain.repository.AggregationsRepository
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileTransactionDao
import dev.ridill.oar.moneyPiles.data.repository.AddEditPileRepositoryImpl
import dev.ridill.oar.moneyPiles.data.repository.AllPilesRepositoryImpl
import dev.ridill.oar.moneyPiles.data.repository.MoneyPileRepositoryImpl
import dev.ridill.oar.moneyPiles.data.repository.PileDetailRepositoryImpl
import dev.ridill.oar.moneyPiles.data.repository.PileFundMovementRepositoryImpl
import dev.ridill.oar.moneyPiles.domain.repository.AddEditPileRepository
import dev.ridill.oar.moneyPiles.domain.repository.AllPilesRepository
import dev.ridill.oar.moneyPiles.domain.repository.MoneyPileRepository
import dev.ridill.oar.moneyPiles.domain.repository.PileDetailRepository
import dev.ridill.oar.moneyPiles.domain.repository.PileFundMovementRepository
import dev.ridill.oar.moneyPiles.presentation.addEditPile.AddEditPileViewModel
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesViewModel
import dev.ridill.oar.moneyPiles.presentation.movePileFund.MovePileFundViewModel
import dev.ridill.oar.moneyPiles.presentation.pileDetails.PileDetailViewModel
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
    fun provideMoneyPileRepository(
        dao: MoneyPileDao,
        transactionDao: MoneyPileTransactionDao,
    ): MoneyPileRepository = MoneyPileRepositoryImpl(
        pileDao = dao,
        transactionDao = transactionDao,
    )

    @Provides
    fun provideAllPilesRepository(
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
        pileRepo: MoneyPileRepository,
    ): AddEditPileRepository = AddEditPileRepositoryImpl(
        db = db,
        pileDao = pileDao,
        pileTransactionDao = pileTransactionDao,
        pileRepo = pileRepo,
    )

    @Provides
    fun provideAddEditPileEventBus(): EventBus<AddEditPileViewModel.AddEditPileEvent> = EventBus()

    @Provides
    fun provideMPileFundMovementRepository(
        pileRepo: MoneyPileRepository,
    ): PileFundMovementRepository = PileFundMovementRepositoryImpl(
        pileRepo = pileRepo,
    )

    @Provides
    fun provideMovePileFundEventBus(): EventBus<MovePileFundViewModel.MovePileFundEvent> =
        EventBus()

    @Provides
    fun providePileDetailRepository(
        db: OarDatabase,
        @ApplicationScope applicationScope: CoroutineScope,
        dao: MoneyPileTransactionDao,
        pileRepo: MoneyPileRepository,
        aggregationsRepo: AggregationsRepository
    ): PileDetailRepository = PileDetailRepositoryImpl(
        db = db,
        applicationScope = applicationScope,
        dao = dao,
        pileRepo = pileRepo,
        aggregationsRepo = aggregationsRepo,
    )

    @Provides
    fun providePileDetailEvent(): EventBus<PileDetailViewModel.PileDetailEvent> = EventBus()
}