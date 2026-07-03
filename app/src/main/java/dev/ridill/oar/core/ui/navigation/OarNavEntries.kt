package dev.ridill.oar.core.ui.navigation

import androidx.compose.material3.MotionScheme
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import dev.ridill.oar.dashboard.presentation.nav.dashboardEntries
import dev.ridill.oar.folders.presentation.nav.folderEntries
import dev.ridill.oar.onboarding.presentation.nav.onboardingEntries
import dev.ridill.oar.schedules.presentation.nav.scheduleEntries
import dev.ridill.oar.settings.presentation.nav.settingsEntries
import dev.ridill.oar.tags.presentation.nav.tagEntries
import dev.ridill.oar.transactions.presentation.nav.transactionEntries

fun buildOarEntryProvider(
    navigator: OarNavigator,
    motionScheme: MotionScheme,
) = entryProvider<NavKey> {
    onboardingEntries(navigator = navigator)
    dashboardEntries(navigator = navigator)
    transactionEntries(navigator = navigator, motionScheme = motionScheme)
    folderEntries(navigator = navigator)
    tagEntries(navigator = navigator)
    scheduleEntries(navigator = navigator)
    settingsEntries(navigator = navigator)
}
