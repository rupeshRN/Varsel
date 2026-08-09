package com.varsel.expensetracker.parser

import com.varsel.expensetracker.domain.model.Transaction

data class StatementImportResult(

    val summary: StatementSummary,

    val transactions: List<Transaction>
)
