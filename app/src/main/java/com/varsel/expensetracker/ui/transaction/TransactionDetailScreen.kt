package com.varsel.expensetracker.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(

    transactionId: Long,

    viewModel: TransactionDetailViewModel,

    onBackClick: () -> Unit

) {

    LaunchedEffect(transactionId) {

        viewModel.loadTransaction(
            transactionId
        )

    }

    val uiState by
        viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {

                    Text("Transaction Details")

                },

                navigationIcon = {

                    IconButton(

                        onClick = onBackClick

                    ) {

                        Icon(

                            imageVector = Icons.Default.ArrowBack,

                            contentDescription = "Back"

                        )

                    }

                }

            )

        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {

when (val state = uiState) {

    TransactionDetailUiState.Loading -> {

        Text("Loading...")

    }

    is TransactionDetailUiState.Error -> {

        Text(state.message)

    }

    is TransactionDetailUiState.Loaded -> {

        Text("Transaction Loaded")

        Text(state.transaction.description)

    }

}

        }

    }

}
