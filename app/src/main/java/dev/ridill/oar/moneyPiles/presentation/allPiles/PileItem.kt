package dev.ridill.oar.moneyPiles.presentation.allPiles

import android.graphics.RuntimeShader
import androidx.annotation.FloatRange
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativePaint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.BuildUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.One
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.ui.components.Spacer
import dev.ridill.oar.core.ui.components.SpacerSmall
import dev.ridill.oar.core.ui.theme.BorderWidthStandard
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.IconSizeMedium
import dev.ridill.oar.core.ui.theme.IconSizeSmall
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.SelectableColorsList
import dev.ridill.oar.core.ui.theme.contentColor
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.TextFormat
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import java.util.Currency

@Composable
internal fun PileGridItem(
    icon: PileIcon,
    accent: Color,
    name: String,
    locked: Boolean,
    currency: Currency,
    savedAmount: Double,
    targetAmount: Double?,
    @FloatRange(from = 0.0, to = 1.0) progressFraction: Float,
    onClick: () -> Unit,
    onQuickAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    animationSeed: Int = 0,
) {
    val animatedProgress = animateFloatAsState(
        targetValue = progressFraction,
        label = "AnimatedProgress"
    )
    val shader = remember {
        if (BuildUtil.isApiLevelAtLeastTiramisu) RuntimeShader(WAVE_SHADER) else null
    }
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 50_000, easing = LinearEasing)
        ),
        label = "time"
    )
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier = modifier
            .height(IntrinsicSize.Max)
            .clip(shape)
            .clickable(
                onClick = onClick,
                onClickLabel = stringResource(R.string.cd_tap_to_view_pile_details)
            )
            .drawWithCache {
                onDrawBehind {
                    val outline = shape.createOutline(size, layoutDirection, Density(density))
                    val contentAlpha = ContentAlpha.PERCENT_16
                    if (BuildUtil.isApiLevelAtLeastTiramisu) {
                        shader?.setFloatUniform("resolution", size.width, size.height)
                        shader?.setFloatUniform("time", time)
                        shader?.setFloatUniform(
                            "level",
                            (animatedProgress.value).coerceIn(0f, 1f)
                        )
                        shader?.setFloatUniform("bufferMin", WAVE_ANIM_LIMIT_LOWER)
                        shader?.setFloatUniform("bufferMax", WAVE_ANIM_LIMIT_UPPER)
                        shader?.setColorUniform("waveColor", accent.toArgb())
                        shader?.setFloatUniform("seed", animationSeed.toFloat())
                        drawIntoCanvas { canvas ->
                            val paint = androidx.compose.ui.graphics.Paint().apply {
                                this.nativePaint.shader = shader
                                this.alpha = contentAlpha
                            }
                            canvas.drawRect(
                                left = 0f,
                                top = 0f,
                                right = size.width,
                                bottom = size.height,
                                paint = paint
                            )
                        }
                    } else {
                        clipRect(
                            left = Float.Zero,
                            top = size.height * (Float.One - animatedProgress.value),
                            right = size.width,
                            bottom = size.height
                        ) {
                            drawOutline(
                                outline = outline,
                                color = accent,
                                alpha = contentAlpha,
                            )
                        }
                    }

                    drawOutline(
                        outline = outline,
                        color = accent,
                        style = Stroke(BorderWidthStandard.toPx())
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier
                .heightIn(min = PileCardMinHeight)
                .padding(MaterialTheme.spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(icon.iconRes),
                            contentDescription = stringResource(icon.labelRes),
                            tint = accent,
                            modifier = Modifier
                                .size(IconSizeMedium)
                        )
                    }
                }
                if (locked) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_circle_lock),
                        contentDescription = stringResource(R.string.pile_locked),
                        modifier = Modifier
                            .size(IconSizeSmall)
                    )
                }
            }

            SpacerSmall()
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(weight = 1f)

            Text(
                text = TextFormat.currencyAmount(amount = savedAmount, currency = currency),
                style = MaterialTheme.typography.titleLarge
            )
            targetAmount?.let {
                Text(
                    text = stringResource(
                        R.string.of_value,
                        TextFormat.currencyAmount(amount = it, currency = currency)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        FilledTonalIconButton(
            onClick = onQuickAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = accent,
                contentColor = accent.contentColor()
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
            )
        }
    }
}

private val PileCardMinHeight = 158.dp
private const val WAVE_ANIM_LIMIT_LOWER = 0.05f
private const val WAVE_ANIM_LIMIT_UPPER = 0.95f

private val WAVE_SHADER = """
    uniform float2 resolution;
    uniform float time;
    uniform float level;
    uniform float bufferMin;
    uniform float bufferMax;
    uniform float seed; // unique per grid item, e.g. 0.0 - 1.0
    layout(color) uniform half4 waveColor;

    // cheap deterministic hash -> pseudo-random float in [0,1)
    float hash(float n) {
        return fract(sin(n * 12.9898) * 43758.5453);
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / resolution;

        float edge = 0.02;
        float fadeIn  = smoothstep(bufferMin - edge, bufferMin + edge, level);
        float fadeOut = 1.0 - smoothstep(bufferMax - edge, bufferMax + edge, level);
        float animFactor = fadeIn * fadeOut;

        float amplitudeSafety = smoothstep(0.0, 0.06, level) * smoothstep(0.0, 0.06, 1.0 - level);
        float amp = animFactor * amplitudeSafety;

        // Derive small per-item variations from the seed.
        // Keep the ranges tight so it still reads as "the same animation".
        float phase1 = hash(seed + 1.0) * 6.2831853; // 0 - 2π
        float phase2 = hash(seed + 2.0) * 6.2831853;
        float phase3 = hash(seed + 3.0) * 6.2831853;

        float speedJitter1 = 0.85 + hash(seed + 4.0) * 0.3; // 0.85 - 1.15
        float speedJitter2 = 0.85 + hash(seed + 5.0) * 0.3;
        float speedJitter3 = 0.85 + hash(seed + 6.0) * 0.3;

        float ampJitter = 0.85 + hash(seed + 7.0) * 0.3; // 0.85 - 1.15

        float wave1 = sin(uv.x * 10.0 + time * 0.9 * speedJitter1 + phase1) * 0.015 * amp * ampJitter;
        float wave2 = sin(uv.x * 20.0 - time * 0.7 * speedJitter2 + phase2) * 0.008 * amp * ampJitter;
        float wave3 = sin(uv.x * 6.0  + time * 0.5 * speedJitter3 + phase3) * 0.010 * amp * ampJitter;

        float waveY = clamp((1.0 - level) + wave1 + wave2 + wave3, 0.0, 1.0);
        float dist = uv.y - waveY;

        half4 bgColor = half4(0.0, 0.0, 0.0, 0.0);
        float mixFactor = smoothstep(0.0, 0.008, dist);

        return mix(bgColor, waveColor, mixFactor);
    }
""".trimIndent()

@PreviewLightDark
@Composable
private fun PreviewPileGridItem() {
    OarTheme {
        Surface {
            PileGridItem(
                icon = PileIcon.LandProperty,
                name = "Japan trip",
                accent = SelectableColorsList.random(),
                locked = true,
                onClick = {},
                onQuickAddClick = {},
                modifier = Modifier.fillMaxWidth(),
                currency = LocaleUtil.defaultCurrency,
                savedAmount = 100.0,
                targetAmount = 1000.0,
                progressFraction = 0.80f
            )
        }
    }
}