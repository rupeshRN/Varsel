package com.varsel.expensetracker.ui.transaction.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.varsel.expensetracker.category.Category

@Composable
fun CategorySection(

    selectedCategory: Category,

    onCategorySelected: (Category) -> Unit

) {

    Text(

        text = "Category",

        style = MaterialTheme.typography.titleMedium,

        modifier = Modifier.padding(bottom = 8.dp)

    )

    val rows = listOf(

        listOf(
            Category.FOOD,
            Category.TRAVEL,
            Category.FUEL
        ),

        listOf(
            Category.GROCERIES,
            Category.BILLS,
            Category.HEALTHCARE
        ),

        listOf(
            Category.ENTERTAINMENT,
            Category.MOBILE,
            null        // Placeholder for + New Category
        )

    )

    Column(

        verticalArrangement = Arrangement.spacedBy(8.dp)

    ) {

        rows.forEach { row ->

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)

            ) {

                row.forEach { category ->

                    if (category != null) {

                        CategoryCard(

                            modifier = Modifier.weight(1f),

                            category = category,

                            selected =
                                category == selectedCategory,

                            onClick = {

                                onCategorySelected(category)

                            }

                        )

                    } else {

                        NewCategoryCard(

                            modifier = Modifier.weight(1f)

                        )

                    }

                }

            }

        }

    }

}
