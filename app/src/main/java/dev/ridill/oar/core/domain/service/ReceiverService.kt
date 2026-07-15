package dev.ridill.oar.core.domain.service

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import dev.ridill.oar.schedules.domain.scheduleReminder.MarkScheduleAsPaidActionReceiver
import dev.ridill.oar.application.BootReceiver
import dev.ridill.oar.application.TimeOrTimezoneChangeReceiver
import dev.ridill.oar.settings.domain.notification.LockAppImmediateReceiver
import dev.ridill.oar.transactions.domain.autoDetection.TransactionSmsReceiver
import dev.ridill.oar.transactions.domain.notification.DeleteTransactionActionReceiver
import dev.ridill.oar.transactions.domain.notification.MarkTransactionExcludedActionReceiver

class ReceiverService(
    private val context: Context
) {
    fun toggleSmsReceiver(enable: Boolean) =
        toggleReceiver(TransactionSmsReceiver::class.java, enable)

    fun toggleNotificationActionReceivers(enable: Boolean) {
        toggleReceiver(DeleteTransactionActionReceiver::class.java, enable)
        toggleReceiver(MarkTransactionExcludedActionReceiver::class.java, enable)
        toggleReceiver(LockAppImmediateReceiver::class.java, enable)
        toggleReceiver(MarkScheduleAsPaidActionReceiver::class.java, enable)
    }

    fun toggleBootAndTimeReceivers(enable: Boolean) {
        toggleReceiver(BootReceiver::class.java, enable)
        toggleReceiver(TimeOrTimezoneChangeReceiver::class.java, enable)
    }

    private fun toggleReceiver(receiverClass: Class<*>, enable: Boolean) {
        val componentName = ComponentName(context, receiverClass)
        val newState = if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        else PackageManager.COMPONENT_ENABLED_STATE_DISABLED

        context.packageManager.setComponentEnabledSetting(
            componentName,
            newState,
            PackageManager.DONT_KILL_APP
        )
    }
}