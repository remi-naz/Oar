package dev.ridill.oar.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.One
import dev.ridill.oar.core.ui.theme.PaddingScrollEnd
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.UiText

@Composable
fun OarModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    ),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = BottomSheetDefaults.Elevation,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.modalWindowInsets },
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
    content: @Composable ColumnScope.() -> Unit,
) = ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    sheetState = sheetState,
    sheetMaxWidth = sheetMaxWidth,
    shape = shape,
    containerColor = containerColor,
    contentColor = contentColor,
    tonalElevation = tonalElevation,
    scrimColor = scrimColor,
    dragHandle = dragHandle,
    contentWindowInsets = contentWindowInsets,
    properties = properties,
    content = content
)

@Composable
fun OutlinedTextFieldSheet(
    @StringRes titleRes: Int,
    inputState: TextFieldState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
    text: String? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    placeholder: String? = null,
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    @StringRes actionLabel: Int = R.string.action_confirm,
    outputTransformation: OutputTransformation? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) = OutlinedTextFieldSheet(
    title = {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.medium)
        )
    },
    inputState = inputState,
    onDismiss = onDismiss,
    actionButton = {
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Text(stringResource(actionLabel))
        }
    },
    modifier = modifier,
    text = text?.let {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
        }
    },
    focusRequester = focusRequester,
    errorMessage = errorMessage,
    placeholder = placeholder,
    label = label,
    keyboardOptions = keyboardOptions,
    prefix = prefix,
    suffix = suffix,
    textStyle = textStyle,
    contentAfterTextField = contentAfterTextField,
    textFieldModifier = textFieldModifier,
    outputTransformation = outputTransformation
)

@Composable
fun OutlinedTextFieldSheet(
    title: @Composable () -> Unit,
    inputState: TextFieldState,
    onDismiss: () -> Unit,
    actionButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
    text: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    placeholder: String? = null,
    label: String? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) {
    OarModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        OutlinedTextFieldSheetContent(
            title = title,
            inputState = inputState,
            actionButton = actionButton,
            textFieldModifier = textFieldModifier,
            text = text,
            textStyle = textStyle,
            focusRequester = focusRequester,
            errorMessage = errorMessage,
            placeholder = placeholder,
            label = label,
            outputTransformation = outputTransformation,
            keyboardOptions = keyboardOptions,
            prefix = prefix,
            suffix = suffix,
            contentAfterTextField = contentAfterTextField
        )
    }
}

@Composable
fun OutlinedTextFieldSheetContent(
    title: @Composable () -> Unit,
    inputState: TextFieldState,
    actionButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
    text: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    placeholder: String? = null,
    label: String? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) {
    val isInputEmpty by remember {
        derivedStateOf { inputState.text.isEmpty() }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        modifier = modifier
    ) {
        title()

        text?.invoke()

        OarOutlinedTextField(
            state = inputState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .focusRequester(focusRequester)
                .then(textFieldModifier),
            keyboardOptions = keyboardOptions,
            label = label?.let { { Text(it) } },
            supportingText = { errorMessage?.let { Text(it.asString()) } },
            isError = errorMessage != null,
            placeholder = placeholder?.let { { Text(it) } },
            trailingIcon = {
                if (!isInputEmpty) {
                    IconButton(onClick = inputState::clearText) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.cd_clear)
                        )
                    }
                }
            },
            prefix = prefix,
            suffix = suffix,
            textStyle = textStyle,
            outputTransformation = outputTransformation
        )

        contentAfterTextField?.invoke(this)

        Box(
            modifier = Modifier
                .align(Alignment.End)
        ) {
            actionButton()
        }
    }
}

@Composable
fun OutlinedTextFieldSheetContent(
    @StringRes titleRes: Int,
    inputState: TextFieldState,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
    text: String? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    placeholder: String? = null,
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    @StringRes actionLabel: Int = R.string.action_confirm,
    outputTransformation: OutputTransformation? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) = OutlinedTextFieldSheetContent(
    title = {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.medium)
        )
    },
    inputState = inputState,
    actionButton = {
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Text(stringResource(actionLabel))
        }
    },
    modifier = modifier,
    text = text?.let {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
        }
    },
    focusRequester = focusRequester,
    errorMessage = errorMessage,
    placeholder = placeholder,
    label = label,
    keyboardOptions = keyboardOptions,
    prefix = prefix,
    suffix = suffix,
    textStyle = textStyle,
    contentAfterTextField = contentAfterTextField,
    textFieldModifier = textFieldModifier,
    outputTransformation = outputTransformation
)

@Composable
fun OutlinedPasswordFieldSheet(
    @StringRes titleRes: Int,
    inputState: TextFieldState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
    text: String? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    placeholder: String? = null,
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    @StringRes actionLabel: Int = R.string.action_confirm,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) = OutlinedPasswordFieldSheet(
    title = {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.medium)
        )
    },
    inputState = inputState,
    onDismiss = onDismiss,
    actionButton = {
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Text(stringResource(actionLabel))
        }
    },
    modifier = modifier,
    text = text?.let {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
        }
    },
    focusRequester = focusRequester,
    errorMessage = errorMessage,
    placeholder = placeholder,
    label = label,
    keyboardOptions = keyboardOptions,
    prefix = prefix,
    suffix = suffix,
    textStyle = textStyle,
    contentAfterTextField = contentAfterTextField,
    textFieldModifier = textFieldModifier
)

@Composable
fun OutlinedPasswordFieldSheet(
    title: @Composable () -> Unit,
    inputState: TextFieldState,
    onDismiss: () -> Unit,
    actionButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
    text: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    placeholder: String? = null,
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) {
    OarModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            modifier = Modifier
        ) {
            title()

            text?.invoke()

            OutlinedPasswordField(
                state = inputState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .focusRequester(focusRequester)
                    .then(textFieldModifier),
                keyboardOptions = keyboardOptions,
                label = label,
                supportingText = errorMessage?.asString(),
                isError = errorMessage != null,
                placeholder = placeholder,
                textStyle = textStyle,
                prefix = prefix,
                suffix = suffix
            )

            contentAfterTextField?.invoke(this)

            Box(
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                actionButton()
            }
        }
    }
}

@Composable
fun TextFieldSheet(
    title: @Composable () -> Unit,
    inputState: TextFieldState,
    onDismiss: () -> Unit,
    actionButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    text: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    placeholder: String? = null,
    label: String? = null,
    showClearOption: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    textFieldColors: TextFieldColors = TextFieldDefaults.colors(),
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) {
    OarModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        TextFieldSheetContent(
            title = title,
            inputState = inputState,
            actionButton = actionButton,
            text = text,
            textStyle = textStyle,
            focusRequester = focusRequester,
            errorMessage = errorMessage,
            placeholder = placeholder,
            label = label,
            showClearOption = showClearOption,
            keyboardOptions = keyboardOptions,
            prefix = prefix,
            suffix = suffix,
            textFieldColors = textFieldColors,
            contentAfterTextField = contentAfterTextField
        )
    }
}

@Composable
fun TextFieldSheetContent(
    title: @Composable () -> Unit,
    inputState: TextFieldState,
    actionButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    text: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    placeholder: String? = null,
    label: String? = null,
    showClearOption: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    textFieldColors: TextFieldColors = TextFieldDefaults.colors(),
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) {
    val isInputEmpty by remember {
        derivedStateOf { inputState.text.isEmpty() }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        modifier = modifier
            .padding(vertical = MaterialTheme.spacing.medium)
    ) {
        title()

        text?.invoke()

        OarTextField(
            state = inputState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .focusRequester(focusRequester),
            keyboardOptions = keyboardOptions,
            label = label?.let { { Text(it) } },
            supportingText = { errorMessage?.let { Text(it.asString()) } },
            isError = errorMessage != null,
            placeholder = placeholder?.let { { Text(it) } },
            trailingIcon = {
                if (showClearOption && !isInputEmpty) {
                    IconButton(onClick = inputState::clearText) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.cd_clear)
                        )
                    }
                }
            },
            prefix = prefix,
            suffix = suffix,
            textStyle = textStyle,
            colors = textFieldColors
        )

        contentAfterTextField?.invoke(this)

        Box(
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            actionButton()
        }
    }
}

@Composable
fun ListSearchSheet(
    inputState: TextFieldState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    placeholder: String? = null,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(
        top = MaterialTheme.spacing.medium,
        bottom = PaddingScrollEnd
    ),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    additionalEndContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: LazyListScope.() -> Unit
) {
    OarModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        ListSearchSheetContent(
            inputState = inputState,
            title = title,
            placeholder = placeholder,
            listState = listState,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            additionalEndContent = additionalEndContent,
            content = content
        )
    }
}

@Composable
fun ListSearchSheetContent(
    inputState: TextFieldState,
    modifier: Modifier = Modifier,
    title: String? = null,
    placeholder: String? = null,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(
        top = MaterialTheme.spacing.medium,
        bottom = PaddingScrollEnd
    ),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    additionalEndContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: LazyListScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        if (title != null) {
            TitleLargeText(
                text = title,
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
        }
        SearchField(
            state = inputState,
            placeholder = placeholder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        )
        LazyColumn(
            state = listState,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .weight(Float.One),
            content = content
        )

        additionalEndContent?.invoke(this)
    }
}

@Composable
fun <T> ItemListSheet(
    onDismiss: () -> Unit,
    items: List<T>,
    modifier: Modifier = Modifier,
    key: ((item: T) -> Any)? = null,
    contentType: (item: T) -> Any? = { null },
    itemContent: @Composable LazyItemScope.(item: T) -> Unit,
) {
    OarModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                bottom = PaddingScrollEnd
            )
        ) {
            items(
                items = items,
                key = key,
                contentType = contentType,
                itemContent = itemContent
            )
        }
    }
}