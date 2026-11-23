package dev.ridill.oar.fakes

import dev.ridill.oar.core.domain.model.DataError
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.remoteConfig.AutoDetectTransactionRegexPatterns
import dev.ridill.oar.core.domain.remoteConfig.RemoteConfig
import dev.ridill.oar.core.domain.remoteConfig.RemoteConfigService

class FakeRemoteConfigService(
    var currentConfig: RemoteConfig = defaultConfig()
) : RemoteConfigService {

    var activateCount = 0
        private set

    override fun init() = Unit

    override suspend fun fetch(interval: Long): Result<Unit, DataError> = Result.Success(Unit)

    override suspend fun activate(): Result<Boolean, DataError> {
        activateCount++
        return Result.Success(true)
    }

    override fun getConfig(): RemoteConfig = currentConfig

    companion object {
        fun defaultConfig(
            patterns: AutoDetectTransactionRegexPatterns? = null
        ) = RemoteConfig(
            websiteUrl = "https://example.com",
            privacyPolicyUrl = "https://example.com/privacy",
            transactionAutoDetectFeatureEnabled = false,
            deleteAccountFeatureEnabled = false,
            autoDetectTransactionRegexPatterns = patterns
        )
    }
}
