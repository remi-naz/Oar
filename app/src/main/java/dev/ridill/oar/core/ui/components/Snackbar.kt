package dev.ridill.oar.core.ui.components

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import dev.ridill.oar.core.ui.util.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun OarSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (SnackbarData) -> Unit = { snackbarData ->
        OarSnackbar(
            snackbarData = snackbarData,
            onSwipeDismiss = { dismissValue ->
                if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                    snackbarData.dismiss()
                }
            }
        )
    }
) = SnackbarHost(
    hostState = hostState,
    modifier = modifier,
    snackbar = snackbar
)

@Composable
fun OarSnackbar(
    snackbarData: SnackbarData,
    onSwipeDismiss: (SwipeToDismissBoxValue) -> Unit,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = false,
    shape: Shape = MaterialTheme.shapes.small,
    containerColor: Color = SnackbarDefaults.color,
    contentColor: Color = SnackbarDefaults.contentColor,
    actionColor: Color = SnackbarDefaults.actionColor,
    actionContentColor: Color = SnackbarDefaults.actionContentColor,
    dismissActionContentColor: Color = SnackbarDefaults.dismissActionContentColor
) {
    val visuals = snackbarData.visuals as OarSnackbarVisuals
    val isError = visuals.isError
    val dismissState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {},
        enableDismissFromEndToStart = true,
        onDismiss = onSwipeDismiss,
        modifier = modifier
    ) {
        Snackbar(
            snackbarData = snackbarData,
            actionOnNewLine = actionOnNewLine,
            shape = shape,
            containerColor = if (isError) MaterialTheme.colorScheme.errorContainer
            else containerColor,
            contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer
            else contentColor,
            actionColor = actionColor,
            actionContentColor = actionContentColor,
            dismissActionContentColor = dismissActionContentColor
        )
    }

}

class OarSnackbarVisuals(
    val isError: Boolean,
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    override val message: String,
    override val withDismissAction: Boolean
) : SnackbarVisuals

class SnackbarController(
    val snackbarHostState: SnackbarHostState,
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private var snackbarJob: Job? = null

    private fun cancelCurrentJob() {
        snackbarJob?.cancel()
    }

    init {
        cancelCurrentJob()
    }

    fun showSnackbar(
        message: String,
        isError: Boolean = false,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short
        else SnackbarDuration.Indefinite,
        onSnackbarResult: ((SnackbarResult) -> Unit)? = null
    ) {
        cancelCurrentJob()
        snackbarJob = coroutineScope.launch {
            val visuals = OarSnackbarVisuals(
                isError = isError,
                actionLabel = actionLabel,
                duration = duration,
                message = message,
                withDismissAction = withDismissAction
            )

            val snackbarResult = snackbarHostState.showSnackbar(visuals)
            onSnackbarResult?.invoke(snackbarResult)
        }
    }

    fun showSnackbar(
        message: UiText,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short
        else SnackbarDuration.Indefinite,
        onSnackbarResult: ((SnackbarResult) -> Unit)? = null
    ) {
        cancelCurrentJob()
        snackbarJob = coroutineScope.launch {
            val visuals = OarSnackbarVisuals(
                isError = message.isErrorText,
                actionLabel = actionLabel,
                duration = duration,
                message = message.asString(context),
                withDismissAction = withDismissAction
            )

            val snackbarResult = snackbarHostState.showSnackbar(visuals)
            onSnackbarResult?.invoke(snackbarResult)
        }
    }
}

@Composable
fun rememberSnackbarController(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): SnackbarController = remember(snackbarHostState, coroutineScope) {
    SnackbarController(
        snackbarHostState = snackbarHostState,
        context = context,
        coroutineScope = coroutineScope
    )
}