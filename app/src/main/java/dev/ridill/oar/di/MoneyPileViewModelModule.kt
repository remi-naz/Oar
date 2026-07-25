package dev.ridill.oar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao

@Module
@InstallIn(ViewModelComponent::class)
object MoneyPileViewModelModule {

    @Provides
    fun provideMoneyPileDao(db: OarDatabase): MoneyPileDao = db.moneyPileDao()
}