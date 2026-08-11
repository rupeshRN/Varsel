package com.varsel.expensetracker.ui.import_statement.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.varsel.expensetracker.developer.ParserDiagnostics

@Composable
fun DeveloperDiagnosticsCard(

    enabled: Boolean,

    diagnostics: ParserDiagnostics

) {

    if (!enabled) return

    var expanded by remember {

        mutableStateOf(false)

    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            TextButton(

                onClick = {

                    expanded = !expanded

                }

            ) {

                Text(

                    if (expanded)
                        "Developer Diagnostics ▼"
                    else
                        "Developer Diagnostics ▶"

                )
            }

            if (!expanded) return@Column

            Spacer(modifier = Modifier.height(8.dp))

            Text("Raw Lines : ${diagnostics.rawLines}")
            Text("Normalized Lines : ${diagnostics.normalizedLines}")

            Spacer(modifier = Modifier.height(8.dp))

            Text("Dates Detected : ${diagnostics.datesDetected}")
            Text("Blocks Built : ${diagnostics.blocksBuilt}")
            Text("Transactions Parsed : ${diagnostics.transactionsParsed}")
            Text("Rejected Blocks : ${diagnostics.rejectedBlocks}")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Last Parsed Date : ${diagnostics.lastParsedDate}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Stop Reason",
                style = MaterialTheme.typography.titleSmall
            )

            Text(diagnostics.stopReason)

            if (diagnostics.missedDateLines.isNotEmpty()) {

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Missed Date Lines",
                    style = MaterialTheme.typography.titleSmall
                )

                diagnostics.missedDateLines.forEach {

                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall
                    )

                }
            }
        }
    }
}
