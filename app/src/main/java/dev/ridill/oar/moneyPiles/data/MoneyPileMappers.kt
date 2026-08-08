package dev.ridill.oar.moneyPiles.data

import androidx.compose.ui.graphics.Color
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileEntity
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileAggregateView
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileEntryUiModel
import dev.ridill.oar.moneyPiles.domain.model.PileTransactionEntry

internal fun MoneyPileAggregateView.toMoneyPile(): MoneyPileEntryUiModel.MoneyPileWithSavedAmount =
    MoneyPileEntryUiModel.MoneyPileWithSavedAmount(
        id = id,
        name = name,
        icon = icon,
        color = Color(color),
        currency = LocaleUtil.currencyForCode(currencyCode),
        targetAmount = remainingAmount,
        savedAmount = aggregate,
        locked = locked,
        createdTimestamp = createdTimestamp,
        completionTimestamp = completionTimestamp,
    )

internal fun MoneyPileEntity.toMoneyPileDetails(): MoneyPileDetails = MoneyPileDetails(
    id = id,
    name = name,
    icon = icon,
    colorCode = color,
    contributionMode = contributionMode,
    targetAmount = targetRemainingAmount,
    locked = locked,
    reminderCadence = reminderCadence,
    reminderBehavior = reminderBehavior,
    reminderAmount = reminderAmount,
    createdTimestamp = createdTimestamp,
    currency = LocaleUtil.currencyForCode(currencyCode),
    targetDate = targetDate,
    completionTimestamp = completionTimestamp
)

internal fun MoneyPileTransactionsEntity.toPileTransactionEntry(): PileTransactionEntry =
    PileTransactionEntry(
        id = id,
        amount = amount,
        movement = movement,
        contributionSource = contributionSource,
        timestamp = createdTimestamp,
        locked = locked,
    )

internal fun MoneyPileDetails.toEntity(remainingAmount: Double?): MoneyPileEntity = MoneyPileEntity(
    id = id,
    name = name,
    icon = icon,
    color = colorCode,
    contributionMode = contributionMode,
    reminderCadence = reminderCadence,
    reminderBehavior = reminderBehavior,
    locked = locked,
    currencyCode = currency.currencyCode,
    targetAmount = targetAmount,
    targetDate = targetDate,
    createdTimestamp = createdTimestamp,
    reminderAmount = reminderAmount,
    completionTimestamp = null,
    targetRemainingAmount = remainingAmount,
)
