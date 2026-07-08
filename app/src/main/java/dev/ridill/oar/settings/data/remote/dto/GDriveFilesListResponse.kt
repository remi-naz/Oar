package dev.ridill.oar.settings.data.remote.dto

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class GDriveFilesListResponse(
    @SerialName("files")
    val files: List<GDriveFileDto>,
)