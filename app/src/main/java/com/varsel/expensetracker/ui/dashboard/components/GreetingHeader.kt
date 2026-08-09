package com.varsel.expensetracker.ui.dashboard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.varsel.expensetracker.ui.design.AppDimensions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun GreetingHeader(
    modifier: Modifier = Modifier
) {

    val now = LocalDateTime.now()

    val greeting = when (now.hour) {

        in 5..11 -> "Good Morning"

        in 12..16 -> "Good Afternoon"

        in 17..21 -> "Good Evening"

        else -> "Good Night"
    }

    val monthYear = now.format(
        DateTimeFormatter.ofPattern(
            "MMMM yyyy",
            Locale.ENGLISH
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = AppDimensions.ScreenPadding,
                vertical = AppDimensions.MediumSpacing
            )
    ) {

        Text(
            text = greeting,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = monthYear,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
