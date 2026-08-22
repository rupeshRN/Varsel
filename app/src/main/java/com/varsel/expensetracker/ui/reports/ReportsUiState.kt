package com.varsel.expensetracker.ui.reports

import java.time.YearMonth

/**
 * Complete UI state for the Reports feature.
 *
 * Rendering belongs in ReportsScreen and its smaller UI components.
 * Business calculations will be handled by ReportsViewModel.
 */
data class ReportsUiState(

    val isLoading: Boolean = true,

    val errorMessage: String? = null,

    /**
     * Month currently displayed by the report.
     */
    val selectedMonth: YearMonth = YearMonth.now(),

    /**
     * Currently selected top-level money-flow view.
     */
    val selectedFlow: ReportsFlow = ReportsFlow.EXPENSES,

    /**
     * Selected expense category.
     *
     * null means Overall.
     */
    val selectedExpenseCategory: String? = null,

    /**
     * Selected income category.
     *
     * null means Overall.
     */
    val selectedIncomeCategory: String? = null,

    /**
     * Monthly cash-flow summary.
     */
    val cashFlow: ReportsCashFlow = ReportsCashFlow(),

    /**
     * Expense categories for the selected month.
     */
    val expenseCategories: List<ReportsExpenseCategory> = emptyList(),

    /**
     * Income categories for the selected month.
     */
    val incomeCategories: List<ReportsIncomeCategory> = emptyList(),

    /**
     * Financial Events belonging to the selected month.
     */
    val financialEvents: List<ReportsFinancialEvent> = emptyList()
) {

    val selectedExpenseCategoryModel: ReportsExpenseCategory?
        get() = selectedExpenseCategory?.let { category ->
            expenseCategories.firstOrNull {
                it.category == category
            }
        }

    val selectedIncomeCategoryModel: ReportsIncomeCategory?
        get() = selectedIncomeCategory?.let { category ->
            incomeCategories.firstOrNull {
                it.category == category
            }
        }
}

/**
 * Top-level money-flow section.
 */
enum class ReportsFlow {
    EXPENSES,
    INCOME
}

/**
 * Monthly cash-flow totals.
 *
 * These values will be calculated by ReportsViewModel.
 * ReportsScreen only displays them.
 */
data class ReportsCashFlow(

    /**
     * Actual income after excluding reimbursements
     * and account transfers.
     */
    val actualIncome: Double = 0.0,

    /**
     * Effective expense after excluding account transfers
     * and subtracting reimbursements.
     */
    val effectiveExpense: Double = 0.0,

    /**
     * actualIncome - effectiveExpense.
     */
    val netCashFlow: Double = 0.0
)

/**
 * Expense category summary.
 */
data class ReportsExpenseCategory(

    /**
     * Category name stored on the transaction.
     */
    val category: String,

    /**
     * Total expense amount represented by this category.
     */
    val totalAmount: Double = 0.0,

    /**
     * Amount from ordinary NORMAL expense transactions.
     */
    val normalAmount: Double = 0.0,

    /**
     * Gross expense amount belonging to Financial Events.
     */
    val financialEventAmount: Double = 0.0,

    /**
     * Reimbursement amount associated with this category.
     */
    val reimbursedAmount: Double = 0.0,

    /**
     * Final Financial Event cost for this category.
     */
    val effectiveFinancialEventAmount: Double = 0.0
)

/**
 * Income category summary.
 */
data class ReportsIncomeCategory(

    /**
     * Category name stored on the transaction.
     */
    val category: String,

    /**
     * Actual reportable income for this category.
     *
     * Reimbursements and transfers are excluded
     * by ReportsViewModel.
     */
    val totalAmount: Double = 0.0
)

/**
 * Financial Event summary used by Reports.
 */
data class ReportsFinancialEvent(

    /**
     * Stable Financial Event link ID.
     */
    val transactionLinkId: String,

    /**
     * User-facing Financial Event name.
     */
    val groupName: String,

    /**
     * Financial Event category.
     */
    val category: String,

    /**
     * Gross expense amount linked to the event.
     */
    val expenseAmount: Double = 0.0,

    /**
     * Reimbursement amount linked to the event.
     */
    val reimbursedAmount: Double = 0.0,

    /**
     * expenseAmount - reimbursedAmount.
     */
    val effectiveCost: Double = 0.0
)
