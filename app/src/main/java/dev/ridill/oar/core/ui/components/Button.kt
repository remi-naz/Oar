package dev.ridill.oar.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.common.SignInButton
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.theme.OarTheme

@Composable
fun BackArrowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.cd_navigate_back)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ButtonWithLoadingIndicator(
    @StringRes textRes: Int,
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) = ButtonWithLoadingIndicator(
    text = stringResource(textRes),
    loading = loading,
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    colors = colors
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ButtonWithLoadingIndicator(
    text: String,
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    Button(
        onClick = {
            if (!loading) onClick()
        },
        modifier = modifier,
        enabled = enabled,
        colors = colors
    ) {
        Crossfade(
            targetState = loading,
            label = "TextLoadingCrossfade"
        ) { isLoading ->
            if (isLoading) {
                LoadingIndicator(
                    modifier = Modifier
                        .size(ButtonDefaults.IconSize),
                    color = colors.contentColor
                )
            } else {
                Text(text)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewButtonWithLoadingIndicator() {
    OarTheme {
        var isLoading by remember { mutableStateOf(false) }
        ButtonWithLoadingIndicator(
            textRes = R.string.action_confirm,
            loading = isLoading,
            onClick = {
                isLoading = !isLoading
            },
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun CancelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = stringResource(R.string.action_cancel)
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = contentDescription
        )
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = {
            SignInButton(it).apply {
                this.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_AUTO)
                this.setOnClickListener { onClick() }
            }
        },
        modifier = modifier
    )
}