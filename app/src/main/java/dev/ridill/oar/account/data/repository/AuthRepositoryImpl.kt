package dev.ridill.oar.account.data.repository

import android.app.PendingIntent
import android.content.Intent
import dev.ridill.oar.R
import dev.ridill.oar.account.domain.model.AuthState
import dev.ridill.oar.account.domain.model.UserAccount
import dev.ridill.oar.account.domain.repository.AuthRepository
import dev.ridill.oar.account.domain.service.AuthService
import dev.ridill.oar.account.presentation.util.AuthorizationFailedThrowable
import dev.ridill.oar.account.presentation.util.AuthorizationNeedsResolutionThrowable
import dev.ridill.oar.account.presentation.util.AuthorizationService
import dev.ridill.oar.account.presentation.util.CredentialService
import dev.ridill.oar.core.data.util.tryNetworkCall
import dev.ridill.oar.core.domain.model.DataError
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.core.domain.util.logW
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.oar.core.ui.util.UiText
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val credentialService: CredentialService,
    private val authService: AuthService,
    private val authorizationService: AuthorizationService
) : AuthRepository {
    override fun getSignedInAccount(): UserAccount? = authService
        .getSignedInAccount()

    override fun getAuthState(): Flow<AuthState> = authService
        .getAuthStateFlow()

    override suspend fun signUserInWithToken(
        idToken: String
    ): Result<Unit, DataError> = tryNetworkCall {
        authService.signinUserWithIdToken(idToken)
        logI { "User signed in" }
        Result.Success(Unit)
    }

    override suspend fun signUserOut(): Result<Unit, DataError> = tryNetworkCall {
        authService.signUserOut()
        credentialService.clearCredentials()
        logI { "User signed out" }
        Result.Success(Unit)
    }

    override suspend fun authorizeUserAccount(): Result<PendingIntent?, AuthorizationService.AuthorizationError> =
        try {
            val result = authorizationService.getIntentSenderForAuthorization()
            Result.Success(result.pendingIntent)
        } catch (t: AuthorizationNeedsResolutionThrowable) {
            logI { "Authorization needs user resolution" }
            Result.Error(
                error = AuthorizationService.AuthorizationError.NEEDS_RESOLUTION,
                message = UiText.StringResource(R.string.error_authorization_required, true),
                data = t.pendingIntent
            )
        } catch (_: AuthorizationFailedThrowable) {
            logW { "Authorization request failed" }
            Result.Error(
                error = AuthorizationService.AuthorizationError.AUTHORIZATION_FAILED,
                message = UiText.StringResource(R.string.error_authorization_failed, true)
            )
        } catch (t: Throwable) {
            t.rethrowIfCoroutineCancellation()
            logW { "Authorization request failed: ${t::class.simpleName}" }
            Result.Error(
                error = AuthorizationService.AuthorizationError.AUTHORIZATION_FAILED,
                message = UiText.StringResource(R.string.error_authorization_failed, true)
            )
        }

    override suspend fun decodeAuthorizationResult(intent: Intent): Result<Unit, AuthorizationService.AuthorizationError> =
        try {
            authorizationService.verifyAuthorizationResult(intent)
            Result.Success(Unit)
        } catch (_: AuthorizationNeedsResolutionThrowable) {
            logI { "Authorization result needs user resolution" }
            Result.Error(
                error = AuthorizationService.AuthorizationError.NEEDS_RESOLUTION,
                message = UiText.StringResource(R.string.error_authorization_required, true)
            )
        } catch (_: AuthorizationFailedThrowable) {
            logW { "Authorization result verification failed" }
            Result.Error(
                error = AuthorizationService.AuthorizationError.AUTHORIZATION_FAILED,
                message = UiText.StringResource(R.string.error_authorization_failed, true)
            )
        } catch (t: Throwable) {
            t.rethrowIfCoroutineCancellation()
            logW { "Authorization result verification failed: ${t::class.simpleName}" }
            Result.Error(
                error = AuthorizationService.AuthorizationError.AUTHORIZATION_FAILED,
                message = UiText.StringResource(R.string.error_authorization_failed, true)
            )
        }

    override suspend fun deleteAccount(): Result<Unit, DataError> = tryNetworkCall {
        authService.deleteAccount()
        Result.Success(Unit)
    }
}