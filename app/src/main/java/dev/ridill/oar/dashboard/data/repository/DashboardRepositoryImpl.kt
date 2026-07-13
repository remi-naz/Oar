package dev.ridill.oar.dashboard.data.repository

import androidx.paging.PagingData
import dev.ridill.oar.account.domain.model.AuthState
import dev.ridill.oar.account.domain.model.UserAccount
import dev.ridill.oar.account.domain.repository.AuthRepository
import dev.ridill.oar.aggregations.domain.repository.AggregationsRepository
import dev.ridill.oar.budgetCycles.domain.repository.BudgetCycleRepository
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.orZero
import dev.ridill.oar.dashboard.domain.repository.DashboardRepository
import dev.ridill.oar.schedules.data.local.SchedulesDao
import dev.ridill.oar.schedules.data.local.entity.ScheduleEntity
import dev.ridill.oar.schedules.data.toActiveSchedule
import dev.ridill.oar.schedules.domain.model.ActiveSchedule
import dev.ridill.oar.transactions.domain.model.TransactionEntry
import dev.ridill.oar.core.domain.model.FundMovement
import dev.ridill.oar.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlin.math.absoluteValue

class DashboardRepositoryImpl(
    private val cycleRepo: BudgetCycleRepository,
    private val authRepo: AuthRepository,
    private val aggRepo: AggregationsRepository,
    private val transactionRepo: TransactionRepository,
    private val schedulesDao: SchedulesDao
) : DashboardRepository {
    private fun activeCycle() = cycleRepo.getActiveCycleFlow()
    private fun activeCurrencyCode() = activeCycle()
        .mapLatest { it?.currency?.currencyCode }
        .distinctUntilChanged()

    override fun getSignedInUser(): Flow<UserAccount?> = authRepo.getAuthState()
        .mapLatest { state ->
            when (state) {
                is AuthState.Authenticated -> state.account
                AuthState.UnAuthenticated -> null
            }
        }.distinctUntilChanged()

    override fun getBudgetForActiveCycle(): Flow<Long> = activeCycle()
        .mapLatest { it?.budget.orZero() }
        .distinctUntilChanged()

    override fun getTotalDebitsForActiveCycle(): Flow<Double> =
        getAmountAggregateForActiveCycle(FundMovement.OUT)

    override fun getTotalCreditsForActiveCycle(): Flow<Double> =
        getAmountAggregateForActiveCycle(FundMovement.IN)

    private fun getAmountAggregateForActiveCycle(
        type: FundMovement
    ): Flow<Double> = activeCycle()
        .flatMapLatest { cycle ->
            aggRepo.getAmountAggregateForCycle(
                cycleId = cycle?.id ?: OarDatabase.INVALID_ID_LONG,
                type = type,
                addExcluded = false,
                currency = cycle?.currency,
            )
        }
        .combine(activeCurrencyCode()) { aggregate, activeCurrency ->
            aggregate.find { it.currency.currencyCode == activeCurrency }
        }
        .mapLatest { it?.amount.orZero() }
        .mapLatest { it.absoluteValue }
        .distinctUntilChanged()

    override fun getSchedulesActiveThisCycle(): Flow<List<ActiveSchedule>> = activeCycle()
        .flatMapLatest { cycle ->
            schedulesDao.getSchedulesActiveAtMonth(cycle?.endDate ?: DateUtil.dateNow())
        }
        .mapLatest { entities -> entities.map(ScheduleEntity::toActiveSchedule) }

    override fun getTransactionsThisCycle(): Flow<PagingData<TransactionEntry>> = activeCycle()
        .flatMapLatest { cycle ->
            transactionRepo.getAllTransactionsPaged(
                cycleIds = cycle?.id?.let { setOf(it) }.orEmpty(),
                type = FundMovement.OUT,
                showExcluded = false,
                tagIds = null,
                folderId = null,
                currency = cycle?.currency
            )
        }
}