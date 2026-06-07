# Data Context — Repositorios, DAOs, y migrations

**Para**: Agentes/asistentes trabajando en Room, repositorios, y data layer
**Última actualización**: 2026-06-06

---

## 🎯 Principios de data layer

### 1. Room es source of truth
- Todas las queries leen de Room, no de network
- Network solo se usa para sync en background
- Repositories retornan `Flow<T>` para reactive UI

### 2. Offline-first siempre
- Writes van directo a Room con `isSynced = false`
- WorkManager sincroniza en background
- UI nunca espera network

### 3. Inmutabilidad
- Entities son data classes inmutables
- Updates crean nuevas instancias con `copy()`

---

## 🗄️ DAO patterns

### Basic CRUD

```kotlin
@Dao
interface AthleteDao {
    // Read (Flow para reactive UI)
    @Query("SELECT * FROM athletes WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAll(): Flow<List<AthleteEntity>>

    @Query("SELECT * FROM athletes WHERE id = :id AND isDeleted = 0")
    fun getById(id: String): Flow<AthleteEntity?>

    @Query("SELECT * FROM athletes WHERE id = :id AND isDeleted = 0")
    suspend fun getByIdOnce(id: String): AthleteEntity?

    // Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(athlete: AthleteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(athletes: List<AthleteEntity>)

    // Update
    @Update
    suspend fun update(athlete: AthleteEntity)

    // Soft delete (preferred en offline-first)
    @Query("""
        UPDATE athletes
        SET isDeleted = 1, updatedAt = :timestamp, isSynced = 0
        WHERE id = :id
    """)
    suspend fun softDelete(id: String, timestamp: Instant)

    // Hard delete (solo para testing o cleanup)
    @Delete
    suspend fun delete(athlete: AthleteEntity)
}
```

### Queries complejas

```kotlin
@Dao
interface LapDao {
    // Mejor tiempo de un atleta en una distancia
    @Query("""
        SELECT MIN(timeInSeconds)
        FROM laps
        WHERE athleteId = :athleteId
          AND distance = :distance
          AND isDeleted = 0
    """)
    fun getBestTime(athleteId: String, distance: Int): Flow<Float?>

    // Ranking de velocidad (con JOIN)
    @Query("""
        SELECT
            a.id as athleteId,
            a.name as athleteName,
            MIN(l.timeInSeconds) as bestTime
        FROM athletes a
        INNER JOIN laps l ON a.id = l.athleteId
        WHERE l.distance = :distance
          AND a.isDeleted = 0
          AND l.isDeleted = 0
        GROUP BY a.id
        ORDER BY bestTime ASC
        LIMIT :limit
    """)
    fun getSpeedRanking(distance: Int, limit: Int = 10): Flow<List<RankingItemEntity>>

    // Historial de vueltas de un atleta
    @Query("""
        SELECT * FROM laps
        WHERE athleteId = :athleteId
          AND isDeleted = 0
        ORDER BY timestamp DESC
    """)
    fun getAthleteHistory(athleteId: String): Flow<List<LapEntity>>
}

data class RankingItemEntity(
    val athleteId: String,
    val athleteName: String,
    val bestTime: Float
)
```

### Relaciones (1:N)

```kotlin
@Dao
interface PhysicalMetricDao {
    // Métricas de un atleta (1:N)
    @Transaction
    @Query("""
        SELECT * FROM athletes
        WHERE id = :athleteId AND isDeleted = 0
    """)
    fun getAthleteWithMetrics(athleteId: String): Flow<AthleteWithMetrics?>

    // Última métrica de cada atleta
    @Query("""
        SELECT pm.*
        FROM physical_metrics pm
        INNER JOIN (
            SELECT athleteId, MAX(measuredAt) as maxDate
            FROM physical_metrics
            WHERE isDeleted = 0
            GROUP BY athleteId
        ) latest ON pm.athleteId = latest.athleteId AND pm.measuredAt = latest.maxDate
        WHERE pm.isDeleted = 0
    """)
    fun getLatestMetrics(): Flow<List<PhysicalMetricEntity>>
}

data class AthleteWithMetrics(
    @Embedded val athlete: AthleteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "athleteId"
    )
    val metrics: List<PhysicalMetricEntity>
)
```

### Relaciones (N:M)

```kotlin
@Dao
interface SessionDao {
    // Sesión con atletas (N:M via join table)
    @Transaction
    @Query("""
        SELECT * FROM sessions
        WHERE id = :sessionId AND isDeleted = 0
    """)
    fun getSessionWithAthletes(sessionId: String): Flow<SessionWithAthletes?>

    // Sesiones de un atleta
    @Transaction
    @Query("""
        SELECT s.* FROM sessions s
        INNER JOIN session_athletes sa ON s.id = sa.sessionId
        WHERE sa.athleteId = :athleteId
          AND s.isDeleted = 0
        ORDER BY s.date DESC
    """)
    fun getAthleteSessions(athleteId: String): Flow<List<SessionEntity>>

    // Insertar sesión con atletas
    @Transaction
    suspend fun insertSessionWithAthletes(
        session: SessionEntity,
        athleteIds: List<String>
    ) {
        insert(session)
        athleteIds.forEach { athleteId ->
            insertSessionAthlete(SessionAthleteEntity(session.id, athleteId))
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSessionAthlete(sessionAthlete: SessionAthleteEntity)
}

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
```

### Sync queries

```kotlin
@Dao
interface AthleteDao {
    // Registros pendientes de sincronización
    @Query("""
        SELECT * FROM athletes
        WHERE isSynced = 0 AND isDeleted = 0
        ORDER BY createdAt ASC
    """)
    suspend fun getUnsyncedAthletes(): List<AthleteEntity>

    // Marcar como sincronizado
    @Query("""
        UPDATE athletes
        SET isSynced = 1
        WHERE id = :id
    """)
    suspend fun markAsSynced(id: String)

    // Batch update
    @Query("""
        UPDATE athletes
        SET isSynced = 1
        WHERE id IN (:ids)
    """)
    suspend fun markAsSynced(ids: List<String>)
}
```

---

## 📦 Repository patterns

### Basic repository

```kotlin
class AthleteRepository @Inject constructor(
    private val athleteDao: AthleteDao,
    private val supabaseClient: SupabaseClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    // Read (Flow para reactive UI)
    fun getAthletes(): Flow<List<Athlete>> =
        athleteDao.getAll()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    fun getAthleteById(id: String): Flow<Athlete?> =
        athleteDao.getById(id)
            .map { it?.toDomain() }
            .flowOn(ioDispatcher)

    // Create (offline-first)
    suspend fun createAthlete(athlete: Athlete): Result<Unit> = withContext(ioDispatcher) {
        try {
            val entity = athlete.toEntity().copy(
                isSynced = false,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now()
            )
            athleteDao.insert(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update (offline-first)
    suspend fun updateAthlete(athlete: Athlete): Result<Unit> = withContext(ioDispatcher) {
        try {
            val entity = athlete.toEntity().copy(
                isSynced = false,
                updatedAt = Clock.System.now()
            )
            athleteDao.update(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete (soft delete)
    suspend fun deleteAthlete(id: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            athleteDao.softDelete(id, Clock.System.now())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sync (llamado por WorkManager)
    suspend fun syncAthletes(): Result<Int> = withContext(ioDispatcher) {
        try {
            val unsyncedAthletes = athleteDao.getUnsyncedAthletes()
            var syncedCount = 0

            unsyncedAthletes.forEach { athlete ->
                try {
                    // Enviar a Supabase
                    supabaseClient.upsertAthlete(athlete.toDTO())
                    // Marcar como sincronizado
                    athleteDao.markAsSynced(athlete.id)
                    syncedCount++
                } catch (e: Exception) {
                    // Log error pero continúa con el siguiente
                    // SyncWorker reintentará en el próximo ciclo
                }
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Repository con relaciones

```kotlin
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val shotDao: ShotDao,
    private val lapDao: LapDao,
    private val supabaseClient: SupabaseClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun getSessionWithDetails(sessionId: String): Flow<SessionDetails?> =
        combine(
            sessionDao.getSessionWithAthletes(sessionId),
            shotDao.getShotsBySession(sessionId),
            lapDao.getLapsBySession(sessionId)
        ) { session, shots, laps ->
            session?.let {
                SessionDetails(
                    session = it.session.toDomain(),
                    athletes = it.athletes.map { athlete -> athlete.toDomain() },
                    shots = shots.map { shot -> shot.toDomain() },
                    laps = laps.map { lap -> lap.toDomain() }
                )
            }
        }.flowOn(ioDispatcher)

    suspend fun createSession(
        session: Session,
        athleteIds: List<String>
    ): Result<String> = withContext(ioDispatcher) {
        try {
            val sessionEntity = session.toEntity().copy(
                isSynced = false,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now()
            )

            sessionDao.insertSessionWithAthletes(sessionEntity, athleteIds)

            Result.success(sessionEntity.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closeSession(sessionId: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            val session = sessionDao.getByIdOnce(sessionId) ?: return@withContext Result.failure(
                Exception("Session not found")
            )

            val closedSession = session.copy(
                status = "COMPLETED",
                completedAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
                isSynced = false
            )

            sessionDao.update(closedSession)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class SessionDetails(
    val session: Session,
    val athletes: List<Athlete>,
    val shots: List<Shot>,
    val laps: List<Lap>
)
```

---

## 🔄 Mappers (Entity ↔ Domain)

```kotlin
// AthleteEntity → Athlete (domain)
fun AthleteEntity.toDomain(): Athlete = Athlete(
    id = id,
    name = name,
    dateOfBirth = dateOfBirth,
    sport = sport,
    position = position,
    gender = gender,
    jerseyNumber = jerseyNumber,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Athlete (domain) → AthleteEntity
fun Athlete.toEntity(): AthleteEntity = AthleteEntity(
    id = id,
    name = name,
    dateOfBirth = dateOfBirth,
    sport = sport,
    position = position,
    gender = gender,
    jerseyNumber = jerseyNumber,
    isSynced = false, // Default para nuevos
    isDeleted = false,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// AthleteEntity → AthleteDTO (network)
fun AthleteEntity.toDTO(): AthleteDTO = AthleteDTO(
    id = id,
    name = name,
    dateOfBirth = dateOfBirth.toEpochMilliseconds(),
    sport = sport,
    position = position,
    gender = gender,
    jerseyNumber = jerseyNumber,
    createdAt = createdAt.toEpochMilliseconds(),
    updatedAt = updatedAt.toEpochMilliseconds()
)

// AthleteDTO → AthleteEntity
fun AthleteDTO.toEntity(): AthleteEntity = AthleteEntity(
    id = id,
    name = name,
    dateOfBirth = Instant.fromEpochMilliseconds(dateOfBirth),
    sport = sport,
    position = position,
    gender = gender,
    jerseyNumber = jerseyNumber,
    isSynced = true, // Viene del servidor, ya está sincronizado
    isDeleted = false,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    updatedAt = Instant.fromEpochMilliseconds(updatedAt)
)
```

---

## 🗃️ Database migrations

### Migration strategy

```kotlin
// core-database/migration/Migrations.kt

object AthleedMigrations {
    // Migration 1 → 2: Agregar campo jerseyNumber
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                ALTER TABLE athletes
                ADD COLUMN jerseyNumber INTEGER DEFAULT NULL
            """)
        }
    }

    // Migration 2 → 3: Agregar tabla match_events
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS match_events (
                    id TEXT PRIMARY KEY NOT NULL,
                    matchId TEXT NOT NULL,
                    athleteId TEXT NOT NULL,
                    eventType TEXT NOT NULL,
                    minute INTEGER NOT NULL,
                    details TEXT,
                    timestamp INTEGER NOT NULL,
                    isSynced INTEGER NOT NULL DEFAULT 0,
                    isDeleted INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(matchId) REFERENCES matches(id) ON DELETE CASCADE,
                    FOREIGN KEY(athleteId) REFERENCES athletes(id) ON DELETE CASCADE
                )
            """)

            database.execSQL("""
                CREATE INDEX index_match_events_matchId ON match_events(matchId)
            """)

            database.execSQL("""
                CREATE INDEX index_match_events_athleteId ON match_events(athleteId)
            """)
        }
    }

    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3
    )
}
```

### Database provider

```kotlin
// core-database/di/DatabaseModule.kt

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAthleedDatabase(
        @ApplicationContext context: Context
    ): AthleedDatabase {
        return Room.databaseBuilder(
            context,
            AthleedDatabase::class.java,
            "athlead.db"
        )
            .addMigrations(*AthleedMigrations.ALL_MIGRATIONS)
            .fallbackToDestructiveMigration() // Solo en dev, remover en prod
            .build()
    }

    @Provides
    fun provideAthleteDao(database: AthleedDatabase): AthleteDao =
        database.athleteDao()

    @Provides
    fun providePhysicalMetricDao(database: AthleedDatabase): PhysicalMetricDao =
        database.physicalMetricDao()

    // ... otros DAOs
}
```

---

## 🧪 Testing

### DAO tests (in-memory database)

```kotlin
@RunWith(AndroidJUnit4::class)
class AthleteDaoTest {
    private lateinit var database: AthleedDatabase
    private lateinit var athleteDao: AthleteDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AthleedDatabase::class.java
        ).build()
        athleteDao = database.athleteDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAthlete_retrievesAthlete() = runTest {
        val athlete = createTestAthlete(id = "1", name = "John Doe")

        athleteDao.insert(athlete)

        val retrieved = athleteDao.getByIdOnce("1")
        assertThat(retrieved).isEqualTo(athlete)
    }

    @Test
    fun softDelete_hidesAthleteFromQueries() = runTest {
        val athlete = createTestAthlete(id = "1", name = "John Doe")
        athleteDao.insert(athlete)

        athleteDao.softDelete("1", Clock.System.now())

        val retrieved = athleteDao.getByIdOnce("1")
        assertThat(retrieved).isNull()
    }

    @Test
    fun getAll_returnsOnlyNonDeletedAthletes() = runTest {
        athleteDao.insert(createTestAthlete(id = "1", name = "John"))
        athleteDao.insert(createTestAthlete(id = "2", name = "Jane"))
        athleteDao.softDelete("2", Clock.System.now())

        val athletes = athleteDao.getAll().first()

        assertThat(athletes).hasSize(1)
        assertThat(athletes[0].name).isEqualTo("John")
    }

    private fun createTestAthlete(id: String, name: String) = AthleteEntity(
        id = id,
        name = name,
        dateOfBirth = Clock.System.now(),
        sport = "BASKETBALL",
        position = "Point Guard",
        gender = "MALE",
        jerseyNumber = null,
        isSynced = false,
        isDeleted = false,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now()
    )
}
```

### Repository tests (fake DAO)

```kotlin
class AthleteRepositoryTest {
    private lateinit var repository: AthleteRepository
    private lateinit var fakeDao: FakeAthleteDao
    private lateinit var fakeSupabaseClient: FakeSupabaseClient

    @Before
    fun setup() {
        fakeDao = FakeAthleteDao()
        fakeSupabaseClient = FakeSupabaseClient()
        repository = AthleteRepository(fakeDao, fakeSupabaseClient)
    }

    @Test
    fun createAthlete_savesToDaoWithIsSyncedFalse() = runTest {
        val athlete = createTestAthlete(id = "1", name = "John")

        repository.createAthlete(athlete)

        val saved = fakeDao.athletes["1"]
        assertThat(saved?.isSynced).isFalse()
    }

    @Test
    fun syncAthletes_marksAsSyncedAfterSuccess() = runTest {
        fakeDao.athletes["1"] = createTestAthleteEntity(id = "1", isSynced = false)

        repository.syncAthletes()

        val synced = fakeDao.athletes["1"]
        assertThat(synced?.isSynced).isTrue()
    }
}

class FakeAthleteDao : AthleteDao {
    val athletes = mutableMapOf<String, AthleteEntity>()

    override fun getAll(): Flow<List<AthleteEntity>> =
        flow { emit(athletes.values.filter { !it.isDeleted }.toList()) }

    override suspend fun insert(athlete: AthleteEntity) {
        athletes[athlete.id] = athlete
    }

    override suspend fun getUnsyncedAthletes(): List<AthleteEntity> =
        athletes.values.filter { !it.isSynced && !it.isDeleted }

    override suspend fun markAsSynced(id: String) {
        athletes[id] = athletes[id]!!.copy(isSynced = true)
    }

    // ... implementar otros métodos
}
```

---

**Última revisión**: 2026-06-06
**Próxima revisión**: Después de cada migration
