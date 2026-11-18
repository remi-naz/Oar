package dev.ridill.oar.core.domain.messaging

/**
 * FCM topics the app subscribes to. Names must match `[a-zA-Z0-9-_.~%]+`.
 */
enum class PushTopic(val topicName: String) {
    /** Receives data pushes of type [PushMessage.Types.REFRESH_REMOTE_CONFIG] sent to every install. */
    REFRESH_REMOTE_CONFIG("refresh_remote_config")
}
