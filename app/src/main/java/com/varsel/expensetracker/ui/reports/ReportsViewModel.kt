package com.varsel.expensetracker.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionLinkGroup
import com.varsel.expensetracker.domain.model.TransactionRole
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.domain.repository.TransactionLinkGroupRepository
import com.varsel.expensetracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel for the production Reports feature.
 *
 * Responsibilities:
 * - Observe the existing Varsel transaction data.
 * - Observe existing Financial Event groups.
 * - Filter data by the selected month.
 * - Calculate report totals.
 * - Build category summaries.
 * - Build Financial Event summaries.
 * - Manage month and section selection.
 *
 * The ViewModel does NOT contain Compose/UI code.
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val transactionLinkGroupRepository: TransactionLinkGroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReportsUiState(
            isLoading = true,
            selectedMonth = YearMonth.now()
        )
    )

    val uiState: StateFlow<ReportsUiState> =
        _uiState.asStateFlow()

    /**
     * Device timezone is used consistently with the rest of Varsel
     * when converting transaction timestamps into calendar dates.
     */
    private val zoneId: ZoneId = ZoneId.systemDefault()

    /**
     * Latest source data.
     *
     * Keeping the latest repository result in memory means month navigation
     * does not require another database query.
     */
    private var latestTransactions: List<Transaction> = emptyList()

    private var latestGroups: List<TransactionLinkGroup> = emptyList()

    init {
        observeReportData()
    }

    // ------------------------------------------------------------------------
    // User actions
    // ------------------------------------------------------------------------

    /**
     * Move the report one month backwards.
     */
    fun previousMonth() {
        updateSelectedMonth(
            _uiState.value.selectedMonth.minusMonths(1)
        )
    }

    /**
     * Move the report one month forwards.
     */
    fun nextMonth() {
        updateSelectedMonth(
            _uiState.value.selectedMonth.plusMonths(1)
        )
    }

    /**
     * Move directly to a specific month.
     */
    fun selectMonth(month: YearMonth) {
        updateSelectedMonth(month)
    }

    /**
     * Select Expenses or Income.
     */
    fun selectFlow(flow: ReportsFlow) {
        _uiState.value = _uiState.value.copy(
            selectedFlow = flow,
            selectedExpenseCategory = null,
            selectedIncomeCategory = null
        )
    }

    /**
     * Select an expense category.
     *
     * null means Overall.
     */
    fun selectExpenseCategory(category: String?) {
        _uiState.value = _uiState.value.copy(
            selectedExpenseCategory = category
        )
    }

    /**
     * Select an income category.
     *
     * null means Overall.
     */
    fun selectIncomeCategory(category: String?) {
        _uiState.value = _uiState.value.copy(
            selectedIncomeCategory = category
        )
    }

    /**
     * Clear the current category selection.
     */
    fun clearCategorySelection() {
        _uiState.value = _uiState.value.copy(
            selectedExpenseCategory = null,
            selectedIncomeCategory = null
        )
    }

    /**
     * Retry report generation after an error.
     *
     * The repository flows are continuously observed, so rebuilding from
     * the latest cached source data is enough here.
     */
    fun retry() {
        rebuildReport()
    }

    // ------------------------------------------------------------------------
    // Repository observation
    // ------------------------------------------------------------------------

    /**
     * Observe the real Varsel transaction and Financial Event streams.
     */
    private fun observeReportData() {

        viewModelScope.launch(Dispatchers.IO) {

            combine(
                transactionRepository.getAllTransactions(),
                transactionLinkGroupRepository.getAllGroups()
            ) { transactions, groups ->
                ReportSourceData(
                    transactions = transactions,
                    groups = groups
                )
            }.collect { sourceData ->

                latestTransactions = sourceData.transactions
                latestGroups = sourceData.groups

                rebuildReport()
            }
        }
    }

    /**
     * Rebuild the current report using the latest repository data.
     */
    private fun rebuildReport() {

        try {

            val currentState = _uiState.value

            val newState = buildUiState(
                transactions = latestTransactions,
                groups = latestGroups,
                selectedMonth = currentState.selectedMonth,
                previousState = currentState
            )

            _uiState.value = newState

        } catch (exception: Exception) {

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = exception.message
                    ?: "Unable to prepare report"
            )
        }
    }

    /**
     * Change the selected month and immediately rebuild from cached data.
     */
    private fun updateSelectedMonth(month: YearMonth) {

        _uiState.value = _uiState.value.copy(
            selectedMonth = month,
            selectedExpenseCategory = null,
            selectedIncomeCategory = null,
            errorMessage = null,
            isLoading = true
        )

        rebuildReport()
    }

    // ------------------------------------------------------------------------
    // State construction
    // ------------------------------------------------------------------------

    private fun buildUiState(
        transactions: List<Transaction>,
        groups: List<TransactionLinkGroup>,
        selectedMonth: YearMonth,
        previousState: ReportsUiState
    ): ReportsUiState {

        val monthTransactions = transactions.filter { transaction ->
            transaction.belongsToMonth(selectedMonth)
        }

        val cashFlow = buildCashFlow(
            transactions = monthTransactions
        )

        val expenseCategories = buildExpenseCategories(
            transactions = monthTransactions
        )

        val incomeCategories = buildIncomeCategories(
            transactions = monthTransactions
        )

        val financialEvents = buildFinancialEvents(
            transactions = monthTransactions,
            groups = groups
        )

        return previousState.copy(
            isLoading = false,
            errorMessage = null,
            selectedMonth = selectedMonth,
            cashFlow = cashFlow,
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories,
            financialEvents = financialEvents
        )
    }

    // ------------------------------------------------------------------------
    // Cash-flow calculations
    // ------------------------------------------------------------------------

    /**
     * Varsel reporting rules:
     *
     * Actual income:
     * - INCOME
     * - excludes REIMBURSEMENT
     * - excludes TRANSFER_IN
     *
     * Effective expense:
     * - EXPENSE
     * - excludes TRANSFER_OUT
     * - subtracts REIMBURSEMENT
     *
     * Transfers therefore never become income or expense.
     */
    private fun buildCashFlow(
        transactions: List<Transaction>
    ): ReportsCashFlow {

        val actualIncome = transactions
            .asSequence()
            .filter { transaction ->

                transaction.type == TransactionType.INCOME &&
                    transaction.role != TransactionRole.REIMBURSEMENT &&
                    transaction.role != TransactionRole.TRANSFER_IN
            }
            .sumOf { transaction ->
                transaction.amount
            }

        val grossExpense = transactions
            .asSequence()
            .filter { transaction ->

                transaction.type == TransactionType.EXPENSE &&
                    transaction.role != TransactionRole.TRANSFER_OUT
            }
            .sumOf { transaction ->
                transaction.amount
            }

        val reimbursements = transactions
            .asSequence()
            .filter { transaction ->

                transaction.role == TransactionRole.REIMBURSEMENT
            }
            .sumOf { transaction ->
                transaction.amount
            }

        val effectiveExpense =
            grossExpense - reimbursements

        val netCashFlow =
            actualIncome - effectiveExpense

        return ReportsCashFlow(
            actualIncome = actualIncome,
            effectiveExpense = effectiveExpense,
            netCashFlow = netCashFlow
        )
    }

    // ------------------------------------------------------------------------
    // Expense categories
    // ------------------------------------------------------------------------

    /**
     * Builds dynamic expense categories from actual transaction data.
     *
     * We deliberately do NOT hardcode the category list here.
     *
     * This means user-created categories and future categories automatically
     * appear in Reports.
     */
    private fun buildExpenseCategories(
        transactions: List<Transaction>
    ): List<ReportsExpenseCategory> {

        val expenseTransactions = transactions.filter { transaction ->

            transaction.type == TransactionType.EXPENSE &&
                transaction.role != TransactionRole.TRANSFER_OUT
        }

        val reimbursementTransactions = transactions.filter { transaction ->

            transaction.role == TransactionRole.REIMBURSEMENT
        }

        val categoryNames = (
            expenseTransactions.map { transaction ->
                transaction.category
            } +
                reimbursementTransactions.map { transaction ->
                    transaction.category
                }
            )
            .filter { category ->
                category.isNotBlank()
            }
            .distinct()

        return categoryNames
            .map { category ->

                val categoryExpenses =
                    expenseTransactions.filter { transaction ->
                        transaction.category == category
                    }

                val categoryReimbursements =
                    reimbursementTransactions.filter { transaction ->
                        transaction.category == category
                    }

                val normalAmount =
                    categoryExpenses
                        .filter { transaction ->
                            transaction.transactionLinkId == null
                        }
                        .sumOf { transaction ->
                            transaction.amount
                        }

                val financialEventAmount =
                    categoryExpenses
                        .filter { transaction ->
                            transaction.transactionLinkId != null
                        }
                        .sumOf { transaction ->
                            transaction.amount
                        }

                val reimbursedAmount =
                    categoryReimbursements.sumOf { transaction ->
                        transaction.amount
                    }

                val effectiveFinancialEventAmount =
                    financialEventAmount - reimbursedAmount

                val totalAmount =
                    normalAmount +
                        financialEventAmount -
                        reimbursedAmount

                ReportsExpenseCategory(
                    category = category,
                    totalAmount = totalAmount,
                    normalAmount = normalAmount,
                    financialEventAmount = financialEventAmount,
                    reimbursedAmount = reimbursedAmount,
                    effectiveFinancialEventAmount =
                        effectiveFinancialEventAmount
                )
            }
            .filter { category ->
                category.totalAmount != 0.0
            }
            .sortedByDescending { category ->
                category.totalAmount
            }
    }

    // ------------------------------------------------------------------------
    // Income categories
    // ------------------------------------------------------------------------

    /**
     * Builds dynamic income categories.
     *
     * Reimbursements and account transfers are excluded from actual income.
     */
    private fun buildIncomeCategories(
        transactions: List<Transaction>
    ): List<ReportsIncomeCategory> {

        return transactions
            .asSequence()
            .filter { transaction ->

                transaction.type == TransactionType.INCOME &&
                    transaction.role == TransactionRole.NORMAL
            }
            .filter { transaction ->
                transaction.category.isNotBlank()
            }
            .groupBy { transaction ->
                transaction.category
            }
            .map { (category, categoryTransactions) ->

                ReportsIncomeCategory(
                    category = category,
                    totalAmount = categoryTransactions.sumOf { transaction ->
                        transaction.amount
                    }
                )
            }
            .sortedByDescending { category ->
                category.totalAmount
            }
    }

    // ------------------------------------------------------------------------
    // Financial Events
    // ------------------------------------------------------------------------

    /**
     * Builds Financial Event summaries from the existing transaction links.
     *
     * Financial Events are identified using transactionLinkId.
     *
     * Account transfers use transferLinkId and therefore do not enter
     * this calculation.
     */
    private fun buildFinancialEvents(
        transactions: List<Transaction>,
        groups: List<TransactionLinkGroup>
    ): List<ReportsFinancialEvent> {

        val linkedTransactions =
            transactions
                .filter { transaction ->
                    transaction.transactionLinkId != null
                }
                .groupBy { transaction ->
                    transaction.transactionLinkId!!
                }

        return linkedTransactions
            .mapNotNull { (transactionLinkId, eventTransactions) ->

                val group =
                    groups.firstOrNull { transactionLinkGroup ->
                        transactionLinkGroup.transactionLinkId ==
                            transactionLinkId
                    }

                if (group == null) {
                    return@mapNotNull null
                }

                val expenseAmount =
                    eventTransactions
                        .asSequence()
                        .filter { transaction ->

                            transaction.type == TransactionType.EXPENSE &&
                                transaction.role != TransactionRole.TRANSFER_OUT
                        }
                        .sumOf { transaction ->
                            transaction.amount
                        }

                val reimbursedAmount =
                    eventTransactions
                        .asSequence()
                        .filter { transaction ->

                            transaction.role ==
                                TransactionRole.REIMBURSEMENT
                        }
                        .sumOf { transaction ->
                            transaction.amount
                        }

                val effectiveCost =
                    expenseAmount - reimbursedAmount

                ReportsFinancialEvent(
                    transactionLinkId = transactionLinkId,
                    groupName = group.groupName,
                    category = group.category,
                    expenseAmount = expenseAmount,
                    reimbursedAmount = reimbursedAmount,
                    effectiveCost = effectiveCost
                )
            }
            .filter { event ->
                event.expenseAmount != 0.0 ||
                    event.reimbursedAmount != 0.0
            }
            .sortedByDescending { event ->
                event.effectiveCost
            }
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private data class ReportSourceData(
        val transactions: List<Transaction>,
        val groups: List<TransactionLinkGroup>
    )

    /**
     * Converts the transaction timestamp using the same device timezone
     * convention used by the application.
     */
    private fun Transaction.belongsToMonth(
        month: YearMonth
    ): Boolean {

        val localDate =
            Instant
                .ofEpochMilli(dateTimestamp)
                .atZone(zoneId)
                .toLocalDate()

        return YearMonth.from(localDate) == month
    }
}
