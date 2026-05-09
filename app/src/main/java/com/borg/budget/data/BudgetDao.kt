package com.borg.budget.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE subscriptionId = :subId AND timestamp >= :start AND timestamp < :end")
    suspend fun getSubscriptionTransactionsInRange(subId: String, start: Long, end: Long): List<TransactionEntity>

    @Query("SELECT * FROM subscriptions WHERE isActive = 1")
    fun getActiveSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE isActive = 1")
    suspend fun getActiveSubscriptionsList(): List<SubscriptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity)

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

    @Query("SELECT * FROM budget_config WHERE id = 0")
    fun getBudgetConfig(): Flow<BudgetConfigEntity?>

    @Query("SELECT * FROM budget_config WHERE id = 0")
    suspend fun getBudgetConfigOnce(): BudgetConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBudgetConfig(config: BudgetConfigEntity)

    @Query("SELECT * FROM holidays ORDER BY startDate DESC")
    fun getAllHolidays(): Flow<List<HolidayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: HolidayEntity)

    @Delete
    suspend fun deleteHoliday(holiday: HolidayEntity)
}
