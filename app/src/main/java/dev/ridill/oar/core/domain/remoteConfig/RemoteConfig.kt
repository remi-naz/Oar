package dev.ridill.oar.core.domain.remoteConfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class RemoteConfig(
    val sourceCodeUrl: String,
    val transactionAutoDetectFeatureEnabled: Boolean,
    val deleteAccountFeatureEnabled: Boolean,
    val autoDetectTransactionRegexPatterns: AutoDetectTransactionRegexPatterns?
)

@Serializable
data class AutoDetectTransactionRegexPatterns(
    @SerialName("originating_address")
    val originatingAddress: String,
    @SerialName("credit")
    val credit: String,
    @SerialName("debit")
    val debit: String,
    @SerialName("timestamp")
    val timestamp: String,
    @SerialName("second_party_start")
    val secondPartyStart: String,
    @SerialName("second_party_end")
    val secondPartyEnd: String,
    @SerialName("misc_payment")
    val miscPayment: String,
)