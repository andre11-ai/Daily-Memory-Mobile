package com.AEAS.dailymemory

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.AEAS.dailymemory.ui.theme.BlueDark
import com.AEAS.dailymemory.ui.theme.FloatingMascot
import com.AEAS.dailymemory.ui.theme.GradientBackground

fun getMemoramaIcon(pairId: Int): ImageVector {
    return when (pairId) {
        1 -> Icons.Default.Pets
        2 -> Icons.Default.DirectionsCar
        3 -> Icons.Default.LocalPizza
        4 -> Icons.Default.SportsEsports
        5 -> Icons.Default.Headphones
        6 -> Icons.Default.CameraAlt
        7 -> Icons.Default.Lightbulb
        8 -> Icons.Default.Public
        else -> Icons.Default.Star
    }
}

@Composable
fun MemoramaScreen(
    difficulty: String,
    onNavigateBack: () -> Unit,
    viewModel: MemoramaViewModel = viewModel()
) {
    LaunchedEffect(difficulty) {
        viewModel.initGame(difficulty)
    }

    val phase by viewModel.phase.collectAsState()
    val cards by viewModel.cards.collectAsState()
    val score by viewModel.score.collectAsState()
    val moves by viewModel.moves.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()

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

                Text(
                    text = "Memorama",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    textAlign = TextAlign.Center
                )

                BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    val isTablet = maxWidth > 700.dp

                    if (isTablet) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(0.6f).padding(end = 24.dp), contentAlignment = Alignment.Center) {
                                MemoramaBoard(cards) { viewModel.flipCard(it) }
                            }
                            Box(modifier = Modifier.weight(0.4f), contentAlignment = Alignment.Center) {
                                MemoramaStats(score, timeLeft, moves, matches)
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            MemoramaStats(score, timeLeft, moves, matches)
                            Spacer(modifier = Modifier.height(24.dp))
                            MemoramaBoard(cards) { viewModel.flipCard(it) }
                        }
                    }
                }
            }
        }

        if (phase != MemoramaPhase.PLAYING) {
            MemoramaModal(
                phase = phase, difficulty = difficulty, time = timeLeft,
                onActionClick = { if (phase == MemoramaPhase.INTRO) viewModel.startGame() else viewModel.playAgain() },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
fun MemoramaBoard(cards: List<MemoramaCard>, onCardClick: (Int) -> Unit) {
    Card(
        modifier = Modifier.widthIn(max = 480.dp).shadow(8.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            userScrollEnabled = false,
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.wrapContentHeight()
        ) {
            itemsIndexed(cards) { index, card ->
                MemoramaCardView(card = card, onClick = { onCardClick(index) })
            }
        }
    }
}

@Composable
fun MemoramaCardView(card: MemoramaCard, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )
    val isFrontVisible = rotation > 90f

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFrontVisible) Color.White else Color(0xFF0081A7))
            .border(3.dp, MintPrimary, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isFrontVisible) {
            Icon(
                imageVector = getMemoramaIcon(card.pairId),
                contentDescription = null,
                tint = BlueDark,
                modifier = Modifier.size(36.dp).graphicsLayer { rotationY = 180f }
            )
        }
    }
}

@Composable
fun MemoramaStats(score: Int, timeLeft: Int, moves: Int, matches: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 350.dp).shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatBox("Score: $score")
            StatBox("Tiempo: $timeLeft s")
            StatBox("Movimientos: $moves")
            StatBox("Aciertos: $matches / 8")
        }
    }
}

@Composable
fun StatBox(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFE3FEFF), RoundedCornerShape(10.dp))
            .border(2.dp, Color(0xFFBDF0EB), RoundedCornerShape(10.dp)).padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color(0xFF045F73), fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun MemoramaModal(phase: MemoramaPhase, difficulty: String, time: Int, onActionClick: () -> Unit, onNavigateBack: () -> Unit) {
    val themeColor = when (phase) { MemoramaPhase.GAMEOVER_WIN -> Color(0xFFFFD700); MemoramaPhase.GAMEOVER_LOSE -> Color(0xFFFF4F68); else -> MintPrimary }
    val eyebrowText = when (phase) { MemoramaPhase.GAMEOVER_WIN -> "¡EXCELENTE!"; MemoramaPhase.GAMEOVER_LOSE -> "¡TIEMPO AGOTADO!"; else -> "NIVEL ${difficulty.uppercase()}" }
    val titleText = when (phase) { MemoramaPhase.GAMEOVER_WIN -> "¡Nivel Completado!"; MemoramaPhase.GAMEOVER_LOSE -> "¡Buen intento!"; else -> "Memorama" }

    val timeLimit = if (difficulty == "Fácil") 120 else if (difficulty == "Medio") 90 else 60

    val msgText = when (phase) {
        MemoramaPhase.GAMEOVER_WIN -> "Has encontrado todos los pares en ${timeLimit - time} segundos.\n¡Gran memoria!"
        MemoramaPhase.GAMEOVER_LOSE -> "Se acabó el tiempo.\nIntenta ser más rápido la próxima vez."
        else -> "Encuentra todos los pares antes de que se acabe el tiempo.\nTiempo límite: $timeLimit segundos."
    }
    val btnText = when (phase) { MemoramaPhase.GAMEOVER_WIN -> "Jugar de nuevo"; MemoramaPhase.GAMEOVER_LOSE -> "Reintentar"; else -> "¡Empezar!" }

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
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        if (phase != MemoramaPhase.INTRO) {
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