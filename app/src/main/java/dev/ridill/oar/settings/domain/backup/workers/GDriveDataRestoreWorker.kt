package dev.ridill.oar.settings.domain.backup.workers

import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.logE
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.oar.di.BackupFeature
import dev.ridill.oar.settings.data.repository.BackupDownloadFailedThrowable
import dev.ridill.oar.settings.data.repository.InvalidEncryptionPasswordThrowable
import dev.ridill.oar.settings.domain.backup.BackupWorkManager
import dev.ridill.oar.settings.domain.repositoty.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.crypto.AEADBadTagException

@HiltWorker
class GDriveDataRestoreWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: BackupRepository,
    @BackupFeature private val notificationHelper: NotificationHelper<String>
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        startForegroundService()
        try {
            val password = inputData.getString(BackupWorkManager.KEY_PASSWORD).orEmpty()
                .ifEmpty { throw InvalidEncryptionPasswordThrowable() }
            val passwordHashSalt = inputData.getString(BackupWorkManager.KEY_PASSWORD_HASH_SALT)
                .orEmpty().ifEmpty { throw InvalidEncryptionPasswordThrowable() }
            val scheme = EncryptionScheme.fromTag(
                inputData.getString(BackupWorkManager.KEY_ENCRYPTION_SCHEME)
            )
            val timestamp = inputData.getString(BackupWorkManager.KEY_BACKUP_TIMESTAMP)
                ?.let { DateUtil.parseDateTimeOrNull(it) }
                ?: throw BackupDownloadFailedThrowable()
            logI(GDriveDataRestoreWorker::class.simpleName) { "Starting data restore from cache" }
            repo.performAppDataRestoreFromCache(
                password = password,
                passwordSalt = passwordHashSalt,
                scheme = scheme,
                timestamp = timestamp
            )
            logI(GDriveDataRestoreWorker::class.simpleName) { "Backup Restored" }
            repo.tryClearLocalCache()
            logI(GDriveDataRestoreWorker::class.simpleName) { "Cleared cache" }
            Result.success()
        } catch (t: AEADBadTagException) {
            logE(
                t,
                GDriveDataRestoreWorker::class.simpleName
            ) { "InvalidEncryptionPasswordThrowable" }
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_app_data_restore_failed)
                )
            )
        } catch (t: Throwable) {
            t.rethrowIfCoroutineCancellation()
            logE(t, GDriveDataRestoreWorker::class.simpleName) { "Throwable" }
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_app_data_restore_failed)
                )
            )
        } finally {
            repo.tryClearLocalCache()
        }
    }

    private suspend fun startForegroundService() {
        setForeground(
            ForegroundInfo(
                BackupWorkManager.BACKUP_WORKER_NOTIFICATION_ID.hashCode(),
                notificationHelper.buildBaseNotification()
                    .setContentTitle(appContext.getString(R.string.restoring_app_data))
                    .setProgress(100, 0, true)
                    .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                    .build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        )
    }
}