package com.varsel.expensetracker.parser

import javax.inject.Inject
import com.varsel.expensetracker.developer.ParserDiagnosticsManager

class TransactionBlockBuilder @Inject constructor() {

    private val dateRegex =
        Regex("^\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}")

        private val footerKeywords = listOf(

    "ENDING BALANCE",

    "TOTAL CREDITS",
    "TOTAL DEBITS",

    "OPENING BALANCE",

    "ACCOUNT SUMMARY",

    "ACCOUNT DETAILS",

    "CUSTOMER'S ADDRESS",

    "IFSC",

    "ACCOUNT HOLDER",

    "ACCOUNT NUMBER"
)

    fun build(normalizedText: String): List<TransactionBlock> {

        val lines = normalizedText
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val transactionLines = mutableListOf<String>()

        val missedDateLines = mutableListOf<String>() //to diagnose the bug

        var accountActivityFound = false

        for (line in lines) {

    val upper = line.uppercase()

    if (!accountActivityFound) {

        if (upper.contains("ACCOUNT ACTIVITY")) {
            accountActivityFound = true
        }

        continue
    }

    // Skip table header
    if (upper.contains("DATE TRANSACTION DETAILS")) {
        continue
    }

    // Stop when footer starts
    val matchedFooter =
    footerKeywords.firstOrNull {
        upper.contains(it)
    }

if (matchedFooter != null) {

    ParserDiagnosticsManager.latest =
        ParserDiagnosticsManager.latest.copy(

            stopReason =
                "Stopped by footer [$matchedFooter]\nLine: $line"

        )

    break
}

    transactionLines.add(line)
        }

        val blocks = mutableListOf<TransactionBlock>()

        var current = mutableListOf<String>()

        for (line in transactionLines) {

    // Ignore table header
    if (line.uppercase().startsWith("DATE TRANSACTION")) {
        continue
    }

    val hasDateAnywhere =
    Regex("\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}")
        .containsMatchIn(line)

val startsWithDate =
    dateRegex.containsMatchIn(line)

if (hasDateAnywhere && !startsWithDate) {

    missedDateLines.add(line)

}

if (startsWithDate) {

    if (current.isNotEmpty()) {

        blocks.add(
            TransactionBlock(current.toList())
        )

        current.clear()
    }
}

current.add(line)
        }

        //updated below code with parser diagnosis
if (current.isNotEmpty()) {

    blocks.add(
        TransactionBlock(current.toList())
    )
}

ParserDiagnosticsManager.latest =
    ParserDiagnosticsManager.latest.copy(

        blocksBuilt = blocks.size,

        rejectedBlocks =
            maxOf(
                0,
                ParserDiagnosticsManager.latest.datesDetected - blocks.size
            ),

        missedDateLines = missedDateLines.take(10)

    )

return blocks

        if (ParserDiagnosticsManager.latest.stopReason == "Not Stopped") {

    ParserDiagnosticsManager.latest =
        ParserDiagnosticsManager.latest.copy(

            stopReason =
                "Reached end of normalized text normally"

        )
        }
    }
}
