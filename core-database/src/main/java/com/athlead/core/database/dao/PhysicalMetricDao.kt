package com.athlead.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.athlead.core.database.entity.PhysicalMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhysicalMetricDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metric: PhysicalMetricEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(metrics: List<PhysicalMetricEntity>)

    @Query("""
        SELECT * FROM physical_metrics
        WHERE athleteId = :athleteId
          AND weight IS NOT NULL
          AND isDeleted = 0
        ORDER BY measuredAt DESC
    """)
    fun getWeightHistory(athleteId: String): Flow<List<PhysicalMetricEntity>>

    @Query("""
        SELECT * FROM physical_metrics
        WHERE athleteId = :athleteId
          AND isDeleted = 0
        ORDER BY measuredAt DESC
        LIMIT 1
    """)
    suspend fun getLatestMetric(athleteId: String): PhysicalMetricEntity?
}
