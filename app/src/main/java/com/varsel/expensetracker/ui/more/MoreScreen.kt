package com.varsel.expensetracker.ui.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MoreScreen(

    onImportClick: () -> Unit,

    onCategoriesClick: () -> Unit,

    onLearningRulesClick: () -> Unit,

    onAppearanceClick: () -> Unit,

    onDeveloperClick: () -> Unit,

    onAboutClick: () -> Unit

) {

    LazyColumn(

        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),

        verticalArrangement = Arrangement.spacedBy(4.dp)

    ) {

        item {

            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {

                Text(
                    text = "More",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        item {

            MoreMenuItem(
                icon = Icons.Outlined.UploadFile,
                title = "Import Statement",
                subtitle = "Import PDF bank statements",
                onClick = onImportClick
            )
        }

        item {

            MoreMenuItem(
                icon = Icons.Outlined.Category,
                title = "Categories",
                subtitle = "Manage spending categories",
                onClick = onCategoriesClick
            )
        }

        item {

            MoreMenuItem(
                icon = Icons.Outlined.AutoAwesome,
                title = "Learning Rules",
                subtitle = "View AI categorization rules",
                onClick = onLearningRulesClick
            )
        }

        item {

            MoreMenuItem(
                icon = Icons.Outlined.Palette,
                title = "Appearance",
                subtitle = "Theme & display preferences",
                onClick = onAppearanceClick
            )
        }

        item {

            MoreMenuItem(
                icon = Icons.Outlined.Code,
                title = "Developer",
                subtitle = "Parser tools & diagnostics",
                onClick = onDeveloperClick
            )
        }

        item {

            MoreMenuItem(
                icon = Icons.Outlined.Info,
                title = "About",
                subtitle = "About Varsel",
                onClick = onAboutClick
            )
        }

        item {

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Varsel  •  Version 1.0.0",
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
