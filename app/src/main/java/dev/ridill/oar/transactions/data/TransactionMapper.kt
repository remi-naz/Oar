package dev.ridill.oar.transactions.data

import androidx.compose.ui.graphics.Color
import dev.ridill.oar.budgetCycles.domain.model.CycleIndicator
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.transactions.data.local.entity.TransactionEntity
import dev.ridill.oar.transactions.data.local.relation.AmountAndCurrencyRelation
import dev.ridill.oar.transactions.data.local.views.TransactionDetailsView
import dev.ridill.oar.transactions.domain.model.AggregateAmountItem
import dev.ridill.oar.transactions.domain.model.FolderIndicator
import dev.ridill.oar.transactions.domain.model.TagIndicator
import dev.ridill.oar.transactions.domain.model.Transaction
import dev.ridill.oar.transactions.domain.model.TransactionEntry

fun TransactionEntity.toTransaction(): Transaction = Transaction(
    id = id,
    amount = TextFormat.number(
        value = amount,
        isGroupingUsed = false,
        maxFractionDigits = Int.MAX_VALUE
    ),
    note = note,
    timestamp = timestamp,
    type = type,
    folderId = folderId,
    tagId = tagId,
    excluded = isExcluded,
    scheduleId = scheduleId,
    currency = LocaleUtil.currencyForCode(currencyCode),
    cycleId = cycleId
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    note = note,
    amount = TextFormat.parseNumber(amount).orZero(),
    currencyCode = currency.currencyCode,
    timestamp = timestamp,
    type = type,
    isExcluded = excluded,
    tagId = tagId,
    folderId = folderId,
    scheduleId = scheduleId,
    cycleId = cycleId
)

fun TransactionDetailsView.toTransactionListItem(): TransactionEntry {
    val cycle = CycleIndicator(
        id = cycleId,
        description = DateUtil.prettyDateRange(cycleStartDate, cycleEndDate),
    )
    val tag = if (tagId != null
        && tagName != null
        && tagColorCode != null
        && tagCreatedTimestamp != null
    ) TagIndicator(
        id = tagId,
        name = tagName,
        color = Color(tagColorCode)
    ) else null

    val folder = if (folderId != null
        && folderName != null
        && folderCreatedTimestamp != null
    ) FolderIndicator(
        id = folderId,
        name = folderName,
    ) else null

    return TransactionEntry(
        id = transactionId,
        note = transactionNote,
        amount = transactionAmount,
        timestamp = transactionTimestamp,
        type = fundMovement,
        excluded = excluded,
        cycle = cycle,
        tag = tag,
        folder = folder,
        scheduleId = scheduleId,
        currency = LocaleUtil.currencyForCode(currencyCode)
    )
}

fun AmountAndCurrencyRelation.toAggregateAmountItem(): AggregateAmountItem = AggregateAmountItem(
    currency = LocaleUtil.currencyForCode(currencyCode),
    amount = amount
)