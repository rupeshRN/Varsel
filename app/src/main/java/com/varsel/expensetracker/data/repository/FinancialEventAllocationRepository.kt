package com.varsel.expensetracker.data.repository

import com.varsel.expensetracker.data.local.dao.FinancialEventAllocationDao
import com.varsel.expensetracker.data.local.entity.FinancialEventAllocationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialEventAllocationRepository @Inject constructor(
    private val dao: FinancialEventAllocationDao
) {

    fun observeAllAllocations():
        Flow<List<FinancialEventAllocationEntity>> =
        dao.getAllAllocations()

    suspend fun getAllocationsForTransaction(
        transactionId: Long
    ): List<FinancialEventAllocationEntity> =
        dao.getAllocationsForTransaction(
            transactionId
        )

        suspend fun getTransactionIdsForFinancialEvent(
    transactionLinkId: String
): List<Long> =
    dao.getTransactionIdsForFinancialEvent(
        transactionLinkId
    )

    suspend fun getAllocationsForFinancialEvent(
        transactionLinkId: String
    ): List<FinancialEventAllocationEntity> =
        dao.getAllocationsForFinancialEvent(
            transactionLinkId
        )

    suspend fun getAllocatedAmountForTransaction(
        transactionId: Long
    ): Double =
        dao.getAllocatedAmountForTransaction(
            transactionId
        )

    suspend fun insertAllocation(
        transactionId: Long,
        transactionLinkId: String,
        allocatedAmount: Double,
        createdAt: Long = System.currentTimeMillis()
    ): Long {

        require(
            allocatedAmount > 0.0
        ) {
            "Allocated amount must be greater than zero."
        }

        /*
         * Do not allow allocations to exceed the
         * transaction amount.
         *
         * The transaction amount itself will be
         * validated by the caller because this
         * repository intentionally does not own
         * TransactionEntity.
         */
        return dao.insertAllocation(
            FinancialEventAllocationEntity(
                transactionId =
                    transactionId,

                transactionLinkId =
                    transactionLinkId,

                allocatedAmount =
                    allocatedAmount,

                createdAt =
                    createdAt
            )
        )
    }

    suspend fun insertAllocations(
        allocations:
            List<FinancialEventAllocationEntity>
    ) {

        require(
            allocations.all {
                it.allocatedAmount > 0.0
            }
        ) {
            "All allocated amounts must be greater than zero."
        }

        dao.insertAllocations(
            allocations
        )
    }

    suspend fun deleteAllocation(
        allocation:
            FinancialEventAllocationEntity
    ) {
        dao.deleteAllocation(
            allocation
        )
    }

    suspend fun deleteAllocationById(
        allocationId: Long
    ) {
        dao.deleteAllocationById(
            allocationId
        )
    }

    suspend fun deleteAllocationsForTransaction(
        transactionId: Long
    ) {
        dao.deleteAllocationsForTransaction(
            transactionId
        )
    }

    suspend fun deleteAllocationsForFinancialEvent(
        transactionLinkId: String
    ) {
        dao.deleteAllocationsForFinancialEvent(
            transactionLinkId
        )
    }
}
