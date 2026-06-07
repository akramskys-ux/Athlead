package com.athlead.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.athlead.core.database.converter.Converters
import com.athlead.core.database.dao.AthleteDao
import com.athlead.core.database.dao.PhysicalMetricDao
import com.athlead.core.database.dao.ShotDao
import com.athlead.core.database.dao.LapDao
import com.athlead.core.database.dao.SessionDao
import com.athlead.core.database.dao.MatchDao
import com.athlead.core.database.dao.MatchEventDao
import com.athlead.core.database.entity.AthleteEntity
import com.athlead.core.database.entity.PhysicalMetricEntity
import com.athlead.core.database.entity.ShotEntity
import com.athlead.core.database.entity.LapEntity
import com.athlead.core.database.entity.SessionEntity
import com.athlead.core.database.entity.SessionAthleteEntity
import com.athlead.core.database.entity.MatchEntity
import com.athlead.core.database.entity.MatchAthleteEntity
import com.athlead.core.database.entity.MatchEventEntity

@Database(
    entities = [
        AthleteEntity::class,
        PhysicalMetricEntity::class,
        ShotEntity::class,
        LapEntity::class,
        SessionEntity::class,
        SessionAthleteEntity::class,
        MatchEntity::class,
        MatchAthleteEntity::class,
        MatchEventEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AthleedDatabase : RoomDatabase() {
    abstract fun athleteDao(): AthleteDao
    abstract fun physicalMetricDao(): PhysicalMetricDao
    abstract fun shotDao(): ShotDao
    abstract fun lapDao(): LapDao
    abstract fun sessionDao(): SessionDao
    abstract fun matchDao(): MatchDao
    abstract fun matchEventDao(): MatchEventDao
}
