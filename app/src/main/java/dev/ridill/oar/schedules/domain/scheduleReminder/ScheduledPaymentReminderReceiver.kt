package dev.ridill.oar.schedules.domain.scheduleReminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.di.ApplicationScope
import dev.ridill.oar.schedules.domain.model.Schedule
import dev.ridill.oar.schedules.domain.repository.SchedulesRepository
import dev.ridill.oar.settings.domain.repositoty.CurrencyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScheduledPaymentReminderReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationContext: CoroutineScope

    @Inject
    lateinit var currencyPrefRepo: CurrencyRepository

    @Inject
    lateinit var repo: SchedulesRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper<Schedule>

    @Inject
    lateinit var scheduleReminder: ScheduleReminder

    override fun onReceive(context: Context?, intent: Intent?) {
        logI(ScheduledPaymentReminderReceiver::class.simpleName) { "onReceive() called" }
        if (intent?.action != ScheduleReminder.ACTION) return
        val id = intent.getLongExtra(ScheduleReminder.EXTRA_SCHEDULE_ID, -1L)
            .takeIf { it > -1L }
            ?: return
        notifyAndSetNextNextReminder(id)
    }

    private fun notifyAndSetNextNextReminder(id: Long) = applicationContext.launch {
        logI(ScheduledPaymentReminderReceiver::class.simpleName) { "notifyAndSetNextNextReminder() called with: id = $id" }
        val schedule = repo.getScheduleById(id)
            ?: return@launch
        notificationHelper.postNotification(
            id = schedule.id.hashCode(),
            data = schedule
        )

        val newReminderDate = repo.calculateNextPaymentTimestampFromDate(
            anchor = DateUtil.now(),
            repetition = schedule.repetition,
        )
        scheduleReminder.setReminder(schedule.copy(nextPaymentTimestamp = newReminderDate))
    }
}