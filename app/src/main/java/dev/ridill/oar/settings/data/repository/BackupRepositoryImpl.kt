package dev.ridill.oar.settings.data.repository

import android.content.Context
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.UserRecoverableAuthException
import dev.ridill.oar.account.domain.repository.AuthRepository
import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.data.preferences.security.SecurityPreferencesManager
import dev.ridill.oar.core.data.util.tryNetworkCall
import dev.ridill.oar.core.data.util.trySuspend
import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import dev.ridill.oar.core.domain.crypto.PasswordBasedCryptoManager
import dev.ridill.oar.core.domain.model.DataError
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.logD
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.di.Argon2PasswordBasedCryptoManager
import dev.ridill.oar.settings.data.local.ConfigDao
import dev.ridill.oar.settings.data.remote.GDriveApi
import dev.ridill.oar.settings.data.remote.MEDIA_PART_KEY
import dev.ridill.oar.settings.data.remote.dto.CreateGDriveFolderRequestDto
import dev.ridill.oar.settings.data.remote.dto.GDriveFileMetadataDto
import dev.ridill.oar.settings.data.toBackupDetails
import dev.ridill.oar.settings.domain.backup.BackupCachingFailedThrowable
import dev.ridill.oar.settings.domain.backup.BackupService
import dev.ridill.oar.settings.domain.backup.BackupWorkManager
import dev.ridill.oar.settings.domain.backup.RestoreFailedThrowable
import dev.ridill.oar.settings.domain.modal.BackupDetails
import dev.ridill.oar.settings.domain.modal.BackupInterval
import dev.ridill.oar.settings.domain.repositoty.BackupRepository
import dev.ridill.oar.settings.domain.repositoty.FatalBackupError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.time.LocalDateTime
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

class BackupRepositoryImpl(
    private val context: Context,
    private val backupService: BackupService,
    private val gDriveApi: GDriveApi,
    private val preferencesManager: PreferencesManager,
    @Argon2PasswordBasedCryptoManager private val argon2CryptoManager: PasswordBasedCryptoManager,
    private val defaultCryptoManager: PasswordBasedCryptoManager,
    private val securityPreferencesManager: SecurityPreferencesManager,
    private val configDao: ConfigDao,
    private val backupWorkManager: BackupWorkManager,
    private val authRepo: AuthRepository,
    private val json: Json
) : BackupRepository {
    override suspend fun checkForBackup(): Result<BackupDetails, DataError> =
        tryNetworkCall {
            logI { "Checking For Backup" }
            val email = authRepo.getSignedInAccount()?.email
                ?: throw GoogleAuthException()
            val backupFolderName = backupFolderName(email)
            logD { "Checking Backup folder - $backupFolderName" }
            val backupFolder = gDriveApi.getFilesList(
                q = "trashed=false and name = '$backupFolderName'"
            ).files.firstOrNull() ?: throw NoBackupFoundThrowable()
            logD { "Backup folder - $backupFolder" }
            logD { "Checking backup file in backup folder: ${backupFolder.id}" }
            val backupFiles = gDriveApi.getFilesList(
                q = "trashed=false and '${backupFolder.id}' in parents and name = '${
                    BackupService.dbBackupFileName(
                        context
                    )
                }'",
            ).files
            logD { "Files in backup folder: ${backupFolder.name} = ${backupFiles.map { it.id }}" }
            val backupFile = backupFiles.firstOrNull() ?: throw NoBackupFoundThrowable()
            logD { "Backup Found - $backupFile" }
            val backupDetails = backupFile.toBackupDetails()
            Result.Success(backupDetails)
        }

    @Throws(
        InvalidEncryptionPasswordThrowable::class,
        BackupCachingFailedThrowable::class,
        IOException::class,
        UserRecoverableAuthException::class,
        GoogleAuthException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    override suspend fun performAppDataBackup() = withContext(Dispatchers.IO) {
        logI { "Performing Data Backup" }
        val securityPreferences = securityPreferencesManager.preferences.first()
        val passwordHash = securityPreferences
            .backupEncryptionHash.orEmpty()
            .ifEmpty { throw InvalidEncryptionPasswordThrowable() }
        val passwordHashSalt = securityPreferences
            .backupEncryptionHashSalt.orEmpty()
            .ifEmpty { throw InvalidEncryptionPasswordThrowable() }
        val email = authRepo.getSignedInAccount()?.email
            ?: throw GoogleAuthException()
        val backupFolderName = backupFolderName(email)
        logD { "Checking Backup folder - $backupFolderName" }
        var backupFolder = gDriveApi.getFilesList(
            q = "name = '$backupFolderName' and trashed=false"
        ).files.firstOrNull()
        logD { "Received backup folder - $backupFolder" }
        if (backupFolder == null) {
            val createBackupFolderRequest = CreateGDriveFolderRequestDto(backupFolderName(email))
            val createBackupFolderMetadataPart = json.encodeToString(createBackupFolderRequest)
                .toRequestBody(JSON_MIME_TYPE.toMediaTypeOrNull())
            logD { "Create backup folder request - $createBackupFolderRequest" }
            backupFolder = gDriveApi.createFolder(createBackupFolderMetadataPart)
        }
        val backupFile = backupService.buildBackupFile(
            password = passwordHash,
            passwordSalt = passwordHashSalt
        )
        val metadataDto = GDriveFileMetadataDto(
            name = backupFile.name,
            parents = listOf(backupFolder.id),
            appProperties = mapOf(
                GDriveApi.APP_PROPERTIES_KEY_HASH_SALT to passwordHashSalt,
                GDriveApi.APP_PROPERTIES_KEY_ENCRYPTION_SCHEME to EncryptionScheme.ARGON2_GCM.name,
                GDriveApi.APP_PROPERTIES_KEY_BACKUP_TIMESTAMP to DateUtil.now()
                    .format(DateUtil.Formatters.isoLocalDateTime)
            )
        )
        val metadataJson = json.encodeToString(metadataDto)
        val metadataPart = metadataJson.toRequestBody(JSON_MIME_TYPE.toMediaTypeOrNull())

        val fileBody = backupFile.asRequestBody(BACKUP_MIME_TYPE.toMediaTypeOrNull())
        val mediaPart = MultipartBody.Part.createFormData(
            MEDIA_PART_KEY,
            backupFile.name,
            fileBody
        )

        logD { "Backup file generated - ${backupFile.name}" }
        val gDriveBackup = gDriveApi.uploadFile(
            metadata = metadataPart,
            file = mediaPart
        )
        logI { "Backup file uploaded - $gDriveBackup" }

        preferencesManager.updateLastBackupTimestamp(DateUtil.now())
        val backupFolderFiles = gDriveApi.getFilesList(
            q = "trashed=false and '${backupFolder.id}' in parents and name = '${
                BackupService.dbBackupFileName(
                    context
                )
            }'"
        ).files
        logD { "Files in backup folder: ${backupFolder.name} = ${backupFolderFiles.map { it.id }}" }
        backupFolderFiles
            .filter { it.id != gDriveBackup.id }
            .map {
                async {
                    logI { "Deleting file ${it.id}" }
                    gDriveApi.deleteFile(it.id)
                }
            }.awaitAll()
        logI { "Cleaned up Drive" }
    }

    @Throws(
        RestoreFailedThrowable::class,
        BackupDownloadFailedThrowable::class,
        BackupCachingFailedThrowable::class
    )
    override suspend fun downloadAndCacheBackupData(
        fileId: String,
        timestamp: LocalDateTime
    ) = withContext(Dispatchers.IO) {
        if (backupService.doesRestoreCacheExist(timestamp)) {
            logI { "Cache already exists, hence skipping download" }
            return@withContext
        }

        logI { "Downloading data from GDrive" }
        val response = gDriveApi.downloadFile(fileId)
        val fileBody = response.body()
            ?: throw BackupDownloadFailedThrowable()
        logI { "Downloaded backup data" }
        backupService.cacheDownloadedRestoreData(fileBody.byteStream(), timestamp)
        logI { "Cached restore data" }
    }

    @Throws(
        RestoreFailedThrowable::class,
        BackupDownloadFailedThrowable::class,
        BackupCachingFailedThrowable::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    override suspend fun performAppDataRestoreFromCache(
        password: String,
        passwordSalt: String,
        scheme: EncryptionScheme,
        timestamp: LocalDateTime
    ) = withContext(Dispatchers.IO) {
        logI { "Restoring Backup from cache" }
        val effectivePasswordHash = when (scheme) {
            EncryptionScheme.ARGON2_GCM -> argon2CryptoManager
                .hash(password, passwordSalt)

            EncryptionScheme.LEGACY_BCRYPT_PBKDF2_CBC -> defaultCryptoManager
                .hash(password, passwordSalt)
        }
        backupService.restoreBackupFromCache(
            passwordHash = effectivePasswordHash.first,
            passwordSalt = passwordSalt,
            scheme = scheme,
            timestamp = timestamp
        )

        // Decrypt succeeding proves the password is correct — safe to (re)persist as the
        // current scheme regardless of which scheme was just used to restore. This lazily
        // migrates legacy-scheme local storage to Argon2 on every successful restore.
        val newSalt = argon2CryptoManager.generateSalt()
        val (newHash, _) = argon2CryptoManager.hash(password, newSalt)
        securityPreferencesManager.updateBackupEncryptionHash(
            hash = newHash,
            salt = newSalt,
            scheme = EncryptionScheme.ARGON2_GCM
        )
        preferencesManager.updateLastBackupTimestamp(timestamp)
        logI { "Updated last backup timestamp" }
    }

    override suspend fun tryClearLocalCache() {
        trySuspend {
            backupService.clearCache()
        }
    }

    private fun backupFolderName(email: String): String = "$email backup"

    override suspend fun setBackupError(error: FatalBackupError?) =
        preferencesManager.updateFatalBackupError(error)

    override suspend fun restoreBackupJobs(): Unit = withContext(Dispatchers.IO) {
        val backupInterval = configDao.getBackupInterval()
            ?.let { BackupInterval.valueOf(it) }
            ?: return@withContext
        if (backupInterval == BackupInterval.MANUAL) backupWorkManager.cancelPeriodicBackupWork()
        backupWorkManager.schedulePeriodicBackupWork(backupInterval)
    }

}

const val JSON_MIME_TYPE = "application/json"
const val BACKUP_MIME_TYPE = "application/octet-stream"
const val APP_DATA_SPACE = "appDataFolder"
const val G_DRIVE_FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
//const val G_DRIVE_SHORTCUT_MIME_TYPE = "application/vnd.google-apps.shortcut"

class NoBackupFoundThrowable : Throwable("No Backups Found")
class BackupDownloadFailedThrowable : Throwable("Failed to download backup data")
class InvalidEncryptionPasswordThrowable : Throwable("")