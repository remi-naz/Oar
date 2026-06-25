package dev.ridill.oar.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner

/**
 * Provides an isolated [androidx.lifecycle.ViewModelStoreOwner] scoped to [key],
 * so that ViewModels obtained inside [content] are tied to that key's lifecycle
 * rather than the enclosing Activity or Navigation destination.
 *
 * Useful for reusable UI components (e.g. bottom sheets, dialogs, list items)
 * that need their own ViewModel instance without leaking into the parent scope.
 *
 * @param key Unique identifier for the scoped store. Changing the key discards
 *   the previous ViewModelStore and creates a new one.
 * @param saveableStateHolder Holder used to persist saveable state across
 *   configuration changes. Defaults to [rememberSaveableStateHolder].
 * @param content Composable content that will receive the scoped ViewModelStoreOwner.
 *
 * Usage:
 * ```
 * ComponentViewModelScope(key = item.id) {
 *     val viewModel: ItemViewModel = viewModel()
 *     ItemCard(viewModel.state)
 * }
 * ```
 */
@Composable
fun ComponentViewModelScope(
    key: Any,
    saveableStateHolder: SaveableStateHolder = rememberSaveableStateHolder(),
    content: @Composable () -> Unit,
) {
    saveableStateHolder.SaveableStateProvider(key) {
        val storeOwner = rememberViewModelStoreOwner()
        CompositionLocalProvider(
            value = LocalViewModelStoreOwner provides storeOwner,
            content = content
        )
    }
}