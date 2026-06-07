package com.athlead.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "athletes",
    indices = [
        Index(value = ["sport"]),
        Index(value = ["isDeleted"]),
        Index(value = ["isSynced"]),
        Index(value = ["coachId"])
    ]
)
data class AthleteEntity(
    @PrimaryKey
    val id: String, // UUID generado localmente
    val coachId: String, // ID del coach propietario

    val name: String,
    val dateOfBirth: Instant,

    val sport: String, // "BASKETBALL", "SOCCER", "TRACK", etc.
    val position: String?, // "Point Guard", "Forward", etc.
    val gender: String, // "MALE", "FEMALE", "OTHER"
    val jerseyNumber: Int?,

    // Flags de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false, // Soft delete

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant
)
