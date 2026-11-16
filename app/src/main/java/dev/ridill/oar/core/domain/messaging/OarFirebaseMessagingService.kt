package dev.ridill.oar.core.domain.messaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.domain.util.logD
import dev.ridill.oar.core.domain.util.logW
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class OarFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onNewToken(token: String) {
        logD(OarFirebaseMessagingService::class.simpleName) { "onNewToken called with: token = $token" }
        super.onNewToken(token)
    }

    override fun onRegistered(installationId: String) {
        logD(OarFirebaseMessagingService::class.simpleName) { "onRegistered called with: installationId = $installationId" }
        super.onRegistered(installationId)
    }

    // Runs on a background thread, so blocking for the short write is fine and guarantees it
    // completes before the system is free to stop the process.
    override fun onMessageReceived(message: RemoteMessage) {
        logD(OarFirebaseMessagingService::class.simpleName) { "Push message received from ${message.from}" }

        when (val pushMessage = message.toPushMessage()) {
            PushMessage.RefreshRemoteConfig -> runBlocking {
                preferencesManager.updateRemoteConfigForceRefreshRequired(true)
            }

            is PushMessage.Unknown -> logW { "Ignoring push of unknown type ${pushMessage.type}" }
        }
    }
}
