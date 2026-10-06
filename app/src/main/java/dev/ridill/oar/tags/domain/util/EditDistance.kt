package dev.ridill.oar.tags.domain.util

/** Damerau-Levenshtein distance (optimal string alignment: a transposition costs 1 edit). */
class EditDistance {

    /** Returns the distance, or null if it exceeds [maxDistance] (early-exit, avoids full DP cost). */
    fun withinDistance(a: String, b: String, maxDistance: Int): Int? {
        if (kotlin.math.abs(a.length - b.length) > maxDistance) return null
        if (a == b) return 0

        val rows = a.length + 1
        val cols = b.length + 1
        val dp = Array(rows) { IntArray(cols) }
        for (i in 0 until rows) dp[i][0] = i
        for (j in 0 until cols) dp[0][j] = j

        for (i in 1 until rows) {
            var rowMin = dp[i][0]
            for (j in 1 until cols) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                var value = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
                if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) {
                    value = minOf(value, dp[i - 2][j - 2] + 1)
                }
                dp[i][j] = value
                rowMin = minOf(rowMin, value)
            }
            if (rowMin > maxDistance) return null
        }

        val result = dp[rows - 1][cols - 1]
        return result.takeIf { it <= maxDistance }
    }
}
