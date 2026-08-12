package com.varsel.expensetracker.parser

import javax.inject.Inject

class TokenNormalizer @Inject constructor() {

    fun normalize(
        tokens: List<String>
    ): List<String> {

        return tokens.map {

            repairHyphenArtifacts(it)

        }
    }

    //----------------------------------------------------
    // Layout repair only
    //----------------------------------------------------

    private fun repairHyphenArtifacts(
        token: String
    ): String {

        var text = token.trim()

        //------------------------------------------------
        // Remove spaces around layout hyphens
        //
        // Example:
        // AJAY - SINGH
        // Indian - Railways
        //------------------------------------------------

        text = text.replace(
            Regex("\\s+-\\s+"),
            " "
        )

        //------------------------------------------------
        // Special case:
        //
        // WAHEGUR - U PETROLEUM
        //
        // becomes
        //
        // WAHEGURU PETROLEUM
        //------------------------------------------------

        text = text.replace(
            Regex("([A-Za-z]{4,})\\s+([A-Z])\\s+"),
            "$1$2 "
        )

        //------------------------------------------------

        return text
            .replace(
                Regex("\\s+"),
                " "
            )
            .trim()
    }
}
