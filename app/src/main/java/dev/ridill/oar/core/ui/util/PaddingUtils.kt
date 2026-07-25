package dev.ridill.oar.core.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import dev.ridill.oar.core.domain.util.orZero

enum class PaddingSide { TOP, BOTTOM, START, END }

@Composable
fun PaddingValues.only(
    vararg sides: PaddingSide,
    layoutDirection: LayoutDirection = LocalLayoutDirection.current
): PaddingValues = PaddingValues(
    start = this.calculateStartPadding(layoutDirection).takeIf { PaddingSide.START in sides }
        .orZero(),
    top = this.calculateTopPadding().takeIf { PaddingSide.TOP in sides }.orZero(),
    end = this.calculateEndPadding(layoutDirection).takeIf { PaddingSide.END in sides }
        .orZero(),
    bottom = this.calculateBottomPadding().takeIf { PaddingSide.BOTTOM in sides }.orZero(),
)

@Composable
fun PaddingValues.exclude(
    vararg sides: PaddingSide,
    layoutDirection: LayoutDirection = LocalLayoutDirection.current
): PaddingValues = PaddingValues(
    start = this.calculateStartPadding(layoutDirection).takeIf { PaddingSide.START !in sides }
        .orZero(),
    top = this.calculateTopPadding().takeIf { PaddingSide.TOP !in sides }.orZero(),
    end = this.calculateEndPadding(layoutDirection).takeIf { PaddingSide.END !in sides }
        .orZero(),
    bottom = this.calculateBottomPadding().takeIf { PaddingSide.BOTTOM !in sides }.orZero(),
)

@Composable
fun PaddingValues.onlyTop(
    layoutDirection: LayoutDirection = LocalLayoutDirection.current
): PaddingValues = this.only(PaddingSide.TOP, layoutDirection = layoutDirection)

@Composable
fun PaddingValues.excludeTop(
    layoutDirection: LayoutDirection = LocalLayoutDirection.current
): PaddingValues = this.exclude(PaddingSide.TOP, layoutDirection = layoutDirection)

@Composable
infix operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        top = this.calculateTopPadding() + other.calculateTopPadding(),
        start = this.calculateStartPadding(layoutDirection) + other.calculateStartPadding(
            layoutDirection
        ),
        end = this.calculateEndPadding(layoutDirection) + other.calculateEndPadding(layoutDirection),
        bottom = this.calculateBottomPadding() + other.calculateBottomPadding(),
    )
}