package dev.ridill.oar.statistics.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.EmptyListIndicator
import dev.ridill.oar.core.ui.components.SpacerLarge
import dev.ridill.oar.core.ui.components.SpacerMedium
import dev.ridill.oar.core.ui.components.TitleLargeText
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing

@Composable
fun StatisticsEmptyState(
    onAddTransactionClick: () -> Unit,
    onChooseCycleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.extraLarge)
    ) {
        TitleLargeText(
            text = stringResource(R.string.statistics_empty_title),
            textAlign = TextAlign.Center
        )

        SpacerMedium()

        EmptyListIndicator(
            rawResId = R.raw.lottie_empty_list_ghost,
            messageRes = R.string.statistics_empty_message
        )

        SpacerLarge()

        Button(onClick = onAddTransactionClick) {
            Text(stringResource(R.string.new_transaction))
        }

        TextButton(onClick = onChooseCycleClick) {
            Text(stringResource(R.string.statistics_choose_another_cycle))
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewStatisticsEmptyState() {
    OarTheme {
        StatisticsEmptyState(
            onAddTransactionClick = {},
            onChooseCycleClick = {},
            modifier = Modifier
                .fillMaxSize()
        )
    }
}
