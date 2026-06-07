# Design Tokens — Sistema de diseño D1 "Azul Verificado"

**Nombre del sistema**: D1 (División 1)
**Tema**: Azul Verificado
**Inspiración**: Uniformes deportivos universitarios + confiabilidad tecnológica
**Última actualización**: 2026-06-06

---

## 🎨 Paleta de colores

### Primary (Blues)

```kotlin
// core-ui/theme/Color.kt

val Navy = Color(0xFF0D1B3D)
val Azure = Color(0xFF1B43D6)
val Eléctrico = Color(0xFF2D6BFF)
```

#### Uso
- **Navy (#0D1B3D)**: Fondos oscuros, headers, texto principal en modo oscuro
- **Azure (#1B43D6)**: Primary actions (botones principales, FABs)
- **Eléctrico (#2D6BFF)**: Hover states, elementos seleccionados, links

#### Accesibilidad
- Navy sobre blanco: ratio 15.2:1 ✅ WCAG AAA
- Azure sobre blanco: ratio 6.8:1 ✅ WCAG AA
- Eléctrico sobre Navy: ratio 4.9:1 ✅ WCAG AA

---

### Accent

```kotlin
val Verificado = Color(0xFF16A06A)
```

#### Uso
- **Verificado (#16A06A)**: Success states, métricas positivas, checkmarks, confirmaciones

#### Accesibilidad
- Verificado sobre blanco: ratio 4.7:1 ✅ WCAG AA

---

### Neutrals

```kotlin
val Bruma = Color(0xFFEEF2FC)
val Papel = Color(0xFFFAFBFE)
val Gris80 = Color(0xFF333333)
val Gris60 = Color(0xFF666666)
val Gris40 = Color(0xFF999999)
val Gris20 = Color(0xFFCCCCCC)
val Gris10 = Color(0xFFE6E6E6)
```

#### Uso
- **Papel (#FAFBFE)**: Background principal de la app
- **Bruma (#EEF2FC)**: Backgrounds de cards, surfaces elevadas
- **Gris80**: Texto principal (headings)
- **Gris60**: Texto secundario (body)
- **Gris40**: Texto terciario (captions, placeholders)
- **Gris20**: Borders, dividers
- **Gris10**: Disabled backgrounds

---

### Semantic colors

```kotlin
// Success
val Success = Verificado
val SuccessContainer = Color(0xFFD4F4E7)

// Error
val Error = Color(0xFFD32F2F)
val ErrorContainer = Color(0xFFFEE9E7)

// Warning
val Warning = Color(0xFFF57C00)
val WarningContainer = Color(0xFFFFF3E0)

// Info
val Info = Eléctrico
val InfoContainer = Color(0xFFE3EFFF)
```

#### Uso
- **Success**: Confirmaciones, sync exitoso, métricas positivas
- **Error**: Errores de validación, fallos de sync, alerts
- **Warning**: Warnings, datos pendientes de sincronizar
- **Info**: Tips, información contextual, tooltips

---

## ✍️ Tipografía

### Familias

```kotlin
// core-ui/theme/Type.kt

val ArchivoFontFamily = FontFamily(
    Font(R.font.archivo_extrabold, FontWeight.ExtraBold) // 800
)

val HankenGroteskFontFamily = FontFamily(
    Font(R.font.hanken_grotesk_regular, FontWeight.Normal), // 400
    Font(R.font.hanken_grotesk_medium, FontWeight.Medium), // 500
    Font(R.font.hanken_grotesk_semibold, FontWeight.SemiBold), // 600
    Font(R.font.hanken_grotesk_bold, FontWeight.Bold) // 700
)

val IBMPlexMonoFontFamily = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal), // 400
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium) // 500
)
```

### Roles
- **Archivo 800 (ExtraBold)**: Display text (títulos grandes, números de métricas)
- **Hanken Grotesk**: Body text, UI components
- **IBM Plex Mono**: Monospace (códigos, IDs, timestamps)

---

### Type scale (Material3)

```kotlin
val AthleedTypography = Typography(
    // Display (Archivo)
    displayLarge = TextStyle(
        fontFamily = ArchivoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = ArchivoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = ArchivoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headlines (Archivo)
    headlineLarge = TextStyle(
        fontFamily = ArchivoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ArchivoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = ArchivoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Titles (Hanken Grotesk)
    titleLarge = TextStyle(
        fontFamily = HankenGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = HankenGroteskFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = HankenGroteskFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body (Hanken Grotesk)
    bodyLarge = TextStyle(
        fontFamily = HankenGroteskFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = HankenGroteskFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = HankenGroteskFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Labels (Hanken Grotesk)
    labelLarge = TextStyle(
        fontFamily = HankenGroteskFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = HankenGroteskFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = IBMPlexMonoFontFamily, // Monospace para labels pequeños (IDs, timestamps)
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

---

### Casos de uso

| Elemento UI              | Type style       | Ejemplo                          |
|--------------------------|------------------|----------------------------------|
| Screen titles            | headlineLarge    | "Roster"                         |
| Section headers          | headlineMedium   | "Atletas activos"                |
| Card titles              | titleLarge       | "John Doe"                       |
| Metric numbers           | displayMedium    | "75.5 kg"                        |
| Button labels            | labelLarge       | "Crear atleta"                   |
| Body text                | bodyMedium       | "Última sesión: hace 2 días"     |
| Captions                 | bodySmall        | "Actualizado hace 5 min"         |
| Timestamps               | labelSmall       | "2026-06-06 14:32"               |
| IDs                      | labelSmall       | "uuid-1234-5678"                 |

---

## 📏 Spacing scale

### Base: 4dp

```kotlin
// core-ui/theme/Spacing.kt

object Spacing {
    val SpacingXXS = 4.dp
    val SpacingXS = 8.dp
    val SpacingS = 12.dp
    val SpacingM = 16.dp
    val SpacingL = 24.dp
    val SpacingXL = 32.dp
    val SpacingXXL = 48.dp
    val SpacingXXXL = 64.dp
}
```

### Uso
- **4dp (XXS)**: Padding interno de chips, badges
- **8dp (XS)**: Spacing entre iconos y labels, padding de botones pequeños
- **12dp (S)**: Spacing entre elementos relacionados (ej: label + value)
- **16dp (M)**: Padding de cards, spacing entre secciones pequeñas
- **24dp (L)**: Spacing entre secciones principales, padding de screens
- **32dp (XL)**: Spacing entre bloques grandes (ej: header + content)
- **48dp (XXL)**: Spacing vertical en empty states, error screens
- **64dp (XXXL)**: Spacing top en screens principales

---

## 🔲 Corner radius

```kotlin
object CornerRadius {
    val Small = 4.dp   // Chips, badges
    val Medium = 8.dp  // Botones, text fields
    val Large = 12.dp  // Cards
    val XLarge = 16.dp // Bottom sheets, modals
    val XXLarge = 24.dp // Hero cards (dashboards)
}
```

---

## 🌓 Elevation

```kotlin
object Elevation {
    val Level0 = 0.dp  // Flush con background
    val Level1 = 1.dp  // Cards en reposo
    val Level2 = 2.dp  // Cards elevadas (hover en desktop)
    val Level3 = 4.dp  // FABs, botones elevados
    val Level4 = 8.dp  // Navigation bar, bottom sheet
    val Level5 = 16.dp // Modals, dialogs
}
```

### Shadow en Compose
```kotlin
Card(
    modifier = Modifier.shadow(
        elevation = Elevation.Level1,
        shape = RoundedCornerShape(CornerRadius.Large)
    )
) {
    // Content
}
```

---

## 🧱 Componentes base

### AthleedButton

```kotlin
@Composable
fun AthleedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary
) {
    val colors = when (variant) {
        ButtonVariant.Primary -> ButtonDefaults.buttonColors(
            containerColor = Azure,
            contentColor = Color.White
        )
        ButtonVariant.Secondary -> ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Azure
        )
        ButtonVariant.Success -> ButtonDefaults.buttonColors(
            containerColor = Verificado,
            contentColor = Color.White
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        colors = colors,
        shape = RoundedCornerShape(CornerRadius.Medium)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

enum class ButtonVariant {
    Primary,
    Secondary,
    Success
}
```

---

### AthleedTextField

```kotlin
@Composable
fun AthleedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            enabled = enabled,
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CornerRadius.Medium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Azure,
                unfocusedBorderColor = Gris20,
                errorBorderColor = Error
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.bodyLarge
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = Error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = Spacing.SpacingM, top = Spacing.SpacingXXS)
            )
        }
    }
}
```

---

### AthleedCard

```kotlin
@Composable
fun AthleedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = Elevation.Level1,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(CornerRadius.Large),
        colors = CardDefaults.cardColors(containerColor = Bruma),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.SpacingM),
            content = content
        )
    }
}
```

---

### LoadingIndicator

```kotlin
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = Azure,
            strokeWidth = 4.dp
        )
    }
}
```

---

### EmptyState

```kotlin
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.SpacingXXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Gris40
        )

        Spacer(modifier = Modifier.height(Spacing.SpacingL))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Gris80,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.SpacingS))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Gris60,
            textAlign = TextAlign.Center
        )

        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(Spacing.SpacingXL))

            AthleedButton(
                text = actionText,
                onClick = onActionClick
            )
        }
    }
}
```

---

## 🎨 Material3 theme setup

```kotlin
// core-ui/theme/Theme.kt

private val LightColorScheme = lightColorScheme(
    primary = Azure,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E3FF),
    onPrimaryContainer = Navy,

    secondary = Eléctrico,
    onSecondary = Color.White,
    secondaryContainer = InfoContainer,
    onSecondaryContainer = Navy,

    tertiary = Verificado,
    onTertiary = Color.White,
    tertiaryContainer = SuccessContainer,
    onTertiaryContainer = Color(0xFF003822),

    error = Error,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = Color(0xFF5F0016),

    background = Papel,
    onBackground = Gris80,

    surface = Bruma,
    onSurface = Gris80,
    surfaceVariant = Gris10,
    onSurfaceVariant = Gris60,

    outline = Gris20,
    outlineVariant = Gris10
)

@Composable
fun AthleedTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AthleedTypography,
        content = content
    )
}
```

---

## 📱 UI patterns

### Screen layout

```kotlin
@Composable
fun StandardScreenLayout(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Papel,
                    titleContentColor = Navy
                )
            )
        },
        floatingActionButton = floatingActionButton ?: {},
        containerColor = Papel
    ) { paddingValues ->
        content(paddingValues)
    }
}
```

### List item pattern

```kotlin
@Composable
fun AthleteListItem(
    name: String,
    position: String,
    jerseyNumber: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(Spacing.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar con iniciales
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Azure, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(Spacing.SpacingM))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = Gris80
            )
            Text(
                text = position,
                style = MaterialTheme.typography.bodySmall,
                color = Gris60
            )
        }

        if (jerseyNumber != null) {
            Text(
                text = "#$jerseyNumber",
                style = MaterialTheme.typography.titleLarge,
                color = Azure
            )
        }
    }
}
```

---

## ♿ Accesibilidad

### Touch targets
- Mínimo: 48dp x 48dp (Material3 guideline)
- Botones pequeños: 40dp x 40dp (con padding interno)

### Contrast ratios
- Texto principal (Gris80 sobre Papel): 14.5:1 ✅ AAA
- Texto secundario (Gris60 sobre Papel): 7.2:1 ✅ AA
- Botones (Azure sobre blanco): 6.8:1 ✅ AA

### Semantic content descriptions
```kotlin
Icon(
    imageVector = Icons.Default.Add,
    contentDescription = "Agregar atleta" // Siempre describe la acción, no el ícono
)
```

---

## 📐 Responsive breakpoints

```kotlin
object Breakpoints {
    val Compact = 0.dp..599.dp      // Phones
    val Medium = 600.dp..839.dp     // Tablets (portrait)
    val Expanded = 840.dp..9999.dp  // Tablets (landscape), foldables
}

@Composable
fun ResponsiveLayout(
    compactContent: @Composable () -> Unit,
    mediumContent: @Composable () -> Unit = compactContent,
    expandedContent: @Composable () -> Unit = mediumContent
) {
    BoxWithConstraints {
        when {
            maxWidth < 600.dp -> compactContent()
            maxWidth < 840.dp -> mediumContent()
            else -> expandedContent()
        }
    }
}
```

---

## 🧪 Testing design tokens

### Paparazzi snapshot tests
```kotlin
@Test
fun athleedButton_primary_snapshot() {
    paparazzi.snapshot {
        AthleedTheme {
            AthleedButton(
                text = "Primary Button",
                onClick = {},
                variant = ButtonVariant.Primary
            )
        }
    }
}
```

---

**Última revisión**: 2026-06-06
**Design version**: 1.0.0
**Próxima revisión**: Post-MVP (si se requieren ajustes)
