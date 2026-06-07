# Data Contracts — Entidades Room y relaciones

**Database**: AthleedDatabase (Room)
**Última actualización**: 2026-06-06

---

## 📊 Diagrama de relaciones

```
AthleteEntity
    ├── 1:N → PhysicalMetricEntity
    ├── N:M → SessionEntity (via SessionAthleteEntity)
    ├── 1:N → ShotEntity
    ├── 1:N → LapEntity
    └── N:M → MatchEntity (via MatchAthleteEntity)

SessionEntity
    ├── N:M → AthleteEntity (via SessionAthleteEntity)
    ├── 1:N → ShotEntity
    └── 1:N → LapEntity

MatchEntity
    ├── N:M → AthleteEntity (via MatchAthleteEntity)
    └── 1:N → MatchEventEntity (local-only, NO se sincroniza)
```

---

## 🏃 AthleteEntity

### Schema
```kotlin
@Entity(
    tableName = "athletes",
    indices = [
        Index(value = ["sport"]),
        Index(value = ["isDeleted"]),
        Index(value = ["isSynced"])
    ]
)
data class AthleteEntity(
    @PrimaryKey
    val id: String, // UUID generado localmente

    val name: String,
    val dateOfBirth: Instant, // Kotlinx-datetime

    val sport: String, // "BASKETBALL", "SOCCER", "TRACK", etc.
    val position: String?, // nullable: "Point Guard", "Forward", etc.
    val gender: String, // "MALE", "FEMALE", "OTHER"

    val jerseyNumber: Int?,

    // Flags de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false, // Soft delete

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### Constraints
- `name`: NOT NULL, min 2 caracteres
- `dateOfBirth`: NOT NULL, debe ser en el pasado
- `sport`: NOT NULL, enum validado en app layer
- `gender`: NOT NULL, enum validado en app layer
- `jerseyNumber`: UNIQUE per team (futuro: agregar `teamId`)

### Relaciones
- **1:N con PhysicalMetricEntity**: Un atleta tiene múltiples métricas físicas
- **N:M con SessionEntity**: Un atleta participa en múltiples sesiones
- **1:N con ShotEntity**: Un atleta registra múltiples tiros
- **1:N con LapEntity**: Un atleta registra múltiples vueltas
- **N:M con MatchEntity**: Un atleta juega múltiples partidos

### Sync behavior
- ✅ Se sincroniza con Supabase
- Campo `isSynced` marca si está pendiente de sync
- Soft delete: `isDeleted = true` se sincroniza (no se borra de Supabase)

---

## 📏 PhysicalMetricEntity

### Schema
```kotlin
@Entity(
    tableName = "physical_metrics",
    foreignKeys = [
        ForeignKey(
            entity = AthleteEntity::class,
            parentColumns = ["id"],
            childColumns = ["athleteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["athleteId"]),
        Index(value = ["measuredAt"]),
        Index(value = ["isSynced"])
    ]
)
data class PhysicalMetricEntity(
    @PrimaryKey
    val id: String, // UUID

    val athleteId: String, // Foreign key

    // Métricas
    val weight: Float?, // kg (nullable porque puede que solo se mida una métrica)
    val height: Float?, // cm
    val bodyFatPercentage: Float?, // %

    val measuredAt: Instant, // Cuándo se tomó la medición
    val notes: String?, // Notas del coach

    // Flags de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### Constraints
- `athleteId`: NOT NULL, FK a `athletes.id`
- Al menos una de `weight`, `height`, `bodyFatPercentage` debe ser no-nula
- `measuredAt`: NOT NULL

### Query patterns
```kotlin
// Historial de peso de un atleta
@Query("""
    SELECT * FROM physical_metrics
    WHERE athleteId = :athleteId
      AND weight IS NOT NULL
      AND isDeleted = 0
    ORDER BY measuredAt DESC
""")
fun getWeightHistory(athleteId: String): Flow<List<PhysicalMetricEntity>>

// Última métrica de un atleta
@Query("""
    SELECT * FROM physical_metrics
    WHERE athleteId = :athleteId
      AND isDeleted = 0
    ORDER BY measuredAt DESC
    LIMIT 1
""")
suspend fun getLatestMetric(athleteId: String): PhysicalMetricEntity?
```

### Sync behavior
- ✅ Se sincroniza con Supabase
- Borrado cascada: Si se borra un atleta, se borran sus métricas

---

## 🏀 ShotEntity

### Schema
```kotlin
@Entity(
    tableName = "shots",
    foreignKeys = [
        ForeignKey(
            entity = AthleteEntity::class,
            parentColumns = ["id"],
            childColumns = ["athleteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["athleteId"]),
        Index(value = ["sessionId"]),
        Index(value = ["timestamp"]),
        Index(value = ["isSynced"])
    ]
)
data class ShotEntity(
    @PrimaryKey
    val id: String, // UUID

    val athleteId: String, // Foreign key
    val sessionId: String, // Foreign key

    // Posición del tiro (normalizada 0-1)
    val positionX: Float, // 0 = izquierda, 1 = derecha
    val positionY: Float, // 0 = baseline, 1 = half court

    // Tipo de tiro
    val shotType: String, // "TWO_POINT", "THREE_POINT", "FREE_THROW"
    val isSuccessful: Boolean, // Acierto o fallo

    val timestamp: Instant, // Cuándo se registró el tiro
    val notes: String?, // Notas opcionales

    // Flags de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### Constraints
- `athleteId`, `sessionId`: NOT NULL, FKs
- `positionX`, `positionY`: 0.0 - 1.0 (validado en app layer)
- `shotType`: NOT NULL, enum validado en app layer

### Query patterns
```kotlin
// Tiros de una sesión
@Query("""
    SELECT * FROM shots
    WHERE sessionId = :sessionId
      AND isDeleted = 0
    ORDER BY timestamp ASC
""")
fun getShotsBySession(sessionId: String): Flow<List<ShotEntity>>

// % de acierto de un atleta
@Query("""
    SELECT
        COUNT(*) as total,
        SUM(CASE WHEN isSuccessful = 1 THEN 1 ELSE 0 END) as successful
    FROM shots
    WHERE athleteId = :athleteId
      AND isDeleted = 0
""")
suspend fun getShotStats(athleteId: String): ShotStats
```

### Sync behavior
- ✅ Se sincroniza con Supabase
- Útil para heatmaps y análisis de rendimiento

---

## ⏱️ LapEntity

### Schema
```kotlin
@Entity(
    tableName = "laps",
    foreignKeys = [
        ForeignKey(
            entity = AthleteEntity::class,
            parentColumns = ["id"],
            childColumns = ["athleteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["athleteId"]),
        Index(value = ["sessionId"]),
        Index(value = ["distance"]),
        Index(value = ["timestamp"]),
        Index(value = ["isSynced"])
    ]
)
data class LapEntity(
    @PrimaryKey
    val id: String, // UUID

    val athleteId: String, // Foreign key
    val sessionId: String, // Foreign key

    val distance: Int, // Metros (100, 200, 400, 800, 1500, etc.)
    val timeInSeconds: Float, // Tiempo en segundos (ej: 12.34 para 100m)

    val timestamp: Instant, // Cuándo se registró la vuelta
    val notes: String?, // Notas opcionales

    // Flags de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### Constraints
- `athleteId`, `sessionId`: NOT NULL, FKs
- `distance`: NOT NULL, valores comunes: 100, 200, 400, 800, 1500, 3000, 5000, 10000 (metros)
- `timeInSeconds`: NOT NULL, > 0

### Query patterns
```kotlin
// Mejor tiempo de un atleta en una distancia
@Query("""
    SELECT MIN(timeInSeconds) FROM laps
    WHERE athleteId = :athleteId
      AND distance = :distance
      AND isDeleted = 0
""")
suspend fun getBestTime(athleteId: String, distance: Int): Float?

// Ranking de velocidad (100m)
@Query("""
    SELECT l.athleteId, a.name, MIN(l.timeInSeconds) as bestTime
    FROM laps l
    INNER JOIN athletes a ON l.athleteId = a.id
    WHERE l.distance = 100
      AND l.isDeleted = 0
      AND a.isDeleted = 0
    GROUP BY l.athleteId
    ORDER BY bestTime ASC
    LIMIT :limit
""")
fun getSpeedRanking(limit: Int = 10): Flow<List<RankingItem>>
```

### Sync behavior
- ✅ Se sincroniza con Supabase
- Útil para rankings y análisis de resistencia

---

## 🏋️ SessionEntity

### Schema
```kotlin
@Entity(
    tableName = "sessions",
    indices = [
        Index(value = ["date"]),
        Index(value = ["sessionType"]),
        Index(value = ["status"]),
        Index(value = ["isSynced"])
    ]
)
data class SessionEntity(
    @PrimaryKey
    val id: String, // UUID

    val date: Instant, // Fecha y hora de la sesión
    val sessionType: String, // "CARDIO", "STRENGTH", "TECHNIQUE", "MATCH"
    val status: String, // "ACTIVE", "COMPLETED", "CANCELLED"

    val notes: String?, // Notas del coach
    val completedAt: Instant?, // Timestamp de cierre (null si está activa)

    // Flags de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### Constraints
- `date`: NOT NULL
- `sessionType`: NOT NULL, enum validado en app layer
- `status`: NOT NULL, valores: "ACTIVE", "COMPLETED", "CANCELLED"

### Relaciones N:M con AthleteEntity
```kotlin
@Entity(
    tableName = "session_athletes",
    primaryKeys = ["sessionId", "athleteId"],
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
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
        Index(value = ["sessionId"]),
        Index(value = ["athleteId"])
    ]
)
data class SessionAthleteEntity(
    val sessionId: String,
    val athleteId: String
)
```

### Query patterns
```kotlin
// Sesiones activas
@Query("""
    SELECT * FROM sessions
    WHERE status = 'ACTIVE'
      AND isDeleted = 0
    ORDER BY date DESC
""")
fun getActiveSessions(): Flow<List<SessionEntity>>

// Sesiones con atletas (relación N:M)
@Transaction
@Query("""
    SELECT * FROM sessions
    WHERE id = :sessionId
      AND isDeleted = 0
""")
suspend fun getSessionWithAthletes(sessionId: String): SessionWithAthletes?

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

### Sync behavior
- ✅ Se sincroniza con Supabase
- La tabla `session_athletes` también se sincroniza

---

## 🏆 MatchEntity

### Schema
```kotlin
@Entity(
    tableName = "matches",
    indices = [
        Index(value = ["date"]),
        Index(value = ["status"]),
        Index(value = ["isSynced"])
    ]
)
data class MatchEntity(
    @PrimaryKey
    val id: String, // UUID

    val date: Instant, // Fecha y hora del partido
    val opponent: String, // Nombre del equipo rival
    val location: String, // Ubicación del partido
    val isHomeTeam: Boolean, // True si jugamos en casa

    val status: String, // "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"

    // Resultado final (nullable hasta que se complete)
    val finalScoreHome: Int?,
    val finalScoreAway: Int?,

    val notes: String?, // Notas del coach
    val completedAt: Instant?, // Timestamp de cierre

    // Flags de sincronización
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### Constraints
- `date`: NOT NULL
- `opponent`: NOT NULL, min 2 caracteres
- `status`: NOT NULL, valores: "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"
- `finalScoreHome`, `finalScoreAway`: NULL hasta que `status = COMPLETED`

### Relaciones N:M con AthleteEntity
```kotlin
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
    val isStarter: Boolean = false // Si está en el lineup inicial
)
```

### Query patterns
```kotlin
// Próximos partidos
@Query("""
    SELECT * FROM matches
    WHERE date > :now
      AND status = 'SCHEDULED'
      AND isDeleted = 0
    ORDER BY date ASC
""")
fun getUpcomingMatches(now: Instant): Flow<List<MatchEntity>>

// Partidos con lineup
@Transaction
@Query("""
    SELECT * FROM matches
    WHERE id = :matchId
      AND isDeleted = 0
""")
suspend fun getMatchWithAthletes(matchId: String): MatchWithAthletes?

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
```

### Sync behavior
- ✅ Se sincroniza con Supabase (resultado final)
- ❌ `MatchEventEntity` NO se sincroniza (local-only)

---

## 🎬 MatchEventEntity (local-only)

### Schema
```kotlin
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
    val athleteId: String, // Foreign key (jugador que realizó el evento)

    val eventType: String, // "POINT", "ASSIST", "REBOUND", "FOUL", "SUBSTITUTION", etc.
    val minute: Int, // Minuto del partido

    // Detalles específicos del evento (JSON nullable)
    // Ejemplo: {"pointType": "THREE_POINT"} o {"reboundType": "DEFENSIVE"}
    val details: String?,

    val timestamp: Instant, // Cuándo se registró

    // FLAGS: isSynced SIEMPRE es false (NO se sincroniza en MVP)
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### Constraints
- `matchId`, `athleteId`: NOT NULL, FKs
- `eventType`: NOT NULL, enum validado en app layer
- `minute`: NOT NULL, >= 0

### Event types
- **POINT**: Punto anotado (details: `{"pointType": "TWO_POINT" | "THREE_POINT" | "FREE_THROW"}`)
- **ASSIST**: Asistencia
- **REBOUND**: Rebote (details: `{"reboundType": "OFFENSIVE" | "DEFENSIVE"}`)
- **FOUL**: Falta
- **SUBSTITUTION**: Sustitución (details: `{"outAthleteId": "uuid", "inAthleteId": "uuid"}`)
- **TIMEOUT**: Tiempo muerto
- **TURNOVER**: Pérdida de balón

### Query patterns
```kotlin
// Eventos de un partido (play-by-play)
@Query("""
    SELECT * FROM match_events
    WHERE matchId = :matchId
      AND isDeleted = 0
    ORDER BY minute ASC, timestamp ASC
""")
fun getMatchEvents(matchId: String): Flow<List<MatchEventEntity>>

// Estadísticas de un jugador en un partido
@Query("""
    SELECT
        SUM(CASE WHEN eventType = 'POINT' THEN 1 ELSE 0 END) as points,
        SUM(CASE WHEN eventType = 'ASSIST' THEN 1 ELSE 0 END) as assists,
        SUM(CASE WHEN eventType = 'REBOUND' THEN 1 ELSE 0 END) as rebounds
    FROM match_events
    WHERE matchId = :matchId
      AND athleteId = :athleteId
      AND isDeleted = 0
""")
suspend fun getPlayerStats(matchId: String, athleteId: String): PlayerMatchStats
```

### Sync behavior
- ❌ **NO se sincroniza** con Supabase en el MVP
- `isSynced` siempre es `false` (se ignora en SyncWorker)
- **Razón**: Backend no está preparado para eventos de partido
- **Futuro**: v1.2 incluirá sync cuando el BFF esté listo

---

## 🗄️ Database configuration

### AthleedDatabase
```kotlin
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
}
```

### Type converters
```kotlin
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun toTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }
}
```

---

## 📊 Resumen de sincronización

| Entity                    | Sincroniza | Notas                              |
|---------------------------|------------|------------------------------------|
| AthleteEntity             | ✅ Sí      | Core entity                        |
| PhysicalMetricEntity      | ✅ Sí      | Métricas físicas                   |
| ShotEntity                | ✅ Sí      | Tiros en cancha                    |
| LapEntity                 | ✅ Sí      | Vueltas en pista                   |
| SessionEntity             | ✅ Sí      | Sesiones de entrenamiento          |
| SessionAthleteEntity      | ✅ Sí      | Join table (session-athlete)       |
| MatchEntity               | ✅ Sí      | Partidos (solo resultado final)    |
| MatchAthleteEntity        | ✅ Sí      | Join table (match-athlete)         |
| **MatchEventEntity**      | ❌ NO      | **Local-only en MVP**              |

---

## 🧪 Testing de data layer

### Unit tests para DAOs
```kotlin
@RunWith(AndroidJUnit4::class)
class AthleteDaoTest {
    private lateinit var database: AthleedDatabase
    private lateinit var athleteDao: AthleteDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
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
        val athlete = AthleteEntity(
            id = "1",
            name = "John Doe",
            dateOfBirth = Clock.System.now(),
            sport = "BASKETBALL",
            gender = "MALE",
            isSynced = false,
            isDeleted = false,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )

        athleteDao.insert(athlete)

        val retrieved = athleteDao.getById("1")
        assertThat(retrieved).isEqualTo(athlete)
    }
}
```

---

**Última revisión**: 2026-06-06
**Schema version**: 1
**Próxima revisión**: Después de cada migration
