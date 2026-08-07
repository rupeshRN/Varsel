package com.varsel.expensetracker.domain.model

data class Transaction(
    val id: Long = 0L,
    val amount: Double,
    val type: TransactionType,
    val description: String,
    val category: String,
    val dateTimestamp: Long,
    val referenceNumber: String? = null
)
