package com.varsel.expensetracker.ui.import_statement

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.varsel.expensetracker.ui.import_statement.components.DeveloperDiagnosticsCard
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBackClick: () -> Unit,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val diagnostics by viewModel.diagnostics.collectAsState()

    val parserDiagnosticsEnabled by
    viewModel.parserDiagnosticsEnabled.collectAsState()
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.processSelectedFile(it, null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Statement") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ImportUiState.Idle -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                launcher.launch(arrayOf("application/pdf", "image/*"))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Upload Icon",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Statement (PDF or Image)")
                        }
                    }
                }
                is ImportUiState.Loading, is ImportUiState.Processing -> {
                    CircularProgressIndicator()
                }
                is ImportUiState.ParsedTransactions -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(state.parsedTransactions) { selectable ->
Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
) {
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
        .clickable {
            selectable.selected = !selectable.selected
        },
    verticalAlignment = Alignment.CenterVertically
) {

Checkbox(
    checked = selectable.selected,
    onCheckedChange = { checked ->
        selectable.selected = checked
    }
)

    Spacer(modifier = Modifier.width(12.dp))

    Column(
        modifier = Modifier.weight(1f)
    ) {

        Text(
            text = selectable.transaction.description,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Amount: ${selectable.transaction.amount} (${selectable.transaction.type})",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
}
                        }
                        }

Spacer(modifier = Modifier.height(12.dp))

DeveloperDiagnosticsCard(

    enabled = parserDiagnosticsEnabled,

    diagnostics = diagnostics

)

Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.confirmAndSaveTransactions(state.parsedTransactions)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text("Confirm & Save (${state.parsedTransactions.size} Transactions)")
                        }
                    }
                }
                is ImportUiState.Saved -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Successfully saved ${state.count} transactions!",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.resetState() }) {
                            Text("Import Another Statement")
                        }
                    }
                }
                is ImportUiState.Error -> 
                    {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.resetState() }) {
                            Text("Try Again")
                        }
                    }
                }
                is ImportUiState.PasswordRequired -> 
                    {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Password protected documents are not supported.")
                    }
                }
            }
        }
  
    }
}
