package dev.ridill.oar.folders.domain.model

import java.time.LocalDateTime

data class FolderDetails(
    val id: Long,
    val name: String,
    val createdTimestamp: LocalDateTime,
    val excluded: Boolean,
)