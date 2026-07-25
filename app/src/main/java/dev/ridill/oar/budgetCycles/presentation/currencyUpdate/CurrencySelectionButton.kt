package dev.ridill.oar.budgetCycles.presentation.currencyUpdate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.ui.components.ComponentViewModelScope
import dev.ridill.oar.core.ui.components.OarModalBottomSheet
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.core.ui.util.plus
import java.util.Currency

@Composable
fun CurrencySelectionButton(
    currency: Currency,
    onCurrencySelect: (Currency) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        CurrencySelectionButton(
            currency = LocaleUtil.defaultCurrency,
            onClick = {},
            modifier = modifier
        )
    } else {
        ComponentViewModelScope("currency_selection_button") {
            val viewModel: CurrencySelectionViewModel = hiltViewModel()
            var showCurrencySelectionSheet by remember { mutableStateOf(false) }
            CurrencySelectionButton(
                currency = currency,
                onClick = { showCurrencySelectionSheet = true },
                modifier = modifier
            )

            if (showCurrencySelectionSheet) {
                OarModalBottomSheet(
                    onDismissRequest = { showCurrencySelectionSheet = false }
                ) {
                    CurrencySelectionSheet(
                        searchQueryState = viewModel.searchQueryState,
                        selectedCode = currency.currencyCode,
                        currenciesPagingData = viewModel.currencyPagingData.collectAsLazyPagingItems(),
                        onConfirm = {
                            onCurrencySelect(it)
                            showCurrencySelectionSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrencySelectionButton(
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentPadding: PaddingValues = ContentPadding,
) {
    val containerShape = shape + contentPadding
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = containerShape,
        color = color
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            modifier = Modifier
                .padding(contentPadding),
        ) {
            val iconContainerSize = IconButtonDefaults.extraSmallContainerSize()
            Box(
                modifier = Modifier
                    .defaultMinSize(
                        minHeight = iconContainerSize.height,
                        minWidth = iconContainerSize.width,
                    )
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = currency.symbol,
                    style = ButtonDefaults.textStyleFor(iconContainerSize.height),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Text(
                text = currency.currencyCode.uppercase(),
                style = ButtonDefaults.textStyleFor(iconContainerSize.height),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private val ContentPadding: PaddingValues = PaddingValues(4.dp)

@PreviewLightDark
@Composable
private fun PreviewCurrencySelectionButton() {
    OarTheme {
        CurrencySelectionButton(
            currency = LocaleUtil.defaultCurrency,
            onCurrencySelect = {}
        )
    }
}
