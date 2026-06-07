package com.athlead.core.database.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.athlead.core.database.entity.AthleteEntity
import com.athlead.core.database.entity.SessionAthleteEntity
import com.athlead.core.database.entity.SessionEntity

data class SessionWithAthletes(
    @Embedded val session: SessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SessionAthleteEntity::class,
            parentColumn = "sessionId",
            entityColumn = "athleteId"
        )
    )
    val athletes: List<AthleteEntity>
)
