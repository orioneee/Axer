@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.orioneee.processors

import io.github.orioneee.domain.requests.Transaction
import io.github.orioneee.extentions.byteSize
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.room.dao.RequestDao


internal class RequestProcessor(
    private val requestMaxAge: Long,
    private val maxTotalRequestSize: Long
) {
    private val dao: RequestDao by IsolatedContext.koin.inject()

    suspend fun deleteRequestIfNotFiltred(id: Long) {
        dao.deleteById(id)
    }

    suspend fun onSend(request: Transaction): Long {
        dao.deleteAllWhichOlderThan(requestMaxAge)
        dao.trimToMaxSize(maxTotalRequestSize)
        val id = dao.upsert(request)
        val firstFive = dao.getFirstFiveNotReaded()
        updateNotification(firstFive)
        return id
    }

    suspend fun onFailed(request: Transaction) {
        val requestWithSize = request.copy(size = request.byteSize())
        dao.upsert(requestWithSize)
        val firstFive = dao.getFirstFiveNotReaded()
        updateNotification(firstFive)
    }

    suspend fun onFinished(request: Transaction) {
        val requestWithSize = request.copy(size = request.byteSize())
        dao.upsert(requestWithSize)
        val firstFive = dao.getFirstFiveNotReaded()
        updateNotification(firstFive)
    }
}

internal expect suspend fun updateNotification(requests: List<Transaction>)