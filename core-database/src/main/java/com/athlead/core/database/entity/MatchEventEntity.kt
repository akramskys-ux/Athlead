package com.athlead.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "match_events",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AthleteEntity::class,
            parentColumns = ["id"],
            childColumns = ["athleteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["matchId"]),
        Index(value = ["athleteId"]),
        Index(value = ["timestamp"])
    ]
)
data class MatchEventEntity(
    @PrimaryKey
    val id: String, // UUID

    val matchId: String, // Foreign key
    val athleteId: String, // Foreign key

    val eventType: String, // "POINT", "ASSIST", "REBOUND", "FOUL", "SUBSTITUTION", etc.
    val minute: Int,

    val details: String?, // JSON string

    val timestamp: Instant,

    // Flags: isSynced always false in MVP
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant
)
