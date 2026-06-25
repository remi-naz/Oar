package dev.ridill.oar.core.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.ridill.oar.aggregations.data.local.AggregationsDao
import dev.ridill.oar.budgetCycles.data.local.BudgetCycleDao
import dev.ridill.oar.budgetCycles.data.local.entity.BudgetCycleEntity
import dev.ridill.oar.budgetCycles.data.local.view.BudgetCycleDetailsView
import dev.ridill.oar.folders.data.local.FolderDao
import dev.ridill.oar.folders.data.local.entity.FolderEntity
import dev.ridill.oar.schedules.data.local.SchedulesDao
import dev.ridill.oar.schedules.data.local.entity.ScheduleEntity
import dev.ridill.oar.settings.data.local.ConfigDao
import dev.ridill.oar.settings.data.local.CurrencyListDao
import dev.ridill.oar.settings.data.local.entity.ConfigEntity
import dev.ridill.oar.settings.data.local.entity.CurrencyListEntity
import dev.ridill.oar.tags.data.local.TagsDao
import dev.ridill.oar.tags.data.local.entity.TagEntity
import dev.ridill.oar.transactions.data.local.TransactionDao
import dev.ridill.oar.transactions.data.local.entity.TransactionEntity
import dev.ridill.oar.transactions.data.local.views.TransactionDetailsView

@Database(
    entities = [
        BudgetCycleEntity::class,
        ConfigEntity::class,
        TransactionEntity::class,
        TagEntity::class,
        FolderEntity::class,
        ScheduleEntity::class,
        CurrencyListEntity::class,
    ],
    views = [
        BudgetCycleDetailsView::class,
        TransactionDetailsView::class,
    ],
    version = 5,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
    ]
)
@TypeConverters(DateTimeConverter::class)
abstract class OarDatabase : RoomDatabase() {
    companion object {
        const val NAME = "Oar.db"
        const val DEFAULT_ID_LONG = 0L
        const val INVALID_ID_LONG = -1L
        const val INVALID_LIMIT = -1
    }

    // Dao Methods
    abstract fun aggregationDao(): AggregationsDao
    abstract fun budgetCycleDao(): BudgetCycleDao
    abstract fun transactionDao(): TransactionDao
    abstract fun tagsDao(): TagsDao
    abstract fun folderDao(): FolderDao
    abstract fun schedulesDao(): SchedulesDao
    abstract fun currencyListDao(): CurrencyListDao
    abstract fun configDao(): ConfigDao
}