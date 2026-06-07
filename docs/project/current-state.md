# Current State — Estado actual del proyecto

**Fecha**: 2026-06-06
**Versión**: 0.1.0 (Pre-MVP)
**Estado**: 🚧 En construcción desde cero

---

## 📊 Estado general

### Fase actual: **Fase 0 — Setup y documentación**

El proyecto Athlead se está reconstruyendo desde cero tras identificar anti-patrones críticos en la versión anterior. En este momento, la estructura de documentación está completa y lista para guiar el desarrollo del MVP.

---

## ✅ Completado

### Documentación (100%)
- [x] CLAUDE.md — Memoria del proyecto
- [x] AGENTS.md — Instrucciones para agentes de IA
- [x] docs/product/north-star.md — Definición del MVP
- [x] docs/product/offline-critical-flows.md — Flujos offline-first
- [x] docs/architecture/android-architecture.md — Arquitectura modular
- [x] docs/architecture/decision-log.md — ADRs (10 decisiones documentadas)
- [x] docs/data/data-contracts.md — Entidades Room
- [x] docs/ux/design-tokens.md — Sistema de diseño D1
- [x] docs/context/ui-context.md — Contexto para trabajo de UI
- [x] docs/context/data-context.md — Contexto para trabajo de datos
- [x] docs/context/sync-context.md — Contexto para sincronización
- [x] docs/context/session-closeout-context.md — Flujo de cierre de sesión
- [x] docs/context/skills-map.md — Mapa de habilidades por área
- [x] docs/project/current-state.md — Este archivo
- [x] docs/project/build-plan.md — Plan de construcción

### Repositorio Git
- [x] Repositorio inicializado
- [x] Main branch configurado
- [x] .gitignore básico
- [x] Estructura de carpetas `/docs`

---

## 🚧 En progreso

**Nada en este momento**. El desarrollo del MVP comenzará tras aprobación de la documentación.

---

## ⏳ Pendiente

### Fase 1: Infraestructura (semanas 1-2)

#### Módulos core
- [ ] Setup de proyecto multi-módulo en Gradle
- [ ] Módulo `app` (aplicación principal)
- [ ] Módulo `core-ui` (componentes reutilizables + theme D1)
- [ ] Módulo `core-data` (repositorios + use cases)
- [ ] Módulo `core-database` (Room entities + DAOs)
- [ ] Módulo `core-network` (Ktor + Supabase)
- [ ] Módulo `core-auth` (Firebase Auth)
- [ ] Módulo `sync` (WorkManager workers)

#### Configuración Hilt
- [ ] Setup de Hilt en módulo `app`
- [ ] Dependency injection para Room
- [ ] Dependency injection para Ktor/Supabase
- [ ] Dependency injection para Firebase Auth

#### Room database
- [ ] Definir AthleedDatabase
- [ ] Crear entidades: AthleteEntity, PhysicalMetricEntity, ShotEntity, LapEntity, SessionEntity, MatchEntity, MatchEventEntity
- [ ] Crear DAOs para cada entidad
- [ ] TypeConverters (Instant ↔ Long)
- [ ] Migrations setup

#### Supabase backend
- [ ] Crear proyecto en Supabase
- [ ] Configurar tablas (athletes, physical_metrics, shots, laps, sessions, matches)
- [ ] Configurar Row-Level Security (RLS) policies
- [ ] Integrar Firebase JWT con Supabase

#### Design system D1
- [ ] Importar fuentes (Archivo, Hanken Grotesk, IBM Plex Mono)
- [ ] Configurar MaterialTheme con paleta D1
- [ ] Crear componentes base (AthleedButton, AthleedTextField, AthleedCard)
- [ ] Spacing, elevation, corner radius tokens

---

### Fase 2: Auth (semana 2)

- [ ] Configurar Firebase Auth en Android
- [ ] Implementar FirebaseAuthManager
- [ ] LoginScreen + LoginViewModel
- [ ] RegisterScreen + RegisterViewModel
- [ ] Google Sign-In integration
- [ ] Recuperación de contraseña
- [ ] AuthCache (EncryptedSharedPreferences)
- [ ] Roles: Coach/Atleta (guardado en Firestore)
- [ ] Navegación condicional (auth vs. main)

---

### Fase 3: Features core (semanas 3-4)

#### feature-coach (Dashboard)
- [ ] CoachDashboardScreen
- [ ] CoachDashboardViewModel
- [ ] Métricas agregadas (atletas activos, sesiones, métricas)
- [ ] Navegación a Roster, Rankings, Profile

#### feature-roster (CRUD atletas)
- [ ] RosterScreen (lista de atletas)
- [ ] RosterViewModel
- [ ] AthleteDetailScreen
- [ ] AthleteDetailViewModel
- [ ] CreateAthleteScreen + formulario
- [ ] EditAthleteScreen
- [ ] Componente AthleteCard
- [ ] Soft delete de atletas
- [ ] Tests: unit (ViewModel, Repository), UI (Compose)

#### feature-rankings
- [ ] RankingsScreen
- [ ] RankingsViewModel
- [ ] Tabs para categorías (Velocidad, Resistencia, Tiros)
- [ ] Filtros (deporte, género)
- [ ] Rankings calculados desde Room
- [ ] Tests

---

### Fase 4: Entrenamiento y partidos (semanas 5-6)

#### Sesiones de entrenamiento
- [ ] SessionListScreen
- [ ] CreateSessionScreen (formulario)
- [ ] SessionDetailScreen (activa)
- [ ] Registrar tiros (ShotEntity)
  - [ ] Canvas interactivo de cancha
  - [ ] Marcador de acierto/fallo
  - [ ] Heatmap de tiros
- [ ] Registrar vueltas (LapEntity)
  - [ ] Cronómetro en Compose
  - [ ] Selección de distancia
  - [ ] Lista de vueltas con tiempos
- [ ] Cierre de sesión (ver docs/context/session-closeout-context.md)
  - [ ] Bottom sheet con resumen
  - [ ] Cálculo de % acierto, mejor tiempo
  - [ ] Actualizar status a COMPLETED
- [ ] Tests

#### Partidos
- [ ] MatchListScreen
- [ ] CreateMatchScreen (formulario)
- [ ] MatchDetailScreen (en progreso)
- [ ] Registrar eventos (MatchEventEntity — local-only)
  - [ ] Puntos, asistencias, rebotes, faltas
  - [ ] Play-by-play list
  - [ ] Estadísticas por jugador
- [ ] Cierre de partido (similar a sesiones)
- [ ] Tests

---

### Fase 5: Sync + Polish (semanas 7-8)

#### Sincronización
- [ ] SyncWorker (WorkManager)
- [ ] SyncRepository (sync de todas las entities excepto MatchEventEntity)
- [ ] SyncManager (schedule periodic sync, manual sync)
- [ ] Conflict resolution (local gana)
- [ ] Sync status UI en ProfileScreen
- [ ] Manejo de errores (retry con backoff)
- [ ] Tests de sync logic

#### Métricas físicas
- [ ] PhysicalMetricListScreen (historial)
- [ ] CreatePhysicalMetricScreen (formulario)
- [ ] Gráfico de tendencias (peso en últimos 30 días)
- [ ] Tests

#### feature-analytics (opcional en MVP)
- [ ] AnalyticsScreen con gráficos
- [ ] Comparación de rendimiento
- [ ] Exportación de datos (JSON)

#### feature-profile
- [ ] ProfileScreen
- [ ] Información del usuario (nombre, email, rol)
- [ ] Sync status indicator
- [ ] Botón de logout
- [ ] Configuración (tema, idioma)
- [ ] Tests

#### Polish
- [ ] Error states en todas las pantallas
- [ ] Empty states
- [ ] Loading indicators
- [ ] Accessibility audit (content descriptions, touch targets)
- [ ] Performance optimization (lazy loading, pagination)
- [ ] Animations (optional)

---

## 🐛 Issues conocidos

**Ninguno aún** — El proyecto se está construyendo desde cero.

---

## 📦 Dependencias (a agregar)

### Core
```kotlin
// Kotlin
kotlin("1.9.20")
kotlinx-coroutines-android:1.7.3
kotlinx-datetime:0.4.1

// Android
androidx-core-ktx:1.12.0
androidx-lifecycle-runtime-ktx:2.6.2
androidx-activity-compose:1.8.1

// Compose
androidx-compose-bom:2023.10.01
material3:1.1.2
androidx-navigation-compose:2.7.5

// Hilt
hilt-android:2.48
hilt-navigation-compose:1.1.0

// Room
room-runtime:2.6.0
room-ktx:2.6.0

// Ktor
ktor-client-android:2.3.5
ktor-client-content-negotiation:2.3.5
ktor-serialization-kotlinx-json:2.3.5

// Supabase
supabase-kotlin:1.3.2

// Firebase
firebase-bom:32.5.0
firebase-auth-ktx
firebase-firestore-ktx

// WorkManager
work-runtime-ktx:2.9.0

// Testing
junit:4.13.2
androidx-test-ext-junit:1.1.5
androidx-test-espresso-core:3.5.1
truth:1.1.4
turbine:1.0.0
```

---

## 🔗 Links útiles

- **Repositorio**: (por definir)
- **Supabase project**: (por crear)
- **Firebase project**: (por crear)
- **Figma designs**: (si aplica)

---

## 📝 Notas

### Decisiones pendientes
- [ ] Definir nombre del paquete (ej: `com.tecmonterrey.athlead`)
- [ ] Configurar CI/CD (GitHub Actions?)
- [ ] Definir estrategia de versionamiento (semantic versioning)

### Riesgos identificados
- **Firebase Auth + Supabase JWT**: Requiere configuración cuidadosa de RLS policies
- **WorkManager constraints**: Puede no ejecutarse en Doze mode (mitigado con constraints)
- **Room migrations**: Requiere testing exhaustivo para evitar pérdida de datos

---

## 🎯 Próximos pasos inmediatos

1. **Revisar y aprobar documentación**: Este archivo + build-plan.md
2. **Crear proyecto Android**: Multi-módulo con Gradle
3. **Setup Hilt**: Configurar DI en módulo `app`
4. **Crear Room database**: Entities + DAOs
5. **Configurar Supabase**: Backend + RLS policies
6. **Implementar auth**: Firebase Auth + login/register

---

**Última actualización**: 2026-06-06
**Actualizar**: Cada viernes al cierre de sprint (semanas)
