package dev.ridill.oar.moneyPiles.data

import androidx.compose.ui.graphics.Color
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.moneyPiles.data.local.view.MoneyPileAggregateView
import dev.ridill.oar.moneyPiles.domain.model.MoneyPileWithSavedAmount

fun MoneyPileAggregateView.toMoneyPile(): MoneyPileWithSavedAmount = MoneyPileWithSavedAmount(
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
