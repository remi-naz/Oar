package dev.ridill.oar.settings.domain.appLock

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.oar.R
import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.util.BuildUtil
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.di.AppLockFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class AppLockService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob())
    private var timerJob: Job? = null

    @AppLockFeature
    @Inject
    lateinit var notificationHelper: NotificationHelper<Unit>

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logI(AppLockService::class.simpleName) { "App lock service onStartCommand - ${intent?.action}" }
        setForeground()
        when (intent?.action) {
            Action.INIT_SERVICE.name -> initService()
            Action.START_AUTO_LOCK_TIMER.name -> startTimer()
            Action.STOP_AUTO_LOCK_TIMER.name -> stopTimer()
            Action.LOCK_APP_IMMEDIATELY.name -> lockAppImmediate()
            Action.STOP_SERVICE.name -> stopService()
        }

        return START_STICKY
    }

    private fun initService() {
        // Some Basic Init
        resetTimerJob()
        logI(AppLockService::class.simpleName) { "Service reset and initialized" }
    }

    private fun startTimer() {
        logI(AppLockService::class.simpleName) { "Starting app lock timer" }
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            val interval = if (BuildUtil.isDebug) 3.seconds
            else preferencesManager.preferences.first().appAutoLockInterval.duration
            logI(AppLockService::class.simpleName) { "Locking app after $interval" }
            delay(interval)
            preferencesManager.updateAppLocked(true)
            logI(AppLockService::class.simpleName) { "App locked" }
            stopSelf()
        }
    }

    private fun stopTimer() {
        logI(AppLockService::class.simpleName) { "Stopping app lock timer" }
        resetTimerJob()
    }

    private fun lockAppImmediate() = serviceScope.launch {
        logI(AppLockService::class.simpleName) { "Locking app and terminating service" }
        preferencesManager.updateAppLocked(true)
        stopSelf()
    }

    private fun stopService() {
        logI(AppLockService::class.simpleName) { "Stopping service" }
        stopSelf()
    }

    @SuppressLint("InlinedApi")
    private fun setForeground() {
        ServiceCompat.startForeground(
            this,
            notificationId,
            notificationHelper.buildBaseNotification()
                .setContentTitle(
                    getString(
                        R.string.app_unlocked,
                        getString(R.string.app_name)
                    )
                )
                .setContentText(getString(R.string.tap_to_open))
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
    }

    private fun resetTimerJob() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCreate() {
        super.onCreate()
        setForeground()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        resetTimerJob()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    enum class Action {
        INIT_SERVICE,
        START_AUTO_LOCK_TIMER,
        STOP_AUTO_LOCK_TIMER,
        LOCK_APP_IMMEDIATELY,
        STOP_SERVICE
    }

    private val notificationId: Int
        get() = "${applicationContext.packageName}.APP_LOCK_NOTIFICATION_ID".hashCode()
}