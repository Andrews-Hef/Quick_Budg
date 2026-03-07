package com.borg.budget.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.borg.budget.data.*
import com.borg.budget.ui.models.ExpenseCategory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = BudgetDatabase.getDatabase(application).budgetDao()

    val budgetConfig: StateFlow<BudgetConfigEntity> = dao.getBudgetConfig()
        .map { it ?: BudgetConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetConfigEntity())

    val transactions: StateFlow<List<TransactionEntity>> = dao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscriptions: StateFlow<List<SubscriptionEntity>> = dao.getActiveSubscriptions()
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
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            dao.deleteTransaction(transaction)
        }
    }

    fun addSubscription(name: String, price: Double, day: Int, category: ExpenseCategory) {
        viewModelScope.launch {
            dao.insertSubscription(
                SubscriptionEntity(
                    name = name,
                    basePrice = price,
                    dayOfPayment = day,
                    category = category.name
                )
            )
        }
    }
}
