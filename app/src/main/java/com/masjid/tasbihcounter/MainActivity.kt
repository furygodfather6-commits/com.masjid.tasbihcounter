// Path: app/src/main/java/com/masjid/tasbihcounter/MainActivity.kt
package com.masjid.tasbihcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masjid.tasbihcounter.ui.theme.MasjidTasbihCounterTheme
import kotlinx.coroutines.launch

// Navigation ke liye Screens
enum class Screen {
    LIST, COUNTER, SETTINGS
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

@Composable
fun TasbihApp(mainViewModel: MainViewModel = viewModel()) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf(Screen.LIST) }
    val activeTasbih = uiState.tasbihList.find { it.id == uiState.activeTasbihId }

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
                    onNavigateToSettings = { currentScreen = Screen.SETTINGS }
                )
            }
            Screen.COUNTER -> {
                if (activeTasbih != null) {
                    CounterScreen(
                        tasbih = activeTasbih,
                        settings = uiState.settings,
                        onIncrement = { mainViewModel.incrementActiveTasbih() },
                        onReset = { mainViewModel.resetActiveTasbih() },
                        onBack = { currentScreen = Screen.LIST }
                    )
                } else {
                    // Agar koi active tasbih nahi hai, toh list par wapas jaao
                    LaunchedEffect(Unit) { currentScreen = Screen.LIST }
                }
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    settings = uiState.settings,
                    onThemeChange = { mainViewModel.updateTheme(it) },
                    onVibrationToggle = { mainViewModel.toggleVibration(it) },
                    onBack = { currentScreen = Screen.LIST }
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
    onNavigateToSettings: () -> Unit
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
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
                    color = Color.Gray
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


@Composable
fun CounterScreen(
    tasbih: Tasbih,
    settings: AppSettings,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010))
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back to List", tint = Color.White)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tasbih.name,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f)
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
                pulse = pulse.value
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$animatedCount",
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
                Text(
                    text = "Target: ${tasbih.target}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.7f)
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
                Text("Reset", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
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
fun AuroraRing(modifier: Modifier = Modifier, progress: Float, pulse: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    val colors = listOf(Color(0xFF03A9F4), Color(0xFF9C27B0), Color(0xFFF44336), Color(0xFF00E676))
    val brush = Brush.sweepGradient(colors)

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
            color = Color(0xFF191919),
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
                    options = ThemeSetting.values().map { it.name },
                    selectedOption = settings.theme.name,
                    onOptionSelect = { onThemeChange(ThemeSetting.valueOf(it)) }
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