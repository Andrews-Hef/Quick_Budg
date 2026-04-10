package com.borg.budget.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.borg.budget.data.*
import com.borg.budget.notifications.NotificationHelper
import com.borg.budget.ui.models.ExpenseCategory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    fun updateIncome(income: Double) {
        viewModelScope.launch {
            val current = budgetConfig.value
            dao.updateBudgetConfig(current.copy(totalIncome = income))
        }
    }

    fun addTransaction(title: String, amount: Double, category: ExpenseCategory, isSub: Boolean = false, isDebt: Boolean = false) {
        viewModelScope.launch {
            dao.insertTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    category = category.name,
                    isSubscription = isSub,
                    isDebt = isDebt
                )
            )
            
            // Notification logic for low budget
            val totalIncome = budgetConfig.value.totalIncome
            val currentTotalSpent = transactions.value.sumOf { it.amount } + amount
            val remaining = totalIncome - currentTotalSpent
            
            if (remaining < (totalIncome * 0.10) && remaining > 0) {
                notificationHelper.sendBudgetAlert(remaining)
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            dao.deleteTransaction(transaction)
        }
    }

    fun addHoliday(title: String, startDate: Long, endDate: Long, daysCount: Int) {
        viewModelScope.launch {
            dao.insertHoliday(
                HolidayEntity(
                    title = title,
                    startDate = startDate,
                    endDate = endDate,
                    daysCount = daysCount
                )
            )
        }
    }

    fun deleteHoliday(holiday: HolidayEntity) {
        viewModelScope.launch {
            dao.deleteHoliday(holiday)
        }
    }
}
