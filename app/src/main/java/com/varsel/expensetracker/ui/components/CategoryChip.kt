package com.varsel.expensetracker.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.varsel.expensetracker.ui.design.CategoryPalette

@Composable
fun CategoryChip(
    category: String,
    modifier: Modifier = Modifier
) {

    val color = categoryColor(category)

    AssistChip(
        onClick = { },
        modifier = modifier,
        label = {
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color
        ),
        border = null
    )
}

private fun categoryColor(
    category: String
): Color {

    return when (category.lowercase()) {

        "food" -> CategoryPalette.Food

        "travel" -> CategoryPalette.Travel

        "shopping" -> CategoryPalette.Shopping

        "fuel" -> CategoryPalette.Fuel

        "medical" -> CategoryPalette.Medical

        "salary" -> CategoryPalette.Salary

        "investment" -> CategoryPalette.Investment

        "bills" -> CategoryPalette.Bills

        "transfer" -> CategoryPalette.Transfer

        else -> CategoryPalette.Uncategorized
    }
}
