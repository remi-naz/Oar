package dev.ridill.oar.core.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import java.time.LocalDateTime
import java.time.ZoneId

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
    version = 8,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 7, to = 8),
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

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val zone = ZoneId.systemDefault()
        migrateColumn(db, zone, "transaction_table", "id", "timestamp")
        migrateColumn(db, zone, "schedules_table", "id", "last_payment_timestamp")
        migrateColumn(db, zone, "schedules_table", "id", "next_payment_timestamp")
        migrateColumn(db, zone, "folder_table", "id", "created_timestamp")
        migrateColumn(db, zone, "tag_table", "id", "created_timestamp")
    }

    private fun migrateColumn(
        db: SupportSQLiteDatabase,
        zone: ZoneId,
        table: String,
        idColumn: String,
        column: String
    ) {
        val cursor = db.query(
            "SELECT $idColumn, $column FROM $table WHERE $column IS NOT NULL"
        )
        try {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val value = cursor.getString(1) ?: continue
                // Skip rows that are already in UTC instant format
                if (value.endsWith('Z') || value.contains('+')) continue
                val utcStr = runCatching {
                    LocalDateTime.parse(value)
                        .atZone(zone)
                        .toInstant()
                        .toString()
                }.getOrNull() ?: continue
                db.execSQL(
                    "UPDATE $table SET $column = ? WHERE $idColumn = ?",
                    arrayOf<Any>(utcStr, id)
                )
            }
        } catch (_: Throwable) {

        } finally {
            cursor.close()
        }
    }
}

// TransactionType(CREDIT, DEBIT) was renamed to FundMovement(IN, OUT); stored enum names must
// be rewritten and the dependent views recreated with the updated column/literal references.
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Views reference the `type` column and old enum literals; drop dependents before views.
        db.execSQL("DROP VIEW IF EXISTS `budget_cycle_details_view`")
        db.execSQL("DROP VIEW IF EXISTS `transaction_details_view`")

        db.execSQL("UPDATE transaction_table SET type = 'IN' WHERE type = 'CREDIT'")
        db.execSQL("UPDATE transaction_table SET type = 'OUT' WHERE type = 'DEBIT'")
        db.execSQL("UPDATE schedules_table SET type = 'IN' WHERE type = 'CREDIT'")
        db.execSQL("UPDATE schedules_table SET type = 'OUT' WHERE type = 'DEBIT'")

        db.execSQL(
            """CREATE VIEW `transaction_details_view` AS SELECT tx.id AS transactionId,
        tx.note AS transactionNote,
        tx.amount AS transactionAmount,
        tx.timestamp AS transactionTimestamp,
        tx.type AS fundMovement,
        tx.currency_code AS currencyCode,
        cyc.id AS cycleId,
        cyc.start_date AS cycleStartDate,
        cyc.end_date AS cycleEndDate,
        tag.id AS tagId,
        tag.name AS tagName,
        tag.color_code AS tagColorCode,
        tag.created_timestamp AS tagCreatedTimestamp,
        folder.id AS folderId,
        folder.name AS folderName,
        folder.created_timestamp AS folderCreatedTimestamp,
        tx.schedule_id as scheduleId,
        (CASE WHEN 1 IN (tx.is_excluded, tag.is_excluded, folder.is_excluded) THEN 1 ELSE 0 END) AS excluded
        FROM transaction_table tx
        JOIN budget_cycle_table cyc ON tx.cycle_id = cyc.id
        LEFT OUTER JOIN tag_table tag ON tx.tag_id = tag.id
        LEFT OUTER JOIN folder_table folder ON tx.folder_id = folder.id"""
        )

        db.execSQL(
            """CREATE VIEW `budget_cycle_details_view` AS SELECT bdgt.id AS id,
        bdgt.start_date AS startDate,
        bdgt.end_date as endDate,
        bdgt.budget AS budget,
        bdgt.currency_code AS currencyCode,
        IFNULL(SUM(
                CASE
                    WHEN tx.fundMovement = 'OUT' THEN tx.transactionAmount
                    WHEN tx.fundMovement = 'IN' THEN -tx.transactionAmount
                END
        ), 0) as aggregate,
        CASE
            WHEN cnfg.config_value IS NOT NULL THEN 1
            ELSE 0
        END AS active
        FROM budget_cycle_table bdgt
        LEFT OUTER JOIN config_table cnfg ON (cnfg.config_key = 'ACTIVE_CYCLE_ID' AND cnfg.config_value = bdgt.id)
        LEFT OUTER JOIN transaction_details_view tx ON (tx.cycleId = bdgt.id AND tx.currencyCode = bdgt.currency_code AND tx.excluded = 0)
        GROUP BY bdgt.id"""
        )
    }
}
