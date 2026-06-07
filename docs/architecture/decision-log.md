# Decision Log — Decisiones arquitectónicas

**Formato**: Architecture Decision Records (ADR)
**Última actualización**: 2026-06-06

---

## ADR-001: Reconstrucción desde cero vs. refactor incremental

**Fecha**: 2026-06-06
**Estado**: ✅ Aceptado
**Contexto**: La versión anterior de Athlead tiene anti-patrones críticos (SessionScreen de 2480 líneas, lógica de negocio en Composables, sincronización manual).

### Decisión
Reconstruir la app desde cero en lugar de refactorizar incrementalmente.

### Razones
1. **Deuda técnica insostenible**: El costo de refactorizar SessionScreen excede el de reescribirlo
2. **Arquitectura inconsistente**: No hay separación clara de capas
3. **Testing imposible**: UI tests frágiles por acoplamiento
4. **Límites no respetados**: Múltiples archivos >1000 líneas

### Consecuencias
- ✅ Oportunidad de aplicar límites estrictos desde el inicio
- ✅ Arquitectura limpia y testeble
- ✅ Documentación completa desde día 1
- ❌ MVP toma 8 semanas (vs. 4 semanas de refactor parcial)
- ❌ Se pierde código legacy (pero era código de baja calidad)

### Alternativas consideradas
- **Refactor incremental**: Descartado por alto acoplamiento
- **Reescribir solo SessionScreen**: Descartado porque el problema es sistémico

---

## ADR-002: Room como source of truth (offline-first)

**Fecha**: 2026-06-06
**Estado**: ✅ Aceptado
**Contexto**: Los coaches entrenan en campos deportivos sin WiFi confiable. La app debe funcionar completamente offline.

### Decisión
Room es la fuente única de verdad. Todas las operaciones CRUD se hacen primero en Room. Supabase es un backup/sync target.

### Razones
1. **Realidad del usuario**: 60%+ de sesiones de entrenamiento ocurren sin conexión
2. **User experience**: UI debe ser instantánea, sin spinners esperando network
3. **Data integrity**: Room garantiza transacciones ACID, network no
4. **Simplicidad**: Un solo source of truth reduce complejidad

### Consecuencias
- ✅ App funciona 100% offline
- ✅ UI siempre responsive (reads desde Room < 50ms)
- ✅ No hay pérdida de datos en campos sin conexión
- ❌ Requiere WorkManager para sincronización en background
- ❌ Potencial conflicto si múltiples dispositivos editan el mismo dato (mitigado en MVP: un coach = un dispositivo)

### Alternativas consideradas
- **Network-first con cache**: Descartado porque no funciona offline
- **Dual source of truth (Room + Supabase)**: Descartado por complejidad de merge

### Implementación
```kotlin
// ✅ Correcto: Room es source of truth
fun getAthletes(): Flow<List<Athlete>> =
    athleteDao.getAll().map { entities -> entities.toDomain() }

// ❌ Incorrecto: Network es source of truth
suspend fun getAthletes(): List<Athlete> =
    supabaseClient.fetchAthletes() // falla si no hay conexión
```

---

## ADR-003: Local gana en conflictos de sincronización

**Fecha**: 2026-06-06
**Estado**: ✅ Aceptado
**Contexto**: Al sincronizar con Supabase, puede haber conflictos si el mismo dato se modificó localmente y remotamente.

### Decisión
En caso de conflicto, **la versión local siempre sobrescribe la remota**.

### Razones
1. **MVP scope**: En MVP, cada coach usa un solo dispositivo → conflictos son raros
2. **Simplicidad**: No requiere merge logic complejo
3. **User trust**: El coach confía en lo que registró localmente, no en cambios remotos inesperados
4. **Data loss prevention**: Preferimos perder cambios remotos que cambios locales del coach

### Consecuencias
- ✅ Lógica de sync simple (compara `updatedAt`, local > remoto → sobrescribe)
- ✅ No hay UX de resolución de conflictos (no hay modal "¿Qué versión quieres?")
- ❌ Cambios remotos pueden perderse (mitigado: en MVP no hay multi-device)
- ❌ No escala a multi-device sin modificar estrategia

### Alternativas consideradas
- **Last-write-wins (por timestamp)**: Descartado porque el reloj del dispositivo puede estar mal configurado
- **Merge inteligente (CRDTs)**: Descartado por complejidad (reservado para post-MVP)
- **Manual resolution**: Descartado porque interrumpe el flujo del usuario

### Implementación
```kotlin
suspend fun syncAthlete(local: AthleteEntity, remote: AthleteDTO) {
    if (local.updatedAt > remote.updatedAt.toInstant()) {
        // Local gana, sobrescribe remoto
        supabaseClient.upsertAthlete(local.toDTO())
        athleteDao.update(local.copy(isSynced = true))
    } else {
        // Remoto gana, sobrescribe local
        athleteDao.update(remote.toEntity().copy(isSynced = true))
    }
}

// Simplificación en MVP: siempre local gana
suspend fun syncAthlete(local: AthleteEntity) {
    supabaseClient.upsertAthlete(local.toDTO())
    athleteDao.update(local.copy(isSynced = true))
}
```

### Roadmap post-MVP
- v1.2: Implementar CRDTs para merge automático
- v1.3: Soporte multi-device con detección de conflictos

---

## ADR-004: MatchEventEntity es local-only en MVP

**Fecha**: 2026-06-06
**Estado**: ✅ Aceptado
**Contexto**: `MatchEventEntity` registra eventos de partido (puntos, asistencias, rebotes, etc.). El backend de Supabase no está preparado para manejar eventos en tiempo real.

### Decisión
`MatchEventEntity` se guarda en Room pero **NO se sincroniza** con Supabase en el MVP.

### Razones
1. **Backend no listo**: Supabase no tiene tablas/endpoints para eventos de partido
2. **Volumen alto**: Un partido puede generar 200+ eventos → costly de sincronizar
3. **Valor local**: Los eventos son útiles para el coach localmente (análisis post-partido)
4. **MVP scope**: El core value es registrar el resultado final del partido, no analytics avanzados

### Consecuencias
- ✅ Desarrollo más rápido (no requiere backend changes)
- ✅ Menor carga en network (menos datos a sincronizar)
- ✅ Eventos disponibles offline indefinidamente
- ❌ No hay analytics remotos de eventos de partido
- ❌ Si se borra la app, se pierden los eventos (mitigado: advertencia al usuario)
- ❌ No se pueden compartir eventos entre coaches

### Alternativas consideradas
- **Sincronizar eventos comprimidos**: Descartado porque Supabase no tiene endpoints
- **Exportar eventos a JSON**: Considerado para post-MVP

### Implementación
```kotlin
@Entity(tableName = "match_events")
data class MatchEventEntity(
    @PrimaryKey val id: String,
    val matchId: String,
    val athleteId: String,
    val eventType: String, // "POINT", "ASSIST", "REBOUND", etc.
    val minute: Int,
    val details: String?, // JSON nullable
    val timestamp: Instant,
    val isSynced: Boolean = false // SIEMPRE false en MVP, NO se sincroniza
)

// En SyncWorker, se excluye explícitamente
suspend fun syncData() {
    syncAthletes()
    syncPhysicalMetrics()
    syncShots()
    syncLaps()
    syncSessions()
    syncMatches() // Solo MatchEntity, NO MatchEventEntity
}
```

### Roadmap post-MVP
- v1.2: Backend soporta eventos de partido → se habilita sync
- v1.2: Analytics remotos (heatmaps, shot charts)

---

## ADR-005: Límites estrictos de tamaño de archivo

**Fecha**: 2026-06-06
**Estado**: ✅ Aceptado
**Contexto**: La versión anterior tenía archivos de 2480 líneas (SessionScreen), imposibles de mantener.

### Decisión
Imponer límites estrictos:
- **Composables**: máximo 300 líneas
- **ViewModels**: máximo 250 líneas
- **Use Cases**: máximo 100 líneas

### Razones
1. **Mantenibilidad**: Archivos pequeños son más fáciles de entender y modificar
2. **Code review**: Pull requests más pequeños → reviews más rápidos
3. **Testing**: Funciones pequeñas son más fáciles de testear
4. **Reusabilidad**: Componentes pequeños se reutilizan más fácilmente

### Consecuencias
- ✅ Código más modular y testeable
- ✅ Onboarding más rápido para nuevos developers
- ✅ Menor probabilidad de merge conflicts
- ❌ Requiere disciplina en code reviews (rechazar PRs que excedan límites)
- ❌ Puede resultar en over-engineering si se divide prematuramente

### Alternativas consideradas
- **Límites por función**: Descartado porque no previene God objects
- **Límites por complejidad ciclomática**: Descartado porque es difícil de medir en tiempo real

### Enforcement
```bash
# Pre-commit hook (futuro)
#!/bin/bash
for file in $(git diff --cached --name-only | grep '.kt$'); do
    lines=$(wc -l < "$file")
    if [[ "$file" == *"Screen.kt" ]] && [[ $lines -gt 300 ]]; then
        echo "Error: $file has $lines lines (max 300 for Composables)"
        exit 1
    fi
    if [[ "$file" == *"ViewModel.kt" ]] && [[ $lines -gt 250 ]]; then
        echo "Error: $file has $lines lines (max 250 for ViewModels)"
        exit 1
    fi
done
```

### Estrategias de refactor cuando se excede
- **Composables**: Extraer componentes (`AthleteCard`, `MetricRow`, etc.)
- **ViewModels**: Mover lógica a Use Cases
- **Use Cases**: Dividir en múltiples use cases especializados

---

## ADR-006: Jetpack Compose con Material3

**Fecha**: 2026-06-06
**Estado**: ✅ Aceptado
**Contexto**: Elegir entre XML Views (legacy) y Jetpack Compose para UI.

### Decisión
Usar Jetpack Compose con Material3 para toda la UI.

### Razones
1. **Modern**: Compose es el futuro de Android UI (XML Views en maintenance mode)
2. **Declarative**: UI = f(state) → más fácil de razonar
3. **Performance**: Menos overdraw, recomposiciones inteligentes
4. **Testing**: Compose tests más simples que Espresso
5. **Material3**: Design system moderno, compatible con D1

### Consecuencias
- ✅ Desarrollo más rápido (menos boilerplate que XML)
- ✅ UI reactiva por defecto (StateFlow → recomposición automática)
- ✅ Previews en Android Studio (sin emulador)
- ❌ Curva de aprendizaje para developers sin experiencia en Compose
- ❌ No hay interop con XML (pero no hay código legacy que reusar)

### Alternativas consideradas
- **XML Views**: Descartado porque es legacy
- **Compose + XML híbrido**: Descartado porque no hay legacy code

---

## ADR-007: Hilt para inyección de dependencias

**Fecha**: 2026-06-06
**Estado**: ✅ Aceptado
**Contexto**: Elegir entre Hilt, Koin, o DI manual.

### Decisión
Usar Hilt (Dagger wrapper oficial de Android).

### Razones
1. **Oficial**: Recomendado por Google para Android
2. **Compile-time safety**: Errores de DI detectados en compilación, no en runtime
3. **Performance**: Graph de dependencias generado en compile-time
4. **Testing**: `@HiltAndroidTest` simplifica injection en tests
5. **Integración**: ViewModel injection con `@HiltViewModel`

### Consecuencias
- ✅ DI type-safe (no runtime crashes por missing dependencies)
- ✅ Scopes automáticos (`@Singleton`, `@ViewModelScoped`, etc.)
- ❌ Build time más lento (kapt de Dagger)
- ❌ Curva de aprendizaje (Hilt/Dagger es complejo)

### Alternativas consideradas
- **Koin**: Descartado porque es runtime DI (crashes en runtime)
- **Manual DI**: Descartado porque no escala (mucho boilerplate)

---

## ADR-008: WorkManager para sincronización periódica

**Fecha**: 2026-06-06
**Estado**: ✅ Aceptado
**Contexto**: Necesitamos sincronizar datos con Supabase en background, incluso si la app está cerrada.

### Decisión
Usar WorkManager para ejecutar `SyncWorker` cada 15 minutos.

### Razones
1. **Garantías**: WorkManager garantiza ejecución (sobrevive a reboots, Doze mode)
2. **Constraints**: Solo ejecuta con conexión (WiFi o cellular)
3. **Backoff**: Retry automático con exponential backoff si falla
4. **Battery-friendly**: Respeta battery optimization de Android

### Consecuencias
- ✅ Sincronización confiable en background
- ✅ No requiere foreground service (mejor UX)
- ✅ Se adapta a condiciones del dispositivo (battery, network)
- ❌ No es tiempo real (latencia de hasta 15 min)

### Alternativas consideradas
- **AlarmManager**: Descartado porque no sobrevive a Doze mode
- **Foreground Service**: Descartado porque molesta al usuario (notificación permanente)
- **Firebase Cloud Messaging**: Considerado para post-MVP (push notifications)

### Implementación
```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
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

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "sync_work",
    ExistingPeriodicWorkPolicy.KEEP,
    syncRequest
)
```

---

## ADR-009: Firebase Auth + Supabase para backend

**Fecha**: 2026-06-06
**Estado**: ✅ Aceptado
**Contexto**: Necesitamos auth (login, Google Sign-In) y backend (database, storage).

### Decisión
Usar **Firebase Auth** para autenticación y **Supabase** para database/storage.

### Razones
1. **Firebase Auth**: Mejor UX para Google Sign-In, recovery password, etc.
2. **Supabase**: PostgreSQL completo, Row-Level Security, real-time (futuro)
3. **JWT bridge**: Firebase genera JWT → Supabase valida con RLS policies
4. **Costos**: Firebase Auth gratis hasta 50k users, Supabase free tier generoso

### Consecuencias
- ✅ Mejor auth UX (Firebase tiene UI components listos)
- ✅ Backend potente (Supabase = PostgreSQL completo)
- ✅ Free tier generoso para MVP
- ❌ Complejidad de integración (2 servicios en vez de 1)
- ❌ Latencia adicional (Firebase genera JWT → Supabase valida)

### Alternativas consideradas
- **Solo Firebase**: Descartado porque Firestore es menos potente que PostgreSQL
- **Solo Supabase**: Descartado porque Auth UX es inferior a Firebase
- **Custom backend**: Descartado por tiempo de desarrollo

---

## ADR-010: UUIDs locales en vez de auto-increment IDs

**Fecha**: 2026-06-06
**Estado**: ✅ Aceptado
**Contexto**: Room entities necesitan primary keys. Elegir entre auto-increment (1, 2, 3...) o UUIDs.

### Decisión
Usar **UUIDs generados localmente** como primary keys.

### Razones
1. **Offline-first**: UUIDs se pueden generar sin conexión al servidor
2. **Merge-friendly**: No hay colisiones al sincronizar con Supabase
3. **Security**: IDs no son secuenciales (no se pueden adivinar)
4. **Multi-device**: Si en el futuro hay multi-device, no hay conflictos

### Consecuencias
- ✅ Creación de entidades 100% offline
- ✅ No hay ID conflicts al sincronizar
- ❌ Primary keys más grandes (36 chars vs. 4 bytes para Int)
- ❌ Índices ligeramente más lentos (mitigado: performance sigue siendo excelente)

### Alternativas consideradas
- **Auto-increment local**: Descartado porque puede colisionar con IDs remotos
- **Timestamp-based IDs**: Descartado porque pueden colisionar si se crean 2 registros en el mismo ms

### Implementación
```kotlin
@Entity(tableName = "athletes")
data class AthleteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    // ...
)
```

---

## 📝 Template para nuevas decisiones

```markdown
## ADR-XXX: [Título de la decisión]

**Fecha**: YYYY-MM-DD
**Estado**: 🟡 Propuesto | ✅ Aceptado | ❌ Rechazado | 🔄 Deprecado
**Contexto**: [Descripción del problema o situación]

### Decisión
[Qué se decidió]

### Razones
1. [Razón 1]
2. [Razón 2]

### Consecuencias
- ✅ [Pro 1]
- ✅ [Pro 2]
- ❌ [Con 1]
- ❌ [Con 2]

### Alternativas consideradas
- **[Alternativa 1]**: [Por qué se descartó]
- **[Alternativa 2]**: [Por qué se descartó]

### Implementación
```kotlin
// Código de ejemplo
```

### Roadmap post-MVP
- [Futuras mejoras]
```

---

**Última revisión**: 2026-06-06
**Próxima revisión**: Después de cada sprint (cada 2 semanas)
