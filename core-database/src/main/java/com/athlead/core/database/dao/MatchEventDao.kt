package com.athlead.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.athlead.core.database.entity.MatchEventEntity
import com.athlead.core.database.model.PlayerMatchStats
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: MatchEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<MatchEventEntity>)

    @Query("""
        SELECT * FROM match_events
        WHERE matchId = :matchId
          AND isDeleted = 0
        ORDER BY minute ASC, timestamp ASC
    """)
    fun getMatchEvents(matchId: String): Flow<List<MatchEventEntity>>

    @Query("""
        SELECT
            SUM(CASE WHEN eventType = 'POINT' THEN 1 ELSE 0 END) as points,
            SUM(CASE WHEN eventType = 'ASSIST' THEN 1 ELSE 0 END) as assists,
            SUM(CASE WHEN eventType = 'REBOUND' THEN 1 ELSE 0 END) as rebounds
        FROM match_events
        WHERE matchId = :matchId
          AND athleteId = :athleteId
          AND isDeleted = 0
    """)
    suspend fun getPlayerStats(matchId: String, athleteId: String): PlayerMatchStats
}
