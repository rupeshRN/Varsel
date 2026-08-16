package com.varsel.expensetracker.domain.model

enum class TransactionRole {

    /**
     * Regular transaction.
     */
    NORMAL,

    /**
     * Expense paid on behalf of another person.
     *
     * Example:
     * ₹1,000 restaurant bill paid for a group.
     */
    LENT,

    /**
     * Money received to recover a previous LENT expense.
     *
     * This is still an INCOME transaction at the bank-account level,
     * but it is not counted as actual income in financial analysis.
     */
    REIMBURSEMENT
}
