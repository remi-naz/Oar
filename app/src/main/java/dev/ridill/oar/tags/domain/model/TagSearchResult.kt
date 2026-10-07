package dev.ridill.oar.tags.domain.model

data class TagSearchResult(
    val exactMatches: List<Tag>,
    val fuzzyMatches: List<Tag>
) {
    val hasExactMatch: Boolean get() = exactMatches.isNotEmpty()
    val all: List<Tag> get() = exactMatches + fuzzyMatches

    companion object {
        val EMPTY = TagSearchResult(emptyList(), emptyList())
    }
}
