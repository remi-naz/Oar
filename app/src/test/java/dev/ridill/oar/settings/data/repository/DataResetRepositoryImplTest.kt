package dev.ridill.oar.settings.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.ridill.oar.budgetCycles.data.local.entity.BudgetCycleEntity
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.crypto.EncryptionScheme
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.fakes.FakeAppInitWorkManager
import dev.ridill.oar.fakes.FakeBackupEncryptionPreferencesManager
import dev.ridill.oar.fakes.FakeBackupRepository
import dev.ridill.oar.fakes.FakeBackupWorkManager
import dev.ridill.oar.fakes.FakeCycleManager
import dev.ridill.oar.fakes.FakePileReminder
import dev.ridill.oar.fakes.FakePreferencesManager
import dev.ridill.oar.fakes.FakeScheduleReminder
import dev.ridill.oar.moneyPiles.data.local.entity.MoneyPileEntity
import dev.ridill.oar.moneyPiles.domain.model.PileContributionMode
import dev.ridill.oar.moneyPiles.domain.model.PileIcon
import dev.ridill.oar.moneyPiles.domain.model.PileReminderBehavior
import dev.ridill.oar.moneyPiles.domain.model.PileReminderCadence
import dev.ridill.oar.schedules.data.local.entity.ScheduleEntity
import dev.ridill.oar.schedules.domain.model.ScheduleRepetition
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
class DataResetRepositoryImplTest {

    private lateinit var db: OarDatabase

    private val events = mutableListOf<String>()
    private val tablesEmptyWhenCalled = mutableMapOf<String, Boolean>()

    private val backupWorkManager = FakeBackupWorkManager()
    private val scheduleReminder = FakeScheduleReminder()
    private val pileReminder = FakePileReminder()
    private val cycleManager = FakeCycleManager()
    private val backupRepository = FakeBackupRepository()
    private val backupEncryptionPreferences = FakeBackupEncryptionPreferencesManager()
    private val preferencesManager = FakePreferencesManager(onReset = { events += RESET_PREFERENCES })
    private val appInitWorkManager = FakeAppInitWorkManager { call ->
        events += call
        tablesEmptyWhenCalled[call] = seededRowCount() == 0
    }

    private lateinit var repo: DataResetRepositoryImpl

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            OarDatabase::class.java
        ).allowMainThreadQueries().build()

        repo = DataResetRepositoryImpl(
            database = db,
            preferencesManager = preferencesManager,
            backupEncryptionPreferencesManager = backupEncryptionPreferences,
            backupRepository = backupRepository,
            backupWorkManager = backupWorkManager,
            appInitWorkManager = appInitWorkManager,
            scheduleReminder = scheduleReminder,
            pileReminder = pileReminder,
            cycleManager = cycleManager
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private class SeededIds(
        val scheduleIds: Set<Long>,
        val pileIds: Set<Long>,
        val cycleIds: Set<Long>
    )

    private suspend fun seedData(): SeededIds {
        val scheduleIds = db.schedulesDao().upsert(schedule(), schedule())
        val pileIds = db.moneyPileDao().upsert(pile("Trip"))
        val cycleIds = db.budgetCycleDao().upsert(
            cycle(startDay = 1, endDay = 10),
            cycle(startDay = 11, endDay = 20),
            cycle(startDay = 21, endDay = 28)
        )
        return SeededIds(scheduleIds.toSet(), pileIds.toSet(), cycleIds.toSet())
    }

    private fun schedule() = ScheduleEntity(
        amount = 100.0,
        note = null,
        currencyCode = "INR",
        type = FundMovement.OUT,
        tagId = null,
        folderId = null,
        repetition = ScheduleRepetition.entries.first(),
        lastPaymentTimestamp = null,
        nextPaymentTimestamp = null,
        originalDueDate = null
    )

    private fun pile(name: String) = MoneyPileEntity(
        name = name,
        icon = PileIcon.entries.first(),
        color = 0,
        contributionMode = PileContributionMode.entries.first(),
        reminderCadence = PileReminderCadence.entries.first(),
        reminderBehavior = PileReminderBehavior.entries.first(),
        reminderAmount = null,
        locked = false,
        currencyCode = "INR",
        targetAmount = null,
        targetRemainingAmount = null,
        targetDate = null,
        createdTimestamp = LocalDateTime.of(2026, 1, 1, 0, 0),
        completionTimestamp = null,
        nextReminderTimestamp = null
    )

    private fun cycle(startDay: Int, endDay: Int) = BudgetCycleEntity(
        startDate = LocalDate.of(2026, 1, startDay),
        endDate = LocalDate.of(2026, 1, endDay),
        budget = 1_000L,
        currencyCode = "INR"
    )

    private fun rowCount(table: String): Int =
        db.query("SELECT COUNT(*) FROM $table", emptyArray()).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    private fun seededRowCount(): Int = SEEDED_TABLES.sumOf(::rowCount)

    @Test
    fun cancelsEveryAlarmByRowId() = runTest {
        // Alarms are keyed by row id, so this only works if the cancels run before the wipe
        val seeded = seedData()

        repo.clearAllLocalData()

        assertThat(scheduleReminder.cancelledIds).containsExactlyElementsIn(seeded.scheduleIds)
        assertThat(pileReminder.cancelledIds).containsExactlyElementsIn(seeded.pileIds)
        assertThat(cycleManager.cancelledCycleIds).containsExactlyElementsIn(seeded.cycleIds)
        assertThat(backupWorkManager.cancelAllCount).isEqualTo(1)
    }

    @Test
    fun clearsAllTables() = runTest {
        seedData()
        assertThat(seededRowCount()).isGreaterThan(0)

        repo.clearAllLocalData()

        assertThat(seededRowCount()).isEqualTo(0)
    }

    @Test
    fun dropsBackupEncryptionHashAndLocalBackupCache() = runTest {
        repo.clearAllLocalData()

        assertThat(backupEncryptionPreferences.hashUpdates).containsExactly(
            FakeBackupEncryptionPreferencesManager.HashUpdate(
                hash = null,
                salt = null,
                scheme = EncryptionScheme.fromTag(null)
            )
        )
        assertThat(backupRepository.clearCacheCount).isEqualTo(1)
    }

    @Test
    fun reseedsInitWorkersOnlyAfterTablesAreCleared() = runTest {
        seedData()

        repo.clearAllLocalData()

        assertThat(appInitWorkManager.calls)
            .containsExactly("startAppInitWorker", "startAlarmsAndReminderInitWorkers")
            .inOrder()
        assertThat(tablesEmptyWhenCalled.values).containsExactly(true, true)
    }

    @Test
    fun resetsPreferencesLast_soOnboardingStartsAfterTheReseed() = runTest {
        repo.clearAllLocalData()

        assertThat(events)
            .containsExactly(
                "startAppInitWorker",
                "startAlarmsAndReminderInitWorkers",
                RESET_PREFERENCES
            )
            .inOrder()
    }

    @Test
    fun clearFailure_propagates_andSkipsReseedAndPreferenceReset() = runTest {
        backupRepository.clearCacheFailure = IllegalStateException("boom")

        val result = runCatching { repo.clearAllLocalData() }

        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(appInitWorkManager.calls).isEmpty()
        assertThat(preferencesManager.resetCount).isEqualTo(0)
    }

    private companion object {
        const val RESET_PREFERENCES = "resetDataPreferences"
        val SEEDED_TABLES = listOf("schedules_table", "money_pile_table", "budget_cycle_table")
    }
}
