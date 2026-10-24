package dev.ridill.oar.settings.domain.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.settings.domain.appLock.AppLockService

class LockAppImmediateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        logI("LockAppReceiver") { "Lock app immediately requested" }
        val serviceIntent = Intent(context, AppLockService::class.java).apply {
            action = AppLockService.Action.LOCK_APP_IMMEDIATELY.name
        }
        context?.let { ContextCompat.startForegroundService(it, serviceIntent) }
    }
}