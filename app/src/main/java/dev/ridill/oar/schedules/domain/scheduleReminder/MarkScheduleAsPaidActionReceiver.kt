package dev.ridill.oar.schedules.domain.scheduleReminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.di.ApplicationScope
import dev.ridill.oar.schedules.domain.model.Schedule
import dev.ridill.oar.schedules.domain.notification.ScheduleReminderNotificationHelper
import dev.ridill.oar.schedules.domain.repository.SchedulesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MarkScheduleAsPaidActionReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var repo: SchedulesRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper<Schedule>

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != ScheduleReminderNotificationHelper.ACTION_MARK_SCHEDULED_AS_PAID)
            return

        applicationScope.launch {
            val scheduleId = intent.getLongExtra(ScheduleReminder.EXTRA_SCHEDULE_ID, -1L)
                .takeIf { it > -1L }
                ?: return@launch
            val schedule = repo.getScheduleById(scheduleId)
                ?: return@launch
            repo.addPaymentToSchedule(schedule)

            notificationHelper.updateNotification(
                id = scheduleId.hashCode(),
                notification = notificationHelper
                    .buildBaseNotification()
                    .setContentTitle(context?.getString(R.string.schedule_marked_as_paid))
                    .setTimeoutAfter(NotificationHelper.Utils.TIMEOUT_MILLIS)
                    .build()
            )
        }
    }
}