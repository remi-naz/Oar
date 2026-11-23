package dev.ridill.oar.core.domain.remoteConfig

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dev.ridill.oar.core.data.util.tryNetworkCall
import dev.ridill.oar.core.domain.model.DataError
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.util.logI
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.seconds

private const val TAG = "FirebaseRemoteConfigService"

class FirebaseRemoteConfigService : RemoteConfigService {

    private val remoteConfig = Firebase.remoteConfig

    override fun init() {
        val configSettings = remoteConfigSettings {
            fetchTimeoutInSeconds = DEFAULT_FETCH_TIMEOUT.inWholeSeconds
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    override suspend fun fetch(
        interval: Long
    ): Result<Unit, DataError> = tryNetworkCall {
        logI(TAG) { "fetch() called with: interval = $interval" }
        remoteConfig.fetch(interval).await()
        logI(TAG) { "fetch() completed" }
        Result.Success(Unit)
    }

    override suspend fun activate(): Result<Boolean, DataError> = tryNetworkCall {
        logI(TAG) { "activate() called" }
        val success = remoteConfig.activate().await()
        logI(TAG) { "activate() completed with: success = $success" }
        Result.Success(success)
    }

    override fun getConfig(): RemoteConfig {
        logI(TAG) { "getConfig() called" }
        val websiteUrl = remoteConfig.getString(Keys.WEBSITE_URL)
        val privacyPolicyUrl = remoteConfig.getString(Keys.PRIVACY_POLICY_URL)
        val deleteAccountFeatureEnabled =
            remoteConfig.getBoolean(Keys.DELETE_ACCOUNT_FEATURE_ENABLED)

        val remoteConfig = RemoteConfig(
            websiteUrl = websiteUrl,
            privacyPolicyUrl = privacyPolicyUrl,
            deleteAccountFeatureEnabled = deleteAccountFeatureEnabled
        )

        logI(TAG) { "getConfig() completed" }
        return remoteConfig
    }

    object Keys {
        const val WEBSITE_URL = "website_url"
        const val PRIVACY_POLICY_URL = "privacy_policy_url"
        const val DELETE_ACCOUNT_FEATURE_ENABLED = "delete_account_feature_enabled"
    }
}

private val DEFAULT_FETCH_TIMEOUT = 30.seconds