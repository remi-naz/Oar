package dev.ridill.oar.core.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import dev.ridill.oar.core.ui.components.OarModalBottomSheet
import dev.ridill.oar.core.ui.navigation.BottomSheetSceneStrategy.Companion.bottomSheet

/** An [OverlayScene] that renders an [entry] within a [ModalBottomSheet]. */
@OptIn(ExperimentalMaterial3Api::class)
internal data class BottomSheetScene<T : Any>(
    override val key: T,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val modalBottomSheetProperties: ModalBottomSheetProperties,
    private val skipPartiallyExpanded: Boolean,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable (() -> Unit) = {
        val lifecycleOwner = rememberLifecycleOwner()
        OarModalBottomSheet(
            onDismissRequest = onBack,
            sheetState = rememberBottomSheetState(
                initialValue = SheetValue.Hidden,
                enabledValues = buildSet {
                    add(SheetValue.Hidden)
                    if (!skipPartiallyExpanded) {
                        add(SheetValue.PartiallyExpanded)
                    }

                    add(SheetValue.Expanded)
                }
            ),
            properties = modalBottomSheetProperties,
        ) {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                entry.Content()
            }
        }
    }
}

/**
 * A [SceneStrategy] that displays entries with [bottomSheet] metadata in a [ModalBottomSheet].
 *
 * Pass this to [NavDisplay.sceneStrategy] before any non-overlay strategies.
 */
@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null

        @Suppress("UNCHECKED_CAST")
        val bottomSheetProperties =
            lastEntry.metadata[BOTTOM_SHEET_KEY] as? ModalBottomSheetProperties ?: return null
        return BottomSheetScene(
            key = lastEntry.contentKey as T,
            previousEntries = entries.dropLast(1),
            overlaidEntries = entries.dropLast(1),
            entry = lastEntry,
            modalBottomSheetProperties = bottomSheetProperties,
            skipPartiallyExpanded = lastEntry.metadata[SKIP_PARTIALLY_EXPANDED_KEY] as? Boolean? != false,
            onBack = onBack,
        )
    }

    companion object {
        private const val BOTTOM_SHEET_KEY = "BottomSheetSceneStrategy.bottomSheet"
        private const val SKIP_PARTIALLY_EXPANDED_KEY =
            "BottomSheetSceneStrategy.skipPartiallyExpanded"

        fun bottomSheet(
            modalBottomSheetProperties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
            skipPartiallyExpanded: Boolean = true
        ): Map<String, Any> = mapOf(
            BOTTOM_SHEET_KEY to modalBottomSheetProperties,
            SKIP_PARTIALLY_EXPANDED_KEY to skipPartiallyExpanded
        )
    }
}
