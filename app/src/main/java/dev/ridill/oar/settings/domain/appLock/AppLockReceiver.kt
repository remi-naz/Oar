package dev.ridill.oar.settings.domain.appLock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.domain.util.logE
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.oar.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppLockReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != ACTION_LOCK_APP) return
        logI(TAG) { "App lock alarm received" }
        val pendingResult = goAsync()
        applicationScope.launch {
            try {
                // Don't leave a stale locked flag behind if app lock was turned off meanwhile
                if (preferencesManager.preferences.first().appLockEnabled) {
                    preferencesManager.updateAppLocked(true)
                    logI(TAG) { "App locked" }
                }
            } catch (t: Throwable) {
                t.rethrowIfCoroutineCancellation()
                logE(t, TAG) { "Failed to lock app" }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_LOCK_APP = "dev.ridill.oar.ACTION_LOCK_APP"
    }
}

private const val TAG = "AppLockReceiver"
