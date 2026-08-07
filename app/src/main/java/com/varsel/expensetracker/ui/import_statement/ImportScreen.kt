package com.varsel.expensetracker.ui.import_statement

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ImportScreen(
    viewModel: ImportViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.processSelectedFile(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Statement") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) { Text("Back") }
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
                    Button(onClick = { filePickerLauncher.launch("*/*") }) {
                        Text("Select Statement (PDF/Image)")
                    }
                }
                is ImportUiState.Processing -> {
                    CircularProgressIndicator()
                }
                is ImportUiState.Success -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Successfully parsed ${state.parsedTransactions.size} transactions!")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.confirmAndSaveTransactions(state.parsedTransactions) }) {
                            Text("Save Transactions")
                        }
                    }
                }
                is ImportUiState.Saved -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Transactions saved successfully!")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.resetState() }) {
                            Text("Import Another")
                        }
                    }
                }
                is ImportUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.resetState() }) {
                            Text("Retry")
                        }
                    }
                }
                is ImportUiState.PasswordRequired -> {
                    var passwordInput by remember { mutableStateOf("") }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PDF is password protected.")
                        if (state.isInvalidPasswordError) {
                            Text("Invalid password, please try again.", color = MaterialTheme.colorScheme.error)
                        }
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Enter Password") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.submitPdfPassword(passwordInput) }) {
                            Text("Submit Password")
                        }
                    }
                }
            }
        }
    }
}
