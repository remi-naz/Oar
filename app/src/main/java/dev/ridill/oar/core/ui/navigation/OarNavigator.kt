package dev.ridill.oar.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

class OarNavigator(
    val backStack: NavBackStack<NavKey>
) {

    fun navigate(route: NavKey) {
        // Guard against double taps stacking the same destination twice
        if (backStack.lastOrNull() == route) return
        backStack.add(route)
    }

    fun goBack() {
        backStack.removeLastOrNull()
    }

    /** Navigate to [route] after dropping every other entry from the back stack. */
    fun navigateClearingBackStack(route: NavKey) {
        backStack.clear()
        backStack.add(route)
    }

    /** Navigate to [route] replacing the current top entry (used for duplicate-transaction flow). */
    fun replaceTop(route: NavKey) {
        backStack.removeLastOrNull()
        backStack.add(route)
    }
}

@Composable
fun rememberOarNavigator(
    backStack: NavBackStack<NavKey>,
): OarNavigator = remember(backStack) { OarNavigator(backStack) }