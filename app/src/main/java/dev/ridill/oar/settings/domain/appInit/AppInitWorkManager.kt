package dev.ridill.oar.settings.domain.appInit

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import dev.ridill.oar.core.domain.util.toUUID
import dev.ridill.oar.moneyPiles.domain.pileReminder.RestorePileRemindersWorker
import dev.ridill.oar.schedules.domain.scheduleReminder.RestoreScheduleRemindersWorker
import dev.ridill.oar.settings.domain.backup.workers.RestoreBackupJobsWorker

class AppInitWorkManager(
    private val context: Context
) {
    companion object {
        const val APP_INIT_NOTIFICATION_ID = "APP_INIT_NOTIFICATION"

        const val RESTORE_WORKER_NOTIFICATION_ID = "RESTORE_WORKER_NOTIFICATION"
    }

    private val workManager = WorkManager.getInstance(context)

    private val appInitWorkerName: String
        get() = "${context.packageName}.APP_INIT_WORKERS"

    private val configRestoreWorkerName: String
        get() = "${context.packageName}.CONFIG_RESTORE_WORKERS"

    private val budgetCycleInitWorkerName: String
        get() = "${context.packageName}.BUDGET_CYCLE_INIT_WORKER"

    private val restoreScheduleRemindersWorkerName: String
        get() = "${context.packageName}.RESTORE_SCHEDULE_REMINDERS_WORKER"

    private val restorePileRemindersWorkerName: String
        get() = "${context.packageName}.RESTORE_PILE_REMINDERS_WORKER"

    private val restoreBackupJobsWorkerName: String
        get() = "${context.packageName}.RESTORE_BACKUP_JOBS_WORKER"

    private val currencyListInitWorkerName: String
        get() = "${context.packageName}.CURRENCY_LIST_INIT_WORKER"

    fun startAppInitWorker() {
        val currencyInitWorker = OneTimeWorkRequestBuilder<CurrencyInitWorker>()
            .setId(currencyListInitWorkerName.toUUID())
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.beginUniqueWork(
            currencyListInitWorkerName,
            ExistingWorkPolicy.REPLACE,
            currencyInitWorker
        ).enqueue()
    }

    fun startConfigRestoreWorkers() {
        val budgetCycleInitWorker = OneTimeWorkRequestBuilder<BudgetCycleInitWorker>()
            .setId(budgetCycleInitWorkerName.toUUID())
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        val restoreBackupJobsWorker = OneTimeWorkRequestBuilder<RestoreBackupJobsWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setId(restoreBackupJobsWorkerName.toUUID())
            .build()

        val restoreScheduleRemindersWorker =
            OneTimeWorkRequestBuilder<RestoreScheduleRemindersWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setId(restoreScheduleRemindersWorkerName.toUUID())
                .build()

        val restorePileRemindersWorker = OneTimeWorkRequestBuilder<RestorePileRemindersWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setId(restorePileRemindersWorkerName.toUUID())
            .build()

        workManager.beginUniqueWork(
            configRestoreWorkerName,
            ExistingWorkPolicy.REPLACE,
            budgetCycleInitWorker
        )
            .then(restoreBackupJobsWorker)
            .then(restoreScheduleRemindersWorker)
            .then(restorePileRemindersWorker)
            .enqueue()
    }

    fun startAlarmsAndReminderInitWorkers() {
        val budgetCycleInitWorker = OneTimeWorkRequestBuilder<BudgetCycleInitWorker>()
            .setId(budgetCycleInitWorkerName.toUUID())
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        val restoreScheduleRemindersWorker =
            OneTimeWorkRequestBuilder<RestoreScheduleRemindersWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setId(restoreScheduleRemindersWorkerName.toUUID())
                .build()

        val restorePileRemindersWorker = OneTimeWorkRequestBuilder<RestorePileRemindersWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setId(restorePileRemindersWorkerName.toUUID())
            .build()

        workManager.beginUniqueWork(
            configRestoreWorkerName,
            ExistingWorkPolicy.REPLACE,
            budgetCycleInitWorker
        )
            .then(restoreScheduleRemindersWorker)
            .then(restorePileRemindersWorker)
            .enqueue()
    }
}