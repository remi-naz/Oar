package dev.ridill.oar.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.receiveAsFlow

class NavResultBus {
    private val _channel = Channel<Any>(Channel.CONFLATED)

    fun <T : Any> sendResult(result: T) { _channel.trySend(result) }

    val results get() = _channel.receiveAsFlow()
}

val LocalResultBus = compositionLocalOf<NavResultBus> { error("No NavResultBus provided") }

@Composable
inline fun <reified T : Any> ResultEffect(crossinline onResult: (T) -> Unit) {
    val bus = LocalResultBus.current
    LaunchedEffect(bus) {
        bus.results.filterIsInstance<T>().collect { onResult(it) }
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
