# AGENTS.md — Instrucciones para agentes de IA

**Para**: Claude Code, Cursor, Windsurf, GitHub Copilot, y otros asistentes trabajando en Athlead
**Última actualización**: 2026-06-06

---

## 🎯 Contexto del proyecto

Estás trabajando en **Athlead**, una aplicación Android offline-first para coaches universitarios del Tec de Monterrey. Lee `CLAUDE.md` para entender la arquitectura completa.

### Puntos críticos que DEBES conocer

1. **Room es source of truth** — NUNCA consideres el backend como fuente primaria
2. **Offline-first** — Todas las features core deben funcionar sin conexión
3. **Local gana** — En conflictos de sincronización, la versión local sobrescribe la remota
4. **Límites estrictos** — Máximo 300 líneas por Composable, 250 por ViewModel
5. **MatchEventEntity es local-only** — NO se sincroniza con Supabase en el MVP

---

## 📚 Documentación a consultar según el tipo de tarea

### Trabajando en UI/UX
1. **Primero lee**: `docs/ux/design-tokens.md` (paleta D1, tipografía, spacing)
2. **Luego consulta**: `docs/context/ui-context.md` (patrones de composables, navegación)
3. **Referencia**: `docs/architecture/android-architecture.md` (reglas de features)

### Trabajando en datos/Room
1. **Primero lee**: `docs/data/data-contracts.md` (entidades Room, relaciones)
2. **Luego consulta**: `docs/context/data-context.md` (repositorios, DAOs, migrations)
3. **Importante**: Todas las entidades tienen `isSynced: Boolean`

### Trabajando en sincronización
1. **Primero lee**: `docs/context/sync-context.md` (WorkManager, conflict resolution)
2. **Luego consulta**: `docs/architecture/decision-log.md` (decisión #003 sobre sync)
3. **Regla oro**: Local gana siempre en conflictos

### Implementando un nuevo feature
1. **Primero lee**: `docs/product/north-star.md` (alcance del MVP)
2. **Luego consulta**: `docs/architecture/android-architecture.md` (estructura modular)
3. **Verifica**: `docs/product/offline-critical-flows.md` (debe funcionar offline?)

### Debugging o refactoring
1. **Primero lee**: `docs/project/current-state.md` (qué está hecho, qué falta)
2. **Luego consulta**: `docs/architecture/decision-log.md` (por qué se hizo así)
3. **Plan**: `docs/project/build-plan.md` (prioridades)

---

## 🚨 Reglas obligatorias

### Arquitectura
- ✅ Features solo dependen de `core-*` modules
- ❌ Features NO pueden depender entre sí
- ✅ `core-data` orquesta Room + Network
- ❌ ViewModels NO pueden hablar directamente con Room o Ktor

### Composables
- ✅ Máximo 300 líneas (si llegas a 250, empieza a refactorizar)
- ✅ Un Composable = una responsabilidad
- ✅ Parámetros explícitos (evita `Modifier = Modifier` sin documentar)
- ❌ No pongas lógica de negocio en `@Composable` (usa ViewModel)

### ViewModels
- ✅ Máximo 250 líneas
- ✅ Usa `StateFlow` para UI state
- ✅ Casos de uso para lógica compleja (ej: `GetRankingsUseCase`)
- ❌ No hagas queries directas a Room (usa Repository)

### Room entities
- ✅ Todas tienen `id: String` (UUID local)
- ✅ Todas tienen `isSynced: Boolean`
- ✅ Todas tienen `createdAt` y `updatedAt` (Instant)
- ❌ NO uses `@PrimaryKey(autoGenerate = true)` (usamos UUIDs)

### Sincronización
- ✅ WorkManager para background sync (cada 15 min)
- ✅ `isSynced = false` al crear/editar localmente
- ✅ `isSynced = true` solo después de sync exitoso
- ❌ NO sincronices `MatchEventEntity` en MVP (es local-only)

---

## 🎨 Sistema de diseño D1

### Paleta (importa de `core-ui/theme`)

```kotlin
// Ya está definido en MaterialTheme
Primary       = Navy (#0D1B3D)
PrimaryVariant = Azure (#1B43D6)
Secondary     = Eléctrico (#2D6BFF)
Success       = Verificado (#16A06A)
Background    = Papel (#FAFBFE)
Surface       = Bruma (#EEF2FC)
```

### Tipografía

```kotlin
// MaterialTheme.typography ya está configurado
displayLarge  → Archivo 800, 57sp
headlineMedium → Archivo 800, 28sp
bodyLarge     → Hanken Grotesk, 16sp
labelSmall    → IBM Plex Mono, 11sp
```

### Spacing (usa `Dp` extensions en `core-ui`)

```kotlin
SpacingXXS = 4.dp
SpacingXS  = 8.dp
SpacingS   = 12.dp
SpacingM   = 16.dp
SpacingL   = 24.dp
SpacingXL  = 32.dp
SpacingXXL = 48.dp
```

---

## 🧩 Patrones recomendados

### Estructura de un feature module

```
feature-roster/
├── RosterScreen.kt           // Screen Composable (root)
├── RosterViewModel.kt        // StateFlow + eventos
├── components/
│   ├── AthleteCard.kt        // Componente reutilizable
│   └── AthleteListItem.kt
└── navigation/
    └── RosterNavigation.kt   // NavGraphBuilder extension
```

### Template de Screen Composable

```kotlin
@Composable
fun RosterScreen(
    viewModel: RosterViewModel = hiltViewModel(),
    onNavigateToDetail: (athleteId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RosterContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToDetail = onNavigateToDetail,
        modifier = modifier
    )
}

@Composable
private fun RosterContent(
    uiState: RosterUiState,
    onEvent: (RosterEvent) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // UI logic aquí
}
```

### Template de ViewModel

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
            // lógica
        }
    }
}
```

---

## 🛠️ Flujo de trabajo sugerido

### Al recibir una tarea nueva:

1. **Lee el contexto apropiado** (ver sección "Documentación a consultar")
2. **Verifica el current state** (`docs/project/current-state.md`)
3. **Consulta el build plan** (`docs/project/build-plan.md`) para ver prioridades
4. **Busca decisiones previas** en `docs/architecture/decision-log.md`
5. **Implementa** siguiendo los límites y patrones
6. **Actualiza `current-state.md`** si completaste un módulo/feature

### Al encontrar código legacy (>300 líneas):

1. **NO lo edites directamente** — crea un issue/tarea de refactor
2. **Propón una estrategia** de división en componentes pequeños
3. **Documenta en `decision-log.md`** si hay un cambio arquitectónico

---

## 🚀 Comandos útiles

### Build y test
```bash
./gradlew assembleDebug           # Build debug APK
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # UI tests (emulador)
./gradlew ktlintCheck             # Linting
```

### Room schema export
```bash
./gradlew kaptDebugKotlin
# Schemas en: app/schemas/
```

### Dependency graph
```bash
./gradlew :app:dependencies --configuration debugRuntimeClasspath
```

---

## 📝 Checklist antes de commit

- [ ] El código respeta los límites (300 líneas Composable, 250 ViewModel)
- [ ] No hay lógica de negocio en `@Composable`
- [ ] Room entities tienen `isSynced: Boolean`
- [ ] Features no dependen entre sí
- [ ] Usa los design tokens D1 (no colores hardcoded)
- [ ] Tests unitarios para ViewModels/UseCases
- [ ] Commit message sigue Conventional Commits (`feat:`, `fix:`, etc.)

---

## 🆘 Cuando tengas dudas

### ¿Es offline-first?
→ Consulta `docs/product/offline-critical-flows.md`

### ¿Cómo se sincroniza?
→ Consulta `docs/context/sync-context.md`

### ¿Qué colores/fuentes uso?
→ Consulta `docs/ux/design-tokens.md`

### ¿Dónde va este código?
→ Consulta `docs/architecture/android-architecture.md`

### ¿Por qué está hecho así?
→ Consulta `docs/architecture/decision-log.md`

---

## 🎓 Skills por área

Ver `docs/context/skills-map.md` para identificar qué tipo de agente/asistente es mejor para cada tarea (UI, Data, Sync, Testing, etc.).

---

**Recuerda**: Room es ley. Offline-first. Local gana. Límites estrictos.

Si algo no está claro en la documentación, **pregunta al humano** antes de asumir. Es mejor clarificar que refactorizar después.
