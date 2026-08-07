package com.varsel.expensetracker.ui.import_statement

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    viewModel: ImportViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.processSelectedFile(it)
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
                    Button(onClick = { pdfPickerLauncher.launch("application/pdf") }) {
                        Text("Select PDF Statement")
                    }
                }
                is ImportUiState.Loading,
                is ImportUiState.Processing -> {
                    CircularProgressIndicator()
                }
                is ImportUiState.ParsedTransactions -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Parsed ${state.parsedTransactions.size} Transactions",
                            style = MaterialTheme.typography.titleMedium
                        )
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.parsedTransactions) { transaction ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = transaction.description, style = MaterialTheme.typography.bodyLarge)
                                        Text(text = "Amount: ${transaction.amount} (${transaction.type})", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                        Button(
                            onClick = { viewModel.confirmAndSaveTransactions(state.parsedTransactions) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirm and Save")
                        }
                    }
                }
                is ImportUiState.Saved -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Transactions saved successfully!", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = { viewModel.resetState() }) {
                            Text("Import Another")
                        }
                    }
                }
                is ImportUiState.PasswordRequired -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("PDF is password protected", style = MaterialTheme.typography.titleMedium)
                        if (state.isInvalidPasswordError) {
                            Text("Incorrect password, please try again", color = MaterialTheme.colorScheme.error)
                        }
                        Button(onClick = { viewModel.resetState() }) {
                            Text("Cancel")
                        }
                    }
                }
                is ImportUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.resetState() }) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
   
    }
}
