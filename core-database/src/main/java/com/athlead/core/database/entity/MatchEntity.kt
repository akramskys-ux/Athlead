package com.athlead.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "matches",
    indices = [
        Index(value = ["date"]),
        Index(value = ["status"]),
        Index(value = ["isSynced"]),
        Index(value = ["coachId"])
    ]
)
data class MatchEntity(
    @PrimaryKey
    val id: String, // UUID
    val coachId: String, // ID del coach propietario

    val date: Instant,
    val opponent: String,
    val location: String,
    val isHomeTeam: Boolean,

    val status: String, // "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"

    val finalScoreHome: Int?,
    val finalScoreAway: Int?,

    val notes: String?,
    val completedAt: Instant?,

    // Flags de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant
)
