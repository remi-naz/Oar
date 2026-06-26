package dev.ridill.oar.budgetCycles.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import androidx.room.withTransaction
import dev.ridill.oar.R
import dev.ridill.oar.aggregations.data.local.AggregationsDao
import dev.ridill.oar.budgetCycles.data.local.BudgetCycleDao
import dev.ridill.oar.budgetCycles.data.local.entity.BudgetCycleEntity
import dev.ridill.oar.budgetCycles.data.toEntity
import dev.ridill.oar.budgetCycles.data.toEntry
import dev.ridill.oar.budgetCycles.data.toHistoryEntry
import dev.ridill.oar.budgetCycles.domain.cycleManager.CycleManager
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleConfig
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleEntry
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleError
import dev.ridill.oar.budgetCycles.domain.model.BudgetCycleSummary
import dev.ridill.oar.budgetCycles.domain.model.CycleDurationUnit
import dev.ridill.oar.budgetCycles.domain.model.CycleHistoryEntry
import dev.ridill.oar.budgetCycles.domain.model.CycleSelector
import dev.ridill.oar.budgetCycles.domain.model.CycleStartDay
import dev.ridill.oar.budgetCycles.domain.model.CycleStartDayType
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.core.domain.util.logD
import dev.ridill.oar.core.domain.util.logE
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.oar.core.domain.util.tryOrNull
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.folders.domain.model.AggregateType
import dev.ridill.oar.settings.data.local.ConfigDao
import dev.ridill.oar.settings.data.local.ConfigKey
import dev.ridill.oar.settings.data.local.entity.ConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Currency
import kotlin.math.absoluteValue

private const val TAG = "BudgetCycleRepository"

class BudgetCycleRepositoryImpl(
    private val db: OarDatabase,
    private val cycleDao: BudgetCycleDao,
    private val aggDao: AggregationsDao,
    private val configDao: ConfigDao,
    private val manager: CycleManager
) : BudgetCycleRepository {

    override fun getActiveCycleFlow(): Flow<BudgetCycleEntry?> = cycleDao
        .getActiveCycleFlow()
        .mapLatest { it?.toEntry() }

    override suspend fun getActiveCycle(): BudgetCycleEntry? = withContext(Dispatchers.IO) {
        cycleDao.getActiveCycleDetailsView()?.toEntry()
    }

    override suspend fun getCycleConfig(): BudgetCycleConfig = withContext(Dispatchers.IO) {
        val budgetAmount = configDao.getCycleBudget()?.toLongOrNull().orZero()
        val budgetCurrency = configDao.getCycleCurrencyCode()
            ?.let { LocaleUtil.currencyForCode(it) }
            ?: LocaleUtil.defaultCurrency

        val type = configDao.getCycleStartDayType()
            ?.let { tryOrNull { CycleStartDayType.valueOf(it) } }
            ?: CycleStartDayType.LAST_DAY_OF_MONTH

        val startDay = when (type) {
            CycleStartDayType.FIRST_DAY_OF_MONTH -> CycleStartDay.FirstDayOfMonth

            CycleStartDayType.LAST_DAY_OF_MONTH -> CycleStartDay.LastDayOfMonth

            CycleStartDayType.SPECIFIC_DAY_OF_MONTH -> {
                val dayOfMonth =
                    configDao.getValueForKey(ConfigKey.CYCLE_START_DAY_OF_MONTH)
                        ?.toIntOrNull() ?: 1
                CycleStartDay.SpecificDayOfMonth(dayOfMonth)
            }
        }

        val duration = configDao.getCycleDuration()?.toLongOrNull() ?: 1L
        val durationUnit = configDao.getCycleDurationUnit()
            ?.let { tryOrNull { CycleDurationUnit.valueOf(it) } }
            ?: CycleDurationUnit.MONTH

        return@withContext BudgetCycleConfig(
            budget = budgetAmount,
            currency = budgetCurrency,
            startDay = startDay,
            duration = duration,
            durationUnit = durationUnit
        )
    }

    override suspend fun updateCycleConfig(
        budget: Long,
        currency: Currency,
        startDay: CycleStartDay,
        duration: Long,
        durationUnit: CycleDurationUnit
    ): Unit = withContext(Dispatchers.IO) {
        db.withTransaction {
            val budgetUpdate = async {
                val entity = ConfigEntity(
                    configKey = ConfigKey.CYCLE_BUDGET_AMOUNT,
                    configValue = budget.toString()
                )
                configDao.upsert(entity)
            }

            val currencyUpdate = async {
                val entity = ConfigEntity(
                    configKey = ConfigKey.CYCLE_CURRENCY_CODE,
                    configValue = currency.currencyCode
                )
                configDao.upsert(entity)
            }

            val startDayTypeUpdate = async {
                val entity = ConfigEntity(
                    configKey = ConfigKey.CYCLE_START_DAY_TYPE,
                    configValue = startDay.type.name
                )
                configDao.upsert(entity)
            }

            val startDayDataUpdate = async {
                val day = when (startDay) {
                    is CycleStartDay.FirstDayOfMonth -> 1
                    is CycleStartDay.SpecificDayOfMonth -> startDay.dayOfMonth
                    is CycleStartDay.LastDayOfMonth -> -1
                }
                val entity = ConfigEntity(
                    configKey = ConfigKey.CYCLE_START_DAY_OF_MONTH,
                    configValue = day.toString()
                )
                configDao.upsert(entity)
            }

            val durationUpdate = async {
                val entity = ConfigEntity(
                    configKey = ConfigKey.CYCLE_DURATION,
                    configValue = duration.toString()
                )
                configDao.upsert(entity)
            }

            val durationUnitUpdate = async {
                val entity = ConfigEntity(
                    configKey = ConfigKey.CYCLE_DURATION_UNIT,
                    configValue = durationUnit.name
                )
                configDao.upsert(entity)
            }

            awaitAll(
                budgetUpdate,
                currencyUpdate,
                startDayTypeUpdate,
                startDayDataUpdate,
                durationUpdate,
                durationUnitUpdate
            )
        }
    }

    override suspend fun updateBudgetForActiveCycle(value: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            configDao.upsert(
                ConfigEntity(
                    configKey = ConfigKey.CYCLE_BUDGET_AMOUNT,
                    configValue = value.toString()
                )
            )
            cycleDao.updateBudgetForActiveCycle(value)
        }
    }

    override suspend fun updateCurrencyForActiveCycle(
        currency: Currency
    ) = withContext(Dispatchers.IO) {
        db.withTransaction {
            configDao.upsert(
                ConfigEntity(
                    configKey = ConfigKey.CYCLE_CURRENCY_CODE,
                    configValue = currency.currencyCode
                )
            )
            cycleDao.updateCurrencyCodeForActiveCycle(currency.currencyCode)
        }
    }

    override suspend fun getLastCycle(): BudgetCycleEntry? = withContext(Dispatchers.IO) {
        cycleDao.getLastCycle()?.toEntry()
    }

    override fun scheduleCycleCompletion(
        cycle: BudgetCycleEntry
    ): Result<Unit, BudgetCycleError> = manager
        .scheduleCycleCompletion(
            cycleId = cycle.id,
            endDate = cycle.endDate
                .plusDays(1)
                .atStartOfDay()
        )

    override suspend fun scheduleLastCycleOrNew(): Result<Unit, BudgetCycleError> =
        withContext(Dispatchers.IO) {
            try {
                val lastCycle = getLastCycle()
                val dateNow = DateUtil.dateNow()
                logD(TAG) { "lastCycle = $lastCycle" }
                val isLastCycleActiveRightNow = lastCycle?.active == true
                        && lastCycle.endDate.isAfter(dateNow)
                logD(TAG) { "isLastCycleActiveRightNow = $isLastCycleActiveRightNow" }

                return@withContext if (isLastCycleActiveRightNow) {
                    // Continue Ongoing cycle
                    // Schedule it's completion alarm
                    logI(TAG) { "Continuing ongoing cycle" }
                    scheduleCycleCompletion(lastCycle)
                } else {
                    logI(TAG) { "Creating new cycle" }
                    createNewCycleAndScheduleCompletion(month = YearMonth.from(dateNow))
                }
            } catch (t: Throwable) {
                t.rethrowIfCoroutineCancellation()
                Result.Error(
                    error = BudgetCycleError.UNKNOWN,
                    message = UiText.StringResource(R.string.error_failed_to_start_cycle, true)
                )
            }
        }

    override suspend fun createNewCycleAndScheduleCompletion(
        month: YearMonth,
        startNow: Boolean
    ): Result<Unit, BudgetCycleError> = withContext(Dispatchers.IO) {
        try {
            logI(TAG) { "createNewCycleAndScheduleCompletion() called with: month = $month" }
            val entry = createCycleEntryFromConfigForMonth(month = month, startNow = startNow)
            logI(TAG) { "inserting cycle = $entry" }
            val existingCycle = cycleDao
                .getCycleForDate(startDate = entry.startDate, endDate = entry.endDate)
            logI(TAG) { "existing cycle found = $existingCycle" }

            val cycleId = existingCycle?.id
                ?: cycleDao.upsert(entry.toEntity()).first()
                    .takeIf { it != -1L }
                ?: throw CycleEntryCreationFailedThrowable(entry)

            logI(TAG) { "continuing with cycleId = $cycleId" }
            updateActiveCycleId(cycleId)
            val activeCycle = cycleDao.getActiveCycle()
                ?: throw CycleNotFoundThrowable(cycleId)
            logD(TAG) { "entry = $entry created with ID = $cycleId" }
            existingCycle?.id?.let { manager.cancelCycleCompletion(it) }
            scheduleCycleCompletion(activeCycle.toEntry(true))
        } catch (t: CycleEntryCreationFailedThrowable) {
            logE(t, TAG) { "createNewCycleAndScheduleCompletion" }
            Result.Error(
                error = BudgetCycleError.CREATION_FAILED,
                message = UiText.StringResource(R.string.error_failed_to_start_cycle, true)
            )
        } catch (t: Throwable) {
            t.rethrowIfCoroutineCancellation()
            logE(t, TAG) { "createNewCycleAndScheduleCompletion" }
            Result.Error(
                error = BudgetCycleError.UNKNOWN,
                message = UiText.StringResource(R.string.error_unknown, true)
            )
        }
    }

    override suspend fun createCycleEntryFromConfigForMonth(
        month: YearMonth,
        startNow: Boolean
    ): BudgetCycleEntry = withContext(Dispatchers.IO) {
        logI(TAG) { "createCycleEntryFromConfigForMonth() called with: month = $month" }
        val config = getCycleConfig()
        val dateNow = DateUtil.dateNow()
        val dateFromYearMonth = DateUtil.dateNow()
            .with(month)
        val startDay = config.startDay
        logD(TAG) { "createCycleEntryFromConfigForMonth: startDay = $startDay" }
        val (startDate, endDate) = when (startDay) {
            CycleStartDay.FirstDayOfMonth -> {
                val startDate = if (startNow) dateNow
                else dateNow.withDayOfMonth(1)

                val endDate = dateFromYearMonth
                    .with(TemporalAdjusters.lastDayOfMonth())

                startDate to endDate
            }

            CycleStartDay.LastDayOfMonth -> {
                val startDate = if (startNow) dateNow
                else dateNow
                    .withMonth(month.monthValue - 1)
                    .with(TemporalAdjusters.lastDayOfMonth())

                val endDate = dateFromYearMonth
                    .with(TemporalAdjusters.lastDayOfMonth())
                    .minusDays(1L)

                startDate to endDate
            }

            is CycleStartDay.SpecificDayOfMonth -> {
                val startDate = if (startNow) dateNow
                else dateNow
                    .withMonth(month.monthValue - 1)
                    .withDayOfMonth(startDay.dayOfMonth)

                val endDate = dateFromYearMonth
                    .withDayOfMonth(startDay.dayOfMonth)
                    .minusDays(1L)

                startDate to endDate
            }
        }

        logD(TAG) { "startDate = $startDate, endDate = $endDate" }

        return@withContext BudgetCycleEntry(
            id = OarDatabase.DEFAULT_ID_LONG,
            startDate = startDate,
            endDate = endDate,
            budget = config.budget,
            currency = config.currency,
            active = false
        )
    }

    override suspend fun completeCycleNowAndStartNext(
        id: Long
    ): Result<BudgetCycleSummary, BudgetCycleError> = withContext(Dispatchers.IO) {
        logI(TAG) { "completeCurrentCycleAndStartNext() called with ID = $id" }
        try {
            db.withTransaction {
                val cycleById = cycleDao.getCycleById(id)
                    ?: throw CycleNotFoundThrowable(id)
                logD(TAG) { "cycle = $cycleById" }

                if (!cycleById.active) throw CycleNotActiveThrowable(id)

                val enforcedCycleEndDate = DateUtil.dateNow().minusDays(1L)
                if (cycleById.startDate.isAfter(enforcedCycleEndDate))
                    throw IllegalCycleThrowable()

                if (cycleById.endDate != enforcedCycleEndDate) {
                    // Update active cycles endDate to yesterday
                    cycleDao.upsert(
                        BudgetCycleEntity(
                            id = id,
                            startDate = cycleById.startDate,
                            endDate = enforcedCycleEndDate,
                            budget = cycleById.budget,
                            currencyCode = cycleById.currencyCode,
                        )
                    )
                }

                // Create Next Cycle Entry
                val newCycleResult = createNewCycleAndScheduleCompletion(
                    month = YearMonth.from(enforcedCycleEndDate.plusMonths(1L)),
                    startNow = true
                )

                return@withTransaction when (newCycleResult) {
                    is Result.Error -> Result.Error(
                        error = newCycleResult.error,
                        message = newCycleResult.message
                    )

                    is Result.Success -> {
                        // Get Aggregate
                        val cycleConfig = getCycleConfig()
                        val aggregateForCycle = aggDao.getAggregateAmountForCycle(id)
                        val aggregateType = AggregateType.fromAmount(aggregateForCycle)
                        val summary = BudgetCycleSummary(
                            aggregateAmount = aggregateForCycle.absoluteValue,
                            aggregateType = aggregateType,
                            currency = cycleConfig.currency
                        )
                        logD(TAG) { "summary = $summary" }

                        Result.Success(summary)
                    }
                }
            }
        } catch (t: CycleNotFoundThrowable) {
            logE(t) { "completeCurrentCycleAndStartNext" }
            Result.Error(
                error = BudgetCycleError.CYCLE_NOT_FOUND,
                message = UiText.StringResource(R.string.error_cycle_not_found, true)
            )
        } catch (t: CycleNotActiveThrowable) {
            logE(t) { "completeCurrentCycleAndStartNext" }
            Result.Error(
                error = BudgetCycleError.CYCLE_NOT_ACTIVE,
                message = UiText.StringResource(R.string.error_cycle_not_active, true)
            )
        } catch (t: IllegalCycleThrowable) {
            logE(t) { "completeCurrentCycleAndStartNext" }
            Result.Error(
                error = BudgetCycleError.ILLEGAL_CYCLE,
                message = UiText.StringResource(R.string.error_illegal_cycle_action, true)
            )
        } catch (t: CycleEntryCreationFailedThrowable) {
            logE(t) { "completeCurrentCycleAndStartNext" }
            Result.Error(
                error = BudgetCycleError.CREATION_FAILED,
                message = UiText.StringResource(R.string.error_failed_to_start_cycle, true)
            )
        } catch (t: Throwable) {
            logE(t) { "completeCurrentCycleAndStartNext" }
            t.rethrowIfCoroutineCancellation()
            Result.Error(
                error = BudgetCycleError.UNKNOWN,
                message = UiText.StringResource(R.string.error_unknown)
            )
        }
    }

    override fun getCycleByIdFlow(id: Long): Flow<BudgetCycleEntry?> = cycleDao
        .getCycleByIdFlow(id)
        .mapLatest { it?.toEntry() }
        .distinctUntilChanged()

    override suspend fun updateConfigAndCreateNewCycle(
        budget: Long,
        currency: Currency,
        startDay: CycleStartDay,
        month: YearMonth,
        duration: Long,
        durationUnit: CycleDurationUnit,
        startNow: Boolean
    ): Result<Unit, BudgetCycleError> = withContext(Dispatchers.IO) {
        db.withTransaction {
            updateCycleConfig(
                budget = budget,
                currency = currency,
                startDay = startDay,
                duration = duration,
                durationUnit = durationUnit
            )

            createNewCycleAndScheduleCompletion(
                month = month,
                startNow = startNow
            )
        }
    }

    private suspend fun updateActiveCycleId(id: Long) = withContext(Dispatchers.IO) {
        logD(TAG) { "updateActiveCycleId() called with: id = $id" }
        configDao.upsert(
            ConfigEntity(
                configKey = ConfigKey.ACTIVE_CYCLE_ID,
                configValue = id.toString()
            )
        )
        logI(TAG) { "set Id = $id as active cycle" }
    }

    override suspend fun getCyclesSelectorsPagingData(
        query: String
    ): Flow<PagingData<CycleSelector>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = { cycleDao.getCycleSelectionItems() }
    ).flow
        .mapLatest { pagingData ->
            pagingData.filter {
                it.description.contains(query, ignoreCase = true)
                        || query.contains(
                    other = it.startDate.month
                        .getDisplayName(TextStyle.SHORT_STANDALONE, LocaleUtil.defaultLocale),
                    ignoreCase = true
                )
                        || query.contains(
                    other = it.endDate.month
                        .getDisplayName(TextStyle.SHORT_STANDALONE, LocaleUtil.defaultLocale),
                    ignoreCase = true
                )
                        || query.contains(it.startDate.year.toString())
                        || query.contains(it.endDate.year.toString())
            }
        }

    override fun getActiveCycleDetails(): Flow<CycleHistoryEntry?> = cycleDao
        .getActiveCycleFlow()
        .mapLatest { it?.toHistoryEntry() }

    override fun getCompletedCycleDetails(): Flow<PagingData<CycleHistoryEntry>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = { cycleDao.getNotActiveCycleDetails() }
    ).flow
        .mapLatest { pagingData ->
            pagingData.map { it.toHistoryEntry() }
        }
}

class CycleEntryCreationFailedThrowable(entity: BudgetCycleEntry) :
    IllegalStateException("Failed to create cycle entity $entity")

class CycleNotFoundThrowable(val id: Long) :
    IllegalStateException("Cycle not found for ID = $id")

class CycleNotActiveThrowable(val id: Long) :
    IllegalStateException("Cycle with ID = $id is not ACTIVE")

class IllegalCycleThrowable : IllegalStateException("Illegal cycle")