package com.athlead.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.athlead.core.database.entity.LapEntity
import com.athlead.core.database.model.RankingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface LapDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lap: LapEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(laps: List<LapEntity>)

    @Query("""
        SELECT MIN(timeInSeconds) FROM laps
        WHERE athleteId = :athleteId
          AND distance = :distance
          AND isDeleted = 0
    """)
    suspend fun getBestTime(athleteId: String, distance: Int): Float?

    @Query("""
        SELECT l.athleteId, a.name, MIN(l.timeInSeconds) as bestTime
        FROM laps l
        INNER JOIN athletes a ON l.athleteId = a.id
        WHERE l.distance = 100
          AND l.isDeleted = 0
          AND a.isDeleted = 0
        GROUP BY l.athleteId
        ORDER BY bestTime ASC
        LIMIT :limit
    """)
    fun getSpeedRanking(limit: Int = 10): Flow<List<RankingItem>>
}
