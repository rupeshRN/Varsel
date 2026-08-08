package com.varsel.expensetracker.parser

import com.varsel.expensetracker.domain.model.Transaction
import javax.inject.Inject

class IndianBankParser @Inject constructor() : StatementParser {

    override fun canParse(rawText: String): Boolean {

        val text = rawText.uppercase()

        return text.contains("INDIAN BANK")
                || text.contains("ACCOUNT ACTIVITY")
                || text.contains("ACCOUNT DETAILS")
                || text.contains("ACCOUNT SUMMARY")
    }

    override fun parse(rawText: String): List<Transaction> {

        // Parsing logic will be implemented in the next step.
        return emptyList()
    }
}
