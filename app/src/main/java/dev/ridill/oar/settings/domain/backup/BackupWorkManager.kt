package dev.ridill.oar.settings.domain.backup

import androidx.work.WorkInfo
import dev.ridill.oar.settings.domain.modal.BackupDetails
import dev.ridill.oar.settings.domain.modal.BackupInterval
import kotlinx.coroutines.flow.Flow

interface BackupWorkManager {
    companion object {
        const val WORK_INTERVAL_TAG_PREFIX = "WORK_INTERVAL-"
        const val KEY_MESSAGE = "KEY_MESSAGE"
        const val KEY_BACKUP_FILE_ID = "KEY_BACKUP_FILE_ID"
        const val KEY_BACKUP_TIMESTAMP = "KEY_BACKUP_DATE"
        const val KEY_PASSWORD = "KEY_PASSWORD_HASH"
        const val KEY_PASSWORD_HASH_SALT = "KEY_PASSWORD_HASH_SALT"
        const val KEY_ENCRYPTION_SCHEME = "KEY_ENCRYPTION_SCHEME"

        const val BACKUP_WORKER_NOTIFICATION_ID = "BACKUP_WORKER_NOTIFICATION"
    }

    fun schedulePeriodicBackupWork(interval: BackupInterval)
    fun getPeriodicBackupWorkInfoFlow(): Flow<WorkInfo?>
    fun cancelPeriodicBackupWork()
    fun cancelAllBackupAndRestoreWork()
    fun runImmediateBackupWork()
    fun getImmediateBackupWorkInfoFlow(): Flow<WorkInfo?>
    fun runImmediateRestoreWork(backupDetails: BackupDetails, password: String)
    fun getRestoreDataDownloadWorkInfoFlow(): Flow<WorkInfo?>
    fun getImmediateDataRestoreWorkInfoFlow(): Flow<WorkInfo?>
    fun getBackupIntervalFromWorkInfo(info: WorkInfo): BackupInterval?
}
