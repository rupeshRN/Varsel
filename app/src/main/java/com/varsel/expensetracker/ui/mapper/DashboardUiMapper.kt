package com.varsel.expensetracker.ui.mapper

import com.varsel.expensetracker.data.local.entity.StatementSnapshotEntity
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionRole
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.ui.dashboard.DashboardUiState
import com.varsel.expensetracker.ui.model.AccountBalanceUiModel
import com.varsel.expensetracker.ui.model.BalanceSummaryUiModel
import javax.inject.Inject

class DashboardUiMapper @Inject constructor(

    private val transactionUiMapper: TransactionUiMapper

) {

    fun map(
        transactions: List<Transaction>,
        snapshots: List<StatementSnapshotEntity>
    ): DashboardUiState {

        //--------------------------------------------------
        // Financial income / expense
        //
        // IMPORTANT:
        //
        // Bank balance is based on actual bank movements.
        //
        // Financial analysis is different:
        //
        // LENT ₹1,000
        // REIMBURSEMENT ₹800
        //
        // = actual expense ₹200
        // = actual income ₹0
        //
        // Therefore LENT and REIMBURSEMENT are netted
        // against each other here.
        //--------------------------------------------------

        val normalIncome =
            transactions
                .filter {
                    it.type == TransactionType.INCOME &&
                    it.role == TransactionRole.NORMAL
                }
                .sumOf {
                    it.amount
                }

        val normalExpense =
            transactions
                .filter {
                    it.type == TransactionType.EXPENSE &&
                    it.role == TransactionRole.NORMAL
                }
                .sumOf {
                    it.amount
                }

        val lentAmount =
            transactions
                .filter {
                    it.type == TransactionType.EXPENSE &&
                    it.role == TransactionRole.LENT
                }
                .sumOf {
                    it.amount
                }

        val reimbursementAmount =
            transactions
                .filter {
                    it.type == TransactionType.INCOME &&
                    it.role == TransactionRole.REIMBURSEMENT
                }
                .sumOf {
                    it.amount
                }

        //--------------------------------------------------
        // Net LENT against REIMBURSEMENT.
        //
        // Example:
        //
        // Lent          = 1000
        // Reimbursement = 800
        //
        // Remaining expense = 200
        // Remaining income  = 0
        //--------------------------------------------------

        val netLentExpense =
            (lentAmount - reimbursementAmount)
                .coerceAtLeast(0.0)

        val excessReimbursement =
            (reimbursementAmount - lentAmount)
                .coerceAtLeast(0.0)

        //--------------------------------------------------
        // Final financial totals.
        //--------------------------------------------------

        val income =
            normalIncome +
                excessReimbursement

        val expense =
            normalExpense +
                netLentExpense

        //--------------------------------------------------
        // Calculate account-wise current balances.
        //
        // NOTE:
        // This uses actual bank movements and therefore
        // DOES NOT apply the LENT/REIMBURSEMENT adjustment.
        //--------------------------------------------------

        val accountBalances =
            calculateAccountBalances(
                transactions = transactions,
                snapshots = snapshots
            )

        //--------------------------------------------------
        // Total bank balance across all accounts.
        //--------------------------------------------------

        val totalBalance =
            accountBalances.sumOf {
                it.balance
            }

        //--------------------------------------------------
        // Financial savings.
        //
        // This represents financial net position from
        // classified transactions, not raw bank balance.
        //--------------------------------------------------

        val savings =
            income - expense

        //--------------------------------------------------
        // Dashboard
        //--------------------------------------------------

        return DashboardUiState(

            balanceSummary =
                BalanceSummaryUiModel(

                    totalBalance = totalBalance,

                    totalIncome = income,

                    totalExpense = expense,

                    savings = savings,

                    accounts = accountBalances

                ),

            recentTransactions =
                transactions
                    .sortedByDescending {
                        it.dateTimestamp
                    }
                    .take(10)
                    .map {
                        transactionUiMapper.map(it)
                    },

            isLoading = false
        )
    }

    //--------------------------------------------------
    // Account balance calculation
    //--------------------------------------------------

    private fun calculateAccountBalances(
        transactions: List<Transaction>,
        snapshots: List<StatementSnapshotEntity>
    ): List<AccountBalanceUiModel> {

        //--------------------------------------------------
        // Group transactions by account.
        //
        // Transactions without an accountId are handled
        // separately so older data is not lost.
        //--------------------------------------------------

        val transactionsByAccount =
            transactions.groupBy {
                it.accountId
            }

        //--------------------------------------------------
        // Known accounts from transactions + snapshots.
        //--------------------------------------------------

        val accountIds =
            (
                transactions.mapNotNull {
                    it.accountId
                } +
                    snapshots.mapNotNull {
                        it.accountId
                    }
                ).distinct()

        val result =
            mutableListOf<AccountBalanceUiModel>()

        //--------------------------------------------------
        // Calculate each known account.
        //--------------------------------------------------

        accountIds.forEach { accountId ->

            val accountTransactions =
                transactionsByAccount[accountId]
                    .orEmpty()

            val latestSnapshot =
                snapshots
                    .filter {
                        it.accountId == accountId
                    }
                    .maxWithOrNull(
                        compareBy<StatementSnapshotEntity> {
                            it.statementEndDate ?: Long.MIN_VALUE
                        }.thenBy {
                            it.importedAt
                        }
                    )

            val balance =
                calculateCurrentBalance(
                    transactions = accountTransactions,
                    snapshot = latestSnapshot
                )

            val accountLast4 =
                latestSnapshot?.accountLast4
                    ?: accountTransactions
                        .firstOrNull()
                        ?.accountLast4

            result.add(
                AccountBalanceUiModel(
                    bankName = "Bank Account",
                    accountDisplayName =
                        accountLast4
                            ?.let {
                                "•••• $it"
                            }
                            ?: "Account",
                    balance = balance
                )
            )
        }

        //--------------------------------------------------
        // Legacy transactions that don't yet have
        // account identity.
        //--------------------------------------------------

        val legacyTransactions =
            transactionsByAccount[null]
                .orEmpty()

        if (legacyTransactions.isNotEmpty()) {

            val legacyBalance =
                legacyTransactions.sumOf {

                    if (
                        it.type ==
                        TransactionType.INCOME
                    ) {
                        it.amount
                    } else {
                        -it.amount
                    }
                }

            result.add(
                AccountBalanceUiModel(
                    bankName = "Other",
                    accountDisplayName = "Unlinked",
                    balance = legacyBalance
                )
            )
        }

        return result
    }

    //--------------------------------------------------
    // Current bank balance for one account.
    //
    // IMPORTANT:
    // This intentionally uses actual bank movements.
    //
    // LENT and REIMBURSEMENT are NOT netted here.
    //
    // Example:
    //
    // Opening/current snapshot = ₹5,000
    // LENT expense             = -₹1,000
    // Reimbursement            = +₹800
    //
    // Bank balance             = ₹4,800
    //
    // That is the actual money in the account.
    //--------------------------------------------------

    private fun calculateCurrentBalance(
        transactions: List<Transaction>,
        snapshot: StatementSnapshotEntity?
    ): Double {

        //--------------------------------------------------
        // No statement snapshot available.
        //--------------------------------------------------

        if (snapshot == null) {

            return transactions.sumOf {

                if (
                    it.type ==
                    TransactionType.INCOME
                ) {
                    it.amount
                } else {
                    -it.amount
                }
            }
        }

        //--------------------------------------------------
        // Start from the latest known bank statement
        // ending balance.
        //--------------------------------------------------

        var balance =
            snapshot.endingBalance ?: 0.0

        //--------------------------------------------------
        // Only apply transactions after the statement
        // end date.
        //--------------------------------------------------

        val statementEnd =
            snapshot.statementEndDate
                ?: Long.MIN_VALUE

        transactions
            .filter {
                it.dateTimestamp > statementEnd
            }
            .forEach { transaction ->

                balance +=
                    if (
                        transaction.type ==
                        TransactionType.INCOME
                    ) {
                        transaction.amount
                    } else {
                        -transaction.amount
                    }
            }

        return balance
    }
}
