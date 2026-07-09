package dev.ridill.oar.arithmetic.presentation.arithmeticNumpad

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ridill.oar.R
import dev.ridill.oar.arithmetic.domain.NumpadAction
import dev.ridill.oar.arithmetic.domain.Operation
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing

private data class NumpadButtonMetrics(
    val fontSize: TextUnit,
    val iconSize: Dp,
)

private val LocalNumpadButtonMetrics = compositionLocalOf {
    NumpadButtonMetrics(fontSize = 16.sp, iconSize = 24.dp)
}

private const val NUMPAD_COLUMNS = 4

@Composable
internal fun ArithmeticNumpad(
    onAction: (NumpadAction) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    doneForEquals: Boolean = false,
) {
    Surface(
        color = containerColor,
    ) {
        BoxWithConstraints(modifier = modifier) {
            val spacing = MaterialTheme.spacing.small
            val buttonSize = (maxWidth - spacing * (NUMPAD_COLUMNS - 1)) / NUMPAD_COLUMNS
            val density = LocalDensity.current
            val metrics = remember(buttonSize) {
                NumpadButtonMetrics(
                    fontSize = with(density) { (buttonSize * FONT_SIZE_FRACTION).toSp() },
                    iconSize = buttonSize * ICON_SIZE_FRACTION
                )
            }

            CompositionLocalProvider(LocalNumpadButtonMetrics provides metrics) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        StringNumpadButton(
                            label = "AC",
                            onClick = { onAction(NumpadAction.Clear) },
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        IconNumpadButton(
                            iconRes = R.drawable.ic_rounded_paranthesis,
                            contentDescription = stringResource(R.string.cd_parenthesis),
                            onClick = { onAction(NumpadAction.Parenthesis) },
                            containerColor = OperatorContainerColor,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        StringNumpadButton(
                            label = "00",
                            onClick = { onAction(NumpadAction.MultiplyHundred) },
                            containerColor = OperatorContainerColor,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        IconNumpadButton(
                            iconRes = Operation.Divide.iconRes,
                            contentDescription = stringResource(Operation.Divide.contentDescriptionRes),
                            onClick = { onAction(NumpadAction.OperatorInput(Operation.Divide)) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        StringNumpadButton(
                            label = "7",
                            onClick = { onAction(NumpadAction.Number('7')) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        StringNumpadButton(
                            label = "8",
                            onClick = { onAction(NumpadAction.Number('8')) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        StringNumpadButton(
                            label = "9",
                            onClick = { onAction(NumpadAction.Number('9')) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        IconNumpadButton(
                            iconRes = Operation.Multiply.iconRes,
                            contentDescription = stringResource(Operation.Multiply.contentDescriptionRes),
                            onClick = { onAction(NumpadAction.OperatorInput(Operation.Multiply)) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        StringNumpadButton(
                            label = "4",
                            onClick = { onAction(NumpadAction.Number('4')) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        StringNumpadButton(
                            label = "5",
                            onClick = { onAction(NumpadAction.Number('5')) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        StringNumpadButton(
                            label = "6",
                            onClick = { onAction(NumpadAction.Number('6')) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        IconNumpadButton(
                            iconRes = Operation.Subtract.iconRes,
                            contentDescription = stringResource(Operation.Subtract.contentDescriptionRes),
                            onClick = { onAction(NumpadAction.OperatorInput(Operation.Subtract)) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        StringNumpadButton(
                            label = "1",
                            onClick = { onAction(NumpadAction.Number('1')) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        StringNumpadButton(
                            label = "2",
                            onClick = { onAction(NumpadAction.Number('2')) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        StringNumpadButton(
                            label = "3",
                            onClick = { onAction(NumpadAction.Number('3')) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        IconNumpadButton(
                            iconRes = Operation.Add.iconRes,
                            contentDescription = stringResource(Operation.Add.contentDescriptionRes),
                            onClick = { onAction(NumpadAction.OperatorInput(Operation.Add)) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        StringNumpadButton(
                            label = "0",
                            onClick = { onAction(NumpadAction.Number('0')) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        StringNumpadButton(
                            label = ".",
                            onClick = { onAction(NumpadAction.Decimal) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        IconNumpadButton(
                            iconRes = R.drawable.ic_outlined_backspace,
                            contentDescription = stringResource(R.string.cd_backspace),
                            containerColor = NumberContainerColor,
                            onClick = { onAction(NumpadAction.Backspace) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )

                        IconNumpadButton(
                            iconRes = if (doneForEquals) R.drawable.ic_rounded_tick
                            else R.drawable.ic_rounded_equal,
                            contentDescription = stringResource(R.string.cd_done),
                            onClick = {
                                onAction(
                                    if (doneForEquals) NumpadAction.Done
                                    else NumpadAction.Equals
                                )
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

private const val FONT_SIZE_FRACTION = 0.32f
private const val ICON_SIZE_FRACTION = 0.40f

@Composable
private fun StringNumpadButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = NumberContainerColor,
    contentColor: Color = contentColorFor(containerColor),
) {
    val metrics = LocalNumpadButtonMetrics.current
    Button(
        onClick = onClick,
        shapes = ButtonDefaults.shapes(),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        contentPadding = PaddingValues(MaterialTheme.spacing.extraSmall),
        modifier = modifier
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Normal,
            fontSize = metrics.fontSize,
        )
    }
}

@Composable
fun IconNumpadButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = OperatorContainerColor,
    contentColor: Color = contentColorFor(containerColor),
) {
    val metrics = LocalNumpadButtonMetrics.current
    FilledTonalIconButton(
        onClick = onClick,
        shapes = IconButtonDefaults.shapes(),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        modifier = modifier
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(metrics.iconSize)
        )
    }
}

private val NumberContainerColor: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceContainer
private val OperatorContainerColor: Color
    @Composable get() = MaterialTheme.colorScheme.secondaryContainer

@PreviewLightDark
@Composable
private fun PreviewArithmeticNumpad() {
    OarTheme {
        ArithmeticNumpad(
            onAction = {}
        )
    }
}
