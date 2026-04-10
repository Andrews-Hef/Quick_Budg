package com.borg.budget.ui.models

enum class ExpenseCategory(val label: String, val percentage: Double) {
    OBLIGATION("Obligations", 0.50),
    PLEASURE("Plaisirs", 0.30),
    SAVING_DEBT("Épargne/Dette", 0.20)
}
