package com.wakepoint.app.core.notification

object AlarmAlertContract {
    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_ALARM_LABEL = "extra_alarm_label"
    const val EXTRA_TARGET_ADDRESS = "extra_target_address"

    fun notificationId(alarmId: String): Int {
        val id = alarmId.hashCode() and Int.MAX_VALUE
        return if (id == 0) DEFAULT_NOTIFICATION_ID else id
    }

    private const val DEFAULT_NOTIFICATION_ID = 2002
}
