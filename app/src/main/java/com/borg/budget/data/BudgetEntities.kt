package com.borg.budget.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.borg.budget.ui.ExpenseCategory
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: String, // Store enum name
    val timestamp: Long = System.currentTimeMillis(),
    val isSubscription: Boolean = false,
    val isDebt: Boolean = false,
    val subscriptionId: String? = null // Link to a subscription template if applicable
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val basePrice: Double,
    val dayOfPayment: Int, // 1 to 31
    val category: String,
    val isActive: Boolean = true
)

@Entity(tableName = "budget_config")
data class BudgetConfigEntity(
    @PrimaryKey val id: Int = 0, // Single config row
    val totalIncome: Double = 0.0,
    val obligationPercent: Float = 0.50f,
    val pleasurePercent: Float = 0.30f,
    val savingsPercent: Float = 0.20f
)
