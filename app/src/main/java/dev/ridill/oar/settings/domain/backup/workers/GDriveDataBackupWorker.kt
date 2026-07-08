package dev.ridill.oar.settings.domain.backup.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.UserRecoverableAuthException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.util.logE
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.oar.di.BackupFeature
import dev.ridill.oar.settings.data.remote.dto.GDriveErrorDto
import dev.ridill.oar.settings.data.repository.InvalidEncryptionPasswordThrowable
import dev.ridill.oar.settings.domain.backup.BackupCachingFailedThrowable
import dev.ridill.oar.settings.domain.backup.BackupWorkManager
import dev.ridill.oar.settings.domain.repositoty.BackupRepository
import dev.ridill.oar.settings.domain.repositoty.FatalBackupError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@HiltWorker
class GDriveDataBackupWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: BackupRepository,
    @BackupFeature private val notificationHelper: NotificationHelper<String>,
    private val workManager: BackupWorkManager,
    private val json: Json
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            repo.performAppDataBackup()
            logI(GDriveDataBackupWorker::class.simpleName) { "Backup Completed" }
            repo.setBackupError(null)
            Result.success(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.backup_complete)
                )
            )
        } catch (t: InvalidEncryptionPasswordThrowable) {
            logE(
                t,
                GDriveDataBackupWorker::class.simpleName
            ) { "InvalidEncryptionPasswordThrowable" }
            repo.setBackupError(FatalBackupError.PASSWORD_CORRUPTED)
            notificationHelper.postNotification(
                BackupWorkManager.BACKUP_WORKER_NOTIFICATION_ID.hashCode(),
                appContext.getString(R.string.error_invalid_encryption_password)
            )
            workManager.cancelPeriodicBackupWork()
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_invalid_encryption_password)
                )
            )
        } catch (e: UserRecoverableAuthException) {
            logE(e, GDriveDataBackupWorker::class.simpleName) { "UserRecoverableAuthException" }
            repo.setBackupError(FatalBackupError.GOOGLE_AUTH_FAILURE)
            notificationHelper.postNotification(
                BackupWorkManager.BACKUP_WORKER_NOTIFICATION_ID.hashCode(),
                appContext.getString(R.string.error_google_auth_failed)
            )
            workManager.cancelPeriodicBackupWork()
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_google_auth_failed)
                )
            )
        } catch (e: GoogleAuthException) {
            logE(e, GDriveDataBackupWorker::class.simpleName) { "GoogleAuthException" }
            repo.setBackupError(FatalBackupError.GOOGLE_AUTH_FAILURE)
            notificationHelper.postNotification(
                BackupWorkManager.BACKUP_WORKER_NOTIFICATION_ID.hashCode(),
                appContext.getString(R.string.error_google_auth_failed)
            )
            workManager.cancelPeriodicBackupWork()
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_google_auth_failed)
                )
            )
        } catch (e: HttpException) {
            logE(e, GDriveDataBackupWorker::class.simpleName) { "HttpException" }
            val errorBody = e.response()?.errorBody()?.charStream()?.let {
                json.decodeFromString<GDriveErrorDto>(it.readText())
            }

            val error = when (errorBody?.data?.reasons?.first()?.reason) {
                "authError" -> FatalBackupError.GOOGLE_AUTH_FAILURE
                "storageQuotaExceeded" -> FatalBackupError.STORAGE_QUOTA_EXCEEDED
                "teamDriveFileLimitExceeded" -> FatalBackupError.STORAGE_QUOTA_EXCEEDED
                else -> null
            }
            repo.setBackupError(error)
            resultForHttpCode(e.code())
        } catch (t: BackupCachingFailedThrowable) {
            logE(t, GDriveDataBackupWorker::class.simpleName) { "BackupCachingFailedThrowable" }
            repo.setBackupError(null)
            Result.retry()
        } catch (t: Throwable) {
            t.rethrowIfCoroutineCancellation()
            logE(t, GDriveDataBackupWorker::class.simpleName) { "Throwable" }
            repo.setBackupError(null)
            Result.retry()
        } finally {
            repo.tryClearLocalCache()
        }
    }

    private fun resultForHttpCode(code: Int): Result = when (code) {
        in listOf(400, 401, 404, 429) -> Result.failure()
        else -> Result.retry()
    }
}