package com.masjid.tasbihcounter.ui

import com.masjid.tasbihcounter.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.masjid.tasbihcounter.AppSettings
import com.masjid.tasbihcounter.ThemeSetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeCustomizationScreen(
    settings: AppSettings,
    onThemeChange: (ThemeSetting) -> Unit,
    onBack: () -> Unit
) {
    val themes = ThemeSetting.values()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customize Theme") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(themes) { theme ->
                ThemeCard(
                    themeSetting = theme,
                    isSelected = settings.theme == theme,
                    onThemeSelected = { onThemeChange(theme) }
                )
            }
        }
    }
}

@Composable
fun ThemeCard(
    themeSetting: ThemeSetting,
    isSelected: Boolean,
    onThemeSelected: () -> Unit
) {
    val cardColors = when (themeSetting) {
        ThemeSetting.LIGHT -> listOf(Color(0xFFF0F0F0), Color(0xFF6200EE), Color.White)
        ThemeSetting.SYSTEM -> listOf(Color.Gray, Color.White, Color.Black)
        ThemeSetting.RETRO_ARCADE -> listOf(ArcadeBlack, NeonPink, NeonCyan)
        ThemeSetting.GALAXY_DREAM -> listOf(GalaxyNight, PulsarPurple, NebulaPink)
        ThemeSetting.NEBULA_BURST -> listOf(NebulaDeepSpace, SupernovaRed, CosmicDust)
    }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onThemeSelected),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(cardColors[0])
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = themeSetting.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (themeSetting == ThemeSetting.LIGHT) Color.Black else Color.White
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = if (themeSetting == ThemeSetting.LIGHT) Color.Black else Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Green, CircleShape)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cardColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, Color.Gray, CircleShape)
                    )
                }
            }
        }
    }
}