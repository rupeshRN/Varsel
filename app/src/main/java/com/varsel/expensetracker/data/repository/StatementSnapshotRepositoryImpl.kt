package com.varsel.expensetracker.data.repository

import com.varsel.expensetracker.data.local.dao.StatementSnapshotDao
import com.varsel.expensetracker.data.local.entity.StatementSnapshotEntity
import com.varsel.expensetracker.domain.repository.StatementSnapshotRepository
import javax.inject.Inject

class StatementSnapshotRepositoryImpl @Inject constructor(
    private val statementSnapshotDao: StatementSnapshotDao
) : StatementSnapshotRepository {

    override suspend fun saveSnapshot(
        snapshot: StatementSnapshotEntity
    ) {
        statementSnapshotDao.insertSnapshot(
            snapshot
        )
    }

    override suspend fun getLatestSnapshot():
        StatementSnapshotEntity? {
        return statementSnapshotDao.getLatestSnapshot()
    }

    override suspend fun getAllSnapshots():
        List<StatementSnapshotEntity> {
        return statementSnapshotDao.getAllSnapshots()
    }
}
