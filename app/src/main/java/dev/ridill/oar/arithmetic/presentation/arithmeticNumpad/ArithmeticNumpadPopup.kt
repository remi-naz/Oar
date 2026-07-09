package dev.ridill.oar.arithmetic.presentation.arithmeticNumpad

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import dev.ridill.oar.arithmetic.domain.NumpadAction
import dev.ridill.oar.core.ui.theme.spacing

@Composable
fun ArithmeticNumpadPopup(
    onDismissRequest: () -> Unit,
    onAction: (NumpadAction) -> Unit,
    modifier: Modifier = Modifier,
    onHeightChanged: (Dp) -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surface,
    doneForEquals: Boolean = false,
) {
    val density = LocalDensity.current
    Popup(
        popupPositionProvider = NumpadPopupPositionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = false,
            dismissOnClickOutside = false,
            // False so windowSize in NumpadPopupPositionProvider is the full display bounds
            // rather than the system-bar-excluded visible frame — otherwise the navigation
            // bar inset is applied twice: once via the already-shrunk window size used for
            // positioning, and again via windowInsetsPadding below.
            clippingEnabled = false,
            excludeFromSystemGesture = false
        ),
    ) {
        // PopupProperties.dismissOnBackPress relies on the popup window receiving back key
        // events, which requires a focusable window — but this popup is non-focusable so the
        // input field underneath keeps focus. BackHandler hooks the OnBackPressedDispatcher
        // directly, independent of window focus, so it also catches the predictive back swipe.
        BackHandler(onBack = onDismissRequest)
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .onSizeChanged { size ->
                    onHeightChanged(with(density) { size.height.toDp() })
                },
            color = containerColor,
            shape = MaterialTheme.shapes.large,
        ) {
            Column {
                HorizontalDivider()
                ArithmeticNumpad(
                    onAction = onAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(MaterialTheme.spacing.small),
                    doneForEquals = doneForEquals
                )
            }
        }
    }
}

/** Pins the popup to the bottom of the window, spanning its full width, regardless of where the anchor field sits in the layout tree — mirrors how the system IME always docks to the screen bottom. */
private object NumpadPopupPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset(
        x = 0,
        y = windowSize.height - popupContentSize.height,
    )
}