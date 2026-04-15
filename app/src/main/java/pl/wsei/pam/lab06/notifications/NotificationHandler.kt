package pl.wsei.pam.lab06.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import pl.wsei.pam.lab01.R
import pl.wsei.pam.lab06.channelID
import pl.wsei.pam.lab06.notificationID

class NotificationHandler(private val context: Context) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun showSimpleNotification() {
        if (!canPostNotifications()) return

        val notification = NotificationCompat.Builder(context, channelID)
            .setContentTitle("Proste powiadomienie")
            .setContentText("Tekst powiadomienia")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationID, notification)
    }

    private fun canPostNotifications(): Boolean {
        return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }
}

