package com.athlead.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "laps",
    foreignKeys = [
        ForeignKey(
            entity = AthleteEntity::class,
            parentColumns = ["id"],
            childColumns = ["athleteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["athleteId"]),
        Index(value = ["sessionId"]),
        Index(value = ["distance"]),
        Index(value = ["timestamp"]),
        Index(value = ["isSynced"]),
        Index(value = ["coachId"])
    ]
)
data class LapEntity(
    @PrimaryKey
    val id: String, // UUID
    val coachId: String, // ID del coach propietario

    val athleteId: String, // Foreign key
    val sessionId: String, // Foreign key

    val distance: Int, // Metros (100, 200, 400, etc.)
    val timeInSeconds: Float, // Tiempo en segundos

    val timestamp: Instant,
    val notes: String?,

    // Flags de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant
)
