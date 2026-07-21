package dev.ridill.oar.core.ui.components

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import dev.ridill.oar.core.ui.theme.ContentAlpha
import dev.ridill.oar.core.ui.theme.spacing

@Composable
fun EmptyListIndicator(
    @RawRes rawResId: Int,
    modifier: Modifier = Modifier,
    @StringRes messageRes: Int? = null,
    size: Dp = DefaultSize,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        InfiniteLottieAnim(
            resId = rawResId,
            modifier = Modifier
                .size(size)
        )
        messageRes?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalContentColor.current
                    .copy(alpha = ContentAlpha.SUB_CONTENT)
            )
        }

        if (actionLabel.isNullOrEmpty().not() && onActionClick != null) {
            TextButton(
                onClick = onActionClick
            ) {
                Text(actionLabel)
            }
        }
    }
}

private val DefaultSize = 80.dp

fun LazyListScope.listEmptyIndicator(
    isListEmpty: Boolean,
    modifier: Modifier = Modifier,
    key: String = DEFAULT_KEY,
    contentType: String = DEFAULT_CONTENT_TYPE,
    @RawRes animResId: Int = R.raw.lottie_empty_list_ghost,
    @StringRes messageRes: Int? = null,
    heightFraction: Float = LIST_EMPTY_INDICATOR_CONTAINER_HEIGHT_FRACTION,
    size: Dp = DefaultSize,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    if (isListEmpty) {
        item(
            key = key,
            contentType = contentType
        ) {
            ListEmptyIndicatorItem(
                modifier = modifier,
                animResId = animResId,
                messageRes = messageRes,
                heightFraction = heightFraction,
                size = size,
                actionLabel = actionLabel,
                onActionClick = onActionClick
            )
        }
    }
}

private const val DEFAULT_KEY = "ListEmptyIndicator"
private const val DEFAULT_CONTENT_TYPE = "ListEmptyIndicator"

@Composable
fun LazyItemScope.ListEmptyIndicatorItem(
    modifier: Modifier = Modifier,
    @RawRes animResId: Int = R.raw.lottie_empty_list_ghost,
    @StringRes messageRes: Int? = null,
    heightFraction: Float = LIST_EMPTY_INDICATOR_CONTAINER_HEIGHT_FRACTION,
    size: Dp = DefaultSize,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillParentMaxWidth()
            .fillParentMaxHeight(heightFraction)
            .animateItem()
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        EmptyListIndicator(
            rawResId = animResId,
            messageRes = messageRes,
            size = size,
            actionLabel = actionLabel,
            onActionClick = onActionClick
        )
    }
}

private const val LIST_EMPTY_INDICATOR_CONTAINER_HEIGHT_FRACTION = 0.5f