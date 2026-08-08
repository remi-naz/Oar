package dev.ridill.oar.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.service.ReceiverService
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileTransactionDao
import dev.ridill.oar.moneyPiles.data.repository.MoneyPileRepositoryImpl
import dev.ridill.oar.moneyPiles.data.repository.PileReminderRepositoryImpl
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.notification.PileReminderNotificationHelper
import dev.ridill.oar.moneyPiles.domain.pileReminder.AlarmManagerPileReminder
import dev.ridill.oar.moneyPiles.domain.pileReminder.PileReminder
import dev.ridill.oar.moneyPiles.domain.repository.MoneyPileRepository
import dev.ridill.oar.moneyPiles.domain.repository.PileReminderRepository

@Module
@InstallIn(SingletonComponent::class)
object MoneyPileSingletonModule {

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
    fun providePileReminder(
        @ApplicationContext context: Context,
        receiverService: ReceiverService,
    ): PileReminder = AlarmManagerPileReminder(
        context = context,
        receiverService = receiverService
    )

    @Provides
    fun providePileReminderNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<MoneyPileDetails> = PileReminderNotificationHelper(context)

    @Provides
    fun providePileReminderRepository(
        db: OarDatabase,
        pileDao: MoneyPileDao,
        pileTransactionDao: MoneyPileTransactionDao,
        pileRepo: MoneyPileRepository,
        reminder: PileReminder,
    ): PileReminderRepository = PileReminderRepositoryImpl(
        db = db,
        pileDao = pileDao,
        pileTransactionDao = pileTransactionDao,
        pileRepo = pileRepo,
        reminder = reminder,
    )
}
