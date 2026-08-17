package com.varsel.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.varsel.expensetracker.domain.model.TransactionRole

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val amount: Double,

    val type: String,

    val description: String,

    val category: String,

    val dateTimestamp: Long,

    val referenceNumber: String? = null,

    val transactionFingerprint: String? = null,

    /**
     * Stable internal account identifier.
     *
     * Contains a SHA-256 hash of the full account number,
     * never the actual account number.
     */
    val accountId: String? = null,

    /**
     * Last four digits of the account number.
     */
    val accountLast4: String? = null,

    /**
     * Internal relationship/group identifier.
     *
     * Multiple related transactions can share this value.
     *
     * Example:
     * Lent ₹3,000
     * Reimbursement ₹1,000
     * Reimbursement ₹1,000
     *
     * All three can have the same transactionLinkId.
     */
    val transactionLinkId: String? = null,

    val role: String = TransactionRole.NORMAL.name
)
