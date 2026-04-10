package com.borg.budget.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    // Subscriptions
    @Query("SELECT * FROM subscriptions WHERE isActive = 1")
    fun getActiveSubscriptions(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity)

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    // Budget Config
    @Query("SELECT * FROM budget_config WHERE id = 0")
    fun getBudgetConfig(): Flow<BudgetConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBudgetConfig(config: BudgetConfigEntity)

    // Holidays
    @Query("SELECT * FROM holidays ORDER BY startDate DESC")
    fun getAllHolidays(): Flow<List<HolidayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: HolidayEntity)

    @Delete
    suspend fun deleteHoliday(holiday: HolidayEntity)
}
