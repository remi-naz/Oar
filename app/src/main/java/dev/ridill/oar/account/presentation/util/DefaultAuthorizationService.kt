package dev.ridill.oar.account.presentation.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.accounts.Account
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import dev.ridill.oar.account.domain.service.AuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DefaultAuthorizationService(
    context: Context,
    private val authService: AuthService
) : AuthorizationService {
    private val authorizationClient = Identity.getAuthorizationClient(context)

    private val scopes
        get() = listOf(Scopes.DRIVE_APPFOLDER)
            .map { Scope(it) }

    private fun buildAuthorizationRequest(): AuthorizationRequest = AuthorizationRequest.Builder()
        .setRequestedScopes(scopes)
        .apply {
            authService.getSignedInAccount()?.email?.let { setAccount(Account(it, "com.google")) }
        }
        .build()

    @Throws(AuthorizationNeedsResolutionThrowable::class)
    override suspend fun getIntentSenderForAuthorization(): AuthorizationResult =
        withContext(Dispatchers.IO) {
            val result = authorizationClient.authorize(buildAuthorizationRequest()).await()
            if (result.hasResolution())
                throw AuthorizationNeedsResolutionThrowable(result.pendingIntent)

            AuthorizationResult(result.pendingIntent)
        }

    @Throws(AuthorizationNeedsResolutionThrowable::class, AuthorizationFailedThrowable::class)
    override fun verifyAuthorizationResult(intent: Intent?) {
        val result = authorizationClient.getAuthorizationResultFromIntent(intent)
        if (result.hasResolution()) throw AuthorizationNeedsResolutionThrowable(result.pendingIntent)
        if (result.accessToken == null) throw AuthorizationFailedThrowable()
    }

    /**
     * Returns a valid access token without UI if the user has already granted the scopes.
     * Play Services caches the token and refreshes it on expiry, so call this before every use
     * instead of persisting the token.
     */
    @Throws(AuthorizationNeedsResolutionThrowable::class, AuthorizationFailedThrowable::class)
    override suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        val result = authorizationClient.authorize(buildAuthorizationRequest()).await()
        if (result.hasResolution()) throw AuthorizationNeedsResolutionThrowable(result.pendingIntent)
        result.accessToken ?: throw AuthorizationFailedThrowable()
    }
}

class AuthorizationNeedsResolutionThrowable(val pendingIntent: PendingIntent?) : Throwable()
class AuthorizationFailedThrowable : Throwable()