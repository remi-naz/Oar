package dev.ridill.oar.core.ui.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.core.domain.model.creditOrDebitLabel

@Composable
fun MovementIndicatorIcon(
    movement: FundMovement,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = ImageVector.vectorResource(movement.iconRes),
        contentDescription = stringResource(movement.creditOrDebitLabel),
        tint = movement.color,
        modifier = modifier
    )
}
