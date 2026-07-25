package dev.ridill.oar.moneyPiles.presentation.nav

import androidx.compose.material3.MotionScheme
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.ridill.oar.core.ui.navigation.AddEditMoneyPileRoute
import dev.ridill.oar.core.ui.navigation.AllMoneyPilesRoute
import dev.ridill.oar.core.ui.navigation.MoneyPileDetailsRoute
import dev.ridill.oar.core.ui.navigation.MoneyPileFundMovementRoute
import dev.ridill.oar.core.ui.navigation.OarNavigator

fun EntryProviderScope<NavKey>.moneyPileEntries(
    navigator: OarNavigator,
    motionScheme: MotionScheme,
) {
    entry<AllMoneyPilesRoute> {}
    entry<MoneyPileDetailsRoute> {}
    entry<AddEditMoneyPileRoute> {}
    entry<MoneyPileFundMovementRoute> {}
}