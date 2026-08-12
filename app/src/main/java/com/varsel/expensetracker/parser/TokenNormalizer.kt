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

    val repaired = mutableListOf<String>()

    var index = 0

    while (index < tokens.size) {

        var current =
            tokens[index].trim()

        if (
            index < tokens.lastIndex &&
            current.length >= 4
        ) {

            val next =
                tokens[index + 1].trim()

            val match =
                Regex("^([A-Za-z])\\s+(.+)$")
                    .find(next)

            if (match != null &&
                !startsWithReservedWord(next)
            ) {

                current += match.groupValues[1]

                repaired.add(current)

                repaired.add(
                    match.groupValues[2]
                )

                index += 2
                continue
            }

            if (
                next.matches(
                    Regex("^[A-Za-z]$")
                )
            ) {

                repaired.add(
                    current + next
                )

                index += 2
                continue
            }
        }

        repaired.add(current)

        index++
    }

    return repaired
}

private fun startsWithReservedWord(
    token: String
): Boolean {

    val upper =
        token.uppercase()

    return upper.startsWith("UPI") ||
           upper.startsWith("IMPS") ||
           upper.startsWith("NEFT") ||
           upper.startsWith("RTGS") ||
           upper.startsWith("BRANCH")
}
}
