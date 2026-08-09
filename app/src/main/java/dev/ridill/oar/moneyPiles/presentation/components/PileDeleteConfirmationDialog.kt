package dev.ridill.oar.moneyPiles.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.ConfirmationDialog
import dev.ridill.oar.core.ui.theme.OarTheme

@Composable
internal fun PileDeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ConfirmationDialog(
        titleRes = R.string.delete_pile_confirmation_title,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        modifier = modifier,
        additionalNote = stringResource(R.string.delete_pile_confirmation_note),
    )
}

@PreviewLightDark
@Composable
private fun PreviewPileDeleteConfirmationDialog() {
    OarTheme {
        PileDeleteConfirmationDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}
