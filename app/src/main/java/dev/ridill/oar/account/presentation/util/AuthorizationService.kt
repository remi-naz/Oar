package dev.ridill.oar.account.presentation.util

import android.app.PendingIntent
import android.content.Intent
import dev.ridill.oar.core.domain.model.Error

interface AuthorizationService {
    suspend fun getIntentSenderForAuthorization(): AuthorizationResult
    fun verifyAuthorizationResult(intent: Intent?)
    suspend fun getAccessToken(): String

    enum class AuthorizationError : Error {
        NEEDS_RESOLUTION,
        AUTHORIZATION_FAILED
    }
}

data class AuthorizationResult(
    val pendingIntent: PendingIntent?
)