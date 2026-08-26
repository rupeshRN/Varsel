package com.varsel.expensetracker.domain.usecase

import com.varsel.expensetracker.data.local.entity.FinancialEventAllocationEntity
import com.varsel.expensetracker.data.repository.FinancialEventAllocationRepository
import com.varsel.expensetracker.data.repository.TransactionRepository
import com.varsel.expensetracker.domain.model.FinancialEventAllocation
import com.varsel.expensetracker.domain.model.FinancialEventAllocationResult
import javax.inject.Inject

class CreateFinancialEventAllocationUseCase @Inject constructor(
    private val allocationRepository:
        FinancialEventAllocationRepository,

    private val transactionRepository:
        TransactionRepository
) {

    suspend operator fun invoke(
        transactionId: Long,
        transactionLinkId: String,
        allocatedAmount: Double
    ): FinancialEventAllocationResult {

        if (
            allocatedAmount <= 0.0
        ) {

            return FinancialEventAllocationResult
                .InvalidAmount(
                    message =
                        "Allocated amount must be greater than zero."
                )
        }

        /*
         * We need the original transaction before
         * validating the allocation.
         *
         * IMPORTANT:
         * Use the existing TransactionRepository API
         * rather than introducing another database path.
         */
        val transaction =
            transactionRepository
                .getTransactionById(
                    transactionId
                )
                ?: return FinancialEventAllocationResult
                    .InvalidAmount(
                        message =
                            "Transaction could not be found."
                    )

        val transactionAmount =
            kotlin.math.abs(
                transaction.amount
            )

        val alreadyAllocated =
            allocationRepository
                .getAllocatedAmountForTransaction(
                    transactionId
                )

        val remainingAmount =
            transactionAmount -
                alreadyAllocated

        if (
            allocatedAmount >
            remainingAmount + 0.000001
        ) {

            return FinancialEventAllocationResult
                .ExceedsTransactionAmount(
                    transactionAmount =
                        transactionAmount,

                    alreadyAllocated =
                        alreadyAllocated,

                    requestedAmount =
                        allocatedAmount,

                    remainingAmount =
                        remainingAmount
                )
        }

        val id =
            allocationRepository
                .insertAllocation(
                    transactionId =
                        transactionId,

                    transactionLinkId =
                        transactionLinkId,

                    allocatedAmount =
                        allocatedAmount
                )

        FinancialEventAllocationResult.Success(
            allocation =
                FinancialEventAllocation(
                    id =
                        id,

                    transactionId =
                        transactionId,

                    transactionLinkId =
                        transactionLinkId,

                    allocatedAmount =
                        allocatedAmount,

                    createdAt =
                        System.currentTimeMillis()
                )
        )
    }
}
