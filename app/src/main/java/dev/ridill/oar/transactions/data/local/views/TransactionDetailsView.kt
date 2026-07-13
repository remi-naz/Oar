package dev.ridill.oar.transactions.data.local.views

import androidx.room.DatabaseView
import dev.ridill.oar.core.domain.model.FundMovement
import java.time.LocalDate
import java.time.LocalDateTime

@DatabaseView(
    value = """SELECT tx.id AS transactionId,
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
        LEFT OUTER JOIN folder_table folder ON tx.folder_id = folder.id
        """,
    viewName = "transaction_details_view"
)
data class TransactionDetailsView(
    val transactionId: Long,
    val transactionNote: String,
    val transactionAmount: Double,
    val transactionTimestamp: LocalDateTime,
    val fundMovement: FundMovement,
    val currencyCode: String,
    val cycleId: Long,
    val cycleStartDate: LocalDate,
    val cycleEndDate: LocalDate,
    val tagId: Long?,
    val tagName: String?,
    val tagColorCode: Int?,
    val tagCreatedTimestamp: LocalDateTime?,
    val folderId: Long?,
    val folderName: String?,
    val folderCreatedTimestamp: LocalDateTime?,
    val scheduleId: Long?,
    val excluded: Boolean
)