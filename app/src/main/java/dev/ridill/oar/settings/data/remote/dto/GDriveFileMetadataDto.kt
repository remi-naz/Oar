package dev.ridill.oar.settings.data.remote.dto

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class GDriveFileMetadataDto(
    @SerialName("name")
    val name: String,
    @SerialName("parents")
    val parents: List<String>,
    @SerialName("appProperties")
    val appProperties: Map<String, String>
)
