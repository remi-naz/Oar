package dev.ridill.oar.settings.presentation.help

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.core.data.logging.LogFileManager
import dev.ridill.oar.core.domain.remoteConfig.RemoteConfigService
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.asStateFlow
import dev.ridill.oar.core.domain.util.logE
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.oar.core.ui.util.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpViewModel @Inject constructor(
    private val remoteConfigService: RemoteConfigService,
    private val logFileManager: LogFileManager,
    private val eventBus: EventBus<HelpEvent>
) : ViewModel(), HelpActions {

    private val websiteUrl = MutableStateFlow<String?>(null)
    private val privacyPolicyUrl = MutableStateFlow<String?>(null)

    val state = combine(
        websiteUrl,
        privacyPolicyUrl
    ) { websiteUrl, privacyPolicyUrl ->
        HelpState(
            websiteUrl = websiteUrl,
            privacyPolicyUrl = privacyPolicyUrl
        )
    }
        .onStart { refreshConfigs() }
        .asStateFlow(viewModelScope, HelpState())

    val events = eventBus.eventFlow

    private var exportLogsJob: Job? = null
    override fun onExportLogsClick() {
        exportLogsJob?.cancel()
        exportLogsJob = viewModelScope.launch {
            try {
                val logsUri = logFileManager.exportLogs()
                eventBus.send(HelpEvent.ShareLogs(logsUri))
            } catch (t: Throwable) {
                t.rethrowIfCoroutineCancellation()
                logE(t) { "Failed to export logs" }
                eventBus.send(
                    HelpEvent.ShowUiMessage(
                        UiText.StringResource(R.string.error_export_logs_failed, true)
                    )
                )
            }
        }
    }

    private fun refreshConfigs() = viewModelScope.launch {
        val config = remoteConfigService.getConfig()
        websiteUrl.value = config.websiteUrl
        privacyPolicyUrl.value = config.privacyPolicyUrl
    }

    sealed interface HelpEvent {
        data class ShowUiMessage(val uiText: UiText) : HelpEvent
        data class ShareLogs(val uri: Uri) : HelpEvent
    }
}
