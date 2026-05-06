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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.AEAS.dailymemory.ui.theme.BlueDark
import com.AEAS.dailymemory.ui.theme.FloatingMascot
import com.AEAS.dailymemory.ui.theme.GradientBackground

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorGameScreen(
    difficulty: String,
    onNavigateBack: () -> Unit,
    viewModel: ColorGameViewModel = viewModel()
) {
    LaunchedEffect(difficulty) {
        viewModel.initGame(difficulty)
    }

    val phase by viewModel.phase.collectAsState()
    val score by viewModel.score.collectAsState()
    val lives by viewModel.lives.collectAsState()
    val colorsToMemorize by viewModel.colorsToMemorize.collectAsState()
    val availableColors by viewModel.availableColors.collectAsState()
    val selectedColors by viewModel.userSelectedColors.collectAsState()

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
                    Text("Memoriza el color", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .shadow(2.dp, RoundedCornerShape(24.dp))
                            .padding(horizontal = 30.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text("Score: $score", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Vidas: ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(3) { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (index < lives) Color(0xFFFF4F68) else Color(0xFFFF4F68).copy(alpha = 0.4f))
                                            .border(1.8.dp, Color(0xFF222222), CircleShape)
                                    )
                                }
                            }
                        }
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
                            if (phase == GamePhase.MEMORIZE) {
                                Text("Memoriza el orden", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(24.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    colorsToMemorize.forEach { color ->
                                        ColorBox(color = color, modifier = Modifier.padding(horizontal = 8.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                                GameButton("¡Listo!") { viewModel.readyToSelect() }

                            } else if (phase == GamePhase.SELECT) {
                                Text("Selecciona los colores en orden", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(24.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    availableColors.forEach { color ->
                                        val isSelected = selectedColors.contains(color)
                                        ColorBox(
                                            color = color,
                                            isSelected = isSelected,
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                            onClick = { viewModel.selectColor(color) }
                                        )
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

        if (phase == GamePhase.INTRO || phase == GamePhase.GAMEOVER_WIN || phase == GamePhase.GAMEOVER_LOSE) {
            ColorGameModal(
                phase = phase,
                difficulty = difficulty,
                score = score,
                targetScore = if (difficulty == "Fácil") 30 else if (difficulty == "Medio") 40 else 60,
                onActionClick = {
                    if (phase == GamePhase.GAMEOVER_WIN || phase == GamePhase.GAMEOVER_LOSE) {
                        viewModel.playAgain()
                    } else {
                        viewModel.startRound()
                    }
                },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
fun ColorBox(color: Color, isSelected: Boolean = false, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.05f else 1f)

    Box(
        modifier = modifier
            .scale(scale)
            .size(90.dp)
            .shadow(if (isSelected) 12.dp else 4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .border(
                width = if (isSelected) 4.dp else 3.dp,
                color = if (isSelected) AccentBlue else MintPrimary,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    )
}

@Composable
fun GameButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4568DC)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(horizontal = 24.dp).height(50.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun ColorGameModal(
    phase: GamePhase, difficulty: String, score: Int, targetScore: Int,
    onActionClick: () -> Unit, onNavigateBack: () -> Unit
) {
    val themeColor = when (phase) {
        GamePhase.GAMEOVER_WIN -> Color(0xFFFFD700)
        GamePhase.GAMEOVER_LOSE -> Color(0xFFFF4F68)
        else -> MintPrimary
    }

    val eyebrowText = when (phase) {
        GamePhase.GAMEOVER_WIN -> "¡VICTORIA!"
        GamePhase.GAMEOVER_LOSE -> "¡FIN DEL JUEGO!"
        else -> "NIVEL ${difficulty.uppercase()}"
    }

    val titleText = when (phase) {
        GamePhase.GAMEOVER_WIN -> "¡Memoria Excelente!"
        GamePhase.GAMEOVER_LOSE -> "¡Inténtalo de nuevo!"
        else -> "Memoriza el Color"
    }

    val msgText = when (phase) {
        GamePhase.GAMEOVER_WIN -> "Has alcanzado la meta de puntos.\n¡Gran trabajo!"
        GamePhase.GAMEOVER_LOSE -> "Has perdido todas tus vidas.\n¡No te rindas!"
        else -> "Meta: consigue $targetScore puntos.\nMemoriza los colores y selecciónalos en orden.\nTienes tres vidas."
    }

    val btnText = when (phase) {
        GamePhase.GAMEOVER_WIN -> "Jugar de nuevo"
        GamePhase.GAMEOVER_LOSE -> "Reintentar"
        else -> "¡Empezar!"
    }

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

                    if (phase != GamePhase.INTRO) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Puntaje final: $score", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueDark)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        if (phase != GamePhase.INTRO) {
                            Text(
                                text = "Volver al Menú", color = Color.Gray, fontSize = 14.sp,
                                modifier = Modifier.clickable { onNavigateBack() }.padding(end = 16.dp)
                            )
                        }
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(24.dp))
                                .background(Brush.horizontalGradient(listOf(MintPrimary, AccentBlue)))
                                .clickable { onActionClick() }.padding(horizontal = 32.dp, vertical = 12.dp)
                        ) {
                            Text(btnText, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}