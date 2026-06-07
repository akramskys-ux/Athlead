package com.athlead.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.athlead.core.database.entity.ShotEntity
import com.athlead.core.database.model.ShotStats
import kotlinx.coroutines.flow.Flow

@Dao
interface ShotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shot: ShotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shots: List<ShotEntity>)

    @Query("""
        SELECT * FROM shots
        WHERE sessionId = :sessionId
          AND isDeleted = 0
        ORDER BY timestamp ASC
    """)
    fun getShotsBySession(sessionId: String): Flow<List<ShotEntity>>

    @Query("""
        SELECT
            COUNT(*) as total,
            SUM(CASE WHEN isSuccessful = 1 THEN 1 ELSE 0 END) as successful
        FROM shots
        WHERE athleteId = :athleteId
          AND isDeleted = 0
    """)
    suspend fun getShotStats(athleteId: String): ShotStats
}
