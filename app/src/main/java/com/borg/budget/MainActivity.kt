package com.borg.budget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.borg.budget.ui.viewmodels.BudgetViewModel
import com.borg.budget.ui.screens.BudgetScreen
import com.borg.budget.ui.screens.ExpenseScreen
import com.borg.budget.ui.theme.Quick_BudgTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Quick_BudgTheme {
                val budgetViewModel: BudgetViewModel = viewModel()
                Quick_BudgApp(budgetViewModel)
            }
        }
    }
}

@Composable
fun Quick_BudgApp(viewModel: BudgetViewModel) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.BUDGET) }
    
    val budgetConfig by viewModel.budgetConfig.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.BUDGET -> {
                    BudgetScreen(
                        totalIncome = budgetConfig.totalIncome,
                        onIncomeChange = { viewModel.updateIncome(it) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                AppDestinations.EXPENSES -> {
                    ExpenseScreen(
                        totalIncome = budgetConfig.totalIncome,
                        transactions = transactions,
                        onAddTransaction = { title, amt, cat, isSub, isDebt ->
                            viewModel.addTransaction(title, amt, cat, isSub, isDebt)
                        },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                else -> {
                    Text(
                        text = "Page en cours de développement",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    BUDGET("Budget", R.drawable.ic_home),
    EXPENSES("Dépenses", R.drawable.ic_favorite),
    PROFILE("Profil", R.drawable.ic_account_box),
}
