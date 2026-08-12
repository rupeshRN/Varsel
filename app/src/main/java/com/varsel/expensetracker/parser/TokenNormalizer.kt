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
    // Rule 1
    // Remove trailing layout hyphen
    //
    // DECATHLON -
    // ->
    // DECATHLON
    //------------------------------------------------

    text = text.replace(
        Regex("\\s*-\\s*$"),
        ""
    )

    //------------------------------------------------
    // Rule 2
    // Remove layout separator
    //
    // AJAY - SINGH
    // Indian - Railways
    // UPI - RVSL
    //------------------------------------------------

    text = text.replace(
        Regex("\\s+-\\s+"),
        " "
    )

    //------------------------------------------------
    // Rule 3
    // Join broken merchant words
    //
    // WAHEGUR U PETROLEUM
    // ->
    // WAHEGURU PETROLEUM
    //
    // HARIPRAS ATH K
    // ->
    // HARIPRASATH K
    //
    // Only joins if:
    // - left word is reasonably long
    // - middle fragment is short
    //------------------------------------------------

    text = text.replace(

        Regex("\\b([A-Za-z]{5,})\\s+([A-Za-z]{1,3})\\b(?=\\s+[A-Za-z])")

    ) {

        val left = it.groupValues[1]
        val right = it.groupValues[2]

        left + right
    }

    //------------------------------------------------
    // Cleanup
    //------------------------------------------------

    return text
        .replace(
            Regex("\\s+"),
            " "
        )
        .trim()
}
}
