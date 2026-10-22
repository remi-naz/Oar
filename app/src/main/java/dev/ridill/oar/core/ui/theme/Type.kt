package dev.ridill.oar.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.ridill.oar.R

private val ClashGroteskFontFamily = FontFamily(
    listOf(
        FontWeight.Thin,
        FontWeight.ExtraLight,
        FontWeight.Light,
        FontWeight.Normal,
        FontWeight.Medium,
        FontWeight.SemiBold,
        FontWeight.Bold,
        FontWeight.ExtraBold,
        FontWeight.Black
    ).map { weight ->
        Font(
            resId = R.font.clash_grotesk,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
        )
    }
)

private fun TextStyle.clashGrotesk() = copy(fontFamily = ClashGroteskFontFamily)

private val DefaultTypography = Typography()
val Typography = with(DefaultTypography) {
    Typography(
        displayLarge = displayLarge.clashGrotesk(),
        displayMedium = displayMedium.clashGrotesk(),
        displaySmall = displaySmall.clashGrotesk(),
        headlineLarge = headlineLarge.clashGrotesk(),
        headlineMedium = headlineMedium.clashGrotesk(),
        headlineSmall = headlineSmall.clashGrotesk(),
        titleLarge = titleLarge.clashGrotesk(),
        titleMedium = titleMedium.clashGrotesk().copy(fontSize = 18.sp),
        titleSmall = titleSmall.clashGrotesk(),
        bodyLarge = bodyLarge.clashGrotesk(),
        bodyMedium = bodyMedium.clashGrotesk(),
        bodySmall = bodySmall.clashGrotesk(),
        labelLarge = labelLarge.clashGrotesk(),
        labelMedium = labelMedium.clashGrotesk(),
        labelSmall = labelSmall.clashGrotesk()
    )
}