package com.varsel.expensetracker.ui.transaction.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.varsel.expensetracker.ui.design.AppDimensions

@Composable
fun TransactionFilterBar(

    filters: List<String>,

    selectedFilter: String,

    onFilterSelected: (String) -> Unit,

    modifier: Modifier = Modifier

) {

    Row(

        modifier = modifier
            .horizontalScroll(rememberScrollState()),

        horizontalArrangement = Arrangement.spacedBy(8.dp)

    ) {

        filters.forEach { filter ->

            FilterChip(

                selected = filter == selectedFilter,

                onClick = {
                    onFilterSelected(filter)
                },

                label = {
                    Text(filter)
                },

                colors = FilterChipDefaults.filterChipColors(

                    selectedContainerColor =
                        MaterialTheme.colorScheme.primary,

                    selectedLabelColor =
                        MaterialTheme.colorScheme.onPrimary

                )

            )
        }
    }
}
