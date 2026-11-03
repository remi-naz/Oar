package dev.ridill.oar.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import dev.ridill.oar.R

@Composable
fun OarImage(
    url: String,
    contentDescription: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: CornerBasedShape = CircleShape,
    @DrawableRes errorRes: Int? = R.drawable.ic_outlined_image_not_found,
    @DrawableRes placeholderRes: Int? = null
) {
    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier
            .clip(shape)
            .size(size),
        contentScale = contentScale,
        error = errorRes?.let { painterResource(it) },
        placeholder = placeholderRes?.let { painterResource(it) }
    )
}