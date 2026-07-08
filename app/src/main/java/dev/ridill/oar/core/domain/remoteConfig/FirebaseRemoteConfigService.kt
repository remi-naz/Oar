package dev.ridill.oar.core.domain.remoteConfig

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dev.ridill.oar.core.data.util.tryNetworkCall
import dev.ridill.oar.core.domain.model.DataError
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.core.domain.util.tryOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

private const val TAG = "FirebaseRemoteConfigService"

class FirebaseRemoteConfigService(
    private val json: Json
) {

    private val remoteConfig = Firebase.remoteConfig

    fun init() {
        val configSettings = remoteConfigSettings {
            fetchTimeoutInSeconds = DEFAULT_FETCH_TIMEOUT.inWholeSeconds
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    suspend fun fetch(
        interval: Long = DEFAULT_FETCH_INTERVAL.inWholeSeconds
    ): Result<Unit, DataError> = tryNetworkCall {
        logI(TAG) { "fetch() called with: interval = $interval" }
        remoteConfig.fetch(interval).await()
        logI(TAG) { "fetch() completed" }
        Result.Success(Unit)
    }

    suspend fun activate(): Result<Boolean, DataError> = tryNetworkCall {
        logI(TAG) { "activate() called" }
        val success = remoteConfig.activate().await()
        logI(TAG) { "activate() completed with: success = $success" }
        Result.Success(success)
    }

    fun getConfig(): RemoteConfig {
        logI(TAG) { "getConfig() called" }
        val sourceCodeUrl = remoteConfig.getString(Keys.SOURCE_CODE_URL)
        val transactionAutoDetectFeatureEnabled =
            remoteConfig.getBoolean(Keys.TRANSACTION_AUTO_DETECT_FEATURE_ENABLED)
        val deleteAccountFeatureEnabled =
            remoteConfig.getBoolean(Keys.DELETE_ACCOUNT_FEATURE_ENABLED)
        val txAutoDetectPatterns = tryOrNull {
            val patternsJson = remoteConfig.getString(Keys.TX_AUTO_DETECT_PATTERNS)
            json.decodeFromString<AutoDetectTransactionRegexPatterns>(patternsJson)
        }

        val remoteConfig = RemoteConfig(
            sourceCodeUrl = sourceCodeUrl,
            transactionAutoDetectFeatureEnabled = transactionAutoDetectFeatureEnabled,
            deleteAccountFeatureEnabled = deleteAccountFeatureEnabled,
            autoDetectTransactionRegexPatterns = txAutoDetectPatterns
        )

        logI(TAG) { "getConfig() completed with: remoteConfig = $remoteConfig" }
        return remoteConfig
    }

    object Keys {
        const val SOURCE_CODE_URL = "code_repo_link"
        const val TRANSACTION_AUTO_DETECT_FEATURE_ENABLED = "transaction_auto_detect_enabled"
        const val DELETE_ACCOUNT_FEATURE_ENABLED = "delete_account_feature_enabled"
        const val TX_AUTO_DETECT_PATTERNS = "tx_auto_detect_patterns"
    }
}

private val DEFAULT_FETCH_INTERVAL = 12.hours
private val DEFAULT_FETCH_TIMEOUT = 30.seconds