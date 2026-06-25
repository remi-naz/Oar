package dev.ridill.oar.tags.presentation.tagSelection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.components.OarModalBottomSheet
import dev.ridill.oar.core.ui.components.TitleLargeText
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.theme.spacing

@Composable
fun SingleTagSelectionSheet(
    onDismiss: () -> Unit,
    preSelectedId: Long?,
    onConfirm: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    OarModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        SingleTagSelectionSheetContent(
            preSelectedId = preSelectedId,
            onConfirm = { id -> onConfirm(id) },
        )
    }
}


@Composable
private fun SingleTagSelectionSheetContent(
    preSelectedId: Long?,
    onConfirm: (Long?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
            .padding(bottom = MaterialTheme.spacing.large)
            .imePadding()
    ) {
        TitleLargeText(text = pluralStringResource(id = R.plurals.select_tag, count = 1))

        TagSelectionField(
            selectedId = preSelectedId,
            onSelectedIdChange = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun PreviewSingleTagSelectionSheetContent() {
    OarTheme {
        Surface {
            SingleTagSelectionSheetContent(
                preSelectedId = null,
                onConfirm = {}
            )
        }
    }
}