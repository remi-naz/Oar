package dev.ridill.oar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.data.repository.AllPilesRepositoryImpl
import dev.ridill.oar.moneyPiles.domain.repository.AllPilesRepository

@Module
@InstallIn(ViewModelComponent::class)
object MoneyPilesViewModelModule {

    @Provides
    fun provideMoneyPilesDao(db: OarDatabase): MoneyPileDao = db.moneyPileDao()

    @Provides
    fun provideAllPilesRepository(
        dao: MoneyPileDao,
    ): AllPilesRepository = AllPilesRepositoryImpl(
        dao = dao
    )
}