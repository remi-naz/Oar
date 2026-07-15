package dev.ridill.oar.application

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.oar.core.domain.util.isAnyOf
import dev.ridill.oar.di.ApplicationScope
import dev.ridill.oar.settings.domain.appInit.AppInitWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimeOrTimezoneChangeReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var appInitManager: AppInitWorkManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (
            intent?.action?.isAnyOf(
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED
            ) != true
        ) return
        applicationScope.launch {
            appInitManager.startAlarmsAndReminderInitWorkers()
        }
    }
}