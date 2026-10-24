package dev.ridill.oar.onboarding.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.DisplayMediumText
import dev.ridill.oar.core.ui.components.LottieAnim
import dev.ridill.oar.core.ui.theme.spacing

@Composable
fun WelcomeMessagePage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        DisplayMediumText(
            text = stringResource(
                R.string.onboarding_page_welcome_title,
                stringResource(R.string.app_name)
            ),
            modifier = Modifier
                .padding(vertical = MaterialTheme.spacing.medium)
        )

        val contentColor = MaterialTheme.colorScheme.onPrimary.toArgb()
        val accentColor = MaterialTheme.colorScheme.inversePrimary.toArgb()
        val dynamicProperties = rememberLottieDynamicProperties(
            // light blue "trait" layers
            *(1..5).map { i ->
                rememberLottieDynamicProperty(
                    property = LottieProperty.STROKE_COLOR,
                    value = accentColor,
                    keyPath = arrayOf("trait | $i", "**", "Contour 1")
                )
            }.toTypedArray(),
            // everything else
            *listOf(
                "rond",
                "rectangle",
                "rectangle 2",
                "ligne 1",
                "ligne 2",
                "billet 1",
                "billet 2"
            ).map { layer ->
                rememberLottieDynamicProperty(
                    property = LottieProperty.STROKE_COLOR,
                    value = contentColor,
                    keyPath = arrayOf(layer, "**", "Contour 1")
                )
            }.toTypedArray()
        )
        LottieAnim(
            resId = R.raw.lottie_wallet,
            modifier = Modifier
                .fillMaxWidth()
                .height(WelcomeAnimHeight),
            dynamicProperties = dynamicProperties
        )
    }
}

private val WelcomeAnimHeight = 200.dp