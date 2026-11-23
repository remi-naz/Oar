package dev.ridill.oar.settings.domain.backup

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.ridill.oar.core.domain.util.toUUID
import dev.ridill.oar.settings.domain.backup.BackupWorkManager.Companion.KEY_BACKUP_FILE_ID
import dev.ridill.oar.settings.domain.backup.BackupWorkManager.Companion.KEY_BACKUP_TIMESTAMP
import dev.ridill.oar.settings.domain.backup.BackupWorkManager.Companion.KEY_ENCRYPTION_SCHEME
import dev.ridill.oar.settings.domain.backup.BackupWorkManager.Companion.KEY_PASSWORD
import dev.ridill.oar.settings.domain.backup.BackupWorkManager.Companion.KEY_PASSWORD_HASH_SALT
import dev.ridill.oar.settings.domain.backup.BackupWorkManager.Companion.WORK_INTERVAL_TAG_PREFIX
import dev.ridill.oar.settings.domain.backup.workers.GDriveDataBackupWorker
import dev.ridill.oar.settings.domain.backup.workers.GDriveDataDownloadWorker
import dev.ridill.oar.settings.domain.backup.workers.GDriveDataRestoreWorker
import dev.ridill.oar.settings.domain.modal.BackupDetails
import dev.ridill.oar.settings.domain.modal.BackupInterval
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

internal class BackupWorkManagerImpl(
    private val context: Context
) : BackupWorkManager {

    private val workManager = WorkManager.getInstance(context)

    private val dataBackupWorkerTag: String
        get() = "${context.packageName}.DATA_BACKUP_WORKER_TAG"

    private val oneTimeBackupWorkName: String
        get() = "${context.packageName}.ONE_TIME_DATA_BACKUP_WORK"

    private val periodicBackupWorkName: String
        get() = "${context.packageName}.PERIODIC_DATA_BACKUP_WORK"

    private val dataRestoreWorkerTag: String
        get() = "${context.packageName}.DATA_RESTORE_WORKER_TAG"

    private val oneTimeDataDownloadWorkName: String
        get() = "${context.packageName}.ONE_TIME_DATA_DOWNLOAD_WORK"

    private val oneTimeDataRestoreWorkName: String
        get() = "${context.packageName}.ONE_TIME_DATA_RESTORE_WORK"

    override fun schedulePeriodicBackupWork(interval: BackupInterval) {
        val backupWorkRequest = PeriodicWorkRequestBuilder<GDriveDataBackupWorker>(
            interval.daysInterval,
            TimeUnit.DAYS
        )
            .setConstraints(buildBackupConstraints())
            .setId(periodicBackupWorkName.toUUID())
            .addTag("$WORK_INTERVAL_TAG_PREFIX${interval.name}")
            .addTag(dataBackupWorkerTag)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_DELAY_MINUTES,
                TimeUnit.MINUTES
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            periodicBackupWorkName,
            ExistingPeriodicWorkPolicy.UPDATE,
            backupWorkRequest
        )
    }

    override fun getPeriodicBackupWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdFlow(periodicBackupWorkName.toUUID())

    override fun cancelPeriodicBackupWork() {
        workManager.cancelWorkById(periodicBackupWorkName.toUUID())
    }

    override fun cancelAllBackupAndRestoreWork() {
        workManager.cancelAllWorkByTag(dataBackupWorkerTag)
        workManager.cancelAllWorkByTag(dataRestoreWorkerTag)
        cancelPeriodicBackupWork()
        // Cancelling leaves finished work in the DB, e.g. a SUCCEEDED restore that onboarding would act on
        workManager.pruneWork()
    }

    override fun runImmediateBackupWork() {
        val workRequest = OneTimeWorkRequestBuilder<GDriveDataBackupWorker>()
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .setId(oneTimeBackupWorkName.toUUID())
            .addTag("$WORK_INTERVAL_TAG_PREFIX${BackupInterval.MANUAL.name}")
            .addTag(dataBackupWorkerTag)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_DELAY_MINUTES,
                TimeUnit.MINUTES
            )
            .build()

        workManager.enqueueUniqueWork(
            oneTimeBackupWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    override fun getImmediateBackupWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdFlow(oneTimeBackupWorkName.toUUID())

    override fun runImmediateRestoreWork(backupDetails: BackupDetails, password: String) {
        val dataDownloadWorkRequest = OneTimeWorkRequestBuilder<GDriveDataDownloadWorker>()
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .addTag(dataRestoreWorkerTag)
            .setId(oneTimeDataDownloadWorkName.toUUID())
            .setInputData(
                workDataOf(
                    KEY_BACKUP_FILE_ID to backupDetails.id,
                    KEY_BACKUP_TIMESTAMP to backupDetails.timestamp
                )
            )
            .build()
        val dataRestoreWorkRequest = OneTimeWorkRequestBuilder<GDriveDataRestoreWorker>()
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .addTag(dataRestoreWorkerTag)
            .setId(oneTimeDataRestoreWorkName.toUUID())
            .setInputData(
                workDataOf(
                    KEY_PASSWORD to password,
                    KEY_PASSWORD_HASH_SALT to backupDetails.hashSalt,
                    KEY_ENCRYPTION_SCHEME to backupDetails.scheme.name,
                )
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresStorageNotLow(true)
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.beginUniqueWork(
            oneTimeDataRestoreWorkName,
            ExistingWorkPolicy.REPLACE,
            dataDownloadWorkRequest
        )
            .then(dataRestoreWorkRequest)
            .enqueue()
    }

    override fun getRestoreDataDownloadWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdFlow(oneTimeDataDownloadWorkName.toUUID())

    override fun getImmediateDataRestoreWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdFlow(oneTimeDataRestoreWorkName.toUUID())

    override fun getBackupIntervalFromWorkInfo(info: WorkInfo): BackupInterval? {
        val intervalTag = info.tags.find { it.startsWith(WORK_INTERVAL_TAG_PREFIX) }

        return intervalTag
            ?.removePrefix(WORK_INTERVAL_TAG_PREFIX)
            ?.let { BackupInterval.valueOf(it) }
    }

    private fun buildBackupConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .setRequiresStorageNotLow(true)
        .build()
}
private const val BACKOFF_DELAY_MINUTES = 10L
