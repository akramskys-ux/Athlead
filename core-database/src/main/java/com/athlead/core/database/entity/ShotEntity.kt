package com.athlead.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "shots",
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
        Index(value = ["timestamp"]),
        Index(value = ["isSynced"]),
        Index(value = ["coachId"])
    ]
)
data class ShotEntity(
    @PrimaryKey
    val id: String, // UUID
    val coachId: String, // ID del coach propietario

    val athleteId: String, // Foreign key
    val sessionId: String, // Foreign key

    val positionX: Float, // 0 = izquierda, 1 = derecha
    val positionY: Float, // 0 = baseline, 1 = half court

    val shotType: String, // "TWO_POINT", "THREE_POINT", "FREE_THROW"
    val isSuccessful: Boolean,

    val timestamp: Instant,
    val notes: String?,

    // Flags de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant
)
