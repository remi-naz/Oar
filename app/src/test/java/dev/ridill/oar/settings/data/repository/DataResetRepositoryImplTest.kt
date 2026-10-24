package dev.ridill.oar.settings.data.repository

import dev.ridill.oar.budgetCycles.data.local.BudgetCycleDao
import dev.ridill.oar.budgetCycles.domain.cycleManager.CycleManager
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.data.preferences.security.SecurityPreferencesManager
import dev.ridill.oar.moneyPiles.data.local.MoneyPileDao
import dev.ridill.oar.moneyPiles.domain.pileReminder.PileReminder
import dev.ridill.oar.schedules.data.local.SchedulesDao
import dev.ridill.oar.schedules.domain.scheduleReminder.ScheduleReminder
import dev.ridill.oar.settings.domain.appInit.AppInitWorkManager
import dev.ridill.oar.settings.domain.backup.BackupWorkManager
import dev.ridill.oar.settings.domain.repositoty.BackupRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class DataResetRepositoryImplTest {

    private val database = mockk<OarDatabase>()
    private val preferencesManager = mockk<PreferencesManager>()
    private val securityPreferencesManager = mockk<SecurityPreferencesManager>()
    private val backupRepository = mockk<BackupRepository>()
    private val backupWorkManager = mockk<BackupWorkManager>()
    private val appInitWorkManager = mockk<AppInitWorkManager>()
    private val scheduleReminder = mockk<ScheduleReminder>()
    private val pileReminder = mockk<PileReminder>()
    private val cycleManager = mockk<CycleManager>()

    private val schedulesDao = mockk<SchedulesDao>()
    private val pileDao = mockk<MoneyPileDao>()
    private val cycleDao = mockk<BudgetCycleDao>()

    private val repo = DataResetRepositoryImpl(
        database = database,
        preferencesManager = preferencesManager,
        securityPreferencesManager = securityPreferencesManager,
        backupRepository = backupRepository,
        backupWorkManager = backupWorkManager,
        appInitWorkManager = appInitWorkManager,
        scheduleReminder = scheduleReminder,
        pileReminder = pileReminder,
        cycleManager = cycleManager
    )

    @Before
    fun setUp() {
        every { database.schedulesDao() } returns schedulesDao
        every { database.moneyPileDao() } returns pileDao
        every { database.budgetCycleDao() } returns cycleDao
        coEvery { schedulesDao.getAllScheduleIds() } returns listOf(1L, 2L)
        coEvery { pileDao.getAllPileIds() } returns listOf(10L)
        coEvery { cycleDao.getAllCycleIds() } returns listOf(100L, 101L, 102L)

        every { backupWorkManager.cancelAllBackupAndRestoreWork() } just Runs
        every { scheduleReminder.cancel(any()) } just Runs
        every { pileReminder.cancel(any()) } just Runs
        every { cycleManager.cancelCycleCompletion(any()) } just Runs
        every { database.clearAllTables() } just Runs
        coEvery { preferencesManager.resetDataPreferences() } just Runs
        coEvery { securityPreferencesManager.updateBackupEncryptionHash(any(), any(), any()) } just Runs
        coEvery { backupRepository.tryClearLocalCache() } just Runs
        every { appInitWorkManager.startAppInitWorker() } just Runs
        every { appInitWorkManager.startAlarmsAndReminderInitWorkers() } just Runs
    }

    @Test
    fun cancelsEveryAlarmByRowId() = runTest {
        repo.clearAllLocalData()

        verify { scheduleReminder.cancel(1L) }
        verify { scheduleReminder.cancel(2L) }
        verify { pileReminder.cancel(10L) }
        listOf(100L, 101L, 102L).forEach { verify { cycleManager.cancelCycleCompletion(it) } }
        verify(exactly = 1) { backupWorkManager.cancelAllBackupAndRestoreWork() }
    }

    @Test
    fun alarmsAreCancelledBeforeTablesAreCleared() = runTest {
        repo.clearAllLocalData()

        // The cancels run in parallel, so only their order relative to the wipe is guaranteed
        coVerifyOrder {
            scheduleReminder.cancel(1L)
            database.clearAllTables()
        }
        coVerifyOrder {
            pileReminder.cancel(10L)
            database.clearAllTables()
        }
        coVerifyOrder {
            cycleManager.cancelCycleCompletion(100L)
            database.clearAllTables()
        }
        coVerifyOrder {
            backupWorkManager.cancelAllBackupAndRestoreWork()
            database.clearAllTables()
        }
    }

    @Test
    fun clearsTablesPreferencesEncryptionHashAndCache() = runTest {
        repo.clearAllLocalData()

        verify(exactly = 1) { database.clearAllTables() }
        coVerify(exactly = 1) { preferencesManager.resetDataPreferences() }
        coVerify(exactly = 1) {
            securityPreferencesManager.updateBackupEncryptionHash(
                hash = null,
                salt = null,
                scheme = any()
            )
        }
        coVerify(exactly = 1) { backupRepository.tryClearLocalCache() }
    }

    @Test
    fun reseedsInitWorkersAfterClearing() = runTest {
        repo.clearAllLocalData()

        coVerifyOrder {
            database.clearAllTables()
            appInitWorkManager.startAppInitWorker()
            appInitWorkManager.startAlarmsAndReminderInitWorkers()
        }
    }

    @Test
    fun clearFailure_propagatesAndSkipsReseed() = runTest {
        every { database.clearAllTables() } throws IllegalStateException("boom")

        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { repo.clearAllLocalData() }
        }

        verify(exactly = 0) { appInitWorkManager.startAppInitWorker() }
        verify(exactly = 0) { appInitWorkManager.startAlarmsAndReminderInitWorkers() }
    }
}
