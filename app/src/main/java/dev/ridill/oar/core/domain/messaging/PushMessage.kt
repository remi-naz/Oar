package dev.ridill.oar.core.domain.messaging

import com.google.firebase.messaging.RemoteMessage

/**
 * What the app does with a received push, decided by the `type` key of the data payload and
 * not by how it was delivered (topic, token), so the same action can be sent through any route.
 */
sealed interface PushMessage {
    data object RefreshRemoteConfig : PushMessage

    /** Unrecognised or missing type; ignored so that older app versions survive new message types. */
    data class Unknown(val type: String?) : PushMessage

    object Types {
        const val KEY = "type"
        const val REFRESH_REMOTE_CONFIG = "refresh_remote_config"
    }
}

fun RemoteMessage.toPushMessage(): PushMessage {
    return when (val type = data[PushMessage.Types.KEY]) {
        PushMessage.Types.REFRESH_REMOTE_CONFIG -> PushMessage.RefreshRemoteConfig
        else -> PushMessage.Unknown(type)
    }
}
