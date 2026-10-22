package dev.ridill.oar.settings.data.remote.interceptors

import dev.ridill.oar.account.presentation.util.AuthorizationService
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class GoogleAccessTokenInterceptor(
    private val authorizationService: AuthorizationService
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            try {
                authorizationService.getAccessToken()
            } catch (t: Throwable) {
                t.rethrowIfCoroutineCancellation()
                throw GoogleAuthorizationRequiredException(t)
            }
        }
        val updatedRequest = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(updatedRequest)
    }
}

/**
 * Thrown when a Google access token cannot be obtained silently,
 * e.g. the user revoked access and needs to authorize the app again.
 */
class GoogleAuthorizationRequiredException(cause: Throwable) : IOException(cause)