package com.athlead.core.database.di

import android.content.Context
import androidx.room.Room
import com.athlead.core.database.AthleedDatabase
import com.athlead.core.database.dao.AthleteDao
import com.athlead.core.database.dao.LapDao
import com.athlead.core.database.dao.MatchDao
import com.athlead.core.database.dao.MatchEventDao
import com.athlead.core.database.dao.PhysicalMetricDao
import com.athlead.core.database.dao.SessionDao
import com.athlead.core.database.dao.ShotDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAthleedDatabase(
        @ApplicationContext context: Context
    ): AthleedDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AthleedDatabase::class.java,
            "athlead_database.db"
        ).build()
    }

    @Provides
    fun provideAthleteDao(database: AthleedDatabase): AthleteDao {
        return database.athleteDao()
    }

    @Provides
    fun providePhysicalMetricDao(database: AthleedDatabase): PhysicalMetricDao {
        return database.physicalMetricDao()
    }

    @Provides
    fun provideShotDao(database: AthleedDatabase): ShotDao {
        return database.shotDao()
    }

    @Provides
    fun provideLapDao(database: AthleedDatabase): LapDao {
        return database.lapDao()
    }

    @Provides
    fun provideSessionDao(database: AthleedDatabase): SessionDao {
        return database.sessionDao()
    }

    @Provides
    fun provideMatchDao(database: AthleedDatabase): MatchDao {
        return database.matchDao()
    }

    @Provides
    fun provideMatchEventDao(database: AthleedDatabase): MatchEventDao {
        return database.matchEventDao()
    }
}
