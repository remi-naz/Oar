package dev.ridill.oar.moneyPiles.presentation.pileDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.ridill.oar.core.ui.theme.OarTheme

@Composable
fun PileJarVisual(
    icon: String,
    accent: Color,
    progressFraction: Float?,
    modifier: Modifier = Modifier
) {
    val jarShape = RoundedCornerShape(
        topStart = 14.dp,
        topEnd = 14.dp,
        bottomStart = 22.dp,
        bottomEnd = 22.dp
    )
    Box(
        modifier = modifier
            .width(JarWidth)
            .height(JarHeight),
        contentAlignment = Alignment.Center
    ) {
        // Lid
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = LidWidth, height = LidHeight)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                .background(accent.copy(alpha = 0.35f))
                .border(
                    width = 2.dp,
                    color = accent.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp)
                )
        )

        // Jar body
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(JarBodyHeight)
                .clip(jarShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .border(width = 2.dp, color = accent.copy(alpha = 0.55f), shape = jarShape)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(progressFraction?.coerceIn(0f, 1f) ?: MinFillFraction)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.55f),
                                accent.copy(alpha = 0.32f)
                            )
                        )
                    )
            )
        }

        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

private val JarWidth = 126.dp
private val JarHeight = 150.dp
private val JarBodyHeight = 144.dp
private val LidWidth = 74.dp
private val LidHeight = 15.dp
private const val MinFillFraction = 0.16f

@PreviewLightDark
@Composable
private fun PreviewPileJarVisual() {
    OarTheme {
        PileJarVisual(
            icon = "🌸",
            accent = Color(0xFFFF4CA6),
            progressFraction = 0.64f
        )
    }
}
