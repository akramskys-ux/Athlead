package com.athlead.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "physical_metrics",
    foreignKeys = [
        ForeignKey(
            entity = AthleteEntity::class,
            parentColumns = ["id"],
            childColumns = ["athleteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["athleteId"]),
        Index(value = ["measuredAt"]),
        Index(value = ["isSynced"]),
        Index(value = ["coachId"])
    ]
)
data class PhysicalMetricEntity(
    @PrimaryKey
    val id: String, // UUID
    val coachId: String, // ID del coach propietario

    val athleteId: String, // Foreign key

    val weight: Float?, // kg
    val height: Float?, // cm
    val bodyFatPercentage: Float?, // %

    val measuredAt: Instant,
    val notes: String?,

    // Flags de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant
)
