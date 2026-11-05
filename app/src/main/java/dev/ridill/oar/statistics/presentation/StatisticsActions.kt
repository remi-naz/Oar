package dev.ridill.oar.statistics.presentation

import java.util.Currency

interface StatisticsActions {
    fun onExcludedToggle()
    fun onCycleSelect(cycleId: Long?)
    fun onCurrencySelect(currency: Currency)
}
