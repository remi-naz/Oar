package dev.ridill.oar.moneyPiles.domain.model

data class MoneyPileDetails(
    val pile: MoneyPile,
    val history: List<PileHistoryEntry>
) {
    val progressPercent: Int?
        get() {
            val target = pile.targetAmount ?: return null
            if (target <= 0) return null
            return (pile.currentAmount / target * 100)
                .toInt()
                .coerceIn(0, 100)
        }

    val isGoalReached: Boolean
        get() {
            val target = pile.targetAmount ?: return false
            return pile.currentAmount >= target
        }

    val canWithdraw: Boolean
        get() = !pile.locked && pile.currentAmount > 0
}
