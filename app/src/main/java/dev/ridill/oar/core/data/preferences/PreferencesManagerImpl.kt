package dev.ridill.oar.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.ridill.oar.core.domain.model.OarPreferences
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.logE
import dev.ridill.oar.core.domain.util.orFalse
import dev.ridill.oar.core.domain.util.orTrue
import dev.ridill.oar.core.domain.util.tryOrNull
import java.time.Instant
import java.time.ZoneId
import dev.ridill.oar.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.oar.settings.domain.modal.AppTheme
import dev.ridill.oar.settings.domain.repositoty.FatalBackupError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDateTime

class PreferencesManagerImpl(
    private val dataStore: DataStore<Preferences>
) : PreferencesManager {

    override val preferences: Flow<OarPreferences> = dataStore.data
        .catch { cause ->
            if (cause is IOException) {
                logE(cause) { "Preferences Exception" }
                emit(emptyPreferences())
            } else throw cause
        }
        .mapLatest { preferences ->
            val showOnboarding = preferences[Keys.SHOW_ONBOARDING].orTrue()
            val appTheme = AppTheme.valueOf(
                preferences[Keys.APP_THEME] ?: AppTheme.SYSTEM_DEFAULT.name
            )
            val dynamicColorsEnabled = preferences[Keys.DYNAMIC_COLORS_ENABLED].orFalse()
            val lastBackupDateTime = preferences[Keys.LAST_BACKUP_TIMESTAMP]
                ?.let { str ->
                    tryOrNull { Instant.parse(str).atZone(ZoneId.systemDefault()).toLocalDateTime() }
                        ?: DateUtil.parseDateTimeOrNull(str)
                }
            val transactionAutoDetectEnabled =
                preferences[Keys.TRANSACTION_AUTO_DETECT_ENABLED].orFalse()
            val allTransactionsShowExcludedOption =
                preferences[Keys.ALL_TX_SHOW_EXCLUDED_OPTION].orTrue()
            val appLockEnabled = preferences[Keys.APP_LOCK_ENABLED].orFalse()
            val appAutoLockInterval = AppAutoLockInterval.valueOf(
                preferences[Keys.APP_AUTO_LOCK_INTERVAL] ?: AppAutoLockInterval.ONE_MINUTE.name
            )
            val isAppLocked = preferences[Keys.IS_APP_LOCKED].orFalse()
            val screenSecurityEnabled = preferences[Keys.SCREEN_SECURITY_ENABLED].orFalse()
            val fatalBackupError = tryOrNull {
                preferences[Keys.FATAL_BACKUP_ERROR]?.let { FatalBackupError.valueOf(it) }
            }
            val showAutoDetectTxInfo = preferences[Keys.SHOW_AUTO_DETECT_TX_INFO].orTrue()

            OarPreferences(
                showOnboarding = showOnboarding,
                appTheme = appTheme,
                dynamicColorsEnabled = dynamicColorsEnabled,
                lastBackupDateTime = lastBackupDateTime,
                transactionAutoDetectEnabled = transactionAutoDetectEnabled,
                allTransactionsShowExcludedOption = allTransactionsShowExcludedOption,
                appLockEnabled = appLockEnabled,
                appAutoLockInterval = appAutoLockInterval,
                isAppLocked = isAppLocked,
                screenSecurityEnabled = screenSecurityEnabled,
                fatalBackupError = fatalBackupError,
                showAutoDetectTxInfo = showAutoDetectTxInfo,
            )
        }

    override suspend fun concludeOnboarding() {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.SHOW_ONBOARDING] = false
            }
        }
    }

    override suspend fun updateAppThem(theme: AppTheme) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.APP_THEME] = theme.name
            }
        }
    }

    override suspend fun updateDynamicColorsEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.DYNAMIC_COLORS_ENABLED] = enabled
            }
        }
    }

    override suspend fun updateLastBackupTimestamp(localDateTime: LocalDateTime) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.LAST_BACKUP_TIMESTAMP] = localDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toString()
            }
        }
    }

    override suspend fun updateTransactionAutoDetectEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.TRANSACTION_AUTO_DETECT_ENABLED] = enabled
            }
        }
    }

    override suspend fun updateAllTransactionsShowExcludedOption(show: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.ALL_TX_SHOW_EXCLUDED_OPTION] = show
            }
        }
    }

    override suspend fun updateAppLockEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.APP_LOCK_ENABLED] = enabled
            }
        }
    }

    override suspend fun updateAppAutoLockInterval(interval: AppAutoLockInterval) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.APP_AUTO_LOCK_INTERVAL] = interval.name
            }
        }
    }

    override suspend fun updateAppLocked(locked: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.IS_APP_LOCKED] = locked
            }
        }
    }

    override suspend fun updateScreenSecurityEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.SCREEN_SECURITY_ENABLED] = enabled
            }
        }
    }

    override suspend fun updateFatalBackupError(error: FatalBackupError?) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.FATAL_BACKUP_ERROR] = error?.name.orEmpty()
            }
        }
    }

    override suspend fun toggleShowAutoDetectTxInfoFalse() {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.SHOW_AUTO_DETECT_TX_INFO] = false
            }
        }
    }

    private object Keys {
        val SHOW_ONBOARDING = booleanPreferencesKey("SHOW_ONBOARDING")
        val APP_THEME = stringPreferencesKey("APP_THEME")
        val DYNAMIC_COLORS_ENABLED = booleanPreferencesKey("DYNAMIC_COLORS_ENABLED")
        val LAST_BACKUP_TIMESTAMP = stringPreferencesKey("LAST_BACKUP_TIMESTAMP")
        val TRANSACTION_AUTO_DETECT_ENABLED =
            booleanPreferencesKey("TRANSACTION_AUTO_DETECT_ENABLED")
        val ALL_TX_SHOW_EXCLUDED_OPTION = booleanPreferencesKey("ALL_TX_SHOW_EXCLUDED_OPTION")
        val APP_LOCK_ENABLED = booleanPreferencesKey("APP_LOCK_ENABLED")
        val APP_AUTO_LOCK_INTERVAL = stringPreferencesKey("APP_AUTO_LOCK_INTERVAL")
        val IS_APP_LOCKED = booleanPreferencesKey("IS_APP_LOCKED")
        val SCREEN_SECURITY_ENABLED = booleanPreferencesKey("SCREEN_SECURITY_ENABLED")
        val FATAL_BACKUP_ERROR = stringPreferencesKey("FATAL_BACKUP_ERROR")
        val SHOW_AUTO_DETECT_TX_INFO = booleanPreferencesKey("SHOW_AUTO_DETECT_TX_INFO")
    }
}