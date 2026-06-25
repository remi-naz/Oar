package dev.ridill.oar.tags.domain.model

import java.time.LocalDateTime

sealed class TagSelectionEntry {
    data class Tag(
        val id: Long,
        val name: String,
        val colorCode: Int,
        val createdTimestamp: LocalDateTime,
        val excluded: Boolean
    ) : TagSelectionEntry() {
        constructor(tag: dev.ridill.oar.tags.domain.model.Tag) : this(
            tag.id,
            tag.name,
            tag.colorCode,
            tag.createdTimestamp,
            tag.excluded
        )
    }

    data class NewTagIndicator(val label: String) : TagSelectionEntry()
}