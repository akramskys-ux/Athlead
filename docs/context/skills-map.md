# Skills Map — Mapa de habilidades por área

**Para**: Identificar qué tipo de agente/asistente es mejor para cada tarea
**Última actualización**: 2026-06-06

---

## 🎯 Propósito

Este documento mapea **tipos de tareas** a **skills requeridos** para ayudar a asignar el trabajo correcto al agente/asistente correcto (Claude Code, Cursor, GitHub Copilot, etc.).

---

## 🎨 UI/UX Development

### Skills requeridos
- ✅ Jetpack Compose
- ✅ Material3 design
- ✅ Navigation Compose
- ✅ State management (StateFlow, remember)
- ✅ Responsive layouts

### Tareas típicas
- Crear/modificar Screen Composables
- Implementar componentes reutilizables (cards, buttons, etc.)
- Diseñar formularios con validación
- Implementar navegación entre pantallas
- Aplicar design tokens D1

### Documentación a consultar
1. **Primero**: `docs/ux/design-tokens.md` (paleta, tipografía, spacing)
2. **Luego**: `docs/context/ui-context.md` (patrones, templates)
3. **Referencia**: `docs/architecture/android-architecture.md` (estructura de features)

### Herramientas recomendadas
- **Cursor**: Excelente para generar Composables completos
- **Claude Code**: Bueno para refactorizar componentes grandes
- **GitHub Copilot**: Útil para autocompletar UI patterns repetitivos

### Ejemplo de prompt
```
Crea un Composable AthleteCard que muestre:
- Avatar con iniciales del atleta
- Nombre y posición
- Número de jersey (si existe)
- Última métrica física
Debe usar los design tokens D1 de docs/ux/design-tokens.md
Máximo 100 líneas.
```

---

## 🗄️ Data Layer Development

### Skills requeridos
- ✅ Room (entities, DAOs, migrations)
- ✅ Kotlin Coroutines + Flow
- ✅ Offline-first architecture
- ✅ Foreign keys y relaciones (1:N, N:M)

### Tareas típicas
- Crear/modificar Room entities
- Escribir DAOs con queries complejas
- Implementar repositorios (Room + Network)
- Crear migrations de Room
- Mapear entities ↔ domain ↔ DTOs

### Documentación a consultar
1. **Primero**: `docs/data/data-contracts.md` (entities, relaciones)
2. **Luego**: `docs/context/data-context.md` (DAOs, repositorios, migrations)
3. **Referencia**: `docs/architecture/decision-log.md` (decisiones de data layer)

### Herramientas recomendadas
- **Claude Code**: Excelente para queries SQL complejas y migrations
- **Cursor**: Bueno para generar DAOs y repositorios
- **GitHub Copilot**: Útil para mappers (entity ↔ domain)

### Ejemplo de prompt
```
Crea un DAO para LapEntity con las siguientes queries:
1. getBestTime(athleteId, distance) → mejor tiempo de un atleta
2. getSpeedRanking(distance, limit) → ranking de velocidad con JOIN
3. getAthleteHistory(athleteId) → historial de vueltas
Todas las queries deben retornar Flow<T> y filtrar isDeleted = 0.
Usa los patterns de docs/context/data-context.md
```

---

## 🔄 Sync & WorkManager

### Skills requeridos
- ✅ WorkManager (PeriodicWorkRequest, constraints)
- ✅ Conflict resolution strategies
- ✅ Background processing
- ✅ Network calls (Ktor, Supabase)

### Tareas típicas
- Implementar SyncWorker
- Configurar constraints (network, battery)
- Manejar errores de sync con retry logic
- Implementar conflict resolution (local gana)
- Mostrar sync status en UI

### Documentación a consultar
1. **Primero**: `docs/context/sync-context.md` (WorkManager, SyncRepository)
2. **Luego**: `docs/architecture/decision-log.md` (ADR-003: local gana)
3. **Referencia**: `docs/product/offline-critical-flows.md` (qué se sincroniza)

### Herramientas recomendadas
- **Claude Code**: Mejor para lógica compleja de conflict resolution
- **Cursor**: Bueno para configurar WorkManager
- **GitHub Copilot**: Útil para boilerplate de workers

### Ejemplo de prompt
```
Implementa SyncWorker que:
1. Ejecuta cada 15 minutos con constraints (network, battery)
2. Sincroniza entities con isSynced = false
3. Marca como isSynced = true si sync exitoso
4. Retries con backoff exponencial si falla
Usa los patterns de docs/context/sync-context.md
```

---

## 🧪 Testing

### Skills requeridos
- ✅ JUnit + Truth assertions
- ✅ Compose UI tests
- ✅ Room in-memory database
- ✅ Fake repositories/DAOs
- ✅ Coroutine testing (runTest)

### Tareas típicas
- Escribir unit tests para ViewModels
- Escribir DAO tests con in-memory database
- Escribir Compose UI tests
- Crear fakes para repositories
- Escribir integration tests

### Documentación a consultar
1. **Referencia**: `docs/context/data-context.md` (testing DAOs)
2. **Referencia**: `docs/context/ui-context.md` (testing Composables)
3. **Referencia**: `docs/context/sync-context.md` (testing SyncWorker)

### Herramientas recomendadas
- **Claude Code**: Excelente para generar test suites completos
- **Cursor**: Bueno para escribir tests individuales
- **GitHub Copilot**: Útil para boilerplate de test setup

### Ejemplo de prompt
```
Crea unit tests para AthleteRepository:
1. createAthlete_savesToDaoWithIsSyncedFalse
2. syncAthletes_marksAsSyncedAfterSuccess
3. syncAthletes_doesNotMarkAsSynced_whenFails
Usa FakeAthleteDao y FakeSupabaseClient.
Sigue los patterns de docs/context/data-context.md
```

---

## 🏗️ Architecture & Refactoring

### Skills requeridos
- ✅ Clean Architecture
- ✅ MVVM pattern
- ✅ Dependency Injection (Hilt)
- ✅ Modularización
- ✅ Refactoring patterns

### Tareas típicas
- Dividir God objects (archivos >300 líneas)
- Extraer Use Cases de ViewModels
- Crear nuevos módulos (features, core-*)
- Refactorizar arquitectura legacy
- Aplicar límites de tamaño

### Documentación a consultar
1. **Primero**: `docs/architecture/android-architecture.md` (estructura modular)
2. **Luego**: `docs/architecture/decision-log.md` (decisiones arquitectónicas)
3. **Referencia**: `AGENTS.md` (reglas y límites)

### Herramientas recomendadas
- **Claude Code**: Mejor para refactorings grandes (SessionScreen 2480 → componentes)
- **Cursor**: Bueno para extraer Use Cases
- **IntelliJ IDEA**: Útil para refactorings automáticos (Extract Method, etc.)

### Ejemplo de prompt
```
Refactoriza RosterScreen (350 líneas) para cumplir límite de 300 líneas:
1. Extrae AthleteList en componente separado
2. Extrae EmptyState y ErrorState
3. Mantén RosterContent como orquestador
Sigue los patterns de docs/context/ui-context.md
```

---

## 🔐 Auth & Security

### Skills requeridos
- ✅ Firebase Auth
- ✅ Google Sign-In (OAuth)
- ✅ JWT tokens
- ✅ Role-based access control
- ✅ Secure storage (EncryptedSharedPreferences)

### Tareas típicas
- Implementar login/register con Firebase
- Configurar Google Sign-In
- Guardar tokens de forma segura
- Validar roles (Coach/Atleta)
- Manejar refresh tokens

### Documentación a consultar
1. **Referencia**: `CLAUDE.md` (auth overview)
2. **Referencia**: `docs/architecture/decision-log.md` (ADR-009: Firebase Auth)
3. **Documentación externa**: Firebase Auth for Android

### Herramientas recomendadas
- **Claude Code**: Bueno para flujos de auth completos
- **Cursor**: Útil para configurar Firebase
- **GitHub Copilot**: Útil para boilerplate de auth

### Ejemplo de prompt
```
Implementa FirebaseAuthManager con:
1. signInWithEmail(email, password)
2. signInWithGoogle(idToken)
3. signOut()
4. getCurrentUser() → Flow<User?>
Cachea el token en EncryptedSharedPreferences.
```

---

## 📊 Analytics & Charts

### Skills requeridos
- ✅ Vico charts library (o similar)
- ✅ Data aggregation
- ✅ Date/time manipulation
- ✅ Statistical calculations

### Tareas típicas
- Crear gráficos de tendencias (peso, rendimiento)
- Calcular rankings
- Agregar métricas por período (semana, mes)
- Mostrar comparaciones (antes/después)

### Documentación a consultar
1. **Referencia**: `docs/data/data-contracts.md` (queries para analytics)
2. **Referencia**: `docs/context/data-context.md` (queries complejas con JOIN)
3. **Documentación externa**: Vico charts

### Herramientas recomendadas
- **Claude Code**: Excelente para cálculos estadísticos complejos
- **Cursor**: Bueno para configurar charts
- **GitHub Copilot**: Útil para transformar data para charts

### Ejemplo de prompt
```
Crea un gráfico de línea que muestre:
- Peso del atleta en los últimos 30 días
- Usa Vico library
- Marca puntos donde peso disminuyó (verde) o aumentó (rojo)
- Tooltip con fecha y valor exacto
```

---

## 📝 Documentation

### Skills requeridos
- ✅ Markdown
- ✅ Technical writing
- ✅ Architecture documentation
- ✅ API documentation (KDoc)

### Tareas típicas
- Actualizar ADRs (decision-log.md)
- Documentar nuevos features en north-star.md
- Escribir KDoc para APIs públicas
- Crear diagramas de flujo
- Actualizar current-state.md

### Documentación a consultar
1. **Referencia**: `docs/architecture/decision-log.md` (template de ADRs)
2. **Referencia**: `AGENTS.md` (guía para agentes)

### Herramientas recomendadas
- **Claude Code**: Excelente para generar documentación completa
- **Cursor**: Bueno para generar KDoc
- **Notion/Obsidian**: Útil para diagramas visuales

### Ejemplo de prompt
```
Crea un ADR (Architecture Decision Record) para:
Decisión: Usar Vico en vez de MPAndroidChart para gráficos
Razones: Mejor integración con Compose, más ligero, mejor docs
Consecuencias: Pros y contras
Usa el template de docs/architecture/decision-log.md
```

---

## 🎬 Feature Implementation (End-to-End)

### Skills requeridos
- ✅ Todos los anteriores (UI, Data, Sync, Testing)
- ✅ Product thinking
- ✅ Task breakdown

### Tareas típicas
- Implementar un feature completo (ej: feature-rankings)
- Coordinar UI + Data + Sync
- Escribir tests para toda la feature
- Actualizar documentación

### Documentación a consultar
1. **Primero**: `docs/product/north-star.md` (alcance del feature)
2. **Luego**: `docs/architecture/android-architecture.md` (estructura del módulo)
3. **Referencia**: Todos los docs de context/

### Herramientas recomendadas
- **Claude Code**: Mejor para features end-to-end (puede coordinar múltiples archivos)
- **Cursor**: Bueno para implementar partes específicas
- **GitHub Copilot**: Útil para acelerar código repetitivo

### Ejemplo de prompt
```
Implementa feature-rankings completo:

1. UI (RankingsScreen):
   - Tabs para categorías (Velocidad, Resistencia, Tiros)
   - Filtros (deporte, género)
   - Lista de rankings con podio (top 3 destacado)

2. Data (RankingsRepository):
   - Query para ranking de velocidad (mejor tiempo en 100m)
   - Query para ranking de tiros (% de acierto)

3. ViewModel (RankingsViewModel):
   - StateFlow con rankings
   - Eventos para cambiar categoría/filtros

4. Tests:
   - Unit tests para repository
   - UI tests para filtros

Usa los patterns de:
- docs/context/ui-context.md (UI)
- docs/context/data-context.md (Data)
- docs/ux/design-tokens.md (Design)

Límites: 300 líneas por Composable, 250 por ViewModel.
```

---

## 📊 Resumen por herramienta

### Claude Code (este asistente)
**Fortalezas**:
- Features end-to-end
- Refactorings grandes
- Queries SQL complejas
- Conflict resolution logic
- Documentación completa
- Multi-file coordination

**Usar para**:
- Implementar features completos
- Refactorizar God objects
- Crear test suites
- Escribir ADRs

---

### Cursor
**Fortalezas**:
- Edición inline rápida
- Generar Composables
- Configurar dependencias
- Escribir DAOs individuales

**Usar para**:
- Crear componentes UI nuevos
- Escribir queries específicas
- Implementar ViewModels
- Configurar Hilt modules

---

### GitHub Copilot
**Fortalezas**:
- Autocompletado inteligente
- Boilerplate rápido
- Patterns repetitivos

**Usar para**:
- Completar mappers
- Generar tests boilerplate
- Escribir KDoc
- Repetir patterns existentes

---

## 🎓 Learning paths

### Para agentes nuevos en el proyecto

#### Paso 1: Entender el contexto
1. Lee `CLAUDE.md` (visión general)
2. Lee `docs/product/north-star.md` (alcance del MVP)
3. Lee `docs/architecture/android-architecture.md` (estructura)

#### Paso 2: Especializarte en un área
- **UI**: Lee `docs/ux/design-tokens.md` + `docs/context/ui-context.md`
- **Data**: Lee `docs/data/data-contracts.md` + `docs/context/data-context.md`
- **Sync**: Lee `docs/context/sync-context.md`

#### Paso 3: Implementar una tarea pequeña
- Crea un componente UI simple (ej: AthleteCard)
- Escribe un DAO query (ej: getAthleteById)
- Implementa un test unitario

#### Paso 4: Implementar un feature completo
- Elige un feature pequeño (ej: feature-profile)
- Implementa UI + Data + Tests
- Actualiza documentación

---

**Última revisión**: 2026-06-06
**Próxima revisión**: Según evolucione el equipo
