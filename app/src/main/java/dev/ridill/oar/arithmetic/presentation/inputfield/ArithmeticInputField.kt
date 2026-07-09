package dev.ridill.oar.arithmetic.presentation.inputfield

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.oar.arithmetic.domain.NumpadAction
import dev.ridill.oar.arithmetic.presentation.arithmeticNumpad.ArithmeticNumpadPopup
import dev.ridill.oar.core.ui.components.OarTextField
import dev.ridill.oar.core.ui.components.rememberAmountOutputTransformation
import dev.ridill.oar.core.ui.theme.OarTheme

@Composable
fun ArithmeticInputField(
    inputState: TextFieldState,
    onAction: (NumpadAction) -> Unit,
    modifier: Modifier = Modifier,
    onNumpadHeightChanged: (Dp) -> Unit = {},
    enabled: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current,
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Inside(),
    label: @Composable (TextFieldLabelScope.() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = rememberAmountOutputTransformation(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    scrollState: ScrollState = rememberScrollState(),
    shape: Shape = MaterialTheme.shapes.medium,
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
    ),
    doneForEquals: Boolean = false,
) {
    var isNumpadVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val windowInfo = LocalWindowInfo.current

    OarTextField(
        state = inputState,
        modifier = modifier
            .onFocusChanged { focusState ->
                // A predictive back swipe transiently takes window focus away (for the
                // system's preview animation) before the gesture is committed, which Compose
                // reports here as the field losing focus. Ignore focus changes that happen
                // while the window itself isn't focused so the popup — and the BackHandler
                // that lets it intercept the swipe — isn't torn down mid-gesture.
                if (!windowInfo.isWindowFocused) return@onFocusChanged
                isNumpadVisible = focusState.isFocused
                if (!focusState.isFocused) {
                    onAction(NumpadAction.Equals)
                    // Return the reserved space immediately rather than waiting on the
                    // popup's own dismissal, which happens asynchronously.
                    onNumpadHeightChanged(0.dp)
                }
            },
        enabled = enabled,
        // Always readOnly so focusing/tapping this field never opens the system IME;
        // all edits are driven programmatically by ArithmeticNumpad via NumpadAction.
        readOnly = true,
        textStyle = textStyle,
        labelPosition = labelPosition,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        inputTransformation = inputTransformation,
        outputTransformation = outputTransformation,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        lineLimits = lineLimits,
        scrollState = scrollState,
        shape = shape,
        colors = colors,
    )

    if (isNumpadVisible) {
        ArithmeticNumpadPopup(
            onDismissRequest = focusManager::clearFocus,
            onAction = {
                if (it is NumpadAction.Done) {
                    focusManager.clearFocus()
                } else {
                    onAction(it)
                }
            },
            onHeightChanged = onNumpadHeightChanged,
            doneForEquals = doneForEquals
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewArithmeticInputField() {
    OarTheme {
        Surface {
            ArithmeticInputField(
                inputState = rememberTextFieldState(),
                onAction = {}
            )
        }
    }
}
