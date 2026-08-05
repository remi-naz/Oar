package dev.ridill.oar.dashboard.domain.repository

import androidx.paging.PagingData
import dev.ridill.oar.account.domain.model.UserAccount
import dev.ridill.oar.schedules.domain.model.ActiveSchedule
import dev.ridill.oar.transactions.domain.model.TransactionEntryUiModel
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getSignedInUser(): Flow<UserAccount?>
    fun getBudgetForActiveCycle(): Flow<Long>
    fun getTotalDebitsForActiveCycle(): Flow<Double>
    fun getTotalCreditsForActiveCycle(): Flow<Double>
    fun getSchedulesActiveThisCycle(): Flow<List<ActiveSchedule>>
    fun getTransactionsThisCycle(): Flow<PagingData<TransactionEntryUiModel>>
}