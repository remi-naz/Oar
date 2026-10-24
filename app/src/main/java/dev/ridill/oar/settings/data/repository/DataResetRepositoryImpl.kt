package dev.ridill.oar.settings.data.repository

import dev.ridill.oar.budgetCycles.domain.cycleManager.CycleManager
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.data.preferences.security.SecurityPreferencesManager
import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.moneyPiles.domain.pileReminder.PileReminder
import dev.ridill.oar.schedules.domain.scheduleReminder.ScheduleReminder
import dev.ridill.oar.settings.domain.appInit.AppInitWorkManager
import dev.ridill.oar.settings.domain.backup.BackupWorkManager
import dev.ridill.oar.settings.domain.repositoty.BackupRepository
import dev.ridill.oar.settings.domain.repositoty.DataResetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class DataResetRepositoryImpl(
    private val database: OarDatabase,
    private val preferencesManager: PreferencesManager,
    private val securityPreferencesManager: SecurityPreferencesManager,
    private val backupRepository: BackupRepository,
    private val backupWorkManager: BackupWorkManager,
    private val appInitWorkManager: AppInitWorkManager,
    private val scheduleReminder: ScheduleReminder,
    private val pileReminder: PileReminder,
    private val cycleManager: CycleManager
) : DataResetRepository {

    override suspend fun clearAllLocalData() = withContext(Dispatchers.IO) {
        // Alarms are keyed by row id, so they have to be cancelled before the rows are gone
        coroutineScope {
            launch { backupWorkManager.cancelAllBackupAndRestoreWork() }
            launch { database.schedulesDao().getAllScheduleIds().forEach(scheduleReminder::cancel) }
            launch { database.moneyPileDao().getAllPileIds().forEach(pileReminder::cancel) }
            launch {
                database.budgetCycleDao().getAllCycleIds()
                    .forEach(cycleManager::cancelCycleCompletion)
            }
        }

        // clearAllTables runs in its own Room transaction; the rest live outside the database
        coroutineScope {
            launch { database.clearAllTables() }
            launch {
                securityPreferencesManager.updateBackupEncryptionHash(
                    hash = null,
                    salt = null,
                    scheme = EncryptionScheme.fromTag(null)
                )
            }
            launch { backupRepository.tryClearLocalCache() }
        }

        // clearAllTables also wipes the currency list and the active cycle, so seed them again
        appInitWorkManager.startAppInitWorker()
        appInitWorkManager.startAlarmsAndReminderInitWorkers()

        // Last, since it drops the onboarding flag and sends the UI back to onboarding
        preferencesManager.resetDataPreferences()
        logI(TAG) { "Local data cleared" }
    }
}

private const val TAG = "DataReset"
