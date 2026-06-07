package com.athlead.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "match_athletes",
    primaryKeys = ["matchId", "athleteId"],
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
        Index(value = ["athleteId"])
    ]
)
data class MatchAthleteEntity(
    val matchId: String,
    val athleteId: String,
    val isStarter: Boolean = false
)
