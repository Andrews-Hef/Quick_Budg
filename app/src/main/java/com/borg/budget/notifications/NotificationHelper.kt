package com.borg.budget.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.borg.budget.R

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "budget_notifications"
        const val CHANNEL_NAME = "Budget Alerts"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications for budget and subscriptions"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendBudgetAlert(remainingBudget: Double) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_home) // Using existing icon
            .setContentTitle("Alerte Budget")
            .setContentText("Attention, il ne vous reste que %.2f € pour finir le mois.".format(remainingBudget))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // Check for permission in MainActivity before calling this
            try {
                notify(1, builder.build())
            } catch (e: SecurityException) {
                // Handle missing permission
            }
        }
    }

    fun sendSubscriptionReminder(subName: String, amount: Double) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_home)
            .setContentTitle("Prélèvement imminent")
            .setContentText("Votre abonnement $subName (%.2f €) sera prélevé bientôt.".format(amount))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(System.currentTimeMillis().toInt(), builder.build())
            } catch (e: SecurityException) { }
        }
    }
}
