package dev.ridill.oar.core.domain.remoteConfig

data class RemoteConfig(
    val websiteUrl: String,
    val privacyPolicyUrl: String,
    val deleteAccountFeatureEnabled: Boolean
)
