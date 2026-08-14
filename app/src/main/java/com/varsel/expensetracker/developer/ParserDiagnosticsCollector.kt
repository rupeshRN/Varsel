package com.varsel.expensetracker.developer

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Collects parser diagnostics.
 *
 * StatementParserEngine should never update
 * ParserDiagnosticsManager directly.
 *
 * All diagnostic updates flow through this class.
 */
@Singleton
class ParserDiagnosticsCollector @Inject constructor() {

    //--------------------------------------------------
    // Reset
    //--------------------------------------------------

    fun reset() {

        ParserDiagnosticsManager.reset()

    }

/**
 * Records normalization statistics directly from raw
 * and normalized statement text.
 */
fun recordNormalization(

    rawText: String,

    normalizedText: String

) {

    val rawLines =

        rawText
            .lines()
            .count { it.isNotBlank() }

    val normalizedLines =

        normalizedText
            .lines()
            .count { it.isNotBlank() }

    ParserDiagnosticsManager.latest =

        ParserDiagnosticsManager.latest.copy(

            rawLines = rawLines,

            normalizedLines = normalizedLines

        )

}

/**
 * Records the number of detected transaction dates.
 */
fun recordDetectedDates(

    normalizedText: String

) {

    val detectedDates =

        Regex("\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}")

            .findAll(normalizedText)

            .count()

    ParserDiagnosticsManager.latest =

        ParserDiagnosticsManager.latest.copy(

            datesDetected = detectedDates

        )

}

    //--------------------------------------------------
    // Parser output
    //--------------------------------------------------

    fun recordTransactions(

        transactionCount: Int,

        lastTimestamp: Long?

    ) {

        ParserDiagnosticsManager.latest =

            ParserDiagnosticsManager.latest.copy(

                transactionsParsed = transactionCount,

                lastParsedDate =

                    lastTimestamp?.let {

                        SimpleDateFormat(

                            "dd MMM yyyy",

                            Locale.ENGLISH

                        ).format(Date(it))

                    } ?: "—"

            )

    }

}
