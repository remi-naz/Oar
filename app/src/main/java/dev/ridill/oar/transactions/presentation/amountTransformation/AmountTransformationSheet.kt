package dev.ridill.oar.transactions.presentation.amountTransformation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.TextFieldSheetContent
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.transactions.domain.model.AmountTransformation

@Composable
fun AmountTransformationSheet(
    selectedTransformation: AmountTransformation,
    onTransformationSelect: (AmountTransformation) -> Unit,
    factorInput: TextFieldState,
    onTransformClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transformationsCount = remember { AmountTransformation.entries.size }
    val keyboardOptions = remember {
        KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        )
    }
    val labelRes = remember(selectedTransformation) {
        when (selectedTransformation) {
            AmountTransformation.DIVIDE_BY -> R.string.enter_divider
            AmountTransformation.MULTIPLIER -> R.string.enter_multiplier
            AmountTransformation.PERCENT -> R.string.enter_percent
        }
    }
    TextFieldSheetContent(
        title = {
            Text(
                text = stringResource(R.string.transform_amount),
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
        },
        inputState = factorInput,
        text = {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) {
                AmountTransformation.entries.forEachIndexed { index, transformation ->
                    SegmentedButton(
                        selected = selectedTransformation == transformation,
                        onClick = { onTransformationSelect(transformation) },
                        shape = SegmentedButtonDefaults
                            .itemShape(index = index, count = transformationsCount)
                    ) {
                        Text(stringResource(transformation.labelRes))
                    }
                }
            }
        },
        actionButton = {
            Button(onClick = onTransformClick) {
                Text(text = stringResource(R.string.transform))
            }
        },
        modifier = modifier,
        keyboardOptions = keyboardOptions,
        label = stringResource(labelRes),
        suffix = { Text(selectedTransformation.symbol) },
        textStyle = LocalTextStyle.current
            .copy(textAlign = TextAlign.End),
        showClearOption = false
    )
}