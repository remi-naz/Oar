package dev.ridill.oar.statistics.presentation

import dev.ridill.oar.core.domain.model.ChartSlice
import dev.ridill.oar.statistics.domain.model.TagSpend

fun TagSpend.toChartSlice(): ChartSlice = ChartSlice(
    id = tagId,
    label = name,
    amount = amount,
    fraction = fraction,
    colorCode = colorCode
)
