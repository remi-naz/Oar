package dev.ridill.oar.transactions.domain.model

import androidx.compose.ui.graphics.Color

data class TagIndicator(
    val id: Long,
    val name: String,
    val color: Color
)

data class FolderIndicator(
    val id: Long,
    val name: String,
)