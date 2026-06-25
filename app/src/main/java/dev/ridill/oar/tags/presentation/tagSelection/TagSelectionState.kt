package dev.ridill.oar.tags.presentation.tagSelection

import dev.ridill.oar.tags.domain.model.Tag

data class TagSelectionState(
    val selectedIds: Set<Long> = emptySet(),
    val selectedTags: List<Tag> = emptyList()
)