# Offline-Critical Flows — Qué funciona sin conexión

**Principio rector**: *"Si el coach está en el campo de entrenamiento sin WiFi, la app debe funcionar al 100%."*

**Última actualización**: 2026-06-06

---

## 🎯 Filosofía offline-first

### Source of truth
- **Room es ley**: Todas las operaciones CRUD se hacen primero en la base de datos local
- **Network es secundario**: Supabase es un backup/sync target, no la fuente primaria
- **UI lee de Room**: Los ViewModels/Repositories siempre consumen `Flow<T>` de Room

### Estrategia de sincronización
1. Usuario crea/edita datos → se guardan en Room con `isSynced = false`
2. WorkManager detecta conexión → intenta sincronizar
3. Si sync exitoso → marca `isSynced = true` en Room
4. Si falla → reintenta con backoff exponencial (15min, 30min, 1h)

### Conflict resolution
- **Local gana siempre** en el MVP
- Comparación por `updatedAt` (Instant)
- Si `updatedAt` local > remoto → sobrescribe remoto
- No hay merge (estrategia simple para MVP)

---

## ✅ Flujos que DEBEN funcionar 100% offline

### 1. Autenticación (offline parcial)

#### ✅ Offline support
- **Login con credenciales cacheadas**: Si el usuario ya hizo login antes, Firebase Auth puede validar offline con tokens cacheados
- **Persistencia de sesión**: El token de Firebase se guarda localmente y es válido por 1 hora (refresh token válido por 30 días)

#### ❌ Requiere conexión
- **Primer login**: Firebase Auth necesita conexión para autenticar
- **Google Sign-In**: OAuth requiere conexión
- **Recuperación de contraseña**: Envío de email requiere conexión

#### 🔧 Mitigación
- Mensaje claro: "Inicia sesión al menos una vez con conexión"
- Cacheado de credenciales en `SharedPreferences` (encriptado)

---

### 2. Gestión de atletas (100% offline)

#### ✅ Crear atleta
```kotlin
// Flow completo offline
1. Usuario llena formulario (nombre, DOB, deporte, posición)
2. Se genera UUID local: UUID.randomUUID().toString()
3. Se guarda en Room con isSynced = false
4. UI se actualiza inmediatamente (Flow<List<AthleteEntity>> de Room)
5. WorkManager sincroniza en background cuando haya conexión
```

**Entidades afectadas**: `AthleteEntity`

**DAOs involucrados**: `AthleteDao.insert()`

**UI**: `CreateAthleteScreen` → `RosterViewModel` → `CreateAthleteUseCase` → `AthleteRepository` → Room

---

#### ✅ Editar atleta
```kotlin
1. Usuario edita campos
2. Se actualiza en Room con:
   - updatedAt = Clock.System.now()
   - isSynced = false (marca para re-sync)
3. UI se actualiza inmediatamente
```

**Edge case**: Si se edita un atleta que ya se sincronizó, `isSynced` vuelve a `false` → se re-sincroniza

---

#### ✅ Eliminar atleta
```kotlin
// Soft delete
1. Usuario confirma eliminación
2. Se marca isDeleted = true en Room
3. Se filtra en queries: WHERE isDeleted = 0
4. WorkManager sincroniza el soft delete con Supabase
```

**Razón del soft delete**: Permite rollback y mantiene integridad referencial con métricas/sesiones

---

### 3. Registro de métricas físicas (100% offline)

#### ✅ Registrar peso/altura/% grasa
```kotlin
1. Coach selecciona atleta
2. Ingresa métricas (peso: 75.5 kg, altura: 182 cm, grasa: 12.3%)
3. Se crea PhysicalMetricEntity con:
   - id = UUID
   - athleteId = foreign key
   - weight, height, bodyFatPercentage
   - measuredAt = Clock.System.now()
   - isSynced = false
4. Se guarda en Room
5. UI muestra métrica en historial inmediatamente
```

**Entidades afectadas**: `PhysicalMetricEntity`

**DAOs involucrados**: `PhysicalMetricDao.insert()`

**Relación**: `@Relation` con `AthleteEntity` (one-to-many)

---

#### ✅ Ver historial de métricas
```kotlin
// Query offline desde Room
SELECT * FROM physical_metrics
WHERE athleteId = :id
  AND isDeleted = 0
ORDER BY measuredAt DESC
```

**Tipo de retorno**: `Flow<List<PhysicalMetricEntity>>` → auto-update en UI

---

### 4. Rankings (100% offline)

#### ✅ Calcular rankings por velocidad
```kotlin
// Query local en Room (no requiere backend)
SELECT a.*, MIN(l.timeInSeconds) as bestTime
FROM athletes a
JOIN laps l ON a.id = l.athleteId
WHERE l.distance = 100 AND a.isDeleted = 0
GROUP BY a.id
ORDER BY bestTime ASC
LIMIT 10
```

**Entidades afectadas**: `AthleteEntity`, `LapEntity`

**Cálculo**: 100% local, sin dependencia de Supabase

**UI**: `RankingsScreen` lee de `Flow<List<RankingItem>>` generado por query

---

#### ✅ Filtrar rankings por deporte/género
```kotlin
// Se agregan filtros a la query
WHERE a.sport = :sport AND a.gender = :gender
```

**Performance**: Con índices en Room, query < 50ms para 1000 atletas

---

### 5. Sesiones de entrenamiento (100% offline)

#### ✅ Crear sesión
```kotlin
1. Coach llena formulario:
   - date, time
   - sessionType (enum: CARDIO, STRENGTH, TECHNIQUE, MATCH)
   - notes
2. Selecciona atletas participantes (multi-select)
3. Se crea SessionEntity con:
   - id = UUID
   - status = ACTIVE
   - isSynced = false
4. Se guardan relaciones en SessionAthleteEntity (join table)
5. UI navega a SessionDetailScreen
```

**Entidades afectadas**: `SessionEntity`, `SessionAthleteEntity`

**Relación many-to-many**: Session ↔ Athlete

---

#### ✅ Registrar tiros en cancha (durante sesión)
```kotlin
1. Coach toca la cancha en posición (x, y)
2. Selecciona tipo de tiro (2pt, 3pt, free throw)
3. Marca acierto/fallo
4. Se crea ShotEntity con:
   - id = UUID
   - athleteId
   - sessionId
   - positionX, positionY (Float, normalizado 0-1)
   - shotType, isSuccessful
   - timestamp
   - isSynced = false
5. UI actualiza el heatmap de tiros en tiempo real
```

**Entidades afectadas**: `ShotEntity`

**Visualización**: Canvas de Compose dibuja círculos en (x, y) con color según acierto

---

#### ✅ Registrar vueltas en pista (durante sesión)
```kotlin
1. Coach selecciona distancia (100m, 200m, 400m, etc.)
2. Inicia cronómetro (Compose con LaunchedEffect)
3. Detiene cronómetro
4. Se crea LapEntity con:
   - id = UUID
   - athleteId
   - sessionId
   - distance (Int, en metros)
   - timeInSeconds (Float)
   - timestamp
   - isSynced = false
5. UI muestra lista de vueltas con tiempo formateado
```

**Entidades afectadas**: `LapEntity`

**Formato de tiempo**: `1:32.45` (min:sec.ms)

---

#### ✅ Cerrar sesión
```kotlin
1. Coach presiona "Finalizar sesión"
2. Se muestra resumen:
   - Total de tiros: X (Y% acierto)
   - Total de vueltas: Z
   - Mejor tiempo: W
   - Duración total: HH:MM
3. Coach confirma
4. Se actualiza SessionEntity:
   - status = COMPLETED
   - completedAt = Clock.System.now()
   - isSynced = false (marca para sync)
5. WorkManager sincroniza en background
```

**Ver flujo completo**: `docs/context/session-closeout-context.md`

---

### 6. Partidos (100% offline)

#### ✅ Crear partido
```kotlin
1. Coach llena formulario:
   - date, time
   - opponent
   - location
   - homeTeam (bool)
2. Selecciona lineup (atletas participantes)
3. Se crea MatchEntity con:
   - id = UUID
   - status = SCHEDULED
   - isSynced = false
4. Se guardan relaciones en MatchAthleteEntity
```

**Entidades afectadas**: `MatchEntity`, `MatchAthleteEntity`

---

#### ✅ Registrar eventos de partido (local-only)
```kotlin
1. Coach marca evento:
   - Punto anotado (jugador, minuto, tipo: 2pt/3pt/FT)
   - Asistencia (jugador)
   - Rebote (jugador, tipo: ofensivo/defensivo)
   - Falta (jugador)
   - Sustitución (sale X, entra Y)
2. Se crea MatchEventEntity con:
   - id = UUID
   - matchId
   - athleteId
   - eventType (enum)
   - minute (Int)
   - details (JSON nullable)
   - timestamp
   - isSynced = false (PERO NO SE SINCRONIZA EN MVP)
3. Se guarda en Room
4. UI actualiza el play-by-play en tiempo real
```

**IMPORTANTE**: `MatchEventEntity` **NO se sincroniza** con Supabase en el MVP. Es **local-only**.

**Razón**: El backend no está preparado para manejar eventos de partido. Solo se sincroniza el resultado final del partido.

---

#### ✅ Finalizar partido
```kotlin
1. Coach presiona "Finalizar partido"
2. Se muestra resumen:
   - Score final: 78-65
   - Estadísticas por jugador (puntos, asistencias, rebotes)
   - MVP del partido (heurística simple: más puntos)
3. Coach confirma
4. Se actualiza MatchEntity:
   - status = COMPLETED
   - finalScoreHome, finalScoreAway
   - completedAt = Clock.System.now()
   - isSynced = false
5. WorkManager sincroniza MatchEntity (sin eventos)
```

**Sync**: Solo `MatchEntity` se sincroniza. `MatchEventEntity` permanece local.

---

### 7. Dashboard coach (100% offline)

#### ✅ Métricas agregadas
```kotlin
// Todo calculado desde Room
- Atletas activos: SELECT COUNT(*) FROM athletes WHERE isDeleted = 0
- Sesiones esta semana: SELECT COUNT(*) FROM sessions WHERE date >= :startOfWeek
- Métricas registradas esta semana: SELECT COUNT(*) FROM physical_metrics WHERE measuredAt >= :startOfWeek
```

**Performance**: Queries agregadas con índices < 100ms

---

### 8. Perfil de usuario (offline parcial)

#### ✅ Ver información
- Nombre, email cacheado de Firebase Auth
- Última sync: timestamp guardado en `SharedPreferences`

#### ❌ Requiere conexión
- Logout (opcional: se puede hacer offline borrando el token local)

---

## ❌ Flujos que requieren conexión

### 1. Primer login
- Firebase Auth necesita validar credenciales remotas
- Google Sign-In requiere OAuth con servidores de Google

### 2. Recuperación de contraseña
- Envío de email requiere Firebase Functions

### 3. Fetch inicial de atletas (si se agregó desde otro dispositivo)
- En MVP, cada coach usa un solo dispositivo → no hay fetch
- Post-MVP: sync bidireccional

---

## 🔄 Sincronización en background

### WorkManager configuration

```kotlin
// En SyncWorker
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED) // WiFi o cellular
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
    .build()
```

### Qué se sincroniza

#### ✅ Sincronizado en MVP
- `AthleteEntity`
- `PhysicalMetricEntity`
- `ShotEntity`
- `LapEntity`
- `SessionEntity`
- `MatchEntity` (solo resultado final, sin eventos)

#### ❌ NO sincronizado en MVP
- `MatchEventEntity` (local-only)

---

## 🧪 Testing de offline flows

### Unit tests
```kotlin
@Test
fun `crear atleta sin conexión guarda en Room con isSynced false`() {
    // Given: repositorio sin conexión
    val repository = AthleteRepository(dao, networkClient = null)

    // When: crear atleta
    val athlete = AthleteEntity(id = UUID, name = "Juan", isSynced = false)
    repository.createAthlete(athlete)

    // Then: se guarda en Room
    val saved = dao.getById(athlete.id)
    assertThat(saved.isSynced).isFalse()
}
```

### Integration tests
```kotlin
@Test
fun `registrar tiro en sesión offline actualiza UI inmediatamente`() {
    // Given: sesión activa sin conexión
    composeTestRule.setContent {
        SessionDetailScreen(sessionId = "session-1")
    }

    // When: registrar tiro
    composeTestRule.onNodeWithTag("court_canvas").performClick()

    // Then: tiro aparece en lista
    composeTestRule.onNodeWithText("Tiro #1").assertExists()
}
```

---

## 📊 Métricas de offline-first

### Objetivos del MVP
- [ ] 100% de features core funcionan sin conexión
- [ ] Sincronización exitosa en 95%+ de intentos (cuando hay conexión)
- [ ] Pérdida de datos < 0.1% (solo en casos de kill process durante write)
- [ ] Tiempo de respuesta UI < 500ms (reads desde Room)

### Monitoreo
- Logs de `isSynced = false` acumulados (cuántos registros pendientes)
- Tasa de éxito de WorkManager sync
- Crashes durante escritura en Room

---

## 🛡️ Edge cases y mitigaciones

### ¿Qué pasa si se mata la app durante una escritura en Room?
- **Room usa transacciones**: Si se interrumpe, rollback automático
- **Mitigación**: Usar `@Transaction` en DAOs para operaciones multi-tabla

### ¿Qué pasa si se llena el almacenamiento local?
- **Room lanza exception**: `SQLiteDiskIOException`
- **Mitigación**: Catch en Repository, mostrar error al usuario
- **Futuro**: Cleanup automático de datos antiguos (>6 meses)

### ¿Qué pasa si el usuario nunca tiene conexión?
- **Datos se acumulan localmente**: `isSynced = false` para todos los registros
- **Límite práctico**: 10,000 registros ≈ 10MB
- **Mitigación**: Mostrar warning si > 1000 registros sin sincronizar

---

**Última revisión**: 2026-06-06
**Validado por**: Tech Lead (placeholder)
**Próxima revisión**: Post-MVP
