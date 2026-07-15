package dev.ridill.oar.schedules.domain.scheduleReminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dev.ridill.oar.core.domain.service.ReceiverService
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.core.domain.util.UtilConstants
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.schedules.domain.model.Schedule

class AlarmManagerScheduleReminder(
    private val context: Context,
    private val receiverService: ReceiverService,
) : ScheduleReminder {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun setReminder(schedule: Schedule) {
        val timeMillis = schedule.nextPaymentTimestamp
            ?.let { DateUtil.toMillis(it) } ?: return
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC,
            timeMillis,
            buildPendingIntent(schedule.id)
        )
        receiverService.toggleBootAndTimeReceivers(true)

        logI(ScheduleReminder::class.simpleName) { "Set reminder for schedule ID ${schedule.id} on ${schedule.nextPaymentTimestamp}" }
    }

    override fun cancel(id: Long) {
        alarmManager.cancel(buildPendingIntent(id))

        logI(ScheduleReminder::class.simpleName) { "Schedule ID $id reminder cancelled" }
    }

    private fun buildPendingIntent(id: Long): PendingIntent {
        val intent = Intent(context, ScheduledPaymentReminderReceiver::class.java).apply {
            action = ScheduleReminder.ACTION
            putExtra(ScheduleReminder.EXTRA_SCHEDULE_ID, id)
        }
        return PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            UtilConstants.pendingIntentFlags
        )
    }
}