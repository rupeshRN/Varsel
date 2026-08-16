package com.varsel.expensetracker.ui.mapper

import com.varsel.expensetracker.data.local.entity.StatementSnapshotEntity
import com.varsel.expensetracker.domain.model.Transaction
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
        // Overall income / expense
        //--------------------------------------------------

        val income =
            transactions
                .filter {
                    it.type == TransactionType.INCOME
                }
                .sumOf {
                    it.amount
                }

        val expense =
            transactions
                .filter {
                    it.type == TransactionType.EXPENSE
                }
                .sumOf {
                    it.amount
                }

        //--------------------------------------------------
        // Calculate account-wise current balances
        //--------------------------------------------------

        val accountBalances =
            calculateAccountBalances(
                transactions = transactions,
                snapshots = snapshots
            )

        //--------------------------------------------------
        // Total balance across all accounts
        //--------------------------------------------------

        val totalBalance =
            accountBalances.sumOf {
                it.balance
            }

        //--------------------------------------------------
        // Dashboard
        //--------------------------------------------------

        return DashboardUiState(

            balanceSummary =
                BalanceSummaryUiModel(

                    totalBalance = totalBalance,

                    totalIncome = income,

                    totalExpense = expense,

                    savings = totalBalance,

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
                            ?.let { "•••• $it" }
                            ?: "Account",
                    balance = balance
                )
            )
        }

        //--------------------------------------------------
        // Legacy transactions that don't yet have
        // account identity.
        //
        // These cannot safely be attached to a specific
        // bank account.
        //--------------------------------------------------

        val legacyTransactions =
            transactionsByAccount[null].orEmpty()

        if (legacyTransactions.isNotEmpty()) {

            val legacyBalance =
                legacyTransactions.sumOf {
                    if (it.type == TransactionType.INCOME) {
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
    // Current balance for one account
    //--------------------------------------------------

    private fun calculateCurrentBalance(
        transactions: List<Transaction>,
        snapshot: StatementSnapshotEntity?
    ): Double {

        //--------------------------------------------------
        // No statement snapshot available.
        //
        // Fall back to transaction-based calculation.
        //--------------------------------------------------

        if (snapshot == null) {

            return transactions.sumOf {

                if (it.type == TransactionType.INCOME) {
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
        // Only apply transactions AFTER the statement
        // end date.
        //
        // This prevents transactions already included
        // in the statement from being counted again.
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
