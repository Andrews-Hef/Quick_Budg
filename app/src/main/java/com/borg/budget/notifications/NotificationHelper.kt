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
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications pour budget et abonnements"
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    fun sendBudgetAlert(remainingBudget: Double) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_home)
            .setContentTitle("Alerte Budget")
            .setContentText("Il ne vous reste que %.2f € pour finir le mois.".format(remainingBudget))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(1, notification)
        } catch (_: SecurityException) {}
    }

    fun sendSubscriptionReminder(subName: String, amount: Double, daysUntil: Int) {
        val text = when (daysUntil) {
            0 -> "$subName (%.2f €) est prélevé aujourd'hui.".format(amount)
            1 -> "$subName sera prélevé demain (%.2f €).".format(amount)
            else -> "$subName sera débité dans $daysUntil jours (%.2f €).".format(amount)
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_home)
            .setContentTitle("Prélèvement imminent")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (_: SecurityException) {}
    }
}
