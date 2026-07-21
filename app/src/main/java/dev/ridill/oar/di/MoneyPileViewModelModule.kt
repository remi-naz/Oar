package dev.ridill.oar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.data.repository.AllPilesRepositoryImpl
import dev.ridill.oar.moneyPiles.domain.repository.AllPilesRepository
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(ViewModelComponent::class)
object MoneyPileViewModelModule {

    @Provides
    fun provideMoneyPileDao(db: OarDatabase): MoneyPileDao = db.moneyPileDao()

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
}