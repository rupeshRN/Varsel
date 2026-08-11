package com.varsel.expensetracker.ui.import_statement

import com.varsel.expensetracker.domain.model.Transaction

data class SelectableTransaction(

    val transaction: Transaction,

    var selected: Boolean = true

)
