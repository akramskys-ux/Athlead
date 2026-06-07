# Build Plan — Plan de construcción del MVP

**Timeline**: 8 semanas
**Inicio**: 2026-06-06
**Target MVP**: 2026-08-01
**Última actualización**: 2026-06-06

---

## 🎯 Objetivo del MVP

Crear una aplicación Android **offline-first** para coaches universitarios del Tec de Monterrey que permita gestionar atletas, registrar métricas físicas, sesiones de entrenamiento y partidos, con sincronización automática cuando haya conexión.

**Criterio de éxito**: 10+ coaches activos, 50+ atletas registrados, 95%+ de features funcionan offline.

---

## 📅 Roadmap de 8 semanas

```
┌─────────────────────────────────────────────────────────┐
│ Semana 1-2: Infraestructura + Auth                      │
│ Semana 3-4: Features core (Roster, Rankings, Dashboard) │
│ Semana 5-6: Sesiones y Partidos                         │
│ Semana 7-8: Sync + Polish                               │
└─────────────────────────────────────────────────────────┘
```

---

## 🗓️ Fase 1: Infraestructura + Auth (Semanas 1-2)

**Objetivo**: Configurar proyecto Android con arquitectura modular, Room, Supabase, Firebase Auth.

### Semana 1 (2026-06-06 → 2026-06-13)

#### Días 1-2: Setup del proyecto

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| Crear proyecto Android multi-módulo | 2h | ⬜ Pendiente | — |
| Configurar Gradle (modules, versions catalog) | 1h | ⬜ Pendiente | — |
| Setup Hilt (AppModule, DatabaseModule, NetworkModule) | 2h | ⬜ Pendiente | — |
| Configurar ktlint + detekt | 1h | ⬜ Pendiente | — |

#### Días 3-4: Room database

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| Crear AthleedDatabase + TypeConverters | 1h | ⬜ Pendiente | — |
| Crear entities (Athlete, PhysicalMetric, Shot, Lap, Session, Match, MatchEvent) | 3h | ⬜ Pendiente | — |
| Crear DAOs para todas las entities | 4h | ⬜ Pendiente | — |
| Escribir tests para DAOs (in-memory database) | 3h | ⬜ Pendiente | — |

#### Día 5: Supabase backend

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| Crear proyecto en Supabase | 0.5h | ⬜ Pendiente | — |
| Crear tablas (athletes, physical_metrics, shots, laps, sessions, matches) | 2h | ⬜ Pendiente | — |
| Configurar RLS policies (solo coaches pueden escribir) | 2h | ⬜ Pendiente | — |
| Configurar SupabaseClient en core-network | 2h | ⬜ Pendiente | — |

---

### Semana 2 (2026-06-13 → 2026-06-20)

#### Días 1-2: Design system D1

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| Importar fuentes (Archivo, Hanken Grotesk, IBM Plex Mono) | 1h | ⬜ Pendiente | — |
| Configurar Theme.kt con paleta D1 | 1h | ⬜ Pendiente | — |
| Crear Color.kt, Type.kt, Spacing.kt | 1h | ⬜ Pendiente | — |
| Crear componentes base (AthleedButton, AthleedTextField, AthleedCard) | 3h | ⬜ Pendiente | — |
| Crear LoadingIndicator, EmptyState, ErrorState | 2h | ⬜ Pendiente | — |

#### Días 3-5: Firebase Auth

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| Configurar Firebase project | 0.5h | ⬜ Pendiente | — |
| Implementar FirebaseAuthManager | 2h | ⬜ Pendiente | — |
| LoginScreen + LoginViewModel | 3h | ⬜ Pendiente | — |
| RegisterScreen + RegisterViewModel | 3h | ⬜ Pendiente | — |
| Google Sign-In integration | 3h | ⬜ Pendiente | — |
| Recuperación de contraseña | 1h | ⬜ Pendiente | — |
| AuthCache (EncryptedSharedPreferences) | 1h | ⬜ Pendiente | — |
| Roles (guardado en Firestore) | 2h | ⬜ Pendiente | — |
| Navegación condicional (auth vs. main) | 2h | ⬜ Pendiente | — |
| Tests para auth flows | 3h | ⬜ Pendiente | — |

**Milestone semana 2**: ✅ Login funcional, Room operativo, Supabase conectado

---

## 🗓️ Fase 2: Features core (Semanas 3-4)

**Objetivo**: Implementar Roster (CRUD atletas), Rankings, y Dashboard coach.

### Semana 3 (2026-06-20 → 2026-06-27)

#### feature-roster (Días 1-4)

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| Crear módulo feature-roster | 0.5h | ⬜ Pendiente | — |
| AthleteRepository + GetAthletesUseCase | 2h | ⬜ Pendiente | — |
| RosterScreen + RosterViewModel | 3h | ⬜ Pendiente | — |
| AthleteCard component | 1h | ⬜ Pendiente | — |
| AthleteDetailScreen + ViewModel | 3h | ⬜ Pendiente | — |
| CreateAthleteScreen + formulario | 4h | ⬜ Pendiente | — |
| EditAthleteScreen | 2h | ⬜ Pendiente | — |
| Soft delete de atletas | 1h | ⬜ Pendiente | — |
| Navegación (Roster → Detail → Edit) | 1h | ⬜ Pendiente | — |
| Tests (unit: ViewModel, Repository; UI: Compose) | 4h | ⬜ Pendiente | — |

#### Día 5: feature-coach (Dashboard)

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| Crear módulo feature-coach | 0.5h | ⬜ Pendiente | — |
| CoachDashboardScreen + ViewModel | 3h | ⬜ Pendiente | — |
| MetricsSummaryCard (atletas activos, sesiones) | 2h | ⬜ Pendiente | — |
| Navegación a Roster, Rankings, Profile | 1h | ⬜ Pendiente | — |

---

### Semana 4 (2026-06-27 → 2026-07-04)

#### feature-rankings (Días 1-3)

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| Crear módulo feature-rankings | 0.5h | ⬜ Pendiente | — |
| RankingsRepository + queries (velocidad, resistencia, tiros) | 3h | ⬜ Pendiente | — |
| RankingsScreen + ViewModel | 3h | ⬜ Pendiente | — |
| Tabs para categorías (Velocidad, Resistencia, Tiros) | 2h | ⬜ Pendiente | — |
| Filtros (deporte, género) | 2h | ⬜ Pendiente | — |
| RankingCard component (con podio para top 3) | 2h | ⬜ Pendiente | — |
| Tests | 3h | ⬜ Pendiente | — |

#### Días 4-5: Métricas físicas

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| PhysicalMetricRepository | 2h | ⬜ Pendiente | — |
| CreatePhysicalMetricScreen (formulario) | 3h | ⬜ Pendiente | — |
| PhysicalMetricListScreen (historial) | 2h | ⬜ Pendiente | — |
| Gráfico de tendencia (peso en últimos 30 días) | 3h | ⬜ Pendiente | — |
| Tests | 2h | ⬜ Pendiente | — |

**Milestone semana 4**: ✅ Roster funcional, Rankings calculados, Dashboard con métricas

---

## 🗓️ Fase 3: Sesiones y Partidos (Semanas 5-6)

**Objetivo**: Implementar registro de sesiones (tiros, vueltas) y partidos (eventos local-only).

### Semana 5 (2026-07-04 → 2026-07-11)

#### Sesiones de entrenamiento (Días 1-5)

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| SessionRepository + queries | 2h | ⬜ Pendiente | — |
| SessionListScreen + ViewModel | 2h | ⬜ Pendiente | — |
| CreateSessionScreen (formulario) | 3h | ⬜ Pendiente | — |
| SessionDetailScreen (sesión activa) | 3h | ⬜ Pendiente | — |
| Registrar tiros: Canvas interactivo de cancha | 4h | ⬜ Pendiente | — |
| Registrar tiros: Marcador acierto/fallo | 2h | ⬜ Pendiente | — |
| Registrar tiros: Heatmap de tiros | 3h | ⬜ Pendiente | — |
| ShotRepository + queries | 1h | ⬜ Pendiente | — |
| Tests para registro de tiros | 2h | ⬜ Pendiente | — |

---

### Semana 6 (2026-07-11 → 2026-07-18)

#### Sesiones: Vueltas y cierre (Días 1-3)

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| Registrar vueltas: Cronómetro en Compose | 3h | ⬜ Pendiente | — |
| Registrar vueltas: Selección de distancia | 1h | ⬜ Pendiente | — |
| Registrar vueltas: Lista de vueltas con tiempos | 2h | ⬜ Pendiente | — |
| LapRepository + queries | 1h | ⬜ Pendiente | — |
| Cierre de sesión: Bottom sheet con resumen | 3h | ⬜ Pendiente | — |
| Cierre de sesión: Cálculo % acierto, mejor tiempo | 2h | ⬜ Pendiente | — |
| Cierre de sesión: Actualizar status a COMPLETED | 1h | ⬜ Pendiente | — |
| Tests para vueltas + cierre | 3h | ⬜ Pendiente | — |

#### Partidos (Días 4-5)

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| MatchRepository + queries | 2h | ⬜ Pendiente | — |
| MatchListScreen + ViewModel | 2h | ⬜ Pendiente | — |
| CreateMatchScreen (formulario) | 3h | ⬜ Pendiente | — |
| MatchDetailScreen (en progreso) | 3h | ⬜ Pendiente | — |
| Registrar eventos: Puntos, asistencias, rebotes | 3h | ⬜ Pendiente | — |
| Registrar eventos: Play-by-play list | 2h | ⬜ Pendiente | — |
| Estadísticas por jugador | 2h | ⬜ Pendiente | — |
| Cierre de partido (similar a sesiones) | 2h | ⬜ Pendiente | — |
| Tests para partidos | 3h | ⬜ Pendiente | — |

**Milestone semana 6**: ✅ Sesiones con tiros/vueltas funcionan offline, Partidos con eventos local-only

---

## 🗓️ Fase 4: Sync + Polish (Semanas 7-8)

**Objetivo**: Implementar sincronización con WorkManager, polish de UI, accessibility, tests finales.

### Semana 7 (2026-07-18 → 2026-07-25)

#### Sincronización (Días 1-4)

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| Crear módulo sync | 0.5h | ⬜ Pendiente | — |
| SyncRepository (sync de todas las entities) | 4h | ⬜ Pendiente | — |
| SyncWorker (WorkManager) | 2h | ⬜ Pendiente | — |
| SyncManager (schedule periodic, manual sync) | 2h | ⬜ Pendiente | — |
| Conflict resolution (local gana) | 2h | ⬜ Pendiente | — |
| Sync status UI en ProfileScreen | 2h | ⬜ Pendiente | — |
| Manejo de errores (retry con backoff) | 2h | ⬜ Pendiente | — |
| Tests de sync logic | 4h | ⬜ Pendiente | — |

#### Día 5: feature-profile

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| ProfileScreen + ViewModel | 2h | ⬜ Pendiente | — |
| Información del usuario (nombre, email, rol) | 1h | ⬜ Pendiente | — |
| Sync status indicator | 1h | ⬜ Pendiente | — |
| Botón de logout | 1h | ⬜ Pendiente | — |
| Configuración (tema, idioma) | 2h | ⬜ Pendiente | — |
| Tests | 2h | ⬜ Pendiente | — |

---

### Semana 8 (2026-07-25 → 2026-08-01)

#### Polish (Días 1-3)

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| Error states en todas las pantallas | 3h | ⬜ Pendiente | — |
| Empty states | 2h | ⬜ Pendiente | — |
| Loading indicators consistentes | 1h | ⬜ Pendiente | — |
| Accessibility audit (content descriptions, touch targets) | 3h | ⬜ Pendiente | — |
| Performance optimization (lazy loading, pagination) | 3h | ⬜ Pendiente | — |
| Animations (opcional) | 2h | ⬜ Pendiente | — |

#### Testing final (Días 4-5)

| Tarea | Esfuerzo | Estado | Asignado |
|-------|----------|--------|----------|
| Integration tests (Room + Repository) | 4h | ⬜ Pendiente | — |
| End-to-end tests (flujos completos) | 4h | ⬜ Pendiente | — |
| Manual testing en dispositivos físicos | 4h | ⬜ Pendiente | — |
| Regression testing (asegurar que no hay bugs) | 3h | ⬜ Pendiente | — |
| Performance testing (tiempo de carga, memoria) | 2h | ⬜ Pendiente | — |

**Milestone semana 8**: 🎉 **MVP completo y listo para beta testing**

---

## 📊 Tracker de progreso

### Resumen por fase

| Fase | Semanas | Tareas | Completadas | % | Estado |
|------|---------|--------|-------------|---|--------|
| Fase 1: Infraestructura + Auth | 1-2 | 30 | 0 | 0% | ⬜ Pendiente |
| Fase 2: Features core | 3-4 | 25 | 0 | 0% | ⬜ Pendiente |
| Fase 3: Sesiones y Partidos | 5-6 | 28 | 0 | 0% | ⬜ Pendiente |
| Fase 4: Sync + Polish | 7-8 | 22 | 0 | 0% | ⬜ Pendiente |
| **Total** | **8** | **105** | **0** | **0%** | **⬜ Pendiente** |

### Progreso por módulo

| Módulo | Tareas | Completadas | % | Estado |
|--------|--------|-------------|---|--------|
| app (setup) | 4 | 0 | 0% | ⬜ Pendiente |
| core-ui | 7 | 0 | 0% | ⬜ Pendiente |
| core-data | 8 | 0 | 0% | ⬜ Pendiente |
| core-database | 8 | 0 | 0% | ⬜ Pendiente |
| core-network | 4 | 0 | 0% | ⬜ Pendiente |
| core-auth | 10 | 0 | 0% | ⬜ Pendiente |
| sync | 7 | 0 | 0% | ⬜ Pendiente |
| feature-auth | 10 | 0 | 0% | ⬜ Pendiente |
| feature-coach | 4 | 0 | 0% | ⬜ Pendiente |
| feature-roster | 10 | 0 | 0% | ⬜ Pendiente |
| feature-rankings | 7 | 0 | 0% | ⬜ Pendiente |
| feature-profile | 6 | 0 | 0% | ⬜ Pendiente |
| Sessions (en feature-coach) | 13 | 0 | 0% | ⬜ Pendiente |
| Matches (en feature-coach) | 9 | 0 | 0% | ⬜ Pendiente |
| Polish + Testing | 14 | 0 | 0% | ⬜ Pendiente |

---

## 🎯 Criterios de aceptación del MVP

### Must-have (bloqueantes para launch)

- [ ] **Auth**: Login con email/password + Google Sign-In
- [ ] **Roster**: CRUD de atletas funciona 100% offline
- [ ] **Métricas**: Registro de peso/altura/% grasa offline
- [ ] **Rankings**: Calculados localmente (velocidad, resistencia, tiros)
- [ ] **Sesiones**: Crear, registrar tiros/vueltas, cerrar (offline)
- [ ] **Partidos**: Crear, registrar eventos, cerrar (eventos local-only)
- [ ] **Sync**: WorkManager sincroniza cada 15 min cuando hay conexión
- [ ] **Data integrity**: App no pierde datos en kill process
- [ ] **Performance**: Tiempo de carga de roster <500ms (100 atletas)
- [ ] **Tests**: 80%+ coverage en ViewModels y Repositories

### Nice-to-have (pueden ir a post-MVP)

- [ ] Gráficos de tendencias en métricas
- [ ] Modo oscuro
- [ ] Animaciones de transición
- [ ] Haptic feedback
- [ ] Notificaciones push

---

## 🚨 Riesgos y mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Firebase Auth + Supabase JWT no funciona | Media | Alto | Probar integración en semana 2 |
| WorkManager no ejecuta en Doze mode | Baja | Medio | Usar constraints correctos |
| Room migrations fallan | Media | Alto | Testing exhaustivo de migrations |
| Canvas de cancha es lento | Baja | Medio | Optimizar con `drawBehind` en vez de `Canvas` |
| Sincronización toma >30s | Media | Bajo | Batch upserts en Supabase |

---

## 📋 Checklist pre-launch

### Semana 8 (antes de MVP)

- [ ] Todos los criterios must-have completados
- [ ] Tests pasan (unit + UI + integration)
- [ ] No hay crashes en flujos críticos
- [ ] App funciona 100% offline
- [ ] Sync funciona correctamente
- [ ] Documentación actualizada (current-state.md)
- [ ] README.md con instrucciones de setup
- [ ] APK de debug generado y probado en dispositivos físicos

---

## 🔄 Proceso de actualización

Este documento se actualiza **cada viernes** al cierre de sprint:

1. Marcar tareas completadas con ✅
2. Actualizar % de progreso en trackers
3. Agregar nuevas tareas si es necesario
4. Documentar riesgos/blockers identificados
5. Revisar si el timeline sigue siendo realista

---

## 📞 Contacto y soporte

- **Project Lead**: (por definir)
- **Tech Lead**: (por definir)
- **Slack channel**: #athlead-dev
- **Daily standup**: 9:00 AM (Zoom link)

---

**Última actualización**: 2026-06-06
**Próxima revisión**: 2026-06-13 (fin de semana 1)
