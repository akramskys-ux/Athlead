package com.athlead.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.athlead.core.database.entity.SessionAthleteEntity
import com.athlead.core.database.entity.SessionEntity
import com.athlead.core.database.relation.SessionWithAthletes
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<SessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionAthlete(association: SessionAthleteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionAthletes(associations: List<SessionAthleteEntity>)

    @Query("DELETE FROM session_athletes WHERE sessionId = :sessionId AND athleteId = :athleteId")
    suspend fun removeAthleteFromSession(sessionId: String, athleteId: String)

    @Query("DELETE FROM session_athletes WHERE sessionId = :sessionId")
    suspend fun clearSessionAthletes(sessionId: String)

    @Query("""
        SELECT * FROM sessions
        WHERE status = 'ACTIVE'
          AND isDeleted = 0
        ORDER BY date DESC
    """)
    fun getActiveSessions(): Flow<List<SessionEntity>>

    @Transaction
    @Query("""
        SELECT * FROM sessions
        WHERE id = :sessionId
          AND isDeleted = 0
    """)
    suspend fun getSessionWithAthletes(sessionId: String): SessionWithAthletes?
}
