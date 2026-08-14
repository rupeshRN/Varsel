package com.varsel.expensetracker.util

import com.varsel.expensetracker.category.CustomRuleEngine
import com.varsel.expensetracker.data.repository.CustomRuleRepository
import com.varsel.expensetracker.developer.ParserDiagnosticsManager
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.parser.BankDetector
import com.varsel.expensetracker.parser.ReconciliationEngine
import com.varsel.expensetracker.parser.StatementImportResult
import com.varsel.expensetracker.parser.StatementSummaryExtractor
import com.varsel.expensetracker.parser.TextNormalizer
import javax.inject.Inject

class StatementParserEngine @Inject constructor(

    private val bankDetector: BankDetector,

    private val textNormalizer: TextNormalizer,

    private val statementSummaryExtractor: StatementSummaryExtractor,

    private val reconciliationEngine: ReconciliationEngine,

    private val customRuleRepository: CustomRuleRepository,

    private val customRuleEngine: CustomRuleEngine

) {

    suspend fun parseStatement(

        rawText: String

    ): StatementImportResult {

        ParserDiagnosticsManager.reset()

        //--------------------------------------------------
        // Load learned rules once
        //--------------------------------------------------

        customRuleEngine.loadCache(

            customRuleRepository.loadRuleCache()

        )

        val normalizedText =

            textNormalizer.normalize(rawText)

        //--------------------------------------------------
        // Diagnostics
        //--------------------------------------------------

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

        val dateRegex =

            Regex("\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}")

        val detectedDates =

            dateRegex
                .findAll(normalizedText)
                .count()

        ParserDiagnosticsManager.latest =

            ParserDiagnosticsManager.latest.copy(

                datesDetected = detectedDates

            )

        //--------------------------------------------------
        // Summary
        //--------------------------------------------------

        val summary =

            statementSummaryExtractor.extract(

                normalizedText

            )

        //--------------------------------------------------
        // Bank parser
        //--------------------------------------------------

        val parser =

            bankDetector.detect(normalizedText)

        val parsedTransactions =

            parser.parse(normalizedText)

        //--------------------------------------------------
        // Apply learned knowledge
        //--------------------------------------------------

        val transactions =

            applyLearning(parsedTransactions)

        //--------------------------------------------------
        // Diagnostics
        //--------------------------------------------------

        ParserDiagnosticsManager.latest =

            ParserDiagnosticsManager.latest.copy(

                transactionsParsed =

                    transactions.size,

                lastParsedDate =

                    transactions

                        .maxByOrNull {

                            it.dateTimestamp

                        }

                        ?.let {

                            java.text.SimpleDateFormat(

                                "dd MMM yyyy",

                                java.util.Locale.ENGLISH

                            ).format(

                                java.util.Date(

                                    it.dateTimestamp

                                )

                            )

                        }

                        ?: "—"

            )

        //--------------------------------------------------
        // Reconciliation
        //--------------------------------------------------

        val reconciliation =

            reconciliationEngine.reconcile(

                summary,

                transactions

            )

        //--------------------------------------------------

        return StatementImportResult(

            summary = summary,

            reconciliation = reconciliation,

            transactions = transactions

        )

    }

    //--------------------------------------------------
    // Apply learned knowledge
    //--------------------------------------------------

    private fun applyLearning(

        transactions: List<Transaction>

    ): List<Transaction> {

        return transactions.map { transaction ->

            val knowledge =

                customRuleEngine.findKnowledge(

                    transaction.description

                )

            if (knowledge == null) {

                transaction

            } else {

                transaction.copy(

                    description =

                        knowledge.displayDescription,

                    category =

                        knowledge.categoryName

                )

            }

        }

    }

}
