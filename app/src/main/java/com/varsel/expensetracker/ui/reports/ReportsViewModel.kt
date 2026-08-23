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
 *
 * - Observe transactions.
 * - Observe Financial Event groups.
 * - Build the available account list.
 * - Apply period filtering.
 * - Apply account filtering.
 * - Calculate cash flow.
 * - Calculate categories.
 * - Calculate Financial Events.
 * - Manage report selections.
 *
 * UI rendering remains outside this class.
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

    private val zoneId: ZoneId =
        ZoneId.systemDefault()

    private var latestTransactions: List<Transaction> =
        emptyList()

    private var latestGroups: List<TransactionLinkGroup> =
        emptyList()

    init {
        observeReportData()
    }

    // ------------------------------------------------------------------------
    // Period actions
    // ------------------------------------------------------------------------

    fun previousMonth() {
        updateSelectedMonth(
            _uiState.value.selectedMonth.minusMonths(1)
        )
    }

    fun nextMonth() {
        updateSelectedMonth(
            _uiState.value.selectedMonth.plusMonths(1)
        )
    }

    fun selectMonth(month: YearMonth) {
        updateSelectedMonth(month)
    }

    private fun updateSelectedMonth(
        month: YearMonth
    ) {
        _uiState.value = _uiState.value.copy(
            period = ReportPeriod.MONTH,
            selectedMonth = month,
            selectedExpenseCategory = null,
            selectedIncomeCategory = null,
            errorMessage = null,
            isLoading = true
        )

        rebuildReport()
    }

    // ------------------------------------------------------------------------
    // Account filter
    // ------------------------------------------------------------------------

    /**
     * Select or deselect an account.
     *
     * Empty selection means All Accounts.
     */
    fun toggleAccount(
        accountId: String
    ) {
        val current =
            _uiState.value.selectedAccountIds

        val updated =
            if (accountId in current) {
                current - accountId
            } else {
                current + accountId
            }

        applyAccountSelection(updated)
    }

    /**
     * Select a single account.
     */
    fun selectAccount(
        accountId: String
    ) {
        applyAccountSelection(
            setOf(accountId)
        )
    }

    /**
     * Show all accounts.
     */
    fun selectAllAccounts() {
        applyAccountSelection(
            emptySet()
        )
    }

    /**
     * Replace the complete account selection.
     */
    fun setSelectedAccounts(
        accountIds: Set<String>
    ) {
        applyAccountSelection(accountIds)
    }

    private fun applyAccountSelection(
        accountIds: Set<String>
    ) {
        _uiState.value = _uiState.value.copy(
            selectedAccountIds = accountIds,
            selectedExpenseCategory = null,
            selectedIncomeCategory = null,
            errorMessage = null,
            isLoading = true
        )

        rebuildReport()
    }

    // ------------------------------------------------------------------------
    // Money Flow
    // ------------------------------------------------------------------------

    fun selectFlow(
        flow: ReportsFlow
    ) {
        _uiState.value = _uiState.value.copy(
            selectedFlow = flow,
            selectedExpenseCategory = null,
            selectedIncomeCategory = null
        )
    }

    fun selectExpenseCategory(
        category: String?
    ) {
        _uiState.value = _uiState.value.copy(
            selectedExpenseCategory = category
        )
    }

    fun selectIncomeCategory(
        category: String?
    ) {
        _uiState.value = _uiState.value.copy(
            selectedIncomeCategory = category
        )
    }

    fun clearCategorySelection() {
        _uiState.value = _uiState.value.copy(
            selectedExpenseCategory = null,
            selectedIncomeCategory = null
        )
    }

    fun retry() {
        rebuildReport()
    }

    // ------------------------------------------------------------------------
    // Repository observation
    // ------------------------------------------------------------------------

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

                latestTransactions =
                    sourceData.transactions

                latestGroups =
                    sourceData.groups

                rebuildReport()
            }
        }
    }

    // ------------------------------------------------------------------------
    // Report rebuilding
    // ------------------------------------------------------------------------

    private fun rebuildReport() {

        try {

            val state =
                _uiState.value

            /*
             * Step 1:
             * Filter by selected reporting period.
             */
            val periodTransactions =
                latestTransactions.filter { transaction ->
                    transaction.belongsToMonth(
                        state.selectedMonth
                    )
                }

            /*
             * Step 2:
             * Filter by selected accounts.
             *
             * Empty Set means All Accounts.
             */
            val filteredTransactions =
                filterByAccounts(
                    transactions = periodTransactions,
                    selectedAccountIds =
                        state.selectedAccountIds
                )

            /*
             * Step 3:
             * Build all report sections from the
             * already-filtered transaction list.
             */
            val cashFlow =
                buildCashFlow(
                    filteredTransactions
                )

            val expenseCategories =
                buildExpenseCategories(
                    transactions = filteredTransactions,
                    groups = latestGroups
                )

            val incomeCategories =
                buildIncomeCategories(
                    filteredTransactions
                )

            val financialEvents =
                buildFinancialEvents(
                    transactions =
                        filteredTransactions,
                    groups = latestGroups
                )

            /*
             * Build the account list from all known
             * transactions, not just the current month.
             *
             * This allows the filter to remain useful
             * when a selected account has no transaction
             * in the current month.
             */
            val accounts =
                buildAccounts(
                    latestTransactions
                )

            _uiState.value =
                state.copy(
                    isLoading = false,
                    errorMessage = null,
                    accounts = accounts,
                    cashFlow = cashFlow,
                    expenseCategories =
                        expenseCategories,
                    incomeCategories =
                        incomeCategories,
                    financialEvents =
                        financialEvents
                )

        } catch (exception: Exception) {

            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    errorMessage =
                        exception.message
                            ?: "Unable to prepare report"
                )
        }
    }

    // ------------------------------------------------------------------------
    // Account filtering
    // ------------------------------------------------------------------------

    private fun filterByAccounts(
        transactions: List<Transaction>,
        selectedAccountIds: Set<String>
    ): List<Transaction> {

        /*
         * Empty means All Accounts.
         */
        if (selectedAccountIds.isEmpty()) {
            return transactions
        }

        return transactions.filter { transaction ->

            transaction.accountId != null &&
                transaction.accountId in selectedAccountIds
        }
    }

    /**
     * Builds the account list from the complete transaction history.
     *
     * The account ID is safe for internal selection.
     * Only accountLast4 is exposed to the UI.
     */
    private fun buildAccounts(
        transactions: List<Transaction>
    ): List<ReportsAccount> {

        return transactions
            .asSequence()
            .filter {
                !it.accountId.isNullOrBlank()
            }
            .groupBy {
                it.accountId!!
            }
            .map { (accountId, accountTransactions) ->

                val last4 =
                    accountTransactions
                        .mapNotNull {
                            it.accountLast4
                        }
                        .firstOrNull()

                ReportsAccount(
                    accountId = accountId,
                    accountLast4 = last4
                )
            }
            .sortedBy {
                it.accountLast4 ?: ""
            }
    }

    // ------------------------------------------------------------------------
    // Cash Flow
    // ------------------------------------------------------------------------

    private fun buildCashFlow(
        transactions: List<Transaction>
    ): ReportsCashFlow {

        val actualIncome =
            transactions
                .asSequence()
                .filter { transaction ->

                    transaction.type ==
                        TransactionType.INCOME &&

                        transaction.role !=
                            TransactionRole.REIMBURSEMENT &&

                        transaction.role !=
                            TransactionRole.TRANSFER_IN
                }
                .sumOf {
                    it.amount
                }

        val grossExpense =
            transactions
                .asSequence()
                .filter { transaction ->

                    transaction.type ==
                        TransactionType.EXPENSE &&

                        transaction.role !=
                            TransactionRole.TRANSFER_OUT
                }
                .sumOf {
                    it.amount
                }

        val reimbursements =
            transactions
                .asSequence()
                .filter { transaction ->

                    transaction.role ==
                        TransactionRole.REIMBURSEMENT
                }
                .sumOf {
                    it.amount
                }

        val effectiveExpense =
            grossExpense - reimbursements

        return ReportsCashFlow(
            actualIncome = actualIncome,
            effectiveExpense = effectiveExpense,
            netCashFlow =
                actualIncome - effectiveExpense
        )
    }

    // ------------------------------------------------------------------------
    // Expense Categories
    // ------------------------------------------------------------------------

private fun buildExpenseCategories(
    transactions: List<Transaction>,
    groups: List<TransactionLinkGroup>
): List<ReportsExpenseCategory> {

    /*
     * Financial Event groups tell us the logical category
     * of the event.
     *
     * This is important because a reimbursement transaction
     * may itself be Uncategorized, while the event it
     * reimburses belongs to a real expense category.
     */
    val eventCategoryByLinkId =
        groups.associate {
            it.transactionLinkId to it.category
        }

    val expenseTransactions =
        transactions.filter { transaction ->

            transaction.type ==
                TransactionType.EXPENSE &&

                transaction.role !=
                    TransactionRole.TRANSFER_OUT
        }

    val reimbursementTransactions =
        transactions.filter { transaction ->

            transaction.role ==
                TransactionRole.REIMBURSEMENT
        }

    /*
     * Normal expenses keep their own category.
     *
     * Financial-event reimbursements use the category
     * of the linked Financial Event instead of the
     * reimbursement transaction's own category.
     */
    val expenseAmountsByCategory =
        expenseTransactions
            .groupBy {
                it.category
            }
            .mapValues { (_, categoryTransactions) ->

                categoryTransactions.sumOf {
                    it.amount
                }
            }

    val reimbursementAmountsByCategory =
        reimbursementTransactions
            .groupBy { transaction ->

                transaction.transactionLinkId
                    ?.let {
                        eventCategoryByLinkId[it]
                    }
                    ?: transaction.category
            }
            .mapValues { (_, categoryTransactions) ->

                categoryTransactions.sumOf {
                    it.amount
                }
            }

    val categories =
        (
            expenseAmountsByCategory.keys +
                reimbursementAmountsByCategory.keys
            )
            .filter {
                it.isNotBlank()
            }
            .distinct()

    return categories
        .map { category ->

            val normalExpenseAmount =
                expenseTransactions
                    .filter {
                        it.category == category &&
                            it.transactionLinkId == null
                    }
                    .sumOf {
                        it.amount
                    }

            val financialEventExpenseAmount =
                expenseTransactions
                    .filter {
                        it.category == category &&
                            it.transactionLinkId != null
                    }
                    .sumOf {
                        it.amount
                    }

            val reimbursedAmount =
                reimbursementAmountsByCategory[
                    category
                ] ?: 0.0

            val totalAmount =
                normalExpenseAmount +
                    financialEventExpenseAmount -
                    reimbursedAmount

            ReportsExpenseCategory(
                category = category,

                totalAmount =
                    totalAmount,

                normalAmount =
                    normalExpenseAmount,

                financialEventAmount =
                    financialEventExpenseAmount,

                reimbursedAmount =
                    reimbursedAmount,

                effectiveFinancialEventAmount =
                    financialEventExpenseAmount -
                        reimbursedAmount
            )
        }
        .filter {
            /*
             * A category that only contains reimbursements
             * is not an expense category.
             *
             * Do not display it as a negative expense.
             */
            it.totalAmount > 0.0
        }
        .sortedByDescending {
            it.totalAmount
        }
}

    // ------------------------------------------------------------------------
    // Income Categories
    // ------------------------------------------------------------------------

    private fun buildIncomeCategories(
        transactions: List<Transaction>
    ): List<ReportsIncomeCategory> {

        return transactions
            .asSequence()
            .filter { transaction ->

                transaction.type ==
                    TransactionType.INCOME &&

                    transaction.role ==
                        TransactionRole.NORMAL
            }
            .filter {
                it.category.isNotBlank()
            }
            .groupBy {
                it.category
            }
            .map { (category, categoryTransactions) ->

                ReportsIncomeCategory(
                    category = category,
                    totalAmount =
                        categoryTransactions
                            .sumOf {
                                it.amount
                            }
                )
            }
            .sortedByDescending {
                it.totalAmount
            }
    }

    // ------------------------------------------------------------------------
    // Financial Events
    // ------------------------------------------------------------------------

    private fun buildFinancialEvents(
        transactions: List<Transaction>,
        groups: List<TransactionLinkGroup>
    ): List<ReportsFinancialEvent> {

        /*
         * IMPORTANT:
         *
         * This receives transactions AFTER the account
         * filter has already been applied.
         *
         * Therefore a selected account cannot accidentally
         * pull unrelated transactions from another account
         * into the report.
         */
        val linkedTransactions =
            transactions
                .filter {
                    it.transactionLinkId != null
                }
                .groupBy {
                    it.transactionLinkId!!
                }

        return linkedTransactions
            .mapNotNull {
                    (transactionLinkId, eventTransactions) ->

                val group =
                    groups.firstOrNull {
                        it.transactionLinkId ==
                            transactionLinkId
                    }

                if (group == null) {
                    return@mapNotNull null
                }

                val expenseAmount =
                    eventTransactions
                        .asSequence()
                        .filter { transaction ->

                            transaction.type ==
                                TransactionType.EXPENSE &&

                                transaction.role !=
                                    TransactionRole.TRANSFER_OUT
                        }
                        .sumOf {
                            it.amount
                        }

                val reimbursedAmount =
                    eventTransactions
                        .asSequence()
                        .filter {

                            it.role ==
                                TransactionRole.REIMBURSEMENT
                        }
                        .sumOf {
                            it.amount
                        }

                ReportsFinancialEvent(
                    transactionLinkId =
                        transactionLinkId,
                    groupName =
                        group.groupName,
                    category =
                        group.category,
                    expenseAmount =
                        expenseAmount,
                    reimbursedAmount =
                        reimbursedAmount,
                    effectiveCost =
                        expenseAmount -
                            reimbursedAmount
                )
            }
            .filter {
                it.expenseAmount != 0.0 ||
                    it.reimbursedAmount != 0.0
            }
            .sortedByDescending {
                it.effectiveCost
            }
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private data class ReportSourceData(
        val transactions: List<Transaction>,
        val groups: List<TransactionLinkGroup>
    )

    private fun Transaction.belongsToMonth(
        month: YearMonth
    ): Boolean {

        val localDate =
            Instant
                .ofEpochMilli(dateTimestamp)
                .atZone(zoneId)
                .toLocalDate()

        return YearMonth.from(localDate) ==
            month
    }
}
