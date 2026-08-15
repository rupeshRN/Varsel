package com.varsel.expensetracker.util

import com.varsel.expensetracker.category.CustomRuleEngine
import com.varsel.expensetracker.data.repository.CustomRuleRepository
import com.varsel.expensetracker.developer.ParserDiagnosticsCollector
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.parser.BankDetector
import com.varsel.expensetracker.parser.ReconciliationEngine
import com.varsel.expensetracker.parser.StatementImportResult
import com.varsel.expensetracker.parser.StatementSummaryExtractor
import com.varsel.expensetracker.parser.TextNormalizer
import javax.inject.Inject

/**
 * Central orchestration engine for importing bank statements.
 *
 * This class coordinates the complete import pipeline but intentionally
 * contains very little business logic itself.
 *
 * Pipeline
 * ----------------------------------------------------
 *
 * Raw PDF / OCR Text
 *          │
 *          ▼
 * TextNormalizer
 *          │
 *          ▼
 * Load Learning Cache
 *          │
 *          ▼
 * BankDetector
 *          │
 *          ▼
 * Bank-specific Parser
 *          │
 *          ▼
 * Apply Learning Engine
 *          │
 *          ▼
 * Statement Summary
 *          │
 *          ▼
 * Reconciliation
 *          │
 *          ▼
 * Import Preview
 *
 * Responsibilities
 * ----------------------------------------------------
 * ✓ Load user-learned knowledge once.
 * ✓ Normalize statement text.
 * ✓ Detect the correct bank parser.
 * ✓ Parse transactions.
 * ✓ Apply learned descriptions/categories.
 * ✓ Build statement summary.
 * ✓ Perform reconciliation.
 * ✓ Produce a single StatementImportResult.
 *
 * This class intentionally does NOT:
 * • parse bank formats itself
 * • categorize transactions
 * • maintain learning rules
 * • access Room directly
 * • update UI
 *
 * Every stage is delegated to a dedicated component.
 */
class StatementParserEngine @Inject constructor(

    /**
     * Selects the correct parser implementation
     * based on statement content.
     */
    private val bankDetector: BankDetector,

    /**
     * Cleans raw OCR/PDF text before parsing.
     */
    private val textNormalizer: TextNormalizer,

    /**
     * Extracts opening/closing balances,
     * statement dates and totals.
     */
    private val statementSummaryExtractor: StatementSummaryExtractor,

    /**
     * Verifies parser output against
     * statement summary values.
     */
    private val reconciliationEngine: ReconciliationEngine,

    /**
     * Loads persisted learning rules.
     */
    private val customRuleRepository: CustomRuleRepository,

    /**
     * Performs fast in-memory learned lookups.
     */
    private val customRuleEngine: CustomRuleEngine,

/**
 * Collects parser diagnostics during the import pipeline.
 *
 * This component isolates all developer-only diagnostic updates
 * from the production parsing logic.
 *
 * StatementParserEngine reports parsing events through this
 * collector instead of writing directly to ParserDiagnosticsManager,
 * keeping the parser focused solely on business logic.
 */
    private val diagnosticsCollector: ParserDiagnosticsCollector

) {

    /**
     * Executes the complete import pipeline.
     */
    suspend fun parseStatement(

        rawText: String

    ): StatementImportResult {

        diagnosticsCollector.reset()

        //--------------------------------------------------
        // Stage 1
        //
        // Load learned knowledge once.
        //
        // Every transaction lookup afterwards happens
        // entirely from memory.
        //--------------------------------------------------

        customRuleEngine.loadCache(

            customRuleRepository.loadRuleCache()

        )

        //--------------------------------------------------
        // Stage 2
        //
        // Normalize statement text before parsing.
        //--------------------------------------------------

        val normalizedText =

            textNormalizer.normalize(rawText)

        //--------------------------------------------------
        // Diagnostics
        //--------------------------------------------------

            diagnosticsCollector.recordNormalization(
                
                    rawText,
                
                    normalizedText
                
                )

            diagnosticsCollector.recordDetectedDates(
            
                normalizedText
            
            )

        //--------------------------------------------------
        // Stage 3
        //
        // Extract statement-level metadata.
        //--------------------------------------------------

            val summary =
                statementSummaryExtractor.extract(
                    rawText
                )

        //--------------------------------------------------
        // Stage 4
        //
        // Detect bank and execute the correct parser.
        //--------------------------------------------------

        val parser =

            bankDetector.detect(normalizedText)

        val parsedTransactions =

            parser.parse(normalizedText)

        //--------------------------------------------------
        // Stage 5
        //
        // Apply user-learned description/category.
        //--------------------------------------------------

        val transactions =

            applyLearning(parsedTransactions)

        //--------------------------------------------------
        // Diagnostics
        //--------------------------------------------------

diagnosticsCollector.recordTransactions(

    transactionCount = transactions.size,

    lastTimestamp =

        transactions

            .maxByOrNull {

                it.dateTimestamp

            }

            ?.dateTimestamp

)

        //--------------------------------------------------
        // Stage 6
        //
        // Verify parsed data against statement totals.
        //--------------------------------------------------

        val reconciliation =

            reconciliationEngine.reconcile(

                summary,

                transactions

            )

        //--------------------------------------------------
        // Final result returned to ImportViewModel.
        //--------------------------------------------------

        return StatementImportResult(

            summary = summary,

            reconciliation = reconciliation,

            transactions = transactions

        )

    }

    //--------------------------------------------------
    // Applies learned merchant knowledge.
    //
    // A learned rule can replace:
    // • Display Description
    // • Category
    //
    // Amount, date and reference remain unchanged.
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
