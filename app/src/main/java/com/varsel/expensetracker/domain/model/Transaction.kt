package com.varsel.expensetracker.domain.model

import com.varsel.expensetracker.domain.model.TransactionRole

data class Transaction(
    val id: Long = 0L,
    val amount: Double,
    val type: TransactionType,
    val description: String,
    val category: String,
    val dateTimestamp: Long,
    val referenceNumber: String? = null,
    val transactionFingerprint: String? = null,

    /**
     * Stable internal identifier for the bank account.
     *
     * This is a SHA-256 hash of the full account number.
     * The actual account number is never stored here.
     */
    val accountId: String? = null,

    /**
     * Last four digits of the account number.
     * Used only for safe user-facing display.
     */
    val accountLast4: String? = null,

    val role: TransactionRole = TransactionRole.NORMAL
)
