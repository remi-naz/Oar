package dev.ridill.oar.core.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.paging.compose.LazyPagingItems
import dev.ridill.oar.core.domain.util.One
import dev.ridill.oar.core.domain.util.Zero
import dev.ridill.oar.core.ui.theme.ContentAlpha

fun <T : Any> LazyPagingItems<T>.isEmpty(): Boolean = this.itemCount == 0
fun <T : Any> LazyPagingItems<T>.isNotEmpty(): Boolean = !this.isEmpty()

fun Modifier.mergedContentDescription(
    contentDescription: String?
): Modifier = this
    .semantics(mergeDescendants = true) {}
    .clearAndSetSemantics {
        contentDescription?.let {
            this.contentDescription = it
        }
    }

fun Modifier.exclusionGraphicsLayer(excluded: Boolean): Modifier = this
    .graphicsLayer {
        alpha = if (excluded) ContentAlpha.EXCLUDED
        else Float.One
    }

private class SubtractedCornerSize(
    private val original: CornerSize,
    private val minus: Dp,
) : CornerSize {
    override fun toPx(
        shapeSize: Size,
        density: Density
    ): Float {
        val originalPx = original.toPx(shapeSize, density)
        val minusPx = with(density) { minus.toPx() }
        return (originalPx - minusPx).coerceAtLeast(Float.Zero)
    }
}

private class AddedCornerSize(
    private val original: CornerSize,
    private val add: Dp,
) : CornerSize {
    override fun toPx(
        shapeSize: Size,
        density: Density
    ): Float {
        val originalPx = original.toPx(shapeSize, density)
        val addPx = with(density) { add.toPx() }
        return originalPx + addPx
    }
}

infix operator fun CornerBasedShape.minus(minus: Dp): CornerBasedShape = this.copy(
    topStart = SubtractedCornerSize(this.topStart, minus),
    topEnd = SubtractedCornerSize(this.topEnd, minus),
    bottomStart = SubtractedCornerSize(this.bottomStart, minus),
    bottomEnd = SubtractedCornerSize(this.bottomEnd, minus),
)

infix operator fun CornerBasedShape.minus(minus: PaddingValues): CornerBasedShape = this.copy(
    topStart = SubtractedCornerSize(this.topStart, minus.calculateTopPadding()),
    topEnd = SubtractedCornerSize(this.topEnd, minus.calculateTopPadding()),
    bottomStart = SubtractedCornerSize(this.bottomStart, minus.calculateBottomPadding()),
    bottomEnd = SubtractedCornerSize(this.bottomEnd, minus.calculateBottomPadding()),
)

infix operator fun CornerBasedShape.plus(minus: Dp): CornerBasedShape = this.copy(
    topStart = AddedCornerSize(this.topStart, minus),
    topEnd = AddedCornerSize(this.topEnd, minus),
    bottomStart = AddedCornerSize(this.bottomStart, minus),
    bottomEnd = AddedCornerSize(this.bottomEnd, minus),
)

infix operator fun CornerBasedShape.plus(minus: PaddingValues): CornerBasedShape = this.copy(
    topStart = AddedCornerSize(this.topStart, minus.calculateTopPadding()),
    topEnd = AddedCornerSize(this.topEnd, minus.calculateTopPadding()),
    bottomStart = AddedCornerSize(this.bottomStart, minus.calculateBottomPadding()),
    bottomEnd = AddedCornerSize(this.bottomEnd, minus.calculateBottomPadding()),
)