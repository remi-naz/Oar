package dev.ridill.oar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.oar.arithmetic.domain.ArithmeticInputProcessor

@Module
@InstallIn(ViewModelComponent::class)
object ArithmeticViewModelModule {

    @Provides
    fun provideArithmeticInputProcessor(): ArithmeticInputProcessor = ArithmeticInputProcessor()
}