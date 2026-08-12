package com.varsel.expensetracker.parser

import javax.inject.Inject

class TokenNormalizer @Inject constructor() {

    /**
     * Normalizes parser tokens before they are interpreted.
     *
     * Version 1:
     * No modifications yet.
     * This class is introduced so future normalization rules
     * can be added without affecting other parser components.
     */
    fun normalize(
        tokens: List<String>
    ): List<String> {

        return repairBrokenWords(tokens)

    }

    /**
     * Placeholder for OCR/PDF word repair.
     *
     * Current implementation intentionally performs
     * no changes.
     */
    private fun repairBrokenWords(
        tokens: List<String>
    ): List<String> {

        return tokens

    }
}
