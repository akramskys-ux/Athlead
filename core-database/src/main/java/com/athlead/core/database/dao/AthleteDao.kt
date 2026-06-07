package com.athlead.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.athlead.core.database.entity.AthleteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface AthleteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(athlete: AthleteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(athletes: List<AthleteEntity>)

    @Query("SELECT * FROM athletes WHERE id = :id AND isDeleted = 0")
    suspend fun getById(id: String): AthleteEntity?

    @Query("SELECT * FROM athletes WHERE coachId = :coachId AND isDeleted = 0")
    fun getAthletesByCoach(coachId: String): Flow<List<AthleteEntity>>

    @Query("UPDATE athletes SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE id = :athleteId")
    suspend fun softDeleteAthlete(athleteId: String, updatedAt: Instant)

    @Query("UPDATE physical_metrics SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE athleteId = :athleteId")
    suspend fun softDeletePhysicalMetricsForAthlete(athleteId: String, updatedAt: Instant)

    @Query("UPDATE shots SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE athleteId = :athleteId")
    suspend fun softDeleteShotsForAthlete(athleteId: String, updatedAt: Instant)

    @Query("UPDATE laps SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE athleteId = :athleteId")
    suspend fun softDeleteLapsForAthlete(athleteId: String, updatedAt: Instant)

    @Transaction
    suspend fun softDeleteAthleteWithChildren(athleteId: String, updatedAt: Instant) {
        softDeleteAthlete(athleteId, updatedAt)
        softDeletePhysicalMetricsForAthlete(athleteId, updatedAt)
        softDeleteShotsForAthlete(athleteId, updatedAt)
        softDeleteLapsForAthlete(athleteId, updatedAt)
    }
}
