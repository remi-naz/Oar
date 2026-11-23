package dev.ridill.oar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ridill.oar.core.data.db.FtsQueryFormatter
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.transactions.data.local.TransactionDao
import dev.ridill.oar.transactions.data.local.TransactionPagedQueryBuilder
import dev.ridill.oar.transactions.data.repository.TransactionRepositoryImpl
import dev.ridill.oar.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
object TransactionSingletonModule {
    @Provides
    fun provideTransactionDao(db: OarDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideTransactionPagedQueryBuilder(
        formatter: FtsQueryFormatter
    ): TransactionPagedQueryBuilder = TransactionPagedQueryBuilder(formatter)

    @Provides
    fun provideTransactionRepository(
        transactionDao: TransactionDao,
        queryBuilder: TransactionPagedQueryBuilder,
        db: OarDatabase,
        @ApplicationScope applicationScope: CoroutineScope,
    ): TransactionRepository = TransactionRepositoryImpl(
        dao = transactionDao,
        queryBuilder = queryBuilder,
        db = db,
        applicationScope = applicationScope
    )
}