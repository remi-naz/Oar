package dev.ridill.oar.statistics.domain.model

import androidx.annotation.StringRes
import dev.ridill.oar.R

enum class StatisticsChartMode(
    @StringRes val labelRes: Int
) {
    IN_VS_OUT(R.string.statistics_mode_in_vs_out),
    SPEND(R.string.statistics_mode_spend)
}
