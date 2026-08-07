package com.varsel.expensetracker.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varsel.expensetracker.data.local.entity.CategoryEntity
import com.varsel.expensetracker.data.local.entity.CustomRuleEntity
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddRuleDialog by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Categories & Rules",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Material You Tab Bar switching between Categories and Custom Rules
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Categories (${uiState.categories.size})") },
                    icon = { Icon(Icons.Default.Category, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Auto-Rules (${uiState.customRules.size})") },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> CategoriesListView(
                    categories = uiState.categories,
                    onDeleteCategory = { viewModel.deleteCategory(it) }
                )

                1 -> RulesListView(
                    rules = uiState.customRules,
                    categories = uiState.categories,
                    onDeleteRule = { viewModel.deleteCustomRule(it) }
                )
            }
        }

        // Floating Action Button to add new items
        FloatingActionButton(
            onClick = {
                if (selectedTabIndex == 0) showAddCategoryDialog = true
                else showAddRuleDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add New")
        }
    }

    // Modal Dialogs
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, budget ->
                viewModel.addCategory(
                    name = name,
                    colorHex = "#3B82F6",
                    iconName = "category",
                    monthlyBudgetLimit = budget
                )
                showAddCategoryDialog = false
            }
        )
    }

    if (showAddRuleDialog) {
        AddRuleDialog(
            categories = uiState.categories,
            onDismiss = { showAddRuleDialog = false },
            onConfirm = { keyword, categoryId ->
                viewModel.addCustomRule(keyword, categoryId)
                showAddRuleDialog = false
            }
        )
    }
}

/**
 * List of configured budget categories with monthly limits.
 */
@Composable
private fun CategoriesListView(
    categories: List<CategoryEntity>,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    if (categories.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No categories defined yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(category.colorHex))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Limit: ${if (category.monthlyBudgetLimit > 0) currencyFormat.format(category.monthlyBudgetLimit) else "No budget set"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { onDeleteCategory(category) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Category",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * List of user-defined merchant auto-categorization override rules.
 */
@Composable
private fun RulesListView(
    rules: List<CustomRuleEntity>,
    categories: List<CategoryEntity>,
    onDeleteRule: (CustomRuleEntity) -> Unit
) {
    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    if (rules.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No custom auto-rules created.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            items(rules, key = { it.id }) { rule ->
                val associatedCategoryName = categoryMap[rule.categoryId]?.name ?: "Unknown"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Keyword: \"${rule.merchantPattern}\"",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Auto-assigns to: $associatedCategoryName",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = { onDeleteRule(rule) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Rule",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog for creating a new budget category.
 */
@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, budget: Double) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var budgetInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text("Monthly Budget Limit (Optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budget = budgetInput.toDoubleOrNull() ?: 0.0
                    onConfirm(nameInput, budget)
                },
                enabled = nameInput.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Dialog for mapping a merchant keyword to a target category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRuleDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (keyword: String, categoryId: Long) -> Unit
) {
    var keywordInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Auto-Categorization Rule") },
        text = {
            Column {
                OutlinedTextField(
                    value = keywordInput,
                    onValueChange = { keywordInput = it },
                    label = { Text("Merchant Keyword (e.g. ZOMATO)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCategory?.let { category ->
                        onConfirm(keywordInput, category.id)
                    }
                },
                enabled = keywordInput.isNotBlank() && selectedCategory != null
            ) {
                Text("Save Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/** Helper to convert hex string color code to Jetpack Compose Color */
private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF3B82F6)
    }
}
