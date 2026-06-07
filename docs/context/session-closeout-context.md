# Session Closeout Context — Flujo de cierre de sesión

**Para**: Agentes/asistentes implementando el cierre de sesiones de entrenamiento
**Última actualización**: 2026-06-06

---

## 🎯 Contexto

El cierre de sesión es un flujo crítico donde el coach finaliza una sesión de entrenamiento y ve un resumen de la actividad registrada (tiros, vueltas, tiempo total). Este flujo debe ser **rápido** (< 30 segundos) y **100% offline**.

---

## 📋 User story

**Como** coach
**Quiero** cerrar una sesión de entrenamiento y ver un resumen
**Para** confirmar que registré correctamente la actividad de mis atletas

---

## 🔄 Flujo completo

### 1. Estado inicial

- Coach está en `SessionDetailScreen`
- Sesión tiene `status = "ACTIVE"`
- Ya se registraron tiros y/o vueltas durante la sesión

### 2. Coach presiona "Finalizar sesión"

UI muestra un bottom sheet con resumen:

```kotlin
@Composable
fun SessionCloseoutBottomSheet(
    sessionDetails: SessionDetails,
    onConfirmClose: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onCancel,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.SpacingL)
        ) {
            Text(
                text = "Resumen de sesión",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(Spacing.SpacingL))

            // Duración total
            SessionDurationCard(
                startTime = sessionDetails.session.date,
                endTime = Clock.System.now()
            )

            Spacer(modifier = Modifier.height(Spacing.SpacingM))

            // Resumen de tiros
            if (sessionDetails.shots.isNotEmpty()) {
                ShotSummaryCard(shots = sessionDetails.shots)
                Spacer(modifier = Modifier.height(Spacing.SpacingM))
            }

            // Resumen de vueltas
            if (sessionDetails.laps.isNotEmpty()) {
                LapSummaryCard(laps = sessionDetails.laps)
                Spacer(modifier = Modifier.height(Spacing.SpacingM))
            }

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.SpacingM)
            ) {
                AthleedButton(
                    text = "Cancelar",
                    onClick = onCancel,
                    variant = ButtonVariant.Secondary,
                    modifier = Modifier.weight(1f)
                )

                AthleedButton(
                    text = "Finalizar",
                    onClick = onConfirmClose,
                    variant = ButtonVariant.Primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
```

### 3. Componentes del resumen

#### SessionDurationCard

```kotlin
@Composable
fun SessionDurationCard(
    startTime: Instant,
    endTime: Instant,
    modifier: Modifier = Modifier
) {
    val duration = endTime - startTime

    AthleedCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(Spacing.SpacingM))

            Column {
                Text(
                    text = "Duración total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = duration.toFormattedDuration(), // "1h 32m"
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}
```

#### ShotSummaryCard

```kotlin
@Composable
fun ShotSummaryCard(
    shots: List<Shot>,
    modifier: Modifier = Modifier
) {
    val totalShots = shots.size
    val successfulShots = shots.count { it.isSuccessful }
    val successRate = if (totalShots > 0) {
        (successfulShots.toFloat() / totalShots * 100).toInt()
    } else 0

    val shotsByType = shots.groupBy { it.shotType }

    AthleedCard(modifier = modifier) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SportsBask etball,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(Spacing.SpacingM))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tiros registrados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "$successfulShots / $totalShots",
                        style = MaterialTheme.typography.displaySmall
                    )
                }

                Text(
                    text = "$successRate%",
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (successRate >= 50) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.SpacingM))

            // Desglose por tipo
            shotsByType.forEach { (type, shots) ->
                val successful = shots.count { it.isSuccessful }
                val total = shots.size
                val rate = (successful.toFloat() / total * 100).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = type.toDisplayName(), // "Tiro de 2", "Tiro de 3", etc.
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "$successful/$total ($rate%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (type != shotsByType.keys.last()) {
                    Spacer(modifier = Modifier.height(Spacing.SpacingXS))
                }
            }
        }
    }
}
```

#### LapSummaryCard

```kotlin
@Composable
fun LapSummaryCard(
    laps: List<Lap>,
    modifier: Modifier = Modifier
) {
    val totalLaps = laps.size
    val lapsByDistance = laps.groupBy { it.distance }
    val bestLap = laps.minByOrNull { it.timeInSeconds }

    AthleedCard(modifier = modifier) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(Spacing.SpacingM))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vueltas registradas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "$totalLaps vueltas",
                        style = MaterialTheme.typography.displaySmall
                    )
                }

                if (bestLap != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Mejor tiempo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = bestLap.timeInSeconds.toFormattedTime(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.SpacingM))

            // Desglose por distancia
            lapsByDistance.forEach { (distance, laps) ->
                val avgTime = laps.map { it.timeInSeconds }.average()
                val bestTime = laps.minOf { it.timeInSeconds }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${distance}m (${laps.size} vueltas)",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Mejor: ${bestTime.toFormattedTime()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )

                        Text(
                            text = "Prom: ${avgTime.toFloat().toFormattedTime()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (distance != lapsByDistance.keys.last()) {
                    Spacer(modifier = Modifier.height(Spacing.SpacingXS))
                }
            }
        }
    }
}
```

### 4. Coach confirma "Finalizar"

ViewModel maneja el cierre:

```kotlin
sealed interface SessionDetailEvent {
    object CloseSession : SessionDetailEvent
    object ConfirmClose : SessionDetailEvent
    object CancelClose : SessionDetailEvent
    // ...
}

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: String = savedStateHandle["sessionId"] ?: error("No sessionId")

    private val _uiState = MutableStateFlow(SessionDetailUiState())
    val uiState: StateFlow<SessionDetailUiState> = _uiState.asStateFlow()

    val sessionDetails: Flow<SessionDetails?> =
        sessionRepository.getSessionWithDetails(sessionId)

    fun onEvent(event: SessionDetailEvent) {
        when (event) {
            SessionDetailEvent.CloseSession -> showCloseoutSheet()
            SessionDetailEvent.ConfirmClose -> closeSession()
            SessionDetailEvent.CancelClose -> hideCloseoutSheet()
            // ...
        }
    }

    private fun showCloseoutSheet() {
        _uiState.update { it.copy(showCloseoutSheet = true) }
    }

    private fun hideCloseoutSheet() {
        _uiState.update { it.copy(showCloseoutSheet = false) }
    }

    private fun closeSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClosing = true) }

            sessionRepository.closeSession(sessionId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isClosing = false,
                            showCloseoutSheet = false,
                            sessionClosed = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isClosing = false,
                            error = error.message
                        )
                    }
                }
        }
    }
}

data class SessionDetailUiState(
    val showCloseoutSheet: Boolean = false,
    val isClosing: Boolean = false,
    val sessionClosed: Boolean = false,
    val error: String? = null
)
```

### 5. Repository actualiza la sesión

```kotlin
// core-data/repository/SessionRepository.kt

suspend fun closeSession(sessionId: String): Result<Unit> = withContext(ioDispatcher) {
    try {
        val session = sessionDao.getByIdOnce(sessionId)
            ?: return@withContext Result.failure(Exception("Session not found"))

        if (session.status == "COMPLETED") {
            return@withContext Result.failure(Exception("Session already closed"))
        }

        val closedSession = session.copy(
            status = "COMPLETED",
            completedAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            isSynced = false // Marca para sincronizar
        )

        sessionDao.update(closedSession)

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### 6. UI navega de vuelta

```kotlin
// SessionDetailScreen.kt

val uiState by viewModel.uiState.collectAsStateWithLifecycle()

LaunchedEffect(uiState.sessionClosed) {
    if (uiState.sessionClosed) {
        onNavigateBack()
    }
}
```

---

## 📊 Cálculos del resumen

### Duración total

```kotlin
fun Duration.toFormattedDuration(): String {
    val hours = inWholeHours
    val minutes = (inWholeMinutes % 60)

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "${inWholeSeconds}s"
    }
}
```

### Tiempo de vuelta

```kotlin
fun Float.toFormattedTime(): String {
    val minutes = (this / 60).toInt()
    val seconds = (this % 60)

    return when {
        minutes > 0 -> String.format("%d:%05.2f", minutes, seconds)
        else -> String.format("%.2fs", this)
    }
}

// Ejemplos:
// 12.34 → "12.34s"
// 72.56 → "1:12.56"
```

### % de acierto

```kotlin
fun List<Shot>.successRate(): Int {
    if (isEmpty()) return 0
    val successful = count { it.isSuccessful }
    return (successful.toFloat() / size * 100).toInt()
}
```

---

## ⚡ Performance considerations

### 1. Cargar detalles en background

```kotlin
@Composable
fun SessionDetailScreen(
    sessionId: String,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val sessionDetails by viewModel.sessionDetails.collectAsStateWithLifecycle(null)

    // Carga datos en LaunchedEffect
    LaunchedEffect(sessionId) {
        // ViewModel ya está cargando con Flow
    }

    when (sessionDetails) {
        null -> LoadingIndicator()
        else -> SessionContent(sessionDetails!!)
    }
}
```

### 2. Calcular resumen solo una vez

```kotlin
@Composable
fun SessionCloseoutBottomSheet(
    sessionDetails: SessionDetails,
    ...
) {
    // Memorize cálculos costosos
    val summary = remember(sessionDetails) {
        SessionSummary(
            duration = Clock.System.now() - sessionDetails.session.date,
            totalShots = sessionDetails.shots.size,
            successfulShots = sessionDetails.shots.count { it.isSuccessful },
            totalLaps = sessionDetails.laps.size,
            bestLap = sessionDetails.laps.minByOrNull { it.timeInSeconds }
        )
    }

    // UI usa summary
}

data class SessionSummary(
    val duration: Duration,
    val totalShots: Int,
    val successfulShots: Int,
    val totalLaps: Int,
    val bestLap: Lap?
)
```

---

## 🧪 Testing

### Unit test: closeSession

```kotlin
@Test
fun closeSession_updatesStatus_toCompleted() = runTest {
    // Given: sesión activa
    val session = createTestSession(id = "1", status = "ACTIVE")
    repository.createSession(session, emptyList())

    // When: cerrar sesión
    repository.closeSession("1")

    // Then: status = COMPLETED
    val closed = repository.getSessionById("1").first()
    assertThat(closed?.status).isEqualTo("COMPLETED")
    assertThat(closed?.completedAt).isNotNull()
}

@Test
fun closeSession_marksisSynced_asFalse() = runTest {
    // Given: sesión activa
    val session = createTestSession(id = "1", status = "ACTIVE")
    repository.createSession(session, emptyList())

    // When: cerrar sesión
    repository.closeSession("1")

    // Then: isSynced = false (para sincronizar)
    val entity = sessionDao.getByIdOnce("1")
    assertThat(entity?.isSynced).isFalse()
}
```

### UI test: bottom sheet

```kotlin
@Test
fun clickFinalizarSesion_showsBottomSheet() {
    composeTestRule.setContent {
        SessionDetailScreen(
            sessionId = "test-session",
            viewModel = fakeViewModel
        )
    }

    composeTestRule.onNodeWithText("Finalizar sesión").performClick()

    composeTestRule.onNodeWithText("Resumen de sesión").assertIsDisplayed()
}

@Test
fun confirmClose_navigatesBack() {
    var navigatedBack = false

    composeTestRule.setContent {
        SessionDetailScreen(
            sessionId = "test-session",
            viewModel = fakeViewModel,
            onNavigateBack = { navigatedBack = true }
        )
    }

    composeTestRule.onNodeWithText("Finalizar sesión").performClick()
    composeTestRule.onNodeWithText("Finalizar").performClick()

    assertThat(navigatedBack).isTrue()
}
```

---

## 🚨 Edge cases

### 1. Sesión sin actividad

Si no hay tiros ni vueltas:

```kotlin
if (sessionDetails.shots.isEmpty() && sessionDetails.laps.isEmpty()) {
    EmptyActivityWarning(
        onConfirmClose = onConfirmClose,
        onCancel = onCancel
    )
} else {
    SessionCloseoutBottomSheet(...)
}

@Composable
fun EmptyActivityWarning(
    onConfirmClose: () -> Unit,
    onCancel: () -> Unit
) {
    // Advertencia: "No se registró actividad. ¿Seguro que quieres cerrar?"
}
```

### 2. Sesión ya cerrada

```kotlin
if (session.status == "COMPLETED") {
    // Mostrar error: "Esta sesión ya está cerrada"
    return Result.failure(Exception("Session already closed"))
}
```

### 3. Error al cerrar

```kotlin
sessionRepository.closeSession(sessionId)
    .onFailure { error ->
        // Mostrar Snackbar con error
        _uiState.update {
            it.copy(
                isClosing = false,
                error = "Error al cerrar sesión: ${error.message}"
            )
        }
    }
```

---

## 📋 Checklist de implementación

- [ ] Bottom sheet con resumen (duración, tiros, vueltas)
- [ ] Cálculo de % de acierto en tiros
- [ ] Cálculo de mejor tiempo en vueltas
- [ ] Confirmación de cierre (botón "Finalizar")
- [ ] Actualizar `status = COMPLETED` en Room
- [ ] Marcar `isSynced = false` para sync
- [ ] Navegación de vuelta a lista de sesiones
- [ ] Manejo de edge cases (sesión vacía, ya cerrada)
- [ ] Loading state durante cierre
- [ ] Error handling con Snackbar
- [ ] Unit tests para closeSession
- [ ] UI tests para bottom sheet

---

**Última revisión**: 2026-06-06
**Próxima revisión**: Post-MVP
