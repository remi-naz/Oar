package dev.ridill.oar.core.domain.messaging

import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dev.ridill.oar.core.domain.util.logD
import dev.ridill.oar.core.domain.util.logW
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PushMessagingManager @Inject constructor() {

    /** Subscribes to all [PushTopic]s in parallel. Re-subscribing is a no-op and subscriptions persist across restarts. */
    suspend fun subscribeToTopics() = coroutineScope {
        PushTopic.entries.map { topic ->
            async {
                try {
                    Firebase.messaging.subscribeToTopic(topic.topicName).await()
                    logD { "Subscribed to topic ${topic.topicName}" }
                } catch (t: Throwable) {
                    t.rethrowIfCoroutineCancellation()
                    logW {
                        "Failed to subscribe to topic ${topic.topicName} - ${t.javaClass.simpleName}"
                    }
                }
            }
        }.awaitAll()
    }
}
