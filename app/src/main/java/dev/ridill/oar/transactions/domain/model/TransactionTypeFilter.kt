package dev.ridill.oar.transactions.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.model.FundMovement

enum class TransactionTypeFilter(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int
) {
    DEBITS(R.drawable.ic_rounded_arrow_down_left, R.string.debits),
    ALL(R.drawable.ic_rounded_arrow_up_down, R.string.all),
    CREDITS(R.drawable.ic_rounded_arrow_up_right, R.string.credits);

    companion object {
        fun mapToTransactionType(
            filter: TransactionTypeFilter
        ): FundMovement? = when (filter) {
            DEBITS -> FundMovement.OUT
            CREDITS -> FundMovement.IN
            ALL -> null
        }
    }
}