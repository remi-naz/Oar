package dev.ridill.oar.moneyPiles.data

import androidx.compose.ui.graphics.Color
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileEntity
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileTransactionsEntity
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileAggregateView
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileDetails
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileWithSavedAmount
import dev.ridill.oar.moneyPiles.domain.model.PileDetail
import dev.ridill.oar.moneyPiles.domain.model.PileTransactionEntry

internal fun MoneyPileAggregateView.toMoneyPile(): MoneyPileWithSavedAmount =
    MoneyPileWithSavedAmount(
        id = id,
        name = name,
        icon = icon,
        color = Color(color),
        currency = LocaleUtil.currencyForCode(currencyCode),
        targetAmount = targetAmount,
        savedAmount = aggregate,
        locked = locked,
        createdTimestamp = createdTimestamp
    )

internal fun MoneyPileEntity.toMoneyPileDetails(): MoneyPileDetails = MoneyPileDetails(
    id = id,
    name = name,
    icon = icon,
    colorCode = color,
    contributionMode = contributionMode,
    targetAmount = targetAmount,
    locked = locked,
    reminderCadence = reminderCadence,
    reminderBehavior = reminderBehavior,
    reminderAmount = reminderAmount,
    createdTimestamp = createdTimestamp,
    currency = LocaleUtil.currencyForCode(currencyCode),
    targetDate = targetDate
)

internal fun MoneyPileDetails.toPileDetail(savedAmount: Double): PileDetail = PileDetail(
    id = id,
    name = name,
    icon = icon,
    color = color,
    currency = currency,
    contributionMode = contributionMode,
    targetAmount = targetAmount,
    savedAmount = savedAmount,
    locked = locked,
    reminderCadence = reminderCadence,
    reminderBehavior = reminderBehavior,
    reminderAmount = reminderAmount,
    createdTimestamp = createdTimestamp,
)

internal fun MoneyPileTransactionsEntity.toPileTransactionEntry(): PileTransactionEntry =
    PileTransactionEntry(
        id = id,
        amount = amount,
        movement = movement,
        contributionSource = contributionSource,
        timestamp = createdTimestamp,
    )

internal fun MoneyPileDetails.toEntity(): MoneyPileEntity = MoneyPileEntity(
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
)
