package pl.wsei.pam.lab06

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import pl.wsei.pam.lab01.R
import pl.wsei.pam.lab06.data.LocalDateConverter
import java.time.ZoneId

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (!canPostNotifications(context)) return

        val taskId = intent?.getIntExtra(taskIdExtra, 0) ?: 0
        val taskTitle = intent?.getStringExtra(titleExtra) ?: "Deadline"
        val taskMessage = intent?.getStringExtra(messageExtra)
            ?: "Zbliza sie termin zakonczenia zadania"
        val taskDeadlineMillis = intent?.getLongExtra(deadlineExtra, 0L) ?: 0L
        val taskDeadline = LocalDateConverter.fromMillis(taskDeadlineMillis)

        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(taskTitle)
            .setContentText("$taskMessage: $taskDeadline")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)

        // Harmonogram kolejnego przypomnienia co 4h aż do deadline.
        val nextTime = System.currentTimeMillis() + FOUR_HOURS_MILLIS
        val deadlineEndExclusive = taskDeadline
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (nextTime < deadlineEndExclusive) {
            scheduleNext(context, taskId, taskTitle, taskMessage, taskDeadlineMillis, nextTime)
        }


    }

    private fun scheduleNext(
        context: Context,
        taskId: Int,
        taskTitle: String,
        taskMessage: String,
        taskDeadlineMillis: Long,
        triggerAtMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            putExtra(taskIdExtra, taskId)
            putExtra(titleExtra, taskTitle)
            putExtra(messageExtra, taskMessage)
            putExtra(deadlineExtra, taskDeadlineMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationID,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    private fun canPostNotifications(context: Context): Boolean {
        return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val FOUR_HOURS_MILLIS = 4 * 60 * 60 * 1000L
    }
}
