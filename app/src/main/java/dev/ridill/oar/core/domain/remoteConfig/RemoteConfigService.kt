package dev.ridill.oar.core.domain.remoteConfig

import dev.ridill.oar.core.domain.model.DataError
import dev.ridill.oar.core.domain.model.Result
import kotlin.time.Duration.Companion.hours

interface RemoteConfigService {
    fun init()
    suspend fun fetch(interval: Long = DEFAULT_FETCH_INTERVAL_SECONDS): Result<Unit, DataError>
    suspend fun activate(): Result<Boolean, DataError>
    fun getConfig(): RemoteConfig
}

val DEFAULT_FETCH_INTERVAL_SECONDS: Long = 12.hours.inWholeSeconds
