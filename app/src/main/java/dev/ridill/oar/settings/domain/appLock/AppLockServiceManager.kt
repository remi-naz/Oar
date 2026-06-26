package dev.ridill.oar.settings.domain.appLock

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.core.domain.util.tryOrNull

class AppLockServiceManager(
    private val context: Context
) {
    fun startAppUnlockedIndicator() {
        performServiceAction(AppLockService.Action.INIT_SERVICE)
    }

    fun stopAppUnlockedIndicator() {
        performServiceAction(AppLockService.Action.STOP_SERVICE)
    }

    fun startAppAutoLockTimer() {
        performServiceAction(AppLockService.Action.START_AUTO_LOCK_TIMER)
    }

    fun stopAppLockTimer() {
        performServiceAction(AppLockService.Action.STOP_AUTO_LOCK_TIMER)
    }

    private fun performServiceAction(
        serviceAction: AppLockService.Action
    ) {
        logI(AppLockService::class.simpleName) { "performServiceAction() called with: action = $serviceAction" }
        tryOrNull(AppLockServiceManager::class.java.name) {
            val serviceIntent = Intent(context, AppLockService::class.java).apply {
                action = serviceAction.name
            }
            Handler(Looper.getMainLooper()).post {
                tryOrNull {
                    ContextCompat.startForegroundService(
                        context,
                        serviceIntent
                    )
                }
            }
        }
    }
}