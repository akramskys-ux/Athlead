package com.athlead.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.athlead.core.database.entity.MatchAthleteEntity
import com.athlead.core.database.entity.MatchEntity
import com.athlead.core.database.relation.MatchWithAthletes
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface MatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: MatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(matches: List<MatchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchAthlete(association: MatchAthleteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchAthletes(associations: List<MatchAthleteEntity>)

    @Query("DELETE FROM match_athletes WHERE matchId = :matchId AND athleteId = :athleteId")
    suspend fun removeAthleteFromMatch(matchId: String, athleteId: String)

    @Query("DELETE FROM match_athletes WHERE matchId = :matchId")
    suspend fun clearMatchAthletes(matchId: String)

    @Query("""
        SELECT * FROM matches
        WHERE date > :now
          AND status = 'SCHEDULED'
          AND isDeleted = 0
        ORDER BY date ASC
    """)
    fun getUpcomingMatches(now: Instant): Flow<List<MatchEntity>>

    @Transaction
    @Query("""
        SELECT * FROM matches
        WHERE id = :matchId
          AND isDeleted = 0
    """)
    suspend fun getMatchWithAthletes(matchId: String): MatchWithAthletes?
}
