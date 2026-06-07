# Android Architecture — Módulos y reglas

**Patrón**: Clean Architecture + MVVM + Multi-module
**Última actualización**: 2026-06-06

---

## 🏗️ Estructura modular

```
Athlead_v2/
│
├── app/                       # Application module (orchestration)
│   ├── AthleedApplication.kt  # Hilt setup, WorkManager init
│   ├── MainActivity.kt        # Single activity (Compose)
│   └── navigation/
│       └── AppNavGraph.kt     # Root navigation graph
│
├── core-ui/                   # Shared UI components, theme
│   ├── components/
│   │   ├── AthleedButton.kt
│   │   ├── AthleedTextField.kt
│   │   ├── AthleedCard.kt
│   │   └── LoadingIndicator.kt
│   ├── theme/
│   │   ├── Color.kt           # D1 palette
│   │   ├── Type.kt            # Typography (Archivo, Hanken Grotesk, IBM Plex Mono)
│   │   ├── Theme.kt           # MaterialTheme setup
│   │   └── Spacing.kt         # Spacing tokens
│   └── util/
│       ├── Formatters.kt      # Date, time, number formatters
│       └── Extensions.kt      # Compose extensions
│
├── core-data/                 # Data layer (repositories, use cases)
│   ├── repository/
│   │   ├── AthleteRepository.kt
│   │   ├── SessionRepository.kt
│   │   ├── MatchRepository.kt
│   │   └── SyncRepository.kt
│   ├── usecase/
│   │   ├── GetAthletesUseCase.kt
│   │   ├── CreateAthleteUseCase.kt
│   │   ├── GetRankingsUseCase.kt
│   │   └── SyncDataUseCase.kt
│   └── model/
│       └── DomainModels.kt    # Domain entities (no Room annotations)
│
├── core-database/             # Room database, DAOs, entities
│   ├── AthleedDatabase.kt     # Room database definition
│   ├── dao/
│   │   ├── AthleteDao.kt
│   │   ├── PhysicalMetricDao.kt
│   │   ├── ShotDao.kt
│   │   ├── LapDao.kt
│   │   ├── SessionDao.kt
│   │   └── MatchDao.kt
│   ├── entity/
│   │   ├── AthleteEntity.kt
│   │   ├── PhysicalMetricEntity.kt
│   │   ├── ShotEntity.kt
│   │   ├── LapEntity.kt
│   │   ├── SessionEntity.kt
│   │   ├── MatchEntity.kt
│   │   └── MatchEventEntity.kt
│   └── migration/
│       └── Migrations.kt      # Room migration strategies
│
├── core-network/              # Ktor client, Supabase config
│   ├── SupabaseClient.kt      # Supabase SDK setup
│   ├── KtorClient.kt          # Ktor HttpClient config
│   ├── dto/
│   │   └── NetworkDTOs.kt     # Data transfer objects
│   └── interceptor/
│       └── AuthInterceptor.kt # JWT token injection
│
├── core-auth/                 # Firebase Auth, role management
│   ├── FirebaseAuthManager.kt
│   ├── GoogleSignInManager.kt
│   ├── model/
│   │   ├── User.kt
│   │   └── Role.kt            # enum: COACH, ATHLETE
│   └── cache/
│       └── AuthCache.kt       # SharedPreferences wrapper
│
├── sync/                      # WorkManager workers
│   ├── SyncWorker.kt          # Periodic sync (every 15 min)
│   ├── SyncManager.kt         # Manual sync trigger
│   └── conflict/
│       └── ConflictResolver.kt # Local-wins strategy
│
├── feature-auth/              # Login, register, Google Sign-In
│   ├── LoginScreen.kt
│   ├── LoginViewModel.kt
│   ├── RegisterScreen.kt
│   ├── RegisterViewModel.kt
│   └── navigation/
│       └── AuthNavigation.kt
│
├── feature-coach/             # Coach dashboard
│   ├── CoachDashboardScreen.kt
│   ├── CoachDashboardViewModel.kt
│   └── components/
│       ├── MetricsSummaryCard.kt
│       └── UpcomingSessionsCard.kt
│
├── feature-roster/            # Athlete management
│   ├── RosterScreen.kt
│   ├── RosterViewModel.kt
│   ├── AthleteDetailScreen.kt
│   ├── AthleteDetailViewModel.kt
│   ├── CreateAthleteScreen.kt
│   └── components/
│       ├── AthleteCard.kt
│       └── AthleteListItem.kt
│
├── feature-rankings/          # Rankings by category
│   ├── RankingsScreen.kt
│   ├── RankingsViewModel.kt
│   └── components/
│       ├── RankingCard.kt
│       └── CategoryFilter.kt
│
├── feature-analytics/         # Performance analytics
│   ├── AnalyticsScreen.kt
│   ├── AnalyticsViewModel.kt
│   └── components/
│       ├── TrendChart.kt
│       └── MetricCard.kt
│
└── feature-profile/           # User profile, settings
    ├── ProfileScreen.kt
    ├── ProfileViewModel.kt
    └── components/
        └── SettingsCard.kt
```

---

## 📐 Dependency rules

### Feature modules
```kotlin
// ✅ PERMITIDO
feature-roster → core-ui
feature-roster → core-data
feature-roster → core-auth (para verificar rol)

// ❌ PROHIBIDO
feature-roster → feature-rankings  // Features NO dependen entre sí
feature-roster → core-database     // Features NO hablan con Room directamente
```

### Core modules
```kotlin
// ✅ PERMITIDO
core-data → core-database
core-data → core-network
core-data → core-auth

// ❌ PROHIBIDO
core-ui → core-data        // UI no tiene lógica de negocio
core-database → core-network // Room no depende de Ktor
core-* → feature-*         // Core modules NO dependen de features
```

### Sync module
```kotlin
// ✅ PERMITIDO
sync → core-data (usa SyncRepository)
sync → core-database (lee isSynced)
sync → core-network (envía a Supabase)

// ❌ PROHIBIDO
sync → feature-* // No depende de UI
```

---

## 🧩 Capas arquitectónicas

### 1. Presentation Layer (Features + core-ui)

**Responsabilidad**: UI y manejo de estado

**Componentes**:
- **Screen Composables**: Root de cada pantalla (ej: `RosterScreen.kt`)
- **ViewModels**: Manejo de estado con `StateFlow`
- **UI State classes**: Data classes inmutables (ej: `RosterUiState`)
- **UI Events**: Sealed classes para acciones (ej: `RosterEvent`)

**Reglas**:
- ✅ ViewModels exponen `StateFlow<UiState>`
- ✅ Screen Composables son stateless (reciben `uiState` y `onEvent`)
- ✅ Máximo 300 líneas por Composable
- ✅ Máximo 250 líneas por ViewModel
- ❌ NO hay lógica de negocio en Composables
- ❌ ViewModels NO hablan con Room/Ktor directamente

**Ejemplo**:
```kotlin
@HiltViewModel
class RosterViewModel @Inject constructor(
    private val getAthletesUseCase: GetAthletesUseCase,
    private val deleteAthleteUseCase: DeleteAthleteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RosterUiState())
    val uiState: StateFlow<RosterUiState> = _uiState.asStateFlow()

    init {
        loadAthletes()
    }

    fun onEvent(event: RosterEvent) {
        when (event) {
            is RosterEvent.DeleteAthlete -> deleteAthlete(event.id)
            is RosterEvent.Refresh -> loadAthletes()
        }
    }

    private fun loadAthletes() {
        viewModelScope.launch {
            getAthletesUseCase()
                .catch { error -> _uiState.update { it.copy(error = error.message) } }
                .collect { athletes -> _uiState.update { it.copy(athletes = athletes) } }
        }
    }
}
```

---

### 2. Domain Layer (core-data)

**Responsabilidad**: Lógica de negocio y orquestación

**Componentes**:
- **Repositories**: Orquestan Room + Network, deciden qué source usar
- **Use Cases**: Lógica de negocio reutilizable (opcional, solo para lógica compleja)
- **Domain Models**: Entities sin anotaciones de Room/Ktor

**Reglas**:
- ✅ Repositories retornan `Flow<T>` de Room (para reactive UI)
- ✅ Use Cases tienen una sola responsabilidad (Single Responsibility Principle)
- ✅ Repositories manejan `isSynced` flag
- ❌ NO hay UI logic en Repositories

**Ejemplo de Repository**:
```kotlin
class AthleteRepository @Inject constructor(
    private val athleteDao: AthleteDao,
    private val supabaseClient: SupabaseClient
) {
    // Room es source of truth
    fun getAthletes(): Flow<List<Athlete>> =
        athleteDao.getAll()
            .map { entities -> entities.map { it.toDomain() } }

    suspend fun createAthlete(athlete: Athlete) {
        val entity = athlete.toEntity().copy(isSynced = false)
        athleteDao.insert(entity)
        // WorkManager se encarga del sync, no bloqueamos la UI
    }

    suspend fun syncAthletes() {
        val unsyncedAthletes = athleteDao.getUnsyncedAthletes()
        unsyncedAthletes.forEach { athlete ->
            try {
                supabaseClient.upsertAthlete(athlete.toDTO())
                athleteDao.update(athlete.copy(isSynced = true))
            } catch (e: Exception) {
                // Log error, reintentará en próximo sync
            }
        }
    }
}
```

**Ejemplo de Use Case**:
```kotlin
class GetRankingsUseCase @Inject constructor(
    private val athleteDao: AthleteDao,
    private val lapDao: LapDao
) {
    suspend operator fun invoke(category: RankingCategory): Flow<List<RankingItem>> {
        return when (category) {
            RankingCategory.SPEED_100M -> {
                // Query compleja que combina atletas y vueltas
                lapDao.getBestTimesByDistance(100)
                    .map { laps -> laps.mapToRankingItems() }
            }
            // ...
        }
    }
}
```

---

### 3. Data Layer (core-database + core-network)

**Responsabilidad**: Acceso a datos (local y remoto)

#### core-database (Room)

**Reglas**:
- ✅ Todas las entities tienen `id: String` (UUID)
- ✅ Todas las entities tienen `isSynced: Boolean`
- ✅ Todas las entities tienen `createdAt: Instant` y `updatedAt: Instant`
- ✅ DAOs retornan `Flow<T>` para queries observables
- ✅ DAOs usan `@Transaction` para operaciones multi-tabla
- ❌ NO hay lógica de negocio en DAOs

**Ejemplo de Entity**:
```kotlin
@Entity(tableName = "athletes")
data class AthleteEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dateOfBirth: Instant,
    val sport: String,
    val position: String?,
    val gender: String,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

**Ejemplo de DAO**:
```kotlin
@Dao
interface AthleteDao {
    @Query("SELECT * FROM athletes WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAll(): Flow<List<AthleteEntity>>

    @Query("SELECT * FROM athletes WHERE id = :id AND isDeleted = 0")
    suspend fun getById(id: String): AthleteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(athlete: AthleteEntity)

    @Update
    suspend fun update(athlete: AthleteEntity)

    @Query("UPDATE athletes SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Instant)

    @Query("SELECT * FROM athletes WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedAthletes(): List<AthleteEntity>
}
```

#### core-network (Ktor + Supabase)

**Reglas**:
- ✅ DTOs para serialización (separados de entities)
- ✅ Interceptor para JWT de Firebase
- ✅ Timeout configurado (30s para requests)
- ❌ NO se cachea en network layer (Room es el cache)

**Ejemplo de SupabaseClient**:
```kotlin
class SupabaseClient @Inject constructor(
    private val httpClient: HttpClient,
    private val authCache: AuthCache
) {
    suspend fun upsertAthlete(dto: AthleteDTO): Result<Unit> {
        return try {
            val response = httpClient.post("$BASE_URL/athletes") {
                header("Authorization", "Bearer ${authCache.getToken()}")
                contentType(ContentType.Application.Json)
                setBody(dto)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## 🔄 Data flow

### Read flow (UI → Room)
```
Screen Composable
    ↓ collectAsStateWithLifecycle()
ViewModel (StateFlow)
    ↓ collect()
Use Case / Repository
    ↓ Flow<T>
Room DAO
    ↓ query
SQLite database
```

**Key point**: Flow auto-update. Si Room cambia, UI se re-compone automáticamente.

### Write flow (Offline-first)
```
Screen Composable (user action)
    ↓ onEvent()
ViewModel
    ↓ invoke()
Use Case / Repository
    ↓ insert/update (isSynced = false)
Room DAO
    ↓ write
SQLite database
    ↓ trigger Flow update
UI auto-refreshes
    ↓ (en background)
WorkManager SyncWorker
    ↓ sync
Supabase
    ↓ success
Room DAO (update isSynced = true)
```

---

## 🔐 Authentication flow

### Login
```
LoginScreen
    ↓ onLogin(email, password)
LoginViewModel
    ↓ signIn()
FirebaseAuthManager
    ↓ signInWithEmailAndPassword()
Firebase Auth SDK
    ↓ success (FirebaseUser)
AuthCache (save token + role)
    ↓ navigation
CoachDashboardScreen
```

### Google Sign-In
```
LoginScreen (Google button)
    ↓ onGoogleSignIn()
GoogleSignInManager
    ↓ launch intent
Google OAuth
    ↓ success (id token)
FirebaseAuthManager (signInWithCredential)
    ↓ success
AuthCache (save token + role)
```

---

## 🧪 Testing strategy

### Unit tests (ViewModels, Use Cases, Repositories)
```kotlin
@Test
fun `loadAthletes emits athletes from repository`() = runTest {
    // Given
    val fakeRepository = FakeAthleteRepository()
    val viewModel = RosterViewModel(
        getAthletesUseCase = GetAthletesUseCase(fakeRepository)
    )

    // When
    viewModel.loadAthletes()

    // Then
    val uiState = viewModel.uiState.value
    assertThat(uiState.athletes).hasSize(3)
}
```

### UI tests (Compose)
```kotlin
@Test
fun `clicking on athlete navigates to detail`() {
    composeTestRule.setContent {
        RosterScreen(
            viewModel = fakeViewModel,
            onNavigateToDetail = { athleteId ->
                assertThat(athleteId).isEqualTo("athlete-1")
            }
        )
    }

    composeTestRule.onNodeWithText("John Doe").performClick()
}
```

### Integration tests (Room + Repository)
```kotlin
@Test
fun `createAthlete saves to room with isSynced false`() = runTest {
    // Given
    val database = Room.inMemoryDatabaseBuilder(context, AthleedDatabase::class.java).build()
    val repository = AthleteRepository(database.athleteDao(), fakeSupabaseClient)

    // When
    repository.createAthlete(Athlete(id = "1", name = "Jane"))

    // Then
    val saved = database.athleteDao().getById("1")
    assertThat(saved?.isSynced).isFalse()
}
```

---

## 📏 Code style rules

### Naming conventions
- **Composables**: `PascalCase` (ej: `AthleteCard`)
- **ViewModels**: `PascalCaseViewModel` (ej: `RosterViewModel`)
- **Use Cases**: `VerbNounUseCase` (ej: `GetAthletesUseCase`)
- **Repositories**: `NounRepository` (ej: `AthleteRepository`)
- **Room Entities**: `PascalCaseEntity` (ej: `AthleteEntity`)
- **DTOs**: `PascalCaseDTO` (ej: `AthleteDTO`)

### File organization
- Un archivo por class/interface (excepto sealed classes pequeñas)
- Agrupar related components en subcarpetas (`components/`, `navigation/`, etc.)
- Tests espejo la estructura de `src/` (ej: `src/main/RosterViewModel.kt` → `src/test/RosterViewModelTest.kt`)

### Límites estrictos
- **Composables**: máximo 300 líneas
- **ViewModels**: máximo 250 líneas
- **Use Cases**: máximo 100 líneas (si es más largo, dividir)
- **Repositories**: máximo 300 líneas

**Qué hacer si se excede**:
- Composables → extraer componentes más pequeños (`AthleteCard`, `AthleteListItem`, etc.)
- ViewModels → mover lógica a Use Cases
- Repositories → dividir en repositorios especializados

---

## 🚀 Build configuration

### Gradle modules
```kotlin
// settings.gradle.kts
include(":app")
include(":core-ui")
include(":core-data")
include(":core-database")
include(":core-network")
include(":core-auth")
include(":sync")
include(":feature-auth")
include(":feature-coach")
include(":feature-roster")
include(":feature-rankings")
include(":feature-analytics")
include(":feature-profile")
```

### Dependencies en feature modules
```kotlin
// feature-roster/build.gradle.kts
dependencies {
    implementation(project(":core-ui"))
    implementation(project(":core-data"))
    implementation(project(":core-auth"))

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)
}
```

---

## 🔗 Referencias

- [Guide to app architecture | Android Developers](https://developer.android.com/topic/architecture)
- [Dependency injection with Hilt | Android Developers](https://developer.android.com/training/dependency-injection/hilt-android)
- [Room Persistence Library | Android Developers](https://developer.android.com/training/data-storage/room)
- [Jetpack Compose | Android Developers](https://developer.android.com/jetpack/compose)

---

**Última revisión**: 2026-06-06
**Aprobado por**: Tech Lead (placeholder)
