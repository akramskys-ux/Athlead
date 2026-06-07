package com.athlead.core.database.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.athlead.core.database.entity.AthleteEntity
import com.athlead.core.database.entity.MatchAthleteEntity
import com.athlead.core.database.entity.MatchEntity

data class MatchWithAthletes(
    @Embedded val match: MatchEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MatchAthleteEntity::class,
            parentColumn = "matchId",
            entityColumn = "athleteId"
        )
    )
    val athletes: List<AthleteEntity>
)
