// Path: app/src/main/java/com/masjid/tasbihcounter/MainActivity.kt
package com.masjid.tasbihcounter
import androidx.compose.material.icons.filled.ColorLens
import com.masjid.tasbihcounter.ui.theme.RetroTypography
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masjid.tasbihcounter.ui.ThemeCustomizationScreen
import com.masjid.tasbihcounter.ui.theme.MasjidTasbihCounterTheme
import kotlinx.coroutines.launch

// Navigation ke liye Screens
enum class Screen {
    LIST, COUNTER, SETTINGS, THEME_CUSTOMIZATION
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            // Theme ab ViewModel se control hogi
            MasjidTasbihCounterTheme(themeSetting = uiState.settings.theme) {
                TasbihApp(mainViewModel = mainViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihApp(mainViewModel: MainViewModel = viewModel()) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf(Screen.COUNTER) }
    val activeTasbih = uiState.tasbihList.find { it.id == uiState.activeTasbihId }

    LaunchedEffect(activeTasbih, uiState.tasbihList) {
        if (activeTasbih == null && uiState.tasbihList.isNotEmpty()) {
            mainViewModel.selectTasbih(uiState.tasbihList.first().id)
        } else if (uiState.tasbihList.isEmpty()){
            currentScreen = Screen.LIST
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.LIST -> {
                TasbihListScreen(
                    tasbihList = uiState.tasbihList,
                    onSelectTasbih = {
                        mainViewModel.selectTasbih(it.id)
                        currentScreen = Screen.COUNTER
                    },
                    onAddTasbih = { name, target -> mainViewModel.addTasbih(name, target) },
                    onDeleteTasbih = { mainViewModel.deleteTasbih(it.id) },
                    // -- FIX IS ON THIS LINE --
                    onBack = { if (uiState.tasbihList.isNotEmpty()) currentScreen = Screen.COUNTER }
                )
            }
            Screen.COUNTER -> {
                if (activeTasbih != null) {
                    CounterScreen(
                        tasbih = activeTasbih,
                        settings = uiState.settings,
                        onIncrement = { mainViewModel.incrementActiveTasbih() },
                        onReset = { mainViewModel.resetActiveTasbih() },
                        onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                        onNavigateToList = { currentScreen = Screen.LIST },
                        onNavigateToThemeCustomization = { currentScreen = Screen.THEME_CUSTOMIZATION }
                    )
                } else {
                    // If there's no active tasbih, go to the list to create one.
                    LaunchedEffect(Unit){
                        currentScreen = Screen.LIST
                    }
                }
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    settings = uiState.settings,
                    onThemeChange = { mainViewModel.updateTheme(it) },
                    onVibrationToggle = { mainViewModel.toggleVibration(it) },
                    onBack = { currentScreen = Screen.COUNTER }
                )
            }
            Screen.THEME_CUSTOMIZATION -> {
                ThemeCustomizationScreen(
                    settings = uiState.settings,
                    onThemeChange = { mainViewModel.updateTheme(it) },
                    onBack = { currentScreen = Screen.COUNTER }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TasbihListScreen(
    tasbihList: List<Tasbih>,
    onSelectTasbih: (Tasbih) -> Unit,
    onAddTasbih: (String, Int) -> Unit,
    onDeleteTasbih: (Tasbih) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Tasbih?>(null) }


    if (showAddDialog) {
        AddTasbihDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, target ->
                onAddTasbih(name, target)
                showAddDialog = false
            }
        )
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Tasbih") },
            text = { Text("Are you sure you want to delete '${showDeleteDialog?.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTasbih(showDeleteDialog!!)
                    showDeleteDialog = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tasbih Collection") },
                navigationIcon = {
                    if (tasbihList.isNotEmpty()) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Tasbih")
            }
        }
    ) { padding ->
        if (tasbihList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No tasbih added yet. Tap '+' to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasbihList) { tasbih ->
                    TasbihListItem(
                        tasbih = tasbih,
                        onClick = { onSelectTasbih(tasbih) },
                        onLongClick = { showDeleteDialog = tasbih }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TasbihListItem(tasbih: Tasbih, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tasbih.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${tasbih.count} / ${tasbih.target}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { tasbih.count.toFloat() / tasbih.target.toFloat() },
                    modifier = Modifier.size(50.dp),
                    strokeWidth = 4.dp
                )
                Text("${(tasbih.count.toFloat() / tasbih.target.toFloat() * 100).toInt()}%", fontSize = 12.sp)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(
    tasbih: Tasbih,
    settings: AppSettings,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToThemeCustomization: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val pulse = remember { Animatable(1f) }

    val animatedCount by animateIntAsState(
        targetValue = tasbih.count,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "countAnimation"
    )
    val progress = tasbih.count.toFloat() / tasbih.target.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "progressAnimation"
    )

    // YEH NAYA BADLAV HAI
    val typography = if (settings.theme == ThemeSetting.RETRO_ARCADE) {
        RetroTypography
    } else {
        MaterialTheme.typography
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                actions = {
                    IconButton(onClick = onNavigateToList) {
                        Icon(Icons.Default.List, contentDescription = "Tasbih List", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onNavigateToThemeCustomization) {
                        Icon(Icons.Filled.ColorLens, contentDescription = "Customize Theme", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tasbih.name,
                    style = typography.displayMedium, // Naya font yahan istemal hoga
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }

            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onIncrement()
                            if (settings.isVibrationOn) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            coroutineScope.launch {
                                pulse.snapTo(1.1f)
                                pulse.animateTo(1f, animationSpec = spring(stiffness = Spring.StiffnessLow))
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                AuroraRing(
                    modifier = Modifier.size(300.dp),
                    progress = animatedProgress,
                    pulse = pulse.value,
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$animatedCount",
                        style = typography.displayLarge, // Naya font yahan istemal hoga
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Target: ${tasbih.target}",
                        style = typography.bodyLarge, // Naya font yahan istemal hoga
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = onReset) {
                    Text("Reset", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 16.sp)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTasbihDialog(onDismiss: () -> Unit, onAdd: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Add New Tasbih", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dhikr Name (e.g., Alhamdulillah)") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = target,
                    onValueChange = { target = it.filter { c -> c.isDigit() } },
                    label = { Text("Target") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = {
                        if (name.isNotBlank()) {
                            onAdd(name, target.toIntOrNull() ?: 100)
                        }
                    }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun AuroraRing(
    modifier: Modifier = Modifier,
    progress: Float,
    pulse: Float,
    colors: List<Color>
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    val brush = Brush.sweepGradient(colors)

    // YEH BADLAV HAI: Humne color ko yahan pehle hi le liya
    val innerRingColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)

    Canvas(modifier = modifier.graphicsLayer {
        scaleX = pulse
        scaleY = pulse
        rotationZ = rotation
    }) {
        val strokeWidth = 80f

        drawArc(
            brush = Brush.sweepGradient(colors.map { it.copy(alpha = 0.3f) }),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth + 40f)
        )

        drawArc(
            color = innerRingColor, // Aur yahan us variable ka istemal kiya (YEHI LINE 454 THI)
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            brush = brush,
            startAngle = -90f,
            sweepAngle = 360 * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeChange: (ThemeSetting) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item { SettingsGroup("General Settings") }
            item {
                SegmentedButtonSetting(
                    title = "App Theme",
                    options = ThemeSetting.values().map { it.name.replace("_", " ") },
                    selectedOption = settings.theme.name.replace("_", " "),
                    onOptionSelect = {
                        val selectedTheme = ThemeSetting.valueOf(it.replace(" ", "_"))
                        onThemeChange(selectedTheme)
                    }
                )
            }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            item { SettingsGroup("Feedback Settings") }
            item {
                SwitchSettingItem(
                    title = "Vibration on Tap",
                    checked = settings.isVibrationOn,
                    onCheckedChange = onVibrationToggle
                )
            }
        }
    }
}

@Composable
fun SettingsGroup(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedButtonSetting(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 16.sp, modifier = Modifier.weight(1f))
        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = { onOptionSelect(label) },
                    selected = label == selectedOption
                ) {
                    Text(label, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SwitchSettingItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = null)
    }
}