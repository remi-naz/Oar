package dev.ridill.oar.settings.data.remote.dto

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class GDriveErrorDto(
    @SerialName("error")
    val data: ErrorData
)

@Serializable
data class ErrorData(
    @SerialName("errors")
    val reasons: List<ErrorReason>,
    @SerialName("code")
    val code: Int,
    @SerialName("message")
    val message: String
)

@Serializable
data class ErrorReason(
    @SerialName("domain")
    val domain: String,
    @SerialName("reason")
    val reason: String,
    @SerialName("message")
    val message: String
)