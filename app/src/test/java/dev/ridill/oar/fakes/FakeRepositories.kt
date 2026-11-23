package dev.ridill.oar.fakes

import android.app.PendingIntent
import android.content.Intent
import androidx.work.WorkInfo
import dev.ridill.oar.account.domain.model.AuthState
import dev.ridill.oar.account.domain.model.UserAccount
import dev.ridill.oar.account.domain.repository.AuthRepository
import dev.ridill.oar.account.presentation.util.AuthorizationService
import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import dev.ridill.oar.core.domain.model.DataError
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.settings.domain.modal.AppTheme
import dev.ridill.oar.settings.domain.modal.BackupDetails
import dev.ridill.oar.settings.domain.modal.BackupInterval
import dev.ridill.oar.settings.domain.repositoty.BackupRepository
import dev.ridill.oar.settings.domain.repositoty.BackupSettingsRepository
import dev.ridill.oar.settings.domain.repositoty.DataResetRepository
import dev.ridill.oar.settings.domain.repositoty.FatalBackupError
import dev.ridill.oar.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime

class FakeAuthRepository(
    initialState: AuthState = AuthState.UnAuthenticated
) : AuthRepository {

    val authState = MutableStateFlow(initialState)

    /** What the next [signUserOut] returns */
    var signOutResult: Result<Unit, DataError> = Result.Success(Unit)
    var signOutCount = 0
        private set

    override fun getSignedInAccount(): UserAccount? =
        (authState.value as? AuthState.Authenticated)?.account

    override fun getAuthState(): Flow<AuthState> = authState

    override suspend fun signUserInWithToken(idToken: String): Result<Unit, DataError> =
        TODO("Not needed by any test yet")

    override suspend fun signUserOut(): Result<Unit, DataError> {
        signOutCount++
        return signOutResult
    }

    override suspend fun authorizeUserAccount(): Result<PendingIntent?, AuthorizationService.AuthorizationError> =
        TODO("Not needed by any test yet")

    override suspend fun decodeAuthorizationResult(intent: Intent): Result<Unit, AuthorizationService.AuthorizationError> =
        TODO("Not needed by any test yet")

    override suspend fun deleteAccount(): Result<Unit, DataError> =
        TODO("Not needed by any test yet")
}

/** [onClear] runs as the clear starts, so a test can capture what had already happened by then */
class FakeDataResetRepository(
    private val onClear: () -> Unit = {}
) : DataResetRepository {

    /** Thrown from [clearAllLocalData] when set */
    var failure: Throwable? = null
    var clearCount = 0
        private set

    override suspend fun clearAllLocalData() {
        clearCount++
        onClear()
        failure?.let { throw it }
    }
}

class FakeBackupSettingsRepository : BackupSettingsRepository {

    val lastBackupTime = MutableStateFlow<LocalDateTime?>(null)
    val fatalBackupError = MutableStateFlow<FatalBackupError?>(null)

    override fun getLastBackupTime(): Flow<LocalDateTime?> = lastBackupTime
    override fun getImmediateBackupWorkInfo(): Flow<WorkInfo?> = flowOf(null)
    override fun getPeriodicBackupWorkInfo(): Flow<WorkInfo?> = flowOf(null)
    override fun getIntervalFromInfo(workInfo: WorkInfo): BackupInterval? = null
    override suspend fun updateBackupIntervalAndScheduleJob(interval: BackupInterval) = Unit
    override fun runBackupJob(interval: BackupInterval) = Unit
    override fun runImmediateBackupJob() = Unit
    override suspend fun restoreBackupJob() = Unit
    override fun getFatalBackupError(): Flow<FatalBackupError?> = fatalBackupError
    override fun isEncryptionPasswordAvailable(): Flow<Boolean> = flowOf(false)
    override suspend fun isCurrentPasswordMatch(currentPasswordInput: String): Boolean = false
    override suspend fun updateEncryptionPassword(password: String) = Unit
}

class FakeSettingsRepository : SettingsRepository {

    val appTheme = MutableStateFlow(AppTheme.SYSTEM_DEFAULT)
    val dynamicColorsEnabled = MutableStateFlow(false)

    override fun refreshCurrentDate() = Unit
    override fun getCurrentAppTheme(): Flow<AppTheme> = appTheme
    override suspend fun updateAppTheme(theme: AppTheme) {
        appTheme.value = theme
    }

    override fun getDynamicColorsEnabled(): Flow<Boolean> = dynamicColorsEnabled
    override suspend fun toggleDynamicColors(enabled: Boolean) {
        dynamicColorsEnabled.value = enabled
    }
}

class FakeBackupRepository : BackupRepository {

    /** Thrown from [tryClearLocalCache] when set */
    var clearCacheFailure: Throwable? = null
    var clearCacheCount = 0
        private set

    override suspend fun checkForBackup(): Result<BackupDetails, DataError> =
        TODO("Not needed by any test yet")

    override suspend fun performAppDataBackup() = Unit
    override suspend fun downloadAndCacheBackupData(fileId: String, timestamp: LocalDateTime) = Unit
    override suspend fun performAppDataRestoreFromCache(
        password: String,
        passwordSalt: String,
        scheme: EncryptionScheme,
        timestamp: LocalDateTime
    ) = Unit

    override suspend fun tryClearLocalCache() {
        clearCacheCount++
        clearCacheFailure?.let { throw it }
    }

    override suspend fun setBackupError(error: FatalBackupError?) = Unit
    override suspend fun restoreBackupJobs() = Unit
}
