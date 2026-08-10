package dev.ridill.oar.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ridill.oar.aggregations.domain.repository.AggregationsRepository
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.data.db.FtsQueryFormatter
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.data.local.MoneyPilePagedQueryBuilder
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileTransactionDao
import dev.ridill.oar.moneyPiles.data.repository.AddEditPileRepositoryImpl
import dev.ridill.oar.moneyPiles.data.repository.AllPilesRepositoryImpl
import dev.ridill.oar.moneyPiles.data.repository.PileDetailRepositoryImpl
import dev.ridill.oar.moneyPiles.data.repository.PileFundMovementRepositoryImpl
import dev.ridill.oar.moneyPiles.domain.model.PileSweepOutRepositoryImpl
import dev.ridill.oar.moneyPiles.domain.pileReminder.PileReminder
import dev.ridill.oar.moneyPiles.domain.repository.AddEditPileRepository
import dev.ridill.oar.moneyPiles.domain.repository.AllPilesRepository
import dev.ridill.oar.moneyPiles.domain.repository.MoneyPileRepository
import dev.ridill.oar.moneyPiles.domain.repository.PileDetailRepository
import dev.ridill.oar.moneyPiles.domain.repository.PileFundMovementRepository
import dev.ridill.oar.moneyPiles.domain.repository.PileSweepOutRepository
import dev.ridill.oar.moneyPiles.presentation.addEditPile.AddEditPileViewModel
import dev.ridill.oar.moneyPiles.presentation.allPiles.AllPilesViewModel
import dev.ridill.oar.moneyPiles.presentation.movePileFund.MovePileFundViewModel
import dev.ridill.oar.moneyPiles.presentation.pileDetails.PileDetailViewModel
import dev.ridill.oar.moneyPiles.presentation.sweepout.PileSweepOutViewModel
import dev.ridill.oar.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(ViewModelComponent::class)
object MoneyPileViewModelModule {

    @Provides
    fun provideMoneyPilePagedQueryBuilder(
        formatter: FtsQueryFormatter
    ): MoneyPilePagedQueryBuilder = MoneyPilePagedQueryBuilder(formatter)

    @Provides
    fun provideAllPilesRepository(
        db: OarDatabase,
        @ApplicationScope applicationScope: CoroutineScope,
        dao: MoneyPileDao,
        queryBuilder: MoneyPilePagedQueryBuilder,
    ): AllPilesRepository = AllPilesRepositoryImpl(
        db = db,
        applicationScope = applicationScope,
        dao = dao,
        queryBuilder = queryBuilder,
    )

    @Provides
    fun provideAllPilesEventBus(): EventBus<AllPilesViewModel.AllPilesEvent> = EventBus()

    @Provides
    fun provideAddEditPileRepository(
        db: OarDatabase,
        pileDao: MoneyPileDao,
        pileTransactionDao: MoneyPileTransactionDao,
        pileRepo: MoneyPileRepository,
        pileReminder: PileReminder,
    ): AddEditPileRepository = AddEditPileRepositoryImpl(
        db = db,
        pileDao = pileDao,
        pileTransactionDao = pileTransactionDao,
        pileRepo = pileRepo,
        pileReminder = pileReminder,
    )

    @Provides
    fun provideAddEditPileEventBus(): EventBus<AddEditPileViewModel.AddEditPileEvent> = EventBus()

    @Provides
    fun provideMPileFundMovementRepository(
        db: OarDatabase,
        pileRepo: MoneyPileRepository,
        transactionRepo: TransactionRepository,
        @ApplicationContext context: Context,
    ): PileFundMovementRepository = PileFundMovementRepositoryImpl(
        db = db,
        pileRepo = pileRepo,
        transactionRepo = transactionRepo,
        context = context,
    )

    @Provides
    fun provideMovePileFundEventBus(): EventBus<MovePileFundViewModel.MovePileFundEvent> =
        EventBus()

    @Provides
    fun providePileDetailRepository(
        db: OarDatabase,
        @ApplicationScope applicationScope: CoroutineScope,
        dao: MoneyPileTransactionDao,
        pileDao: MoneyPileDao,
        pileRepo: MoneyPileRepository,
        aggregationsRepo: AggregationsRepository,
        pileReminder: PileReminder,
    ): PileDetailRepository = PileDetailRepositoryImpl(
        db = db,
        applicationScope = applicationScope,
        dao = dao,
        pileDao = pileDao,
        pileRepo = pileRepo,
        aggregationsRepo = aggregationsRepo,
        pileReminder = pileReminder,
    )

    @Provides
    fun providePileDetailEvent(): EventBus<PileDetailViewModel.PileDetailEvent> = EventBus()

    @Provides
    fun providePileSweepOutRepository(
        db: OarDatabase,
        pileDao: MoneyPileDao,
        pileTxDao: MoneyPileTransactionDao,
        pileRepo: MoneyPileRepository,
        cycleRepo: BudgetCycleRepository,
        aggRepo: AggregationsRepository,
        transactionRepo: TransactionRepository,
    ): PileSweepOutRepository = PileSweepOutRepositoryImpl(
        db = db,
        pileDao = pileDao,
        pileTxDao = pileTxDao,
        pileRepo = pileRepo,
        cycleRepo = cycleRepo,
        aggRepo = aggRepo,
        transactionRepo = transactionRepo,
    )

    @Provides
    fun providePileSweepOutEventBus(): EventBus<PileSweepOutViewModel.PileSweepOutEvent> =
        EventBus()
}