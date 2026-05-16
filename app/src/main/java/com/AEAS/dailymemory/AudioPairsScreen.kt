package com.AEAS.dailymemory

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.AEAS.dailymemory.ui.theme.BlueDark
import com.AEAS.dailymemory.ui.theme.FloatingMascot
import com.AEAS.dailymemory.ui.theme.GradientBackground

val CardMatchedColor = Color(0xFF7B83EB)
val CardHiddenColor = Color(0xFF3ECA9A)
val CardSelectedColor = Color(0xFF2B9672)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AudioPairsScreen(
    difficulty: String,
    onNavigateBack: () -> Unit,
    viewModel: AudioPairsViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(difficulty) {
        viewModel.initGame(context, difficulty)
    }

    val phase by viewModel.phase.collectAsState()
    val cards by viewModel.cards.collectAsState()
    val score by viewModel.score.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        GradientBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().shadow(4.dp).background(Color(0xFFF8F9FA).copy(alpha = 0.95f))) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Memoria Ecoica", color = MintPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = BlueDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Volver", color = BlueDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 16.dp, start = 24.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Parejas Auditivas ($difficulty)",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF222222),
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    Box(modifier = Modifier.background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, MintPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)).padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Text("Score: $score", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF222222))
                    }
                    Box(modifier = Modifier.background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, MintPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)).padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Text("Tiempo: ${timeLeft}s", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF222222))
                    }
                }

                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp, vertical = 8.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val isTablet = maxWidth > 700.dp
                    val columnCount = if (isTablet) 6 else if (cards.size <= 12) 4 else 4

                    Card(
                        modifier = Modifier.widthIn(max = 900.dp).shadow(8.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            OutlinedButton(
                                onClick = { viewModel.playAgain() },
                                border = BorderStroke(2.dp, MintPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MintPrimary, containerColor = Color(0xFFE0FFFA)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(bottom = 24.dp)
                            ) {
                                Text("Reiniciar Juego", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(columnCount),
                                userScrollEnabled = false,
                                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.wrapContentSize()
                            ) {
                                itemsIndexed(cards) { index, card ->
                                    AudioCardView(card = card, onClick = { viewModel.selectCard(index) })
                                }
                            }

                            Text(
                                text = feedbackMessage,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (feedbackMessage == "Intenta de nuevo.") Color.Red else if (feedbackMessage.contains("Pareja") || feedbackMessage.contains("¡Correcto!")) Color(0xFF7B83EB) else BlueDark,
                                modifier = Modifier.padding(top = 24.dp).height(30.dp)
                            )
                        }
                    }
                }
            }
        }

        if (phase != AudioPairsPhase.PLAYING) {
            AudioPairsModal(
                phase = phase, difficulty = difficulty, score = score, time = timeLeft,
                onActionClick = { if (phase == AudioPairsPhase.INTRO) viewModel.startGame() else viewModel.playAgain() },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
fun AudioCardView(card: AudioCard, onClick: () -> Unit) {
    val scale by animateFloatAsState(targetValue = if (card.isSelected) 0.95f else 1f)

    val bgColor = if (card.isMatched) CardMatchedColor else if (card.isSelected) CardSelectedColor else CardHiddenColor
    val symbol = if (card.isMatched) "✔" else "?"

    Box(
        modifier = Modifier
            .size(75.dp)
            .scale(scale)
            .shadow(if (card.isMatched) 2.dp else 6.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(enabled = !card.isMatched && !card.isSelected) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AudioPairsModal(phase: AudioPairsPhase, difficulty: String, score: Int, time: Int, onActionClick: () -> Unit, onNavigateBack: () -> Unit) {
    val themeColor = when (phase) { AudioPairsPhase.GAMEOVER_WIN -> Color(0xFFFFD700); AudioPairsPhase.GAMEOVER_LOSE -> Color(0xFFFF4F68); else -> MintPrimary }
    val eyebrowText = when (phase) { AudioPairsPhase.GAMEOVER_WIN -> "¡EXCELENTE!"; AudioPairsPhase.GAMEOVER_LOSE -> "TIEMPO AGOTADO"; else -> "MEMORIA ECOICA" }
    val titleText = when (phase) { AudioPairsPhase.GAMEOVER_WIN -> "¡Nivel Completado!"; AudioPairsPhase.GAMEOVER_LOSE -> "Inténtalo de nuevo"; else -> "Nivel $difficulty" }

    val timeLimit = if (difficulty == "Fácil") 60 else if (difficulty == "Medio") 50 else 40

    val msgText = when (phase) {
        AudioPairsPhase.GAMEOVER_WIN -> "Has encontrado todas las parejas a tiempo.\nTe sobraron $time segundos."
        AudioPairsPhase.GAMEOVER_LOSE -> "Se acabó el tiempo. Escucha con atención."
        else -> "Encuentra las parejas de sonidos iguales.\nTienes $timeLimit segundos."
    }
    val btnText = when (phase) { AudioPairsPhase.GAMEOVER_WIN -> "Jugar de nuevo"; AudioPairsPhase.GAMEOVER_LOSE -> "Reintentar"; else -> "¡Empezar!" }

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

                    if (phase != AudioPairsPhase.INTRO) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Puntaje final: $score", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueDark)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        if (phase != AudioPairsPhase.INTRO) {
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