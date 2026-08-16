package com.varsel.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statement_snapshots")
data class StatementSnapshotEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val statementStartDate: Long? = null,

    val statementEndDate: Long? = null,

    val openingBalance: Double? = null,

    val totalCredits: Double? = null,

    val totalDebits: Double? = null,

    val endingBalance: Double? = null,

    /**
     * Time when Varsel successfully processed
     * this statement.
     *
     * Used to resolve multiple statements having
     * the same statement end date.
     */
    val importedAt: Long
)
