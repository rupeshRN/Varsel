package com.varsel.expensetracker.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionLinkGroup
import com.varsel.expensetracker.domain.model.TransactionRole
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.domain.repository.TransactionLinkGroupRepository
import com.varsel.expensetracker.domain.repository.TransactionRepository
import com.varsel.expensetracker.data.repository.FinancialEventAllocationRepository
import com.varsel.expensetracker.data.local.entity.FinancialEventAllocationEntity
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
 * - Calculate category reports.
 * - Calculate Financial Events.
 * - Manage report selections.
 *
 * IMPORTANT REPORTING RULE:
 *
 * A transaction linked to a Financial Event must NOT appear
 * inside its original transaction category.
 *
 * Instead:
 *
 *     linked expense transactions
 *                 +
 *     linked reimbursement transactions
 *                 ↓
 *          Financial Event
 *                 ↓
 *        effective cost
 *                 ↓
 *       Financial Event category
 *
 * Example:
 *
 *     Food transaction       ₹1,000
 *     Travel transaction     ₹1,500
 *     Uncategorized         ₹500
 *     Reimbursement        -₹2,000
 *     --------------------------------
 *     Financial Event        ₹1,000
 *
 * If the Financial Event category is Travel, only ₹1,000
 * is added to Travel.
 *
 * The original Food / Travel / Uncategorized transactions
 * disappear from normal category reporting.
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val transactionLinkGroupRepository: TransactionLinkGroupRepository,
    private val financialEventAllocationRepository: FinancialEventAllocationRepository
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

    private var latestAllocations:
    List<FinancialEventAllocationEntity> =
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

    fun selectMonth(
        month: YearMonth
    ) {
        updateSelectedMonth(month)
    }

    private fun updateSelectedMonth(
        month: YearMonth
    ) {
        _uiState.value =
            _uiState.value.copy(
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
        _uiState.value =
            _uiState.value.copy(
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
        _uiState.value =
            _uiState.value.copy(
                selectedFlow = flow,
                selectedExpenseCategory = null,
                selectedIncomeCategory = null
            )
    }

    fun selectExpenseCategory(
        category: String?
    ) {
        _uiState.value =
            _uiState.value.copy(
                selectedExpenseCategory = category
            )
    }

    fun selectIncomeCategory(
        category: String?
    ) {
        _uiState.value =
            _uiState.value.copy(
                selectedIncomeCategory = category
            )
    }

    fun clearCategorySelection() {
        _uiState.value =
            _uiState.value.copy(
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
            transactionLinkGroupRepository.getAllGroups(),
            financialEventAllocationRepository.observeAllAllocations()
        ) { transactions, groups, allocations ->

            ReportSourceData(
                transactions = transactions,
                groups = groups,
                allocations = allocations
            )

        }.collect { sourceData ->

            latestTransactions =
                sourceData.transactions

            latestGroups =
                sourceData.groups

            latestAllocations =
                sourceData.allocations

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
             *
             * Filter transactions by the selected reporting
             * period.
             */
            val periodTransactions =
                latestTransactions.filter { transaction ->

                    transaction.belongsToMonth(
                        state.selectedMonth
                    )
                }

            /*
             * Step 2:
             *
             * Apply account filter.
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
 * Account-filtered transactions across the complete
 * transaction history.
 *
 * This is NOT period filtered.
 *
 * It is used only to determine whether a Financial Event
 * spans multiple months.
 */
val accountFilteredAllTransactions =
    filterByAccounts(
        transactions = latestTransactions,
        selectedAccountIds =
            state.selectedAccountIds
    )

            /*
             * Step 4:
             *
             * Build report sections from the same filtered
             * transaction collection.
             */
            val cashFlow =
                buildCashFlow(
                    filteredTransactions
                )

val expenseCategories =
    buildExpenseCategories(
        transactions =
            filteredTransactions,

        groups =
            latestGroups,

        allocations =
            latestAllocations
    )

            val incomeCategories =
                buildIncomeCategories(
                    filteredTransactions
                )

val financialEvents =
    buildFinancialEvents(
        transactions =
            filteredTransactions,

        allAccountTransactions =
            accountFilteredAllTransactions,

        groups =
            latestGroups
    )

            /*
             * Account list comes from the complete transaction
             * history so accounts remain available to the filter
             * even if they have no transaction in the current
             * month.
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
                transaction.accountId in
                selectedAccountIds
        }
    }

    /**
     * Builds the account list from the complete transaction
     * history.
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

        /*
         * Actual income:
         *
         * - NORMAL income is real income.
         * - REIMBURSEMENT is not income.
         * - TRANSFER_IN is not income.
         */
        val actualIncome =
            transactions
                .asSequence()
                .filter { transaction ->

                    transaction.type ==
                        TransactionType.INCOME &&

                        transaction.role ==
                            TransactionRole.NORMAL
                }
                .sumOf {
                    it.amount
                }

        /*
         * Gross expense:
         *
         * - NORMAL expenses
         * - LENT expenses
         *
         * TRANSFER_OUT is excluded.
         */
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

        /*
         * Reimbursements reduce effective expense.
         */
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
            grossExpense -
                reimbursements

        return ReportsCashFlow(
            actualIncome =
                actualIncome,

            effectiveExpense =
                effectiveExpense,

            netCashFlow =
                actualIncome -
                    effectiveExpense
        )
    }

    // ------------------------------------------------------------------------
    // Expense Categories
    // ------------------------------------------------------------------------

    /**
     * Builds Money Flow expense categories.
     *
     * IMPORTANT:
     *
     * A Financial Event is treated as one logical report item.
     *
     * Therefore:
     *
     *     linked transaction A
     *     linked transaction B
     *     linked transaction C
     *
     * are NOT included in their original categories.
     *
     * Instead:
     *
     *     event expense total
     *     - event reimbursement total
     *     = event effective cost
     *
     * and the effective cost is added to the Financial Event's
     * selected category.
     */
    private fun buildExpenseCategories(
        transactions: List<Transaction>,
        groups: List<TransactionLinkGroup>,
        allocations: List<FinancialEventAllocationEntity>
    ): List<ReportsExpenseCategory> {

        /*
         * Map Financial Event ID -> Financial Event.
         */
        val groupsByLinkId =
            groups.associateBy {
                it.transactionLinkId
            }

        /*
         * ----------------------------------------------------
         * NORMAL EXPENSES
         * ----------------------------------------------------
         *
         * Only completely normal, unlinked expenses belong
         * directly to their transaction category.
         *
         * Any transaction with transactionLinkId is handled
         * through the Financial Event section below.
         */
        val normalExpenseTransactions =
            transactions.filter { transaction ->

                transaction.type ==
                    TransactionType.EXPENSE &&

                    transaction.role ==
                        TransactionRole.NORMAL &&

                    transaction.transactionLinkId == null &&

                    transaction.transferLinkId == null
            }

        /*
         * ----------------------------------------------------
         * NORMAL CATEGORY TOTALS
         * ----------------------------------------------------
         */
        val normalAmountsByCategory =
            normalExpenseTransactions
                .groupBy {
                    it.category
                }
                .mapValues { (_, categoryTransactions) ->

                    categoryTransactions.sumOf {
                        it.amount
                    }
                }

        /*
         * ----------------------------------------------------
         * FINANCIAL EVENT TOTALS
         * ----------------------------------------------------
         *
         * Every linked transaction is grouped by its
         * Financial Event.
         */
        val linkedTransactionsByEvent =
            transactions
                .filter {
                    it.transactionLinkId != null
                }
                .groupBy {
                    it.transactionLinkId!!
                }

        /*
         * For every Financial Event:
         *
         * expense total
         * -
         * reimbursement total
         * =
         * effective cost
         *
         * The result is then assigned to the Financial
         * Event's category.
         */
        val financialEventAmountsByCategory =
            mutableMapOf<String, Double>()

        val financialEventGrossExpensesByCategory =
            mutableMapOf<String, Double>()

        val financialEventReimbursementsByCategory =
            mutableMapOf<String, Double>()

        linkedTransactionsByEvent.forEach {
                (transactionLinkId, eventTransactions) ->

            val group =
                groupsByLinkId[
                    transactionLinkId
                ] ?: return@forEach

            val category =
                group.category.trim()

            if (category.isBlank()) {
                return@forEach
            }

            /*
             * All EXPENSE transactions belonging to the
             * Financial Event.
             *
             * Their original categories are deliberately
             * ignored.
             */
            val eventExpense =
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

            /*
             * All REIMBURSEMENT transactions belonging to
             * this Financial Event.
             */
            val eventReimbursement =
                eventTransactions
                    .asSequence()
                    .filter { transaction ->

                        transaction.role ==
                            TransactionRole.REIMBURSEMENT
                    }
                    .sumOf {
                        it.amount
                    }

            /*
             * This is the amount the Financial Event actually
             * costs the user.
             */
            val effectiveCost =
                eventExpense -
                    eventReimbursement

            /*
             * Keep the event gross expense and reimbursement
             * values available in the category model.
             */
            financialEventGrossExpensesByCategory[category] =
                (
                    financialEventGrossExpensesByCategory[
                        category
                    ] ?: 0.0
                ) + eventExpense

            financialEventReimbursementsByCategory[category] =
                (
                    financialEventReimbursementsByCategory[
                        category
                    ] ?: 0.0
                ) + eventReimbursement

            /*
             * Only positive effective expense contributes to
             * the expense category chart.
             *
             * A fully reimbursed event contributes ₹0.
             *
             * An event whose reimbursement is larger than
             * its expense is not rendered as a negative
             * expense category.
             */
            if (effectiveCost > 0.0) {

                financialEventAmountsByCategory[category] =
                    (
                        financialEventAmountsByCategory[
                            category
                        ] ?: 0.0
                    ) + effectiveCost
            }
        }

        /*
         * ----------------------------------------------------
         * COMBINE NORMAL + FINANCIAL EVENT CATEGORIES
         * ----------------------------------------------------
         */
        val allCategories =
            (
                normalAmountsByCategory.keys +
                    financialEventAmountsByCategory.keys
                )
                .filter {
                    it.isNotBlank()
                }
                .distinct()

        return allCategories
            .map { category ->

                val normalAmount =
                    normalAmountsByCategory[
                        category
                    ] ?: 0.0

                val financialEventAmount =
                    financialEventAmountsByCategory[
                        category
                    ] ?: 0.0

                val grossFinancialEventAmount =
                    financialEventGrossExpensesByCategory[
                        category
                    ] ?: 0.0

                val reimbursedAmount =
                    financialEventReimbursementsByCategory[
                        category
                    ] ?: 0.0

                val totalAmount =
                    normalAmount +
                        financialEventAmount

                ReportsExpenseCategory(
                    category =
                        category,

                    totalAmount =
                        totalAmount,

                    normalAmount =
                        normalAmount,

                    /*
                     * This field now represents the amount
                     * actually contributed by Financial Events
                     * to this category.
                     */
                    financialEventAmount =
                        financialEventAmount,

                    /*
                     * Preserve the gross reimbursement
                     * information for the category model.
                     */
                    reimbursedAmount =
                        reimbursedAmount,

                    effectiveFinancialEventAmount =
                        financialEventAmount
                )
            }
            .filter {
                it.totalAmount > 0.0
            }
            .sortedByDescending {
                it.totalAmount
            }
    }

    // ------------------------------------------------------------------------
    // Income Categories
    // ------------------------------------------------------------------------

    /**
     * Builds actual income categories.
     *
     * Financial Event reimbursements are deliberately excluded.
     *
     * Account transfers are deliberately excluded.
     *
     * This means the Income chart represents genuine income,
     * not money received to recover a Financial Event expense
     * and not money moved between the user's own accounts.
     */
    private fun buildIncomeCategories(
        transactions: List<Transaction>
    ): List<ReportsIncomeCategory> {

        return transactions
            .asSequence()
            .filter { transaction ->

                transaction.type ==
                    TransactionType.INCOME &&

                    transaction.role ==
                        TransactionRole.NORMAL &&

                    transaction.transactionLinkId == null &&

                    transaction.transferLinkId == null
            }
            .filter {
                it.category.isNotBlank()
            }
            .groupBy {
                it.category
            }
            .map { (category, categoryTransactions) ->

                ReportsIncomeCategory(
                    category =
                        category,

                    totalAmount =
                        categoryTransactions.sumOf {
                            it.amount
                        }
                )
            }
            .sortedByDescending {
                it.totalAmount
            }
    }

/**
 * Builds Financial Event summaries.
 *
 * The transaction list is already filtered by period and
 * account before reaching this function.
 *
 * IMPORTANT:
 *
 * The financial amounts shown in the report remain restricted
 * to the selected reporting period.
 *
 * The complete account-filtered transaction history is used
 * only to determine whether an event spans multiple months.
 */
private fun buildFinancialEvents(
    transactions: List<Transaction>,
    allAccountTransactions: List<Transaction>,
    groups: List<TransactionLinkGroup>
): List<ReportsFinancialEvent> {

    /*
     * Transactions belonging to the currently selected
     * reporting period.
     */
    val linkedTransactions =
        transactions
            .filter {
                it.transactionLinkId != null
            }
            .groupBy {
                it.transactionLinkId!!
            }

    /*
     * Complete account-filtered transaction history.
     *
     * This is intentionally NOT restricted to the selected
     * month.
     */
    val allLinkedTransactions =
        allAccountTransactions
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

            /*
             * Gross expense belonging to this event
             * within the selected report period.
             */
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

            /*
             * Reimbursement belonging to this event
             * within the selected report period.
             */
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

            /*
             * Effective cost for the SELECTED REPORT PERIOD.
             *
             * This calculation intentionally does not include
             * transactions from other months.
             */
            val effectiveCost =
                expenseAmount -
                    reimbursedAmount

            /*
             * ------------------------------------------------
             * MULTI-MONTH EVENT DETECTION
             * ------------------------------------------------
             *
             * Look at the complete account-filtered history
             * for this Financial Event.
             *
             * We only consider expense/reimbursement
             * transactions because those are the transactions
             * that determine the Financial Event's reporting
             * lifecycle.
             */
            val coveredMonths =
                allLinkedTransactions[
                    transactionLinkId
                ]
                    .orEmpty()
                    .asSequence()
                    .filter { transaction ->

                        transaction.type ==
                            TransactionType.EXPENSE ||

                            transaction.role ==
                                TransactionRole.REIMBURSEMENT
                    }
                    .map {
                        transactionYearMonth(
                            it.dateTimestamp
                        )
                    }
                    .distinct()
                    .sorted()
                    .toList()

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
                    effectiveCost,

                coveredMonths =
                    coveredMonths
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
        val groups: List<TransactionLinkGroup>,
        val allocations: List<FinancialEventAllocationEntity>
    )

    private fun transactionYearMonth(
    dateTimestamp: Long
): YearMonth {

    return Instant
        .ofEpochMilli(dateTimestamp)
        .atZone(zoneId)
        .let {
            YearMonth.from(
                it.toLocalDate()
            )
        }
}
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
