package com.AEAS.dailymemory

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.AEAS.dailymemory.ui.theme.BlueDark
import com.AEAS.dailymemory.ui.theme.FloatingMascot
import com.AEAS.dailymemory.ui.theme.GradientBackground

@Composable
fun StoryScreen(
    onNavigateBack: () -> Unit,
    onLevelClick: (Int) -> Unit,
    viewModel: StoryViewModel = viewModel()
) {
    val currentLevel by viewModel.currentLevel.collectAsState()
    val showHelpModal by viewModel.showHelpModal.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        GradientBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().shadow(4.dp).background(Color(0xFFF8F9FA).copy(alpha = 0.95f))) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Historia", color = MintPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = BlueDark); Text("Menú", color = BlueDark) }
                    }
                }

                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nivel $currentLevel", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(32.dp))
                    for (i in 1..20) {
                        MapNode(i, currentLevel, i == 20) { if (i <= currentLevel) onLevelClick(i) }
                    }
                }
            }
        }
        FloatingActionButton(onClick = { viewModel.openHelp() }, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)) {
            Icon(Icons.Default.QuestionMark, null)
        }
    }
}

@Composable
fun MapNode(level: Int, currentLevel: Int, isLast: Boolean, onClick: () -> Unit) {
    val isOdd = level % 2 != 0
    val xOffset = if (isOdd) (-40).dp else 40.dp
    val state = when { level == currentLevel -> "CURRENT"; level < currentLevel -> "UNLOCKED"; else -> "LOCKED" }

    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
        if (!isLast) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = AccentBlue.copy(alpha = 0.5f),
                    start = Offset(size.width / 2 + (if (isOdd) -40.dp.toPx() else 40.dp.toPx()), size.height / 2),
                    end = Offset(size.width / 2 + (if (isOdd) 40.dp.toPx() else -40.dp.toPx()), size.height / 2 + 100.dp.toPx()),
                    strokeWidth = 8f
                )
            }
        }
        Box(modifier = Modifier.offset(x = xOffset).size(70.dp).clip(CircleShape)
            .background(if (state == "CURRENT") MintPrimary else if (state == "UNLOCKED") AccentBlue else Color.LightGray)
            .clickable(enabled = state != "LOCKED") { onClick() }, contentAlignment = Alignment.Center) {
            Text(level.toString(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}