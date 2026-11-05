package dev.ridill.oar.statistics.data

import dev.ridill.oar.statistics.data.local.relation.BusiestTagRelation
import dev.ridill.oar.statistics.data.local.relation.LargestSpendRelation
import dev.ridill.oar.statistics.data.local.relation.TagSpendRelation
import dev.ridill.oar.statistics.domain.model.BusiestTag
import dev.ridill.oar.statistics.domain.model.LargestSpend
import dev.ridill.oar.statistics.domain.model.TagSpend
import java.util.Currency

fun TagSpendRelation.toTagSpend(totalSpend: Double): TagSpend {
    val fraction = if (totalSpend > 0) (amount / totalSpend).toFloat() else 0f
    return if (tagId == null
        || tagName.isNullOrEmpty()
        || tagColorCode == null
    ) TagSpend(
        tagId = null,
        name = null,
        colorCode = null,
        amount = amount,
        fraction = fraction
    ) else TagSpend(
        tagId = tagId,
        name = tagName,
        colorCode = tagColorCode,
        amount = amount,
        fraction = fraction
    )
}

fun LargestSpendRelation.toLargestSpend(currency: Currency): LargestSpend = LargestSpend(
    amount = amount,
    note = note,
    currency = currency
)

fun BusiestTagRelation.toBusiestTag(): BusiestTag = BusiestTag(
    name = tagName,
    transactionCount = transactionCount
)
