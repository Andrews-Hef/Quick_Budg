package com.borg.budget.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.borg.budget.data.BudgetDatabase
import java.util.Calendar

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val dao = BudgetDatabase.getDatabase(context).budgetDao()
        val config = dao.getBudgetConfigOnce() ?: return Result.success()
        val notifyDays = config.subscriptionNotifyDays
        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val helper = NotificationHelper(context)

        dao.getActiveSubscriptionsList().forEach { sub ->
            val daysUntil = sub.dayOfPayment - today
            if (daysUntil in 0..notifyDays) {
                helper.sendSubscriptionReminder(sub.name, sub.basePrice, daysUntil)
            }
        }

        return Result.success()
    }
}
