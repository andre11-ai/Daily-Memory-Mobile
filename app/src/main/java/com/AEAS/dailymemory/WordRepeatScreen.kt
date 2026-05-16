package com.AEAS.dailymemory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
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
fun WordRepeatScreen(
    difficulty: String,
    onNavigateBack: () -> Unit,
    viewModel: WordRepeatViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(difficulty) {
        viewModel.initGame(context, difficulty)
    }

    val phase by viewModel.phase.collectAsState()
    val score by viewModel.score.collectAsState()
    val round by viewModel.round.collectAsState()
    val availableWords by viewModel.availableWords.collectAsState()
    val userSelection by viewModel.userSelection.collectAsState()
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

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Repetir Palabra ($difficulty)", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
                    Spacer(modifier = Modifier.width(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, MintPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)).padding(horizontal = 20.dp, vertical = 8.dp)) {
                            Text("Score: $score", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF222222))
                        }
                        Box(modifier = Modifier.background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, MintPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)).padding(horizontal = 20.dp, vertical = 8.dp)) {
                            Text("Ronda: $round", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF222222))
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                    Card(
                        modifier = Modifier.fillMaxWidth().widthIn(max = 800.dp).shadow(8.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp).defaultMinSize(minHeight = 350.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {

                            if (phase == WordPhase.LISTENING || phase == WordPhase.PLAYING || phase == WordPhase.ROUND_RESULT) {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(60.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = feedbackMessage,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (feedbackMessage == "Incorrecto.") Color.Red else if (feedbackMessage.contains("Correcto")) Color(0xFF28CF5F) else BlueDark,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                            }

                            if (userSelection.isNotEmpty()) {
                                Text(
                                    text = userSelection.joinState(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AccentBlue,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            if (phase == WordPhase.PLAYING) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    availableWords.forEach { word ->
                                        WordButton(word = word) { viewModel.selectWord(word) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (phase == WordPhase.INTRO || phase == WordPhase.GAMEOVER_WIN || phase == WordPhase.GAMEOVER_LOSE) {
            WordRepeatModal(
                phase = phase, difficulty = difficulty, score = score,
                onActionClick = { if (phase == WordPhase.INTRO) viewModel.startNextRound() else viewModel.playAgain() },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

fun List<String>.joinState(): String {
    return this.joinToString(" ➔ ")
}

@Composable
fun WordButton(word: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .background(Color(0xFFE0FFFA), RoundedCornerShape(16.dp))
            .border(2.dp, MintPrimary, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(word, color = MintPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun WordRepeatModal(phase: WordPhase, difficulty: String, score: Int, onActionClick: () -> Unit, onNavigateBack: () -> Unit) {
    val themeColor = when (phase) { WordPhase.GAMEOVER_WIN -> Color(0xFFFFD700); WordPhase.GAMEOVER_LOSE -> Color(0xFFFF4F68); else -> MintPrimary }
    val eyebrowText = when (phase) { WordPhase.GAMEOVER_WIN -> "¡MUY BIEN!"; WordPhase.GAMEOVER_LOSE -> "FALLASTE"; else -> "MEMORIA ECOICA" }
    val titleText = when (phase) { WordPhase.GAMEOVER_WIN -> "¡Nivel Completado!"; WordPhase.GAMEOVER_LOSE -> "Secuencia incorrecta"; else -> "Repetir Palabra" }

    val target = if (difficulty == "Fácil") 8 else if (difficulty == "Medio") 10 else 12

    val msgText = when (phase) {
        WordPhase.GAMEOVER_WIN -> "Gran memoria auditiva.\nHas superado el reto."
        WordPhase.GAMEOVER_LOSE -> "Intenta concentrarte más en el sonido."
        else -> "Nivel $difficulty: Escucha la secuencia y selecciona el orden correcto.\nMeta: $target rondas."
    }
    val btnText = when (phase) { WordPhase.GAMEOVER_WIN -> "Jugar de nuevo"; WordPhase.GAMEOVER_LOSE -> "Reintentar"; else -> "¡Empezar!" }

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

                    if (phase != WordPhase.INTRO) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Puntaje final: $score", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueDark)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        if (phase != WordPhase.INTRO) {
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