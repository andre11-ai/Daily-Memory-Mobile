package com.AEAS.dailymemory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.AEAS.dailymemory.ui.theme.BlueDark
import com.AEAS.dailymemory.ui.theme.FloatingMascot
import com.AEAS.dailymemory.ui.theme.GradientBackground
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DefendBaseScreen(
    difficulty: String,
    onNavigateBack: () -> Unit,
    viewModel: DefendBaseViewModel = viewModel()
) {
    LaunchedEffect(difficulty) {
        viewModel.initGame(difficulty)
    }

    val phase by viewModel.phase.collectAsState()
    val score by viewModel.score.collectAsState()
    val lives by viewModel.lives.collectAsState() // --- AGREGADO: Leer estado de vidas
    val enemies by viewModel.enemies.collectAsState()

    var currentFrameTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    if (phase == DefendPhase.PLAYING) {
        LaunchedEffect(Unit) {
            while (true) {
                withFrameMillis {
                    currentFrameTime = System.currentTimeMillis()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GradientBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- NAVBAR ---
                Box(modifier = Modifier.fillMaxWidth().shadow(4.dp).background(Color(0xFFF8F9FA).copy(alpha = 0.95f))) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Memoria Muscular", color = MintPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = BlueDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Volver", color = BlueDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // --- HEADER DEL JUEGO CON VIDAS ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Defiende la Base", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
                        Text("Dificultad: $difficulty", fontSize = 16.sp, color = Color.Gray)
                    }

                    // --- MODIFICADO: Barra de Score y Vidas ---
                    Row(
                        modifier = Modifier.background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, MintPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)).padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Score: $score", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF222222))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Vidas: ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF222222))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(3) { index ->
                                    Box(
                                        modifier = Modifier.size(14.dp).clip(CircleShape).background(if (index < lives) Color(0xFFFF4F68) else Color(0xFFFF4F68).copy(alpha = 0.3f))
                                            .border(1.5.dp, Color(0xFF222222), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                // --- ÁREA DE JUEGO LÍQUIDA ---
                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(start = 24.dp, end = 24.dp), contentAlignment = Alignment.Center) {
                    Card(
                        modifier = Modifier.fillMaxSize().shadow(12.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)).background(Color(0xFFE0FFFA))) {
                            val centerPx = Offset(constraints.maxWidth / 2f, constraints.maxHeight / 2f)
                            val maxRadiusPx = kotlin.math.min(centerPx.x, centerPx.y)

                            Canvas(
                                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        val logicalX = (offset.x - centerPx.x) / maxRadiusPx
                                        val logicalY = (offset.y - centerPx.y) / maxRadiusPx
                                        viewModel.onPlayerTap(logicalX, logicalY, currentFrameTime)
                                    }
                                }
                            ) {
                                val canvasCenter = Offset(size.width / 2f, size.height / 2f)
                                val baseRadiusPx = maxRadiusPx * 0.15f
                                val enemyRadiusPx = maxRadiusPx * 0.1f

                                drawCircle(color = BlueDark, radius = baseRadiusPx, center = canvasCenter)
                                drawCircle(color = Color.White, radius = baseRadiusPx * 0.5f, center = canvasCenter)

                                val spawnDistance = 1.2f

                                enemies.forEach { enemy ->
                                    val aliveTime = (currentFrameTime - enemy.spawnTime).coerceAtLeast(0L)
                                    val progress = aliveTime.toFloat() / viewModel.timeToReachCenterMs.toFloat()

                                    val currentDistance = spawnDistance * (1f - progress)

                                    val enemyPxX = canvasCenter.x + (cos(enemy.angle) * currentDistance * maxRadiusPx)
                                    val enemyPxY = canvasCenter.y + (sin(enemy.angle) * currentDistance * maxRadiusPx)

                                    drawCircle(
                                        color = enemy.color,
                                        radius = enemyRadiusPx,
                                        center = Offset(enemyPxX, enemyPxY)
                                    )

                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.3f),
                                        radius = enemyRadiusPx * 0.6f,
                                        center = Offset(enemyPxX - (enemyRadiusPx * 0.2f), enemyPxY - (enemyRadiusPx * 0.2f))
                                    )
                                }
                            }

                            if (phase == DefendPhase.INTRO) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
                                    Text("¡Toca los círculos antes de que lleguen al centro!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BlueDark)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (phase != DefendPhase.PLAYING) {
            DefendHelpModal(
                phase = phase, difficulty = difficulty, score = score, target = viewModel.targetScore,
                onActionClick = { viewModel.playAgain() },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
fun DefendHelpModal(phase: DefendPhase, difficulty: String, score: Int, target: Int, onActionClick: () -> Unit, onNavigateBack: () -> Unit) {
    val themeColor = when (phase) { DefendPhase.GAMEOVER_WIN -> Color(0xFFFFD700); DefendPhase.GAMEOVER_LOSE -> Color(0xFFFF4F68); else -> MintPrimary }
    val eyebrowText = when (phase) { DefendPhase.GAMEOVER_WIN -> "¡VICTORIA!"; DefendPhase.GAMEOVER_LOSE -> "¡SIN VIDAS!"; else -> "NIVEL $difficulty" }
    val titleText = when (phase) { DefendPhase.GAMEOVER_WIN -> "¡Base Salvada!"; DefendPhase.GAMEOVER_LOSE -> "Fin del Juego"; else -> "Defiende la Base" }

    val msgText = when (phase) {
        DefendPhase.GAMEOVER_WIN -> "Has destruido a todos los invasores. ¡Tus dedos son rápidos!"
        DefendPhase.GAMEOVER_LOSE -> "Los invasores destruyeron tu base. ¡Más cuidado!"
        else -> "Toca (Tap) a los enemigos para destruirlos antes de que toquen el centro.\nDestruye: $target enemigos.\nTienes 3 vidas."
    }
    val btnText = when (phase) { DefendPhase.GAMEOVER_WIN -> "Jugar de nuevo"; DefendPhase.GAMEOVER_LOSE -> "Reintentar"; else -> "¡Empezar!" }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF001E28).copy(alpha = 0.6f)).padding(24.dp).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FloatingMascot(size = 180.dp)
            Canvas(modifier = Modifier.size(width = 16.dp, height = 24.dp)) {
                val path = Path().apply { moveTo(size.width, 0f); lineTo(0f, size.height / 2f); lineTo(size.width, size.height); close() }
                drawPath(path, themeColor)
                val innerPath = Path().apply { moveTo(size.width, 2f); lineTo(3f, size.height / 2f); lineTo(size.width, size.height - 2f); close() }
                drawPath(innerPath, Color.White)
            }
            Card(
                shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White),
                border = BorderStroke(2.dp, themeColor), modifier = Modifier.widthIn(min = 350.dp, max = 500.dp)
            ) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text(eyebrowText, color = themeColor, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(titleText, color = BlueDark, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(msgText, color = Color.Gray, fontSize = 16.sp, lineHeight = 24.sp)

                    if (phase != DefendPhase.INTRO) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Puntaje final: $score", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueDark)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        if (phase != DefendPhase.INTRO) {
                            Text("Volver al Menú", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.clickable { onNavigateBack() }.padding(end = 16.dp))
                        }
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(Brush.horizontalGradient(listOf(MintPrimary, AccentBlue)))
                                .clickable { onActionClick() }.padding(horizontal = 32.dp, vertical = 12.dp)
                        ) { Text(btnText, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp) }
                    }
                }
            }
        }
    }
}