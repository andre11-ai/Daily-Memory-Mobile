package com.AEAS.dailymemory

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.AEAS.dailymemory.ui.theme.BlueDark
import com.AEAS.dailymemory.ui.theme.FloatingMascot
import com.AEAS.dailymemory.ui.theme.GradientBackground

fun getIconForId(id: Int): ImageVector {
    return when (id) {
        1 -> Icons.Default.Star
        2 -> Icons.Default.Favorite
        3 -> Icons.Default.AcUnit
        4 -> Icons.Default.LocalFlorist
        5 -> Icons.Default.Pets
        6 -> Icons.Default.LightMode
        7 -> Icons.Default.Air
        8 -> Icons.Default.WaterDrop
        else -> Icons.Default.Extension
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SequenceGameScreen(
    difficulty: String,
    onNavigateBack: () -> Unit,
    viewModel: SequenceGameViewModel = viewModel()
) {
    LaunchedEffect(difficulty) {
        viewModel.initGame(difficulty)
    }

    val phase by viewModel.phase.collectAsState()
    val score by viewModel.score.collectAsState()
    val lives by viewModel.lives.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()
    val correctSequence by viewModel.correctSequence.collectAsState()
    val poolImages by viewModel.poolImages.collectAsState()
    val poolHidden by viewModel.poolHidden.collectAsState()
    val userSlots by viewModel.userSlots.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        GradientBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().shadow(4.dp).background(Color(0xFFF8F9FA).copy(alpha = 0.95f))) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Memoria Icónica", color = MintPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = BlueDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Volver", color = BlueDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Secuencia de Imágenes", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .shadow(2.dp, RoundedCornerShape(24.dp))
                            .padding(horizontal = 30.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text("Score: $score", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Vidas: ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(3) { index ->
                                    Box(
                                        modifier = Modifier.size(14.dp).clip(CircleShape)
                                            .background(if (index < lives) Color(0xFFFF4F68) else Color(0xFFFF4F68).copy(alpha = 0.4f))
                                            .border(1.5.dp, Color(0xFF222222), CircleShape)
                                    )
                                }
                            }
                        }
                        Text("Nivel: $currentLevel / 10", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (phase == SequencePhase.MEMORIZE) {
                                Text("Memoriza las imágenes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF045F73))
                                Spacer(modifier = Modifier.height(24.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    correctSequence.forEach { id ->
                                        ImageBox(icon = getIconForId(id), modifier = Modifier.padding(horizontal = 8.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                                GameButton("¡Listo!") { viewModel.readyToRecall() }

                            } else if (phase == SequencePhase.RECALL) {
                                Text("Presiona una imagen para acomodarla", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF01739B))
                                Spacer(modifier = Modifier.height(24.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    userSlots.forEachIndexed { index, id ->
                                        SlotBox(
                                            icon = if (id != null) getIconForId(id) else null,
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                            onClick = { viewModel.deselectFromSlot(index) }
                                        )
                                    }
                                }

                                Divider(modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 24.dp), color = Color(0xFF005F73).copy(alpha = 0.15f), thickness = 2.dp)

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    poolImages.forEachIndexed { index, id ->
                                        val isHidden = poolHidden[index]
                                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                                            if (isHidden) {
                                                Box(modifier = Modifier.size(80.dp))
                                            } else {
                                                ImageBox(
                                                    icon = getIconForId(id),
                                                    onClick = { viewModel.selectFromPool(index, id) }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                                GameButton("Verificar") { viewModel.verifySelection() }
                            }
                        }
                    }
                }
            }
        }

        if (phase == SequencePhase.INTRO || phase == SequencePhase.GAMEOVER_WIN || phase == SequencePhase.GAMEOVER_LOSE) {
            SequenceModal(
                phase = phase, difficulty = difficulty, score = score,
                onActionClick = {
                    if (phase == SequencePhase.INTRO) viewModel.startRound() else viewModel.playAgain()
                },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
fun ImageBox(icon: ImageVector, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val scale by animateFloatAsState(targetValue = 1f)
    Box(
        modifier = modifier.scale(scale).size(80.dp).shadow(6.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp))
            .background(Color.White).border(2.dp, MintPrimary, RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(45.dp))
    }
}

@Composable
fun SlotBox(icon: ImageVector?, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Box(
        modifier = modifier.size(80.dp).clip(RoundedCornerShape(16.dp))
            .background(if (icon != null) Color.White else Color(0xFF005F73).copy(alpha = 0.05f))
            .border(
                width = 2.dp,
                color = if (icon != null) MintPrimary else Color(0xFF0095C8),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = onClick != null && icon != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(45.dp))
        }
    }
}

@Composable
fun SequenceModal(phase: SequencePhase, difficulty: String, score: Int, onActionClick: () -> Unit, onNavigateBack: () -> Unit) {
    val themeColor = when (phase) { SequencePhase.GAMEOVER_WIN -> Color(0xFFFFD700); SequencePhase.GAMEOVER_LOSE -> Color(0xFFFF4F68); else -> MintPrimary }
    val eyebrowText = when (phase) { SequencePhase.GAMEOVER_WIN -> "¡FELICIDADES!"; SequencePhase.GAMEOVER_LOSE -> "¡GAME OVER!"; else -> "NIVEL ${difficulty.uppercase()}" }
    val titleText = when (phase) { SequencePhase.GAMEOVER_WIN -> "¡Juego Completado!"; SequencePhase.GAMEOVER_LOSE -> "¡Buen intento!"; else -> "Secuencia de Imágenes" }
    val msgText = when (phase) {
        SequencePhase.GAMEOVER_WIN -> "Has completado los 10 niveles con éxito.\n¡Tu memoria visual es excelente!"
        SequencePhase.GAMEOVER_LOSE -> "Se acabaron las vidas.\n¡No te rindas y vuelve a intentarlo!"
        else -> "Meta: Completa los 10 niveles o consigue un alto puntaje.\nMemoriza el orden exacto de las imágenes."
    }
    val btnText = when (phase) { SequencePhase.GAMEOVER_WIN -> "Jugar de nuevo"; SequencePhase.GAMEOVER_LOSE -> "Reintentar"; else -> "¡Empezar!" }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF001E28).copy(alpha = 0.6f)).padding(24.dp).clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
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
                    if (phase != SequencePhase.INTRO) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Puntaje final: $score", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueDark)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        if (phase != SequencePhase.INTRO) {
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