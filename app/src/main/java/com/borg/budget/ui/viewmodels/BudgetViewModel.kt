package com.borg.budget.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.borg.budget.data.*
import com.borg.budget.notifications.NotificationHelper
import com.borg.budget.notifications.NotificationWorker
import com.borg.budget.ui.models.ExpenseCategory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = BudgetDatabase.getDatabase(application).budgetDao()
    private val notificationHelper = NotificationHelper(application)

    val budgetConfig: StateFlow<BudgetConfigEntity> = dao.getBudgetConfig()
        .map { it ?: BudgetConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetConfigEntity())

    val transactions: StateFlow<List<TransactionEntity>> = dao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscriptions: StateFlow<List<SubscriptionEntity>> = dao.getActiveSubscriptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val holidays: StateFlow<List<HolidayEntity>> = dao.getAllHolidays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { injectSubscriptionsForCurrentMonth() }
        scheduleSubscriptionNotifications(application)
    }

    fun updateIncome(income: Double) {
        viewModelScope.launch {
            dao.updateBudgetConfig(budgetConfig.value.copy(totalIncome = income))
        }
    }

    fun updateNotifyDays(days: Int) {
        viewModelScope.launch {
            dao.updateBudgetConfig(budgetConfig.value.copy(subscriptionNotifyDays = days))
        }
    }

    fun addTransaction(title: String, amount: Double, category: ExpenseCategory, isSub: Boolean = false, isDebt: Boolean = false) {
        viewModelScope.launch {
            dao.insertTransaction(TransactionEntity(title = title, amount = amount, category = category.name, isSubscription = isSub, isDebt = isDebt))
            val totalIncome = budgetConfig.value.totalIncome
            val remaining = totalIncome - (transactions.value.sumOf { it.amount } + amount)
            if (remaining < totalIncome * 0.10 && remaining > 0) {
                notificationHelper.sendBudgetAlert(remaining)
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { dao.deleteTransaction(transaction) }
    }

    fun addSubscription(name: String, amount: Double, dayOfPayment: Int, frequency: String, category: ExpenseCategory, billingMonth: Int = 0) {
        viewModelScope.launch {
            val sub = SubscriptionEntity(
                name = name,
                basePrice = amount,
                dayOfPayment = dayOfPayment,
                category = category.name,
                frequency = frequency,
                billingMonth = billingMonth
            )
            dao.insertSubscription(sub)
            injectSubscriptionForCurrentMonth(sub)
        }
    }

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch { dao.deleteSubscription(subscription) }
    }

    fun addHoliday(title: String, startDate: Long, endDate: Long, daysCount: Int) {
        viewModelScope.launch {
            dao.insertHoliday(HolidayEntity(title = title, startDate = startDate, endDate = endDate, daysCount = daysCount))
        }
    }

    fun deleteHoliday(holiday: HolidayEntity) {
        viewModelScope.launch { dao.deleteHoliday(holiday) }
    }

    private suspend fun injectSubscriptionsForCurrentMonth() {
        dao.getActiveSubscriptionsList().forEach { injectSubscriptionForCurrentMonth(it) }
    }

    private suspend fun injectSubscriptionForCurrentMonth(sub: SubscriptionEntity) {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH)

        if (sub.frequency == "ANNUAL" && sub.billingMonth != 0 && sub.billingMonth != month + 1) return

        val startOfMonth = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfMonth = Calendar.getInstance().apply {
            set(year, month, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        if (dao.getSubscriptionTransactionsInRange(sub.id, startOfMonth, endOfMonth).isNotEmpty()) return

        val payDay = sub.dayOfPayment.coerceIn(1, now.getActualMaximum(Calendar.DAY_OF_MONTH))
        val payTimestamp = Calendar.getInstance().apply {
            set(year, month, payDay, 8, 0, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        dao.insertTransaction(
            TransactionEntity(
                title = sub.name,
                amount = sub.basePrice,
                category = sub.category,
                timestamp = payTimestamp,
                isSubscription = true,
                subscriptionId = sub.id
            )
        )
    }

    private fun scheduleSubscriptionNotifications(application: Application) {
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntilNineAm(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(application).enqueueUniquePeriodicWork(
            "subscription_notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun delayUntilNineAm(): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }
}
