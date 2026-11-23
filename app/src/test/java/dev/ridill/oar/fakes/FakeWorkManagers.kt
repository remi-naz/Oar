package dev.ridill.oar.fakes

import androidx.work.WorkInfo
import dev.ridill.oar.settings.domain.appInit.AppInitWorkManager
import dev.ridill.oar.settings.domain.backup.BackupWorkManager
import dev.ridill.oar.settings.domain.modal.BackupDetails
import dev.ridill.oar.settings.domain.modal.BackupInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeBackupWorkManager : BackupWorkManager {

    var cancelAllCount = 0
        private set

    override fun schedulePeriodicBackupWork(interval: BackupInterval) = Unit
    override fun getPeriodicBackupWorkInfoFlow(): Flow<WorkInfo?> = flowOf(null)
    override fun cancelPeriodicBackupWork() = Unit
    override fun cancelAllBackupAndRestoreWork() {
        cancelAllCount++
    }

    override fun runImmediateBackupWork() = Unit
    override fun getImmediateBackupWorkInfoFlow(): Flow<WorkInfo?> = flowOf(null)
    override fun runImmediateRestoreWork(backupDetails: BackupDetails, password: String) = Unit
    override fun getRestoreDataDownloadWorkInfoFlow(): Flow<WorkInfo?> = flowOf(null)
    override fun getImmediateDataRestoreWorkInfoFlow(): Flow<WorkInfo?> = flowOf(null)
    override fun getBackupIntervalFromWorkInfo(info: WorkInfo): BackupInterval? = null
}

/**
 * [onCall] runs at the moment each worker is started, so a test can capture the state the
 * system was in at that point, e.g. whether the database had already been wiped.
 */
class FakeAppInitWorkManager(
    private val onCall: (String) -> Unit = {}
) : AppInitWorkManager {

    private val _calls = mutableListOf<String>()
    val calls: List<String> get() = _calls

    private fun record(name: String) {
        _calls += name
        onCall(name)
    }

    override fun startAppInitWorker() = record("startAppInitWorker")
    override fun startCycleCatchUpWorker() = record("startCycleCatchUpWorker")
    override fun scheduleDailyCycleCatchUp() = record("scheduleDailyCycleCatchUp")
    override fun startConfigRestoreWorkers() = record("startConfigRestoreWorkers")
    override fun startAlarmsAndReminderInitWorkers() = record("startAlarmsAndReminderInitWorkers")
}
