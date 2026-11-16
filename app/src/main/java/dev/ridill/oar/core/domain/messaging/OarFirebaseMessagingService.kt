package dev.ridill.oar.core.domain.messaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.oar.core.domain.util.logD

@AndroidEntryPoint
class OarFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        logD { "FCM token refreshed = $token" }
    }

    override fun onRegistered(installationId: String) {
        super.onRegistered(installationId)
        logD { "FCM registered with ID - $installationId" }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        logD { "Push message received from ${message.from}" }
    }
}
