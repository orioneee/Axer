package io.github.orioneee.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import io.github.orioneee.domain.requests.Transaction
import io.github.orioneee.domain.requests.TrimItem
import io.github.orioneee.logger.formateAsTime
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Dao
internal interface RequestDao {
    @Query("SELECT * FROM Transactions ORDER BY sendTime DESC")
    fun getAll(): Flow<List<Transaction>>


    @Upsert
    suspend fun upsert(request: Transaction): Long

    @Delete
    suspend fun delete(request: Transaction)

    @Query("DELETE FROM Transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM Transactions WHERE isViewed = 0 ORDER BY id DESC LIMIT 5")
    suspend fun getFirstFiveNotReaded(): List<Transaction>


    @Query("DELETE FROM Transactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM Transactions WHERE id = :id")
    fun getById(id: Long?): Flow<Transaction?>

    @Query("UPDATE Transactions SET isViewed = :isViewed WHERE id = :id")
    suspend fun updateViewed(id: Long, isViewed: Boolean)

    @Query("DELETE FROM Transactions WHERE sendTime < :thresholdTime")
    suspend fun deleteOlderThan(thresholdTime: Long)


    @OptIn(ExperimentalTime::class)
    suspend fun deleteAllWhichOlderThan(timestampInSeconds: Long) {
        val miliseconds = timestampInSeconds * 1000
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val thresholdTime = currentTime - miliseconds
        deleteOlderThan(thresholdTime)
    }

    @Query("SELECT id, size, sendTime FROM Transactions ORDER BY sendTime DESC")
    suspend fun getAllForTrim(): List<TrimItem>

    @Query("DELETE FROM Transactions WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)


    suspend fun trimToMaxSize(maxSizeBytes: Long) {
        val rows = getAllForTrim()

        var total = 0L
        val toDelete = mutableListOf<Long>()

        for (row in rows) {
            total += row.size
            if (total > maxSizeBytes) {
                toDelete.add(row.id)
            }
        }
        if (toDelete.isNotEmpty()) {
            deleteByIds(toDelete)
        }
    }
}