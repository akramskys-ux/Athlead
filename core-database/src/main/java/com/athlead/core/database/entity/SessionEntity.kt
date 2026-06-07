package com.athlead.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "sessions",
    indices = [
        Index(value = ["date"]),
        Index(value = ["sessionType"]),
        Index(value = ["status"]),
        Index(value = ["isSynced"]),
        Index(value = ["coachId"])
    ]
)
data class SessionEntity(
    @PrimaryKey
    val id: String, // UUID
    val coachId: String, // ID del coach propietario

    val date: Instant,
    val sessionType: String, // "CARDIO", "STRENGTH", "TECHNIQUE", "MATCH"
    val status: String, // "ACTIVE", "COMPLETED", "CANCELLED"

    val notes: String?,
    val completedAt: Instant?,

    // Flags de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant
)
