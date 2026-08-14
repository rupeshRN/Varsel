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

    //--------------------------------------------------
    // Text normalization
    //--------------------------------------------------

    fun recordNormalization(

        rawLines: Int,

        normalizedLines: Int

    ) {

        ParserDiagnosticsManager.latest =

            ParserDiagnosticsManager.latest.copy(

                rawLines = rawLines,

                normalizedLines = normalizedLines

            )

    }

    //--------------------------------------------------
    // Date detection
    //--------------------------------------------------

    fun recordDates(

        datesDetected: Int

    ) {

        ParserDiagnosticsManager.latest =

            ParserDiagnosticsManager.latest.copy(

                datesDetected = datesDetected

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
