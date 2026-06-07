# North Star — MVP de Athlead

**Versión**: 1.0.0 (MVP)
**Target**: Coaches universitarios del Tec de Monterrey
**Timeline**: 8 semanas
**Última actualización**: 2026-06-06

---

## 🎯 Visión del MVP

**Objetivo principal**: Permitir a los coaches universitarios gestionar atletas, registrar métricas físicas, sesiones de entrenamiento y partidos de forma completamente offline, con sincronización automática cuando haya conexión.

**Principio rector**: *"Si no funciona sin WiFi en el campo de entrenamiento, no se incluye en el MVP."*

---

## 🏆 Features incluidas en el MVP

### 1. Autenticación (feature-auth)

#### ✅ Login y registro
- Login con email/password
- Google Sign-In (OAuth)
- Recuperación de contraseña por email
- Persistencia de sesión (Firebase Auth + local cache)

#### ✅ Gestión de roles
- **Coach**: acceso completo (crear atletas, sesiones, partidos)
- **Atleta**: solo lectura de sus propias métricas (scope futuro, MVP es coach-only)

#### 🔒 Scope del MVP
- Solo coaches pueden usar la app en MVP
- Rol de atleta queda preparado pero sin pantallas dedicadas

---

### 2. Gestión de roster (feature-roster)

#### ✅ CRUD de atletas
- Crear atleta (nombre, fecha de nacimiento, deporte, posición)
- Editar información básica
- Eliminar atleta (soft delete con flag `isDeleted`)
- Ver lista de atletas (ordenada por nombre)

#### ✅ Detalle de atleta
- Ver métricas físicas históricas (peso, altura, % grasa)
- Ver historial de sesiones
- Ver tiros y vueltas registrados

#### 🔒 Scope del MVP
- No se incluye foto del atleta (solo iniciales en avatar)
- No se incluye importación masiva desde CSV (manual only)

---

### 3. Métricas físicas

#### ✅ Registro de métricas
- **Peso** (kg)
- **Altura** (cm)
- **% de grasa corporal**
- Timestamp automático
- Offline-first (se guarda en Room)

#### ✅ Visualización
- Historial de métricas por atleta
- Gráfico simple de tendencia (peso en los últimos 30 días)

#### 🔒 Scope del MVP
- No se incluyen métricas avanzadas (VO2 max, fuerza de piernas, etc.)
- Gráficos básicos (sin exportación a PDF)

---

### 4. Rankings (feature-rankings)

#### ✅ Rankings por categoría
- **Velocidad** (mejor tiempo en 100m, 400m, etc.)
- **Resistencia** (mejor tiempo en 5K, 10K)
- **Tiros** (% de acierto en cancha)
- Calculados localmente desde Room

#### ✅ Filtros
- Por deporte (basketball, soccer, track)
- Por género
- Por categoría de peso

#### 🔒 Scope del MVP
- Rankings estáticos (no se comparan con otras universidades)
- Sin rankings históricos (solo estado actual)

---

### 5. Sesiones de entrenamiento

#### ✅ Crear sesión
- Fecha y hora
- Tipo de sesión (cardio, fuerza, técnica, partido)
- Atletas participantes (selección múltiple)
- Notas del coach

#### ✅ Registro durante la sesión
- **Tiros en cancha** (ShotEntity):
  - Posición (x, y en cancha)
  - Acierto/Fallo
  - Tipo (2 puntos, 3 puntos, tiro libre)
  - Timestamp

- **Vueltas en pista** (LapEntity):
  - Distancia (100m, 200m, 400m, etc.)
  - Tiempo
  - Timestamp

#### ✅ Cerrar sesión
- Ver `docs/context/session-closeout-context.md` para el flujo completo
- Resumen de actividad (total de tiros, vueltas, tiempo total)
- Marca automática de timestamp de cierre

#### 🔒 Scope del MVP
- No hay video recording
- No hay análisis de biomecánica
- No hay comparación con sesiones previas (se prepara pero no se muestra)

---

### 6. Partidos (MatchEntity + MatchEventEntity)

#### ✅ Crear partido
- Fecha y hora
- Oponente
- Ubicación
- Atletas en lineup

#### ✅ Registro de eventos (MatchEventEntity — local-only)
- **Puntos anotados** (con jugador, minuto, tipo)
- **Asistencias**
- **Rebotes** (ofensivos/defensivos)
- **Faltas**
- **Sustituciones**

#### 🔒 Scope del MVP
- **MatchEventEntity NO se sincroniza** con Supabase (local-only)
- Solo se guarda el resultado final del partido
- No hay estadísticas avanzadas (PER, +/-, etc.)

---

### 7. Dashboard coach (feature-coach)

#### ✅ Vista principal
- Resumen de atletas activos
- Próximas sesiones programadas
- Métricas agregadas:
  - Promedio de asistencia a sesiones
  - Total de métricas registradas esta semana
  - Atletas con mejoras recientes

#### ✅ Navegación rápida
- Acceso a roster
- Acceso a rankings
- Botón para crear nueva sesión

#### 🔒 Scope del MVP
- Dashboard estático (no se personaliza por coach)
- Sin exportación de reportes

---

### 8. Perfil de usuario (feature-profile)

#### ✅ Información básica
- Nombre del coach
- Email
- Deporte(s) que entrena
- Logout

#### ✅ Configuración
- Tema (Light/Dark — opcional, puede ser solo Light en MVP)
- Idioma (solo español en MVP)
- Ver estado de sincronización (última sync exitosa)

#### 🔒 Scope del MVP
- No hay edición de foto de perfil
- No hay notificaciones push

---

### 9. Sincronización (sync module)

#### ✅ Background sync
- WorkManager ejecuta `SyncWorker` cada 15 minutos
- Solo si hay conexión WiFi/cellular
- Sincroniza entidades con `isSynced = false`

#### ✅ Conflict resolution
- **Local gana siempre** (sobrescribe remoto con `updatedAt` local)
- No hay merge de conflictos (estrategia simple para MVP)

#### ✅ Manual sync
- Botón de "Sincronizar ahora" en perfil
- Muestra progreso (syncing X de Y registros)

#### 🔒 Scope del MVP
- **MatchEventEntity NO se sincroniza** (local-only)
- No hay detección de cambios remotos (pull only en futuras versiones)

---

## ❌ Features explícitamente fuera del MVP

### No se incluyen
- ❌ Rol de atleta (UI dedicada para atletas)
- ❌ Video recording de sesiones/partidos
- ❌ Análisis de biomecánica
- ❌ Notificaciones push
- ❌ Chat coach-atleta
- ❌ Comparación con otras universidades
- ❌ Exportación de reportes a PDF
- ❌ Integración con wearables (Garmin, Apple Watch)
- ❌ Modo oscuro (opcional, puede quedar para post-MVP)
- ❌ Multi-idioma (solo español)
- ❌ Onboarding tutorial (se asume que el coach ya sabe usar la app)

### Se preparan pero no se implementan
- 🔮 Sincronización bidireccional (MVP es push-only desde local)
- 🔮 Real-time updates (MVP es polling cada 15 min)
- 🔮 Estadísticas avanzadas de partido
- 🔮 Machine learning para predicciones de rendimiento

---

## 📊 Métricas de éxito del MVP

### Uso
- [ ] 10+ coaches activos en las primeras 2 semanas
- [ ] Promedio de 3+ sesiones registradas por coach/semana
- [ ] 50+ atletas registrados en total

### Técnicas
- [ ] 100% de features core funcionan offline
- [ ] Sincronización exitosa en 95%+ de intentos
- [ ] App no crashea en flujos críticos (crear atleta, registrar sesión)
- [ ] Tiempo de carga de roster <500ms (100 atletas)

### UX
- [ ] Net Promoter Score (NPS) > 7/10
- [ ] Tiempo promedio para crear un atleta <60 segundos
- [ ] Tiempo promedio para cerrar una sesión <30 segundos

---

## 🚦 Criterios de aceptación del MVP

### Must-have (bloqueantes para launch)
- ✅ Auth funciona (login, Google Sign-In, logout)
- ✅ CRUD de atletas offline
- ✅ Registro de métricas físicas offline
- ✅ Rankings se calculan localmente
- ✅ Sesiones se pueden crear, editar, cerrar offline
- ✅ Tiros y vueltas se registran offline
- ✅ Partidos se pueden crear offline
- ✅ WorkManager sincroniza cada 15 min
- ✅ App no pierde datos en kill process

### Nice-to-have (pueden ir a post-MVP)
- 🟡 Gráficos de tendencias en métricas
- 🟡 Modo oscuro
- 🟡 Animaciones de transición
- 🟡 Haptic feedback en acciones críticas

---

## 🛣️ Roadmap post-MVP (ideas para v1.1+)

### v1.1 — Atletas como usuarios
- UI dedicada para atletas (ver sus métricas, sesiones)
- Notificaciones de nuevas sesiones asignadas

### v1.2 — Analytics avanzados
- Estadísticas de partido completas (PER, +/-, etc.)
- Exportación de reportes a PDF
- Comparación de métricas entre atletas

### v1.3 — Integración con wearables
- Importar métricas desde Garmin, Apple Watch
- Auto-registro de vueltas con GPS

### v2.0 — Multi-universidad
- Rankings comparativos entre universidades
- Sistema de torneos
- Leaderboards globales

---

## 📝 Decisiones de scope

### ¿Por qué MatchEventEntity es local-only?
- **Razón**: El backend de Supabase no está preparado para manejar eventos en tiempo real
- **Trade-off**: Se pierde la posibilidad de analytics de partido remotos
- **Mitigación**: Se exporta el resultado final del partido (score, ganador)
- **Futuro**: En v1.2 se sincronizará cuando el BFF esté listo

### ¿Por qué solo coaches en MVP?
- **Razón**: El 80% del valor está en la gestión por coaches
- **Trade-off**: Atletas no pueden auto-registrar métricas
- **Mitigación**: Coach puede registrar métricas en nombre del atleta
- **Futuro**: v1.1 incluye UI para atletas

### ¿Por qué local gana siempre en conflictos?
- **Razón**: Simplifica la lógica de merge, evita pérdida de datos en campo
- **Trade-off**: Cambios remotos pueden perderse
- **Mitigación**: En MVP, cada coach tiene su dispositivo (no hay multi-device)
- **Futuro**: v1.2 incluye merge inteligente con CRDTs

---

**Última revisión**: 2026-06-06
**Aprobado por**: Product Owner (placeholder)
**Próxima revisión**: Post-MVP (semana 9)
