package com.masjid.tasbihcounter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// हिस्ट्री सत्र के लिए डेटा क्लास
data class TasbihSession(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val date: Date,
    val count: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    sessions: List<TasbihSession>,
    onBack: () -> Unit
) {
    val ivoryBackground = Color(0xFFFBF7EE)
    val neutralInk = Color(0xFF1B1F23)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", color = neutralInk) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = neutralInk)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ivoryBackground)
            )
        },
        containerColor = ivoryBackground
    ) { paddingValues ->
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No history yet.", color = neutralInk.copy(alpha = 0.7f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sessions) { session ->
                    HistoryItem(session = session)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(session: TasbihSession) {
    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    val tealPrimary = Color(0xFF14937C)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = dateFormat.format(session.date),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = session.count.toString(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = tealPrimary
            )
        }
    }
}