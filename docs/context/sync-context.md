# Sync Context — WorkManager y sincronización

**Para**: Agentes/asistentes trabajando en sincronización background
**Última actualización**: 2026-06-06

---

## 🎯 Filosofía de sync

### Principios
1. **Sync es transparente**: El usuario no debe esperar sync, sucede en background
2. **Local gana**: En conflictos, la versión local sobrescribe la remota
3. **Resiliente**: Si falla, reintenta con backoff exponencial
4. **Battery-friendly**: Solo sincroniza con conexión, respeta Doze mode

---

## ⚙️ WorkManager configuration

### SyncWorker

```kotlin
// sync/SyncWorker.kt

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SyncWorker", "Starting sync...")

            val syncResult = syncRepository.syncAll()

            when {
                syncResult.isSuccess -> {
                    val syncedCount = syncResult.getOrDefault(0)
                    Log.d("SyncWorker", "Sync successful: $syncedCount records")
                    Result.success(
                        workDataOf("synced_count" to syncedCount)
                    )
                }
                else -> {
                    Log.e("SyncWorker", "Sync failed: ${syncResult.exceptionOrNull()}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync error", e)
            Result.retry()
        }
    }
}
```

### Enqueue periodic work

```kotlin
// sync/SyncManager.kt

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager
) {
    fun schedulePeriodic sync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // WiFi o cellular
            .setRequiresBatteryNotLow(true) // No sincronizar si batería < 15%
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("sync_periodic")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "periodic_sync",
            ExistingPeriodicWorkPolicy.KEEP, // No reemplaza si ya existe
            syncRequest
        )
    }

    // Sync manual (llamado desde UI)
    fun syncNow(): LiveData<WorkInfo> {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("sync_manual")
            .build()

        workManager.enqueueUniqueWork(
            "manual_sync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )

        return workManager.getWorkInfoByIdLiveData(syncRequest.id)
    }

    fun cancelSync() {
        workManager.cancelAllWorkByTag("sync_periodic")
    }

    fun getSyncStatus(): Flow<SyncStatus> {
        return workManager.getWorkInfosByTagFlow("sync_periodic")
            .map { workInfos ->
                val latestWork = workInfos.firstOrNull()
                when (latestWork?.state) {
                    WorkInfo.State.RUNNING -> SyncStatus.Syncing
                    WorkInfo.State.SUCCEEDED -> {
                        val syncedCount = latestWork.outputData.getInt("synced_count", 0)
                        val finishTime = latestWork.outputData.getLong("finish_time", 0)
                        SyncStatus.Success(syncedCount, Instant.fromEpochMilliseconds(finishTime))
                    }
                    WorkInfo.State.FAILED -> SyncStatus.Failed(latestWork.outputData.getString("error"))
                    else -> SyncStatus.Idle
                }
            }
    }
}

sealed interface SyncStatus {
    object Idle : SyncStatus
    object Syncing : SyncStatus
    data class Success(val recordsSynced: Int, val timestamp: Instant) : SyncStatus
    data class Failed(val error: String?) : SyncStatus
}
```

---

## 📦 SyncRepository

```kotlin
// core-data/repository/SyncRepository.kt

class SyncRepository @Inject constructor(
    private val athleteDao: AthleteDao,
    private val physicalMetricDao: PhysicalMetricDao,
    private val shotDao: ShotDao,
    private val lapDao: LapDao,
    private val sessionDao: SessionDao,
    private val matchDao: MatchDao,
    private val supabaseClient: SupabaseClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun syncAll(): Result<Int> = withContext(ioDispatcher) {
        try {
            var totalSynced = 0

            totalSynced += syncAthletes().getOrDefault(0)
            totalSynced += syncPhysicalMetrics().getOrDefault(0)
            totalSynced += syncShots().getOrDefault(0)
            totalSynced += syncLaps().getOrDefault(0)
            totalSynced += syncSessions().getOrDefault(0)
            totalSynced += syncMatches().getOrDefault(0)

            // MatchEventEntity NO se sincroniza (local-only)

            Result.success(totalSynced)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncAthletes(): Result<Int> {
        return try {
            val unsynced = athleteDao.getUnsyncedAthletes()
            var syncedCount = 0

            unsynced.forEach { athlete ->
                try {
                    supabaseClient.upsertAthlete(athlete.toDTO())
                    athleteDao.markAsSynced(athlete.id)
                    syncedCount++
                } catch (e: Exception) {
                    // Log pero continúa (reintentará en próximo sync)
                    Log.e("SyncRepository", "Failed to sync athlete ${athlete.id}", e)
                }
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncPhysicalMetrics(): Result<Int> {
        return try {
            val unsynced = physicalMetricDao.getUnsyncedMetrics()
            var syncedCount = 0

            unsynced.forEach { metric ->
                try {
                    supabaseClient.upsertPhysicalMetric(metric.toDTO())
                    physicalMetricDao.markAsSynced(metric.id)
                    syncedCount++
                } catch (e: Exception) {
                    Log.e("SyncRepository", "Failed to sync metric ${metric.id}", e)
                }
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Similar para shots, laps, sessions, matches...

    suspend fun getPendingSyncCount(): Int {
        return athleteDao.getUnsyncedAthletes().size +
                physicalMetricDao.getUnsyncedMetrics().size +
                shotDao.getUnsyncedShots().size +
                lapDao.getUnsyncedLaps().size +
                sessionDao.getUnsyncedSessions().size +
                matchDao.getUnsyncedMatches().size
    }
}
```

---

## 🔄 Conflict resolution

### Estrategia: Local gana

```kotlin
suspend fun syncWithConflictResolution(local: AthleteEntity): Result<Unit> {
    return try {
        // 1. Fetch remoto (si existe)
        val remote = supabaseClient.getAthleteById(local.id)

        when {
            remote == null -> {
                // No existe en remoto, crear
                supabaseClient.insertAthlete(local.toDTO())
                athleteDao.markAsSynced(local.id)
                Result.success(Unit)
            }

            local.updatedAt > remote.updatedAt -> {
                // Local es más reciente, sobrescribir remoto
                supabaseClient.updateAthlete(local.toDTO())
                athleteDao.markAsSynced(local.id)
                Result.success(Unit)
            }

            else -> {
                // Remoto es más reciente, pero EN MVP LOCAL GANA SIEMPRE
                // (evita pérdida de datos ingresados offline)
                supabaseClient.updateAthlete(local.toDTO())
                athleteDao.markAsSynced(local.id)
                Result.success(Unit)
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

**Nota**: En el MVP, **local SIEMPRE gana**. No se implementa merge inteligente hasta v1.2.

---

## 🌐 Supabase client

```kotlin
// core-network/SupabaseClient.kt

class SupabaseClient @Inject constructor(
    private val httpClient: HttpClient,
    private val authCache: AuthCache
) {
    companion object {
        private const val BASE_URL = "https://your-project.supabase.co"
    }

    suspend fun upsertAthlete(dto: AthleteDTO): Result<Unit> {
        return try {
            httpClient.post("$BASE_URL/rest/v1/athletes") {
                header("Authorization", "Bearer ${authCache.getToken()}")
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                header("Prefer", "resolution=merge-duplicates") // Upsert
                contentType(ContentType.Application.Json)
                setBody(dto)
            }
            Result.success(Unit)
        } catch (e: ClientRequestException) {
            Result.failure(NetworkException("HTTP ${e.response.status.value}: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAthleteById(id: String): AthleteDTO? {
        return try {
            httpClient.get("$BASE_URL/rest/v1/athletes") {
                header("Authorization", "Bearer ${authCache.getToken()}")
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                parameter("id", "eq.$id")
            }.body<List<AthleteDTO>>().firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun batchUpsertAthletes(dtos: List<AthleteDTO>): Result<Unit> {
        return try {
            httpClient.post("$BASE_URL/rest/v1/athletes") {
                header("Authorization", "Bearer ${authCache.getToken()}")
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                header("Prefer", "resolution=merge-duplicates")
                contentType(ContentType.Application.Json)
                setBody(dtos)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## 📊 Sync UI

### Sync indicator en ProfileScreen

```kotlin
@Composable
fun SyncStatusCard(
    syncStatus: SyncStatus,
    pendingCount: Int,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    AthleedCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (syncStatus) {
                    is SyncStatus.Syncing -> Icons.Default.Sync
                    is SyncStatus.Success -> Icons.Default.CloudDone
                    is SyncStatus.Failed -> Icons.Default.CloudOff
                    SyncStatus.Idle -> Icons.Default.Cloud
                },
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .then(
                        if (syncStatus is SyncStatus.Syncing) {
                            Modifier.rotate(animateFloatAsState(
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                )
                            ).value)
                        } else Modifier
                    ),
                tint = when (syncStatus) {
                    is SyncStatus.Success -> MaterialTheme.colorScheme.tertiary
                    is SyncStatus.Failed -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.width(Spacing.SpacingM))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (syncStatus) {
                        is SyncStatus.Syncing -> "Sincronizando..."
                        is SyncStatus.Success -> "Sincronizado"
                        is SyncStatus.Failed -> "Error de sincronización"
                        SyncStatus.Idle -> "Esperando conexión"
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = when (syncStatus) {
                        is SyncStatus.Success -> {
                            val timeAgo = syncStatus.timestamp.toTimeAgo()
                            "Última sync: $timeAgo"
                        }
                        is SyncStatus.Failed -> {
                            syncStatus.error ?: "Error desconocido"
                        }
                        else -> {
                            if (pendingCount > 0) {
                                "$pendingCount registros pendientes"
                            } else {
                                "Todo sincronizado"
                            }
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (syncStatus != SyncStatus.Syncing) {
                IconButton(onClick = onSyncNow) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sincronizar ahora"
                    )
                }
            }
        }
    }
}
```

### ProfileViewModel con sync

```kotlin
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val syncRepository: SyncRepository,
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    val syncStatus: Flow<SyncStatus> = syncManager.getSyncStatus()

    val pendingSyncCount: Flow<Int> = flow {
        while (true) {
            emit(syncRepository.getPendingSyncCount())
            delay(5000) // Poll cada 5 segundos
        }
    }

    fun syncNow() {
        syncManager.syncNow()
    }
}
```

---

## 🧪 Testing sync logic

### Test SyncWorker

```kotlin
@RunWith(AndroidJUnit4::class)
class SyncWorkerTest {
    private lateinit var context: Context
    private lateinit var executor: Executor
    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        executor = Executors.newSingleThreadExecutor()

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(executor)
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)
    }

    @Test
    fun syncWorker_succeeds_whenDataExists() = runTest {
        // Given: datos no sincronizados en Room
        val fakeRepository = FakeSyncRepository(hasData = true, shouldFail = false)

        // When: ejecutar worker
        val request = OneTimeWorkRequestBuilder<SyncWorker>().build()
        workManager.enqueue(request).result.get()

        // Then: worker succeed
        val workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    }

    @Test
    fun syncWorker_retries_whenNetworkFails() = runTest {
        // Given: network failure
        val fakeRepository = FakeSyncRepository(hasData = true, shouldFail = true)

        // When: ejecutar worker
        val request = OneTimeWorkRequestBuilder<SyncWorker>().build()
        workManager.enqueue(request).result.get()

        // Then: worker retries
        val workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo.state).isIn(listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING))
    }
}
```

### Test SyncRepository

```kotlin
class SyncRepositoryTest {
    private lateinit var repository: SyncRepository
    private lateinit var fakeAthleteDao: FakeAthleteDao
    private lateinit var fakeSupabaseClient: FakeSupabaseClient

    @Before
    fun setup() {
        fakeAthleteDao = FakeAthleteDao()
        fakeSupabaseClient = FakeSupabaseClient()
        repository = SyncRepository(fakeAthleteDao, /* ... */, fakeSupabaseClient)
    }

    @Test
    fun syncAthletes_marksAsSynced_whenSuccess() = runTest {
        // Given: atleta no sincronizado
        fakeAthleteDao.athletes["1"] = createTestAthlete(id = "1", isSynced = false)

        // When: sync
        val result = repository.syncAll()

        // Then: marcado como sincronizado
        assertThat(result.isSuccess).isTrue()
        assertThat(fakeAthleteDao.athletes["1"]?.isSynced).isTrue()
    }

    @Test
    fun syncAthletes_doesNotMarkAsSynced_whenFails() = runTest {
        // Given: atleta no sincronizado, network fail
        fakeAthleteDao.athletes["1"] = createTestAthlete(id = "1", isSynced = false)
        fakeSupabaseClient.shouldFail = true

        // When: sync
        val result = repository.syncAll()

        // Then: NO marcado como sincronizado
        assertThat(fakeAthleteDao.athletes["1"]?.isSynced).isFalse()
    }
}
```

---

## 🔍 Monitoring y logging

### Log sync events

```kotlin
class SyncLogger @Inject constructor() {
    fun logSyncStart() {
        Log.d("Sync", "=== SYNC START ===")
    }

    fun logSyncSuccess(entity: String, count: Int) {
        Log.d("Sync", "✅ Synced $count $entity")
    }

    fun logSyncFailure(entity: String, error: Throwable) {
        Log.e("Sync", "❌ Failed to sync $entity", error)
    }

    fun logSyncEnd(totalSynced: Int, duration: Duration) {
        Log.d("Sync", "=== SYNC END === ($totalSynced records in ${duration.inWholeSeconds}s)")
    }
}
```

### Crashlytics integration (futuro)

```kotlin
fun logSyncErrorToCrashlytics(error: Throwable) {
    FirebaseCrashlytics.getInstance().apply {
        setCustomKey("sync_error", error.message ?: "Unknown")
        recordException(error)
    }
}
```

---

**Última revisión**: 2026-06-06
**Próxima revisión**: Post-MVP (v1.2 con sync bidireccional)
