# Athlead — Project Memory

**Última actualización**: 2026-06-06

## 🎯 Resumen ejecutivo

Athlead es una aplicación Android **offline-first** para coaches universitarios del Tec de Monterrey que permite la gestión de atletas, métricas físicas, rankings, sesiones de entrenamiento y partidos. La app se reconstruye desde cero tras identificar anti-patrones críticos en la versión anterior (SessionScreen de 2480 líneas).

**Filosofía core**: Room es source of truth. La app funciona completamente sin conexión. WorkManager sincroniza con Supabase en segundo plano. En conflictos, la versión local siempre gana.

---

## 📱 Stack tecnológico

### Core
- **Lenguaje**: Kotlin 1.9+
- **UI**: Jetpack Compose + Material3
- **Inyección de dependencias**: Hilt
- **Base de datos**: Room (source of truth)
- **Networking**: Ktor Client + Supabase SDK
- **Auth**: Firebase Auth (Email + Google Sign-In)
- **Sincronización**: WorkManager (PeriodicWorkRequest)
- **Navegación**: Navigation Compose

### Versiones objetivo
- Min SDK: 26 (Android 8.0)
- Target SDK: 34 (Android 14)
- Compile SDK: 34

---

## 🏗️ Arquitectura modular

```
app/
├── core-ui/          → Componentes Compose reutilizables, theme D1
├── core-data/        → Repositorios, lógica de sync
├── core-database/    → Room entities, DAOs, migrations
├── core-network/     → Ktor client, Supabase config
├── core-auth/        → Firebase Auth, gestión de roles
├── sync/             → WorkManager workers para background sync
├── feature-auth/     → Login, Google Sign-In, recuperación
├── feature-coach/    → Dashboard coach, métricas agregadas
├── feature-roster/   → Gestión de roster, atletas
├── feature-rankings/ → Rankings por categoría (velocidad, fuerza, etc.)
├── feature-analytics/→ Análisis de rendimiento, gráficos
└── feature-profile/  → Perfil de usuario, configuración
```

### Reglas de dependencia
- ✅ Features pueden depender de `core-*`
- ❌ Features NO pueden depender entre sí
- ❌ `core-*` NO pueden depender de features
- ✅ `core-data` depende de `core-database` y `core-network`

---

## 🗄️ Modelo de datos (Room)

### Entidades principales

```kotlin
// Todas las entidades tienen:
// - id: String (UUID generado localmente)
// - isSynced: Boolean (false hasta sync exitoso con Supabase)
// - createdAt: Instant
// - updatedAt: Instant

AthleteEntity
PhysicalMetricEntity  // Métricas como peso, altura, % grasa
ShotEntity            // Tiros en cancha (basket/soccer)
LapEntity             // Vueltas en pista
SessionEntity         // Sesiones de entrenamiento
MatchEntity           // Partidos
MatchEventEntity      // Eventos de partido (local-only en MVP, NO se sincroniza)
```

### Flujo de sincronización
1. Usuario crea/edita datos → se guardan en Room con `isSynced = false`
2. WorkManager ejecuta `SyncWorker` cada 15 min (si hay conexión)
3. `SyncWorker` envía registros con `isSynced = false` a Supabase
4. Si sync exitoso → marca `isSynced = true`
5. Si hay conflicto → **local gana siempre** (sobrescribe remoto)

---

## 🎨 Sistema de diseño D1 "Azul Verificado"

### Paleta de colores

```kotlin
// Primary (Blues)
Navy      = #0D1B3D   // Fondos oscuros, headers
Azure     = #1B43D6   // Primary actions
Eléctrico = #2D6BFF   // Hover, selección

// Accent
Verificado = #16A06A  // Success, métricas positivas

// Neutrals
Bruma     = #EEF2FC   // Fondos claros
Papel     = #FAFBFE   // Background principal
```

### Tipografía
- **Display**: Archivo 800 (títulos, números grandes)
- **Body**: Hanken Grotesk (texto general, UI)
- **Mono**: IBM Plex Mono (códigos, IDs, timestamps)

### Scale tokens
- Spacing: 4dp base (4, 8, 12, 16, 24, 32, 48, 64)
- Border radius: 4dp (small), 8dp (medium), 12dp (large)
- Elevation: 0dp, 2dp, 4dp, 8dp

---

## 🔐 Autenticación y roles

### Firebase Auth
- Email/Password (registro manual)
- Google Sign-In (OAuth)
- Recuperación de contraseña por email

### Roles
- **Coach**: acceso completo (CRUD atletas, sesiones, partidos)
- **Atleta**: solo lectura de sus propias métricas

**Nota**: El rol se guarda en Firestore (`users/{uid}/role`) y se cachea localmente en `SharedPreferences` tras login exitoso.

---

## 🚫 Anti-patrones identificados (versión anterior)

### ❌ SessionScreen de 2480 líneas
- **Problema**: God object, imposible de mantener
- **Solución**: Límite estricto de **300 líneas por Composable**, **250 por ViewModel**
- **Estrategia**: Componentes pequeños, un ViewModel por pantalla, casos de uso para lógica compleja

### ❌ Lógica de negocio en Composables
- **Problema**: UI tests frágiles, lógica no reutilizable
- **Solución**: ViewModels con StateFlow, casos de uso en `core-data`

### ❌ Sincronización manual
- **Problema**: Usuario debía tocar "Sync" manualmente
- **Solución**: WorkManager automático + UI para forzar sync si es necesario

---

## 📦 Offline-first: Flujos críticos

### Funcionan sin conexión (100% offline)
1. **Crear/editar atletas**
2. **Registrar métricas físicas** (peso, altura, % grasa)
3. **Registrar tiros en cancha** (ShotEntity)
4. **Registrar vueltas en pista** (LapEntity)
5. **Crear/cerrar sesiones de entrenamiento**
6. **Ver rankings locales** (calculados desde Room)
7. **Registrar eventos de partido** (MatchEventEntity — local-only)

### Requieren conexión eventual
- **Auth (login/registro)**: requiere Firebase online
- **Sincronización de datos**: WorkManager intenta cada 15 min
- **Fetch de atletas remotos**: solo si se agrega un nuevo atleta desde otro dispositivo

---

## 🧪 Testing strategy

### Unit tests
- ViewModels (StateFlow, casos de uso)
- Repositorios (mocks de Room + Supabase)
- Casos de uso (lógica de negocio aislada)

### UI tests
- Compose tests con `ComposeTestRule`
- Screenshots con Paparazzi
- Accessibility checks

### Integration tests
- Room migrations
- Sync logic (WorkManager + fake network)

---

## 🚀 Roadmap MVP

### Fase 1: Infraestructura (semana 1-2)
- [ ] Setup modular + Hilt
- [ ] Room entities + DAOs
- [ ] Supabase client + auth
- [ ] Design system D1

### Fase 2: Auth (semana 2)
- [ ] Firebase Auth + Google Sign-In
- [ ] Roles (Coach/Atleta)
- [ ] Navegación condicional por rol

### Fase 3: Features core (semana 3-4)
- [ ] feature-roster: CRUD atletas
- [ ] feature-rankings: Rankings por categoría
- [ ] feature-coach: Dashboard con métricas

### Fase 4: Entrenamiento y partidos (semana 5-6)
- [ ] Registro de sesiones
- [ ] Tiros y vueltas (ShotEntity, LapEntity)
- [ ] MatchEntity + MatchEventEntity (local-only)

### Fase 5: Sync + Polish (semana 7-8)
- [ ] WorkManager sync workers
- [ ] Manejo de conflictos (local gana)
- [ ] Error states + retry logic
- [ ] Accessibility audit

---

## 📋 Reglas de desarrollo

### Límites de tamaño
- **Composables**: máximo 300 líneas
- **ViewModels**: máximo 250 líneas
- Si se excede → refactorizar en componentes/casos de uso

### Naming conventions
- Composables: `PascalCase` (ej: `AthleteCard`)
- ViewModels: `PascalCaseViewModel` (ej: `RosterViewModel`)
- Casos de uso: `VerbNounUseCase` (ej: `GetAthletesUseCase`)
- Room entities: `PascalCaseEntity` (ej: `AthleteEntity`)

### Commits
- Convencional: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`
- Scope: `feat(roster): add athlete detail screen`

---

## 🔗 Referencias

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Room Persistence](https://developer.android.com/training/data-storage/room)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Material3 Design Kit](https://m3.material.io/)
- [Firebase Auth for Android](https://firebase.google.com/docs/auth/android/start)

---

**Mantra del proyecto**: *Offline-first. Room es ley. Local gana siempre.*
