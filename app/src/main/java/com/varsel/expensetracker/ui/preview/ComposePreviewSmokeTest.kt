package com.varsel.expensetracker.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(
    name = "Compose Preview Smoke Test",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun ComposePreviewSmokeTest() {

    MaterialTheme {

        Surface(
            modifier =
                Modifier.fillMaxSize()
        ) {

            Column(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),

                verticalArrangement =
                    Arrangement.Center,

                horizontalAlignment =
                    Alignment.CenterHorizontally

            ) {

                Text(
                    text = "Varsel Compose Preview"
                )

                Text(
                    text = "Preview pipeline is working.",
                    modifier =
                        Modifier.padding(
                            top = 8.dp
                        )
                )
            }
        }
    }
}
