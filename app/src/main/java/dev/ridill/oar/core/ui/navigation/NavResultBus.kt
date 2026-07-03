package dev.ridill.oar.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance

class NavResultBus {
    private val _results = MutableSharedFlow<Any>(replay = 1)

    fun <T : Any> sendResult(result: T) {
        _results.tryEmit(result)
    }

    val results: SharedFlow<Any> = _results.asSharedFlow()

    fun consumeResult() {
        _results.resetReplayCache()
    }
}

val LocalResultBus = compositionLocalOf<NavResultBus> { error("No NavResultBus provided") }

@Composable
inline fun <reified T : Any> ResultEffect(crossinline onResult: (T) -> Unit) {
    val bus = LocalResultBus.current
    LaunchedEffect(bus) {
        bus.results.filterIsInstance<T>().collect {
            onResult(it)
            bus.consumeResult()
        }
    }
}

@Composable
fun rememberNavResultBusNavEntryDecorator(): NavEntryDecorator<NavKey> {
    val resultBus = remember { NavResultBus() }
    return remember(resultBus) {
        NavEntryDecorator { entry ->
            CompositionLocalProvider(LocalResultBus provides resultBus) {
                entry.Content()
            }
        }
    }
}
