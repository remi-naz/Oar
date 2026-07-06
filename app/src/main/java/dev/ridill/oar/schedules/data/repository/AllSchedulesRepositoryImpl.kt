package dev.ridill.oar.schedules.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import dev.ridill.oar.R
import dev.ridill.oar.core.data.preferences.animPreferences.AnimPreferencesManager
import dev.ridill.oar.core.domain.model.Result
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.core.domain.util.isSameMonthAs
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.schedules.data.local.SchedulesDao
import dev.ridill.oar.schedules.domain.model.ScheduleListItemUiModel
import dev.ridill.oar.schedules.domain.repository.AllSchedulesRepository
import dev.ridill.oar.schedules.domain.repository.ScheduleError
import dev.ridill.oar.schedules.domain.repository.SchedulesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class AllSchedulesRepositoryImpl(
    private val dao: SchedulesDao,
    private val repo: SchedulesRepository,
    private val animPreferencesManager: AnimPreferencesManager,
) : AllSchedulesRepository {
    private val _currentDate = MutableStateFlow(DateUtil.dateNow())
    private val currentDate = _currentDate.asStateFlow()

    override fun refreshCurrentDate() {
        _currentDate.update { DateUtil.dateNow() }
    }

    override fun getSchedulesPagingData(): Flow<PagingData<ScheduleListItemUiModel>> = currentDate
        .flatMapLatest { dateNow ->
            Pager(
                config = PagingConfig(UtilConstants.DEFAULT_PAGE_SIZE),
                pagingSourceFactory = { dao.getSchedulesPaged(dateNow) }
            ).flow.mapLatest { pagingData ->
                val currentMonthStartDateTime = dateNow
                    .withDayOfMonth(1)
                    .atStartOfDay()
                val nextMonthStartDateTime = dateNow
                    .withDayOfMonth(1)
                    .plusMonths(1)
                    .atStartOfDay()

                pagingData.map { entity ->
                    ScheduleListItemUiModel.ScheduleItem(
                        scheduleItem = entity,
                        canMarkPaid = entity.nextPaymentTimestamp?.isSameMonthAs(dateNow) == true
                                || entity.nextPaymentTimestamp?.isBefore(currentMonthStartDateTime) == true
                    )
                }
                    .insertSeparators<ScheduleListItemUiModel.ScheduleItem, ScheduleListItemUiModel> { before, after ->
                        when {
                            before?.nextPaymentTimestamp
                                ?.isSameMonthAs(currentMonthStartDateTime) != true
                                    && after?.nextPaymentTimestamp
                                ?.isSameMonthAs(currentMonthStartDateTime) == true
                                -> ScheduleListItemUiModel.TypeSeparator(UiText.StringResource(R.string.this_month))

                            before?.nextPaymentTimestamp
                                ?.isBefore(currentMonthStartDateTime) != true
                                    && after?.nextPaymentTimestamp
                                ?.isBefore(currentMonthStartDateTime) == true
                                -> ScheduleListItemUiModel.TypeSeparator(UiText.StringResource(R.string.past_due))

                            before?.nextPaymentTimestamp
                                ?.isAfter(nextMonthStartDateTime) != true
                                    && after?.nextPaymentTimestamp
                                ?.isAfter(nextMonthStartDateTime) == true
                                -> ScheduleListItemUiModel.TypeSeparator(UiText.StringResource(R.string.upcoming))

                            (before == null
                                    || before.nextPaymentTimestamp != null)
                                    && after != null
                                    && after.nextPaymentTimestamp == null ->
                                ScheduleListItemUiModel.TypeSeparator(UiText.StringResource(R.string.retired))

                            else -> null
                        }
                    }
            }
        }

    override suspend fun markScheduleAsPaid(
        id: Long
    ): Result<Unit, ScheduleError> = withContext(Dispatchers.IO) {
        try {
            val schedule = repo.getScheduleById(id)
                ?: throw ScheduleNotFoundThrowable()
            repo.addPaymentToSchedule(schedule)
            Result.Success(Unit)
        } catch (_: ScheduleNotFoundThrowable) {
            Result.Error(
                ScheduleError.SCHEDULE_NOT_FOUND,
                UiText.StringResource(R.string.error_schedule_not_found)
            )
        } catch (t: Throwable) {
            t.rethrowIfCoroutineCancellation()
            Result.Error(
                ScheduleError.UNKNOWN,
                t.localizedMessage?.let {
                    UiText.DynamicString(it)
                } ?: UiText.StringResource(R.string.error_unknown)
            )
        }
    }

    override suspend fun deleteSchedulesById(ids: Set<Long>) = withContext(Dispatchers.IO) {
        repo.deleteSchedulesByIds(ids)
    }

    override fun shouldShowActionPreview(): Flow<Boolean> = animPreferencesManager.preferences
        .mapLatest { it.showScheduleItemActionPreview }
        .distinctUntilChanged()

    override suspend fun disableActionPreview() =
        animPreferencesManager.disableScheduleItemActionPreview()
}

class ScheduleNotFoundThrowable : Throwable("Schedule not found")