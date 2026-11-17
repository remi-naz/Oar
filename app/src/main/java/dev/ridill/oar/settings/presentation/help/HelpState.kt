package dev.ridill.oar.settings.presentation.help

data class HelpState(
    val websiteUrl: String? = null,
    val privacyPolicyUrl: String? = null
) {
    val hasValidWebsiteUrl: Boolean
        get() = !websiteUrl.isNullOrEmpty()

    val hasValidPrivacyPolicyUrl: Boolean
        get() = !privacyPolicyUrl.isNullOrEmpty()
}
