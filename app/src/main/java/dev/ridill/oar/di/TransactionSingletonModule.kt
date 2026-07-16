package dev.ridill.oar.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.crashlytics.CrashlyticsManager
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.remoteConfig.FirebaseRemoteConfigService
import dev.ridill.oar.transactions.data.local.TransactionDao
import dev.ridill.oar.transactions.data.repository.TransactionRepositoryImpl
import dev.ridill.oar.transactions.domain.autoDetection.RegexTransactionDataExtractor
import dev.ridill.oar.transactions.domain.autoDetection.TransactionAutoDetectService
import dev.ridill.oar.transactions.domain.autoDetection.TransactionDataExtractor
import dev.ridill.oar.transactions.domain.model.Transaction
import dev.ridill.oar.transactions.domain.notification.TransactionAutoDetectNotificationHelper
import dev.ridill.oar.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
object TransactionSingletonModule {
    @Provides
    fun provideTransactionDao(db: OarDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideTransactionRepository(
        transactionDao: TransactionDao,
        db: OarDatabase,
        @ApplicationScope applicationScope: CoroutineScope,
    ): TransactionRepository = TransactionRepositoryImpl(
        dao = transactionDao,
        db = db,
        applicationScope = applicationScope
    )

    @Provides
    fun provideTransactionDataExtractor(
        @ApplicationScope applicationScope: CoroutineScope,
        remoteConfigService: FirebaseRemoteConfigService
    ): TransactionDataExtractor = RegexTransactionDataExtractor(
        applicationScope = applicationScope,
        remoteConfigService = remoteConfigService
    )

    @Provides
    fun provideTransactionSmsService(
        extractor: TransactionDataExtractor,
        transactionRepository: TransactionRepository,
        crashlyticsManager: CrashlyticsManager,
        notificationHelper: NotificationHelper<Transaction>,
        @ApplicationScope applicationScope: CoroutineScope,
    ): TransactionAutoDetectService = TransactionAutoDetectService(
        extractor = extractor,
        transactionRepo = transactionRepository,
        crashlyticsManager = crashlyticsManager,
        notificationHelper = notificationHelper,
        applicationScope = applicationScope
    )

    @Provides
    fun provideTransactionAutoDetectNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<Transaction> = TransactionAutoDetectNotificationHelper(context)
}