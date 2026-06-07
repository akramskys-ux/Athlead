# UI Context — Patrones y guías para trabajo de UI

**Para**: Agentes/asistentes trabajando en Composables, navegación, y UI state
**Última actualización**: 2026-06-06

---

## 🎯 Principios de UI en Athlead

### 1. Stateless Composables
- Composables reciben `uiState` y `onEvent` como parámetros
- NO mantienen estado interno (excepto UI state efímero como scroll position)
- ViewModel es el único owner de state

### 2. Unidirectional Data Flow
```
User action → Event → ViewModel → Update State → Recomposition
```

### 3. Composición sobre herencia
- Componentes pequeños se componen en componentes grandes
- Máximo 300 líneas por Composable

---

## 🧩 Anatomía de un Screen

### Template completo

```kotlin
// RosterScreen.kt (feature-roster/)

@Composable
fun RosterScreen(
    viewModel: RosterViewModel = hiltViewModel(),
    onNavigateToDetail: (athleteId: String) -> Unit,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RosterContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToCreate = onNavigateToCreate,
        modifier = modifier
    )
}

@Composable
private fun RosterContent(
    uiState: RosterUiState,
    onEvent: (RosterEvent) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    StandardScreenLayout(
        title = "Roster",
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar atleta")
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingIndicator()

            uiState.error != null -> ErrorState(
                message = uiState.error,
                onRetry = { onEvent(RosterEvent.Retry) }
            )

            uiState.athletes.isEmpty() -> EmptyState(
                icon = Icons.Default.PersonOff,
                title = "No hay atletas",
                description = "Agrega tu primer atleta para comenzar",
                actionText = "Agregar atleta",
                onActionClick = onNavigateToCreate
            )

            else -> AthleteList(
                athletes = uiState.athletes,
                onAthleteClick = onNavigateToDetail,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun AthleteList(
    athletes: List<Athlete>,
    onAthleteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(Spacing.SpacingM)
    ) {
        items(athletes, key = { it.id }) { athlete ->
            AthleteListItem(
                athlete = athlete,
                onClick = { onAthleteClick(athlete.id) },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}
```

---

## 📦 UI State

### Pattern
```kotlin
data class RosterUiState(
    val athletes: List<Athlete> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val filterBy: FilterOption = FilterOption.ALL
)

sealed interface RosterEvent {
    data class SearchQueryChanged(val query: String) : RosterEvent
    data class FilterChanged(val filter: FilterOption) : RosterEvent
    data class DeleteAthlete(val id: String) : RosterEvent
    object Retry : RosterEvent
    object Refresh : RosterEvent
}
```

### ViewModel

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
            is RosterEvent.SearchQueryChanged -> updateSearchQuery(event.query)
            is RosterEvent.FilterChanged -> updateFilter(event.filter)
            is RosterEvent.DeleteAthlete -> deleteAthlete(event.id)
            RosterEvent.Retry -> loadAthletes()
            RosterEvent.Refresh -> loadAthletes()
        }
    }

    private fun loadAthletes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getAthletesUseCase()
                .catch { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
                .collect { athletes ->
                    _uiState.update {
                        it.copy(athletes = athletes, isLoading = false)
                    }
                }
        }
    }

    private fun deleteAthlete(id: String) {
        viewModelScope.launch {
            deleteAthleteUseCase(id)
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }
}
```

---

## 🧭 Navegación

### Navigation graph

```kotlin
// app/navigation/AppNavGraph.kt

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.CoachDashboard.route,
        modifier = modifier
    ) {
        // Auth
        authNavGraph(navController)

        // Coach Dashboard
        composable(Screen.CoachDashboard.route) {
            CoachDashboardScreen(
                onNavigateToRoster = { navController.navigate(Screen.Roster.route) },
                onNavigateToRankings = { navController.navigate(Screen.Rankings.route) }
            )
        }

        // Roster
        rosterNavGraph(navController)

        // Rankings
        rankingsNavGraph(navController)

        // Profile
        profileNavGraph(navController)
    }
}
```

### Feature navigation

```kotlin
// feature-roster/navigation/RosterNavigation.kt

sealed class RosterScreen(val route: String) {
    object List : RosterScreen("roster")
    object Detail : RosterScreen("roster/{athleteId}") {
        fun createRoute(athleteId: String) = "roster/$athleteId"
    }
    object Create : RosterScreen("roster/create")
}

fun NavGraphBuilder.rosterNavGraph(navController: NavHostController) {
    navigation(
        startDestination = RosterScreen.List.route,
        route = "roster_graph"
    ) {
        composable(RosterScreen.List.route) {
            RosterScreen(
                onNavigateToDetail = { athleteId ->
                    navController.navigate(RosterScreen.Detail.createRoute(athleteId))
                },
                onNavigateToCreate = {
                    navController.navigate(RosterScreen.Create.route)
                }
            )
        }

        composable(
            route = RosterScreen.Detail.route,
            arguments = listOf(navArgument("athleteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val athleteId = backStackEntry.arguments?.getString("athleteId") ?: return@composable

            AthleteDetailScreen(
                athleteId = athleteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(RosterScreen.Create.route) {
            CreateAthleteScreen(
                onNavigateBack = { navController.popBackStack() },
                onAthleteCreated = {
                    navController.popBackStack()
                    // Opcionalmente: navegar al detalle
                }
            )
        }
    }
}
```

---

## 🎨 Componentes reutilizables

### AthleteCard

```kotlin
@Composable
fun AthleteCard(
    athlete: Athlete,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showMetrics: Boolean = true
) {
    AthleedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = athlete.initials,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(Spacing.SpacingM))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = athlete.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "${athlete.sport} • ${athlete.position ?: "Sin posición"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (showMetrics && athlete.latestMetric != null) {
                    Spacer(modifier = Modifier.height(Spacing.SpacingXXS))
                    Text(
                        text = "Peso: ${athlete.latestMetric.weight} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            if (athlete.jerseyNumber != null) {
                Text(
                    text = "#${athlete.jerseyNumber}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
```

### MetricCard

```kotlin
@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String?,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trend: Trend? = null
) {
    AthleedCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(Spacing.SpacingM))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (trend != null) {
                TrendIndicator(trend = trend)
            }
        }
    }
}

@Composable
fun TrendIndicator(trend: Trend) {
    val (icon, color) = when (trend) {
        is Trend.Up -> Icons.Default.TrendingUp to MaterialTheme.colorScheme.tertiary
        is Trend.Down -> Icons.Default.TrendingDown to MaterialTheme.colorScheme.error
        Trend.Neutral -> Icons.Default.TrendingFlat to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

sealed interface Trend {
    data class Up(val percentage: Float) : Trend
    data class Down(val percentage: Float) : Trend
    object Neutral : Trend
}
```

---

## 📝 Formularios

### Form state pattern

```kotlin
data class CreateAthleteFormState(
    val name: String = "",
    val nameError: String? = null,

    val dateOfBirth: LocalDate? = null,
    val dateOfBirthError: String? = null,

    val sport: String = "",
    val sportError: String? = null,

    val position: String = "",

    val gender: String = "",
    val genderError: String? = null,

    val jerseyNumber: String = "",
    val jerseyNumberError: String? = null
) {
    fun isValid(): Boolean =
        nameError == null &&
        dateOfBirthError == null &&
        sportError == null &&
        genderError == null &&
        jerseyNumberError == null &&
        name.isNotBlank() &&
        dateOfBirth != null &&
        sport.isNotBlank() &&
        gender.isNotBlank()
}

sealed interface CreateAthleteEvent {
    data class NameChanged(val name: String) : CreateAthleteEvent
    data class DateOfBirthChanged(val date: LocalDate) : CreateAthleteEvent
    data class SportChanged(val sport: String) : CreateAthleteEvent
    data class PositionChanged(val position: String) : CreateAthleteEvent
    data class GenderChanged(val gender: String) : CreateAthleteEvent
    data class JerseyNumberChanged(val number: String) : CreateAthleteEvent
    object Submit : CreateAthleteEvent
}
```

### Form UI

```kotlin
@Composable
fun CreateAthleteForm(
    formState: CreateAthleteFormState,
    onEvent: (CreateAthleteEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.SpacingM),
        verticalArrangement = Arrangement.spacedBy(Spacing.SpacingM)
    ) {
        AthleedTextField(
            value = formState.name,
            onValueChange = { onEvent(CreateAthleteEvent.NameChanged(it)) },
            label = "Nombre completo",
            placeholder = "Juan Pérez",
            isError = formState.nameError != null,
            errorMessage = formState.nameError
        )

        // DatePicker
        DatePickerField(
            selectedDate = formState.dateOfBirth,
            onDateSelected = { onEvent(CreateAthleteEvent.DateOfBirthChanged(it)) },
            label = "Fecha de nacimiento",
            isError = formState.dateOfBirthError != null,
            errorMessage = formState.dateOfBirthError
        )

        // Dropdown para deporte
        ExposedDropdownMenu(
            options = listOf("BASKETBALL", "SOCCER", "TRACK", "SWIMMING"),
            selectedOption = formState.sport,
            onOptionSelected = { onEvent(CreateAthleteEvent.SportChanged(it)) },
            label = "Deporte",
            isError = formState.sportError != null,
            errorMessage = formState.sportError
        )

        AthleedTextField(
            value = formState.position,
            onValueChange = { onEvent(CreateAthleteEvent.PositionChanged(it)) },
            label = "Posición (opcional)",
            placeholder = "Point Guard"
        )

        // Radio buttons para género
        GenderSelector(
            selectedGender = formState.gender,
            onGenderSelected = { onEvent(CreateAthleteEvent.GenderChanged(it)) },
            isError = formState.genderError != null
        )

        AthleedTextField(
            value = formState.jerseyNumber,
            onValueChange = { onEvent(CreateAthleteEvent.JerseyNumberChanged(it)) },
            label = "Número de jersey (opcional)",
            placeholder = "23",
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(Spacing.SpacingL))

        AthleedButton(
            text = "Crear atleta",
            onClick = { onEvent(CreateAthleteEvent.Submit) },
            enabled = formState.isValid(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

---

## 🔄 Loading & Error states

### Loading state

```kotlin
@Composable
fun LoadingScreen(
    message: String = "Cargando...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.SpacingM)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

### Error state

```kotlin
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.SpacingXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(Spacing.SpacingL))

        Text(
            text = "Algo salió mal",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.SpacingS))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.SpacingXL))

        AthleedButton(
            text = "Reintentar",
            onClick = onRetry,
            variant = ButtonVariant.Primary
        )
    }
}
```

---

## 🎯 Best practices

### 1. Avoid deep nesting
```kotlin
// ❌ Evitar
@Composable
fun BadExample() {
    Column {
        Row {
            Column {
                Row {
                    // Muy anidado, difícil de leer
                }
            }
        }
    }
}

// ✅ Correcto
@Composable
fun GoodExample() {
    Column {
        HeaderSection()
        ContentSection()
        FooterSection()
    }
}

@Composable
private fun HeaderSection() { /* ... */ }

@Composable
private fun ContentSection() { /* ... */ }

@Composable
private fun FooterSection() { /* ... */ }
```

### 2. Use keys in LazyColumn
```kotlin
// ✅ Correcto
LazyColumn {
    items(athletes, key = { it.id }) { athlete ->
        AthleteListItem(athlete = athlete)
    }
}

// ❌ Evitar (sin key)
LazyColumn {
    items(athletes) { athlete ->
        AthleteListItem(athlete = athlete)
    }
}
```

### 3. Remember expensive computations
```kotlin
@Composable
fun RankingsScreen(athletes: List<Athlete>) {
    val sortedAthletes = remember(athletes) {
        athletes.sortedByDescending { it.bestTime }
    }

    // UI con sortedAthletes
}
```

### 4. Side effects correctamente
```kotlin
@Composable
fun SessionDetailScreen(sessionId: String, viewModel: SessionViewModel) {
    // ✅ Correcto: LaunchedEffect para cargar datos
    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    // UI
}
```

---

**Última revisión**: 2026-06-06
**Próxima revisión**: Post-MVP
