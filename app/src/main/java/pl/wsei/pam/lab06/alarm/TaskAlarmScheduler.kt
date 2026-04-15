package pl.wsei.pam.lab06.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.first
import pl.wsei.pam.lab06.NotificationBroadcastReceiver
import pl.wsei.pam.lab06.data.LocalDateConverter
import pl.wsei.pam.lab06.data.TodoTaskRepository
import pl.wsei.pam.lab06.deadlineExtra
import pl.wsei.pam.lab06.notificationID
import pl.wsei.pam.lab06.taskIdExtra
import pl.wsei.pam.lab06.titleExtra
import java.time.ZoneId

class TaskAlarmScheduler(
    private val context: Context,
    private val repository: TodoTaskRepository
) {
    suspend fun rescheduleNearestTaskAlarm() {
        val now = System.currentTimeMillis()
        val nearestTask = repository.getAllAsStream()
            .first()
            .filter { !it.isDone }
            .sortedBy { it.deadline }
            .firstOrNull()


        if (nearestTask == null) {
            cancelCurrentAlarm()
            return
        }

        val deadlineMillis = LocalDateConverter.toMillis(nearestTask.deadline)
        val deadlineAtStartOfDayMillis = nearestTask.deadline
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val firstReminder = deadlineAtStartOfDayMillis - ONE_DAY_MILLIS
        val triggerAt = when {
            firstReminder > now -> firstReminder
            now < deadlineAtStartOfDayMillis -> now + FOUR_HOURS_MILLIS
            else -> {
                cancelCurrentAlarm()
                return
            }
        }

        scheduleAlarm(
            taskId = nearestTask.id,
            title = nearestTask.title,
            deadlineMillis = deadlineMillis,
            triggerAtMillis = triggerAt
        )
    }

    fun cancelCurrentAlarm() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationID,
            Intent(context, NotificationBroadcastReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleAlarm(
        taskId: Int,
        title: String,
        deadlineMillis: Long,
        triggerAtMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            putExtra(taskIdExtra, taskId)
            putExtra(titleExtra, title)
            putExtra(deadlineExtra, deadlineMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    companion object {
        private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
        private const val FOUR_HOURS_MILLIS = 4 * 60 * 60 * 1000L
    }
}

