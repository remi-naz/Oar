package dev.ridill.oar.settings.data.remote.dto

import androidx.annotation.Keep
import dev.ridill.oar.settings.data.repository.APP_DATA_SPACE
import dev.ridill.oar.settings.data.repository.G_DRIVE_FOLDER_MIME_TYPE
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class CreateGDriveFolderRequestDto(
    @SerialName("name")
    val name: String,
    @SerialName("parents")
    val parents: List<String> = listOf(APP_DATA_SPACE),
    @SerialName("mimeType")
    val mimeType: String = G_DRIVE_FOLDER_MIME_TYPE
)