package dev.ridill.oar.settings.domain.appInit

interface AppInitWorkManager {
    companion object {
        const val APP_INIT_NOTIFICATION_ID = "APP_INIT_NOTIFICATION"

        const val RESTORE_WORKER_NOTIFICATION_ID = "RESTORE_WORKER_NOTIFICATION"
    }

    fun startAppInitWorker()
    fun startCycleCatchUpWorker()
    fun scheduleDailyCycleCatchUp()
    fun startConfigRestoreWorkers()
    fun startAlarmsAndReminderInitWorkers()
}
